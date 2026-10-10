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
import org.dromara.edu.domain.EduImportBatch;
import org.dromara.edu.domain.EduImportError;
import org.dromara.edu.mapper.EduAsyncTaskMapper;
import org.dromara.edu.mapper.EduFileRefMapper;
import org.dromara.edu.mapper.EduImportBatchMapper;
import org.dromara.edu.mapper.EduImportErrorMapper;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 导入任务的异步执行阶段（GAP-094 第四批）。
 *
 * 流程：抢占 `queued` 的导入任务 → 按批次重读文件并重跑校验（校验是只读的，保证只执行通过的行）→
 * 逐行交给模块执行器写业务数据 → 统计成功 / 跳过 / 失败并写 `edu_import_error`（result=failed）→
 * 回写 `edu_import_batch`（success_count / skipped_count / import_status）与 `edu_async_task`（状态与计数）。
 *
 * 没有登记执行器的模块不会被领取（{@link #tryRun} 返回 false），任务保持在 queued，
 * 避免把「还没实现」误标成失败。
 *
 * @author Codex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EduImportTaskRunner {

    private static final String STATUS_QUEUED = "queued";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_SUCCEEDED = "succeeded";
    private static final String STATUS_PARTIAL_FAILED = "partial_failed";

    private static final String RESULT_FAILED = "failed";

    private static final String BATCH_COMPLETED = "completed";

    /** 文件类型（06-field-dictionary.yaml 的 file_kind） */
    private static final String FILE_KIND_RESULT = "import_result";
    private static final String FILE_KIND_FAILED_ROWS = "failed_rows";

    /** 结果与失败明细文件保留天数（与导出结果一致，BR-IMP-013） */
    private static final int RESULT_KEEP_DAYS = 7;
    private static final String BATCH_PARTIAL_FAILED = "partial_failed";

    private final EduAsyncTaskMapper taskMapper;
    private final EduImportBatchMapper batchMapper;
    private final EduImportErrorMapper errorMapper;
    private final EduFileRefMapper fileRefMapper;
    private final EduImportFileReader importFileReader;
    private final List<EduImportHandler> importHandlers;
    private final List<EduImportExecutor> importExecutors;

    @DubboReference
    private RemoteFileService remoteFileService;

    private Map<String, EduImportHandler> handlerMap;
    private Map<String, EduImportExecutor> executorMap;

    /** 能否执行该任务：只有登记了模块执行器的导入任务才被领取 */
    public boolean tryRun(EduAsyncTask task) {
        if (task == null || StringUtils.isBlank(task.getBatchNo())) {
            return false;
        }
        EduImportBatch batch = batchMapper.selectOne(new LambdaQueryWrapper<EduImportBatch>()
            .eq(EduImportBatch::getBatchNo, task.getBatchNo()).last("limit 1"));
        if (batch == null) {
            return false;
        }
        EduImportExecutor executor = executorMap().get(batch.getModuleCode());
        if (executor == null) {
            return false;
        }
        if (!claim(task)) {
            return false;
        }
        try {
            run(task, batch, executor);
            return true;
        } catch (Exception e) {
            log.error("导入任务执行失败：{}", task.getTaskNo(), e);
            markFailed(task, e.getMessage());
            return true;
        }
    }

    private void run(EduAsyncTask task, EduImportBatch batch, EduImportExecutor executor) {
        EduImportHandler handler = handlerMap().get(batch.getModuleCode());
        if (handler == null) {
            throw new IllegalStateException("模块缺少校验器，无法重读文件：" + batch.getModuleCode());
        }
        EduFileRef fileRef = fileRefMapper.selectOne(new LambdaQueryWrapper<EduFileRef>()
            .eq(EduFileRef::getFileId, batch.getSourceFileId()).last("limit 1"));
        if (fileRef == null) {
            throw new IllegalStateException("源文件引用不存在或已清理：" + batch.getSourceFileId());
        }
        String url = remoteFileService.selectUrlByIds(String.valueOf(fileRef.getFileId()));
        if (StringUtils.isBlank(url)) {
            throw new IllegalStateException("源文件不存在或已清理，请重新上传");
        }
        byte[] bytes = remoteFileService.downloadByUrl(url.split(",")[0].trim());
        List<EduImportRow> rows = importFileReader.read(bytes, fileRef.getFileName(), handler.templateHeaders());
        EduImportContext context = context(task, batch, fileRef);
        // 重跑校验：只执行校验通过的行（校验是只读的，不需要信任上一次结果）
        handler.validateRows(context, rows);
        int success = 0;
        int skipped = 0;
        int failed = 0;
        List<String[]> resultRows = new ArrayList<>();
        List<String[]> failedRows = new ArrayList<>();
        for (EduImportRow row : rows) {
            if (StringUtils.isNotBlank(row.getFailReason())) {
                // 校验阶段已写过 invalid 行，这里只收进失败明细文件
                failedRows.add(new String[]{String.valueOf(row.getRowNo()), name(row), row.getFailReason()});
                continue;
            }
            EduImportRowResult result;
            try {
                result = executor.execute(context, row);
            } catch (Exception e) {
                result = EduImportRowResult.failed(e.getMessage());
            }
            if (result == null || "success".equals(result.getResult())) {
                success++;
                String studentNo = result == null ? null : result.getExtras().get("学号");
                if (StringUtils.isNotBlank(studentNo)) {
                    resultRows.add(new String[]{String.valueOf(row.getRowNo()), name(row), studentNo});
                }
            } else if ("skipped".equals(result.getResult())) {
                skipped++;
            } else {
                failed++;
                writeFailedRow(batch, row, result.getFailReason());
                failedRows.add(new String[]{String.valueOf(row.getRowNo()), name(row),
                    StringUtils.defaultString(result.getFailReason())});
            }
        }
        Long resultFileId = uploadCsv(batch, "import-result-" + batch.getBatchNo() + ".csv",
            FILE_KIND_RESULT, "原始行号,姓名,系统学号", resultRows);
        Long failedFileId = uploadCsv(batch, "import-failed-" + batch.getBatchNo() + ".csv",
            FILE_KIND_FAILED_ROWS, "原始行号,姓名,失败原因", failedRows);
        updateBatch(batch, success, skipped, failed, resultFileId, failedFileId);
        updateTask(task, success, skipped, failed, failedFileId);
    }

    private EduImportContext context(EduAsyncTask task, EduImportBatch batch, EduFileRef fileRef) {
        EduImportContext context = new EduImportContext();
        context.setBatchNo(batch.getBatchNo());
        context.setSchoolId(batch.getSchoolId());
        context.setModuleCode(batch.getModuleCode());
        context.setFileId(String.valueOf(fileRef.getFileId()));
        context.setFileName(fileRef.getFileName());
        Map<String, Object> params = parseParams(task.getParamsSummary());
        context.setTermId(toLong(params.get("termId")));
        context.setTargetClassId(toLong(params.get("targetClassId")));
        context.setStrategy((String) params.get("strategy"));
        return context;
    }

    private Map<String, Object> parseParams(String summary) {
        if (StringUtils.isBlank(summary)) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> parsed = JsonUtils.parseMap(summary);
            return parsed == null ? new HashMap<>() : parsed;
        } catch (Exception e) {
            return new HashMap<>();
        }
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

    private void writeFailedRow(EduImportBatch batch, EduImportRow row, String reason) {
        EduImportError error = new EduImportError();
        error.setSchoolId(batch.getSchoolId());
        error.setBatchNo(batch.getBatchNo());
        error.setRowNo(row.getRowNo());
        error.setResult(RESULT_FAILED);
        error.setFailReason(StringUtils.substring(StringUtils.defaultString(reason), 0, 500));
        error.setObjectName(row.getCells().get("姓名"));
        error.setRawData(JsonUtils.toJsonString(row.getCells()));
        errorMapper.insert(error);
    }

    private void updateBatch(EduImportBatch batch, int success, int skipped, int failed,
                             Long resultFileId, Long failedFileId) {
        EduImportBatch update = new EduImportBatch();
        update.setBatchId(batch.getBatchId());
        update.setSuccessCount(success);
        update.setSkippedCount(skipped);
        update.setImportStatus(failed > 0 ? BATCH_PARTIAL_FAILED : BATCH_COMPLETED);
        if (resultFileId != null) {
            update.setResultFileId(resultFileId);
        }
        if (failedFileId != null) {
            update.setFailedFileId(failedFileId);
        }
        batchMapper.updateById(update);
    }

    private String name(EduImportRow row) {
        return StringUtils.defaultString(row.getCells().get("姓名"));
    }

    /**
     * 生成 CSV 并上传到统一文件服务，登记 `edu_file_ref`（REQ-STU-058 学号对照表 / BR-IMP-006 失败明细）。
     *
     * 内容为空时不生成文件（避免落下只有表头的空文件）。CSV 带 UTF-8 BOM，便于 Excel 直接打开中文不乱码。
     */
    private Long uploadCsv(EduImportBatch batch, String fileName, String fileKind, String header,
                           List<String[]> rows) {
        if (rows.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("\ufeff").append(header).append('\n');
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(csv(row[i]));
            }
            sb.append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        RemoteFile uploaded = remoteFileService.upload(fileName, fileName, "text/csv;charset=UTF-8", bytes);
        if (uploaded == null || uploaded.getOssId() == null) {
            log.warn("导入结果文件上传失败：{}", fileName);
            return null;
        }
        EduFileRef ref = new EduFileRef();
        ref.setFileId(uploaded.getOssId());
        ref.setFileKind(fileKind);
        ref.setFileName(fileName);
        ref.setStorageKey(uploaded.getUrl());
        ref.setContentType("text/csv;charset=UTF-8");
        ref.setFileSize((long) bytes.length);
        ref.setBizType("import_batch");
        ref.setBizId(batch.getBatchNo());
        ref.setSchoolId(batch.getSchoolId());
        ref.setExpireTime(new Date(DateUtils.getNowDate().getTime() + RESULT_KEEP_DAYS * 24L * 60 * 60 * 1000));
        fileRefMapper.insert(ref);
        return uploaded.getOssId();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") || value.contains("\"") || value.contains("\n")
            ? "\"" + value.replace("\"", "\"\"") + "\""
            : value;
    }

    private void updateTask(EduAsyncTask task, int success, int skipped, int failed, Long failedFileId) {
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(task.getTaskId());
        update.setTaskStatus(failed > 0 ? STATUS_PARTIAL_FAILED : STATUS_SUCCEEDED);
        update.setProgressPercent(100);
        update.setSuccessCount(success);
        update.setSkippedCount(skipped);
        update.setFailedCount(failed);
        if (failedFileId != null) {
            update.setFailedFileId(failedFileId);
        }
        update.setFinishTime(DateUtils.getNowDate());
        taskMapper.updateById(update);
        log.info("导入任务完成：{}（成功 {} / 跳过 {} / 失败 {}）", task.getTaskNo(), success, skipped, failed);
    }

    private void markFailed(EduAsyncTask task, String message) {
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(task.getTaskId());
        update.setTaskStatus("failed");
        update.setErrorMsg(StringUtils.substring(StringUtils.defaultString(message), 0, 500));
        update.setFinishTime(DateUtils.getNowDate());
        taskMapper.updateById(update);
    }

    /** 条件更新抢占（与导出侧同一套口径：多实例下只有一个能拿到） */
    private boolean claim(EduAsyncTask task) {
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(task.getTaskId());
        update.setTaskStatus(STATUS_RUNNING);
        update.setProgressPercent(5);
        update.setStartTime(DateUtils.getNowDate());
        int rows = taskMapper.update(update, new LambdaQueryWrapper<EduAsyncTask>()
            .eq(EduAsyncTask::getTaskId, task.getTaskId())
            .eq(EduAsyncTask::getTaskStatus, STATUS_QUEUED));
        return rows > 0;
    }

    private Map<String, EduImportHandler> handlerMap() {
        if (handlerMap == null) {
            handlerMap = importHandlers.stream()
                .collect(Collectors.toMap(EduImportHandler::moduleCode, Function.identity(), (a, b) -> a));
        }
        return handlerMap;
    }

    private Map<String, EduImportExecutor> executorMap() {
        if (executorMap == null) {
            executorMap = importExecutors.stream()
                .collect(Collectors.toMap(EduImportExecutor::moduleCode, Function.identity(), (a, b) -> a));
        }
        return executorMap;
    }
}
