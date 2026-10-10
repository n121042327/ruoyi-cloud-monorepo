package org.dromara.edu.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.edu.domain.EduAsyncTask;
import org.dromara.edu.domain.EduFileRef;
import org.dromara.edu.mapper.EduAsyncTaskMapper;
import org.dromara.edu.mapper.EduFileRefMapper;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * edu 异步任务执行者（导入导出引擎的调度侧）。
 *
 * 口径（REQ-IMP-002 / 017 / 029 / 031）：
 * - 轮询 `edu_async_task` 里 `queued` 的**导出**任务，先以「状态条件更新」抢占（queued → running），
 *   避免多实例重复执行；失败写 failed + errorMsg，成功写 succeeded + resultFileId；
 * - 导出文件由模块导出器生成（{@link EduExportHandler}），统一上传到文件服务后登记 `edu_file_ref`
 *   （结果文件默认保留 7 天，BR-IMP-013），任务中心的下载接口按该引用签发短时链接；
 * - 导入任务的执行器（文件解析 + 逐行校验 + 执行）在 GAP-094a 落地前**不领取**，
 *   避免把导入任务误标为失败。领取条件写死在 {@link #claimableTasks()} 里。
 *
 * @author Codex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EduAsyncTaskExecutor {

    private static final String STATUS_QUEUED = "queued";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_SUCCEEDED = "succeeded";
    private static final String STATUS_FAILED = "failed";

    private static final String TASK_TYPE_EXPORT = "export";
    private static final String TASK_TYPE_IMPORT = "import";

    private static final String FILE_KIND_EXPORT_RESULT = "export_result";
    private static final String FILE_BIZ_TYPE = "async_task";

    /** 单轮最多领取的任务数（避免一次轮询长时间占用） */
    private static final int BATCH_SIZE = 5;

    /** 结果文件保留天数（BR-IMP-013） */
    private static final int RESULT_KEEP_DAYS = 7;

    private final EduAsyncTaskMapper taskMapper;
    private final EduFileRefMapper fileRefMapper;
    private final List<EduExportHandler> exportHandlers;
    private final EduImportTaskRunner importTaskRunner;

    @DubboReference
    private RemoteFileService remoteFileService;

    /** 模块编码 → 导出器 */
    private Map<String, EduExportHandler> handlerMap;

    @Scheduled(fixedDelayString = "${edu.async-task.poll-interval-ms:5000}", initialDelay = 15000)
    public void poll() {
        List<EduAsyncTask> tasks;
        try {
            tasks = claimableTasks();
        } catch (Exception e) {
            log.warn("异步任务轮询失败：{}", e.getMessage());
            return;
        }
        for (EduAsyncTask task : tasks) {
            try {
                if (TASK_TYPE_IMPORT.equals(task.getTaskType())) {
                    // 导入执行阶段（GAP-094 第四批）：只有登记了模块执行器的任务会被领取，
                    // 其余保持 queued，避免把「还没实现」误标成失败
                    importTaskRunner.tryRun(task);
                    continue;
                }
                execute(task);
            } catch (Exception e) {
                log.error("异步任务执行失败：{}", task.getTaskNo(), e);
                markFailed(task, e.getMessage());
            }
        }
    }

    /**
     * 待领取任务：导出任务 + 导入任务。
     *
     * 导入任务由 {@link EduImportTaskRunner} 判断是否有对应模块执行器；没有执行器的模块不会被领取
     * （任务留在 queued，等模块执行器落地后自动继续）。
     */
    private List<EduAsyncTask> claimableTasks() {
        return taskMapper.selectList(new LambdaQueryWrapper<EduAsyncTask>()
            .eq(EduAsyncTask::getTaskStatus, STATUS_QUEUED)
            .in(EduAsyncTask::getTaskType, TASK_TYPE_EXPORT, TASK_TYPE_IMPORT)
            .orderByAsc(EduAsyncTask::getTaskId)
            .last("limit " + BATCH_SIZE));
    }

    private void execute(EduAsyncTask task) {
        if (!claim(task)) {
            return;
        }
        EduExportContext context = buildContext(task);
        EduExportHandler handler = handlerMap().get(context.getModuleCode());
        if (handler == null) {
            throw new IllegalStateException("模块未登记导出器：" + context.getModuleCode()
                + "（已登记：" + String.join(",", handlerMap().keySet()) + "）");
        }
        taskMapper.updateById(withStatus(task.getTaskId(), STATUS_RUNNING, 10, null, null));

        EduExportedFile file = handler.export(context);
        RemoteFile uploaded = remoteFileService.upload(file.getFileName(), file.getFileName(),
            file.getContentType(), file.getBytes());
        if (uploaded == null || uploaded.getOssId() == null) {
            throw new IllegalStateException("导出文件上传失败");
        }
        EduFileRef ref = new EduFileRef();
        ref.setFileId(uploaded.getOssId());
        ref.setFileKind(FILE_KIND_EXPORT_RESULT);
        ref.setFileName(file.getFileName());
        ref.setStorageKey(uploaded.getUrl());
        ref.setContentType(file.getContentType());
        ref.setFileSize((long) file.getBytes().length);
        ref.setBizType(FILE_BIZ_TYPE);
        ref.setBizId(task.getTaskNo());
        ref.setSchoolId(task.getSchoolId());
        ref.setExpireTime(new Date(DateUtils.getNowDate().getTime() + RESULT_KEEP_DAYS * 24L * 60 * 60 * 1000));
        fileRefMapper.insert(ref);

        EduAsyncTask done = withStatus(task.getTaskId(), STATUS_SUCCEEDED, 100, null, uploaded.getOssId());
        done.setTotalCount(file.getRowCount());
        done.setSuccessCount(file.getRowCount());
        done.setFinishTime(DateUtils.getNowDate());
        taskMapper.updateById(done);
        log.info("导出任务完成：{}（{} 行，文件 {}）", task.getTaskNo(), file.getRowCount(), file.getFileName());
    }

    /** 条件更新抢占任务：只有仍处于 queued 的行会被更新，多实例下只有一个能拿到 */
    private boolean claim(EduAsyncTask task) {
        EduAsyncTask update = withStatus(task.getTaskId(), STATUS_RUNNING, 5, null, null);
        update.setStartTime(DateUtils.getNowDate());
        int rows = taskMapper.update(update, new LambdaQueryWrapper<EduAsyncTask>()
            .eq(EduAsyncTask::getTaskId, task.getTaskId())
            .eq(EduAsyncTask::getTaskStatus, STATUS_QUEUED));
        return rows > 0;
    }

    private EduAsyncTask withStatus(Long taskId, String status, Integer progress, String errorMsg, Long resultFileId) {
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(taskId);
        update.setTaskStatus(status);
        update.setProgressPercent(progress);
        if (StringUtils.isNotBlank(errorMsg)) {
            update.setErrorMsg(errorMsg);
        }
        if (resultFileId != null) {
            update.setResultFileId(resultFileId);
        }
        return update;
    }

    private void markFailed(EduAsyncTask task, String message) {
        EduAsyncTask update = withStatus(task.getTaskId(), STATUS_FAILED, task.getProgressPercent(),
            message == null ? null : message.substring(0, Math.min(500, message.length())), null);
        update.setFinishTime(DateUtils.getNowDate());
        taskMapper.updateById(update);
    }

    /** 从 params_summary 还原导出上下文（发起导出时落库，REQ-IMP-025） */
    @SuppressWarnings("unchecked")
    private EduExportContext buildContext(EduAsyncTask task) {
        EduExportContext context = new EduExportContext();
        context.setTaskNo(task.getTaskNo());
        context.setSchoolId(task.getSchoolId());
        context.setFormat("xlsx");
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(task.getParamsSummary())) {
            try {
                Map<String, Object> parsed = JsonUtils.parseMap(task.getParamsSummary());
                if (parsed != null) {
                    params = parsed;
                }
            } catch (Exception e) {
                throw new IllegalStateException("任务参数解析失败：" + e.getMessage());
            }
        }
        context.setModuleCode(StringUtils.trimToEmpty((String) params.get("moduleCode")));
        if (StringUtils.isNotBlank((String) params.get("format"))) {
            context.setFormat((String) params.get("format"));
        }
        context.setTermId(toLong(params.get("termId")));
        context.setGradeId(toLong(params.get("gradeId")));
        context.setClassId(toLong(params.get("classId")));
        context.setStudentId(toLong(params.get("studentId")));
        context.setKeyword((String) params.get("keyword"));
        Object filters = params.get("filters");
        if (filters instanceof Map<?, ?> map) {
            context.setFilters((Map<String, Object>) map);
        }
        return context;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, EduExportHandler> handlerMap() {
        if (handlerMap == null) {
            handlerMap = exportHandlers.stream()
                .collect(Collectors.toMap(EduExportHandler::moduleCode, Function.identity(), (a, b) -> a));
        }
        return handlerMap;
    }
}
