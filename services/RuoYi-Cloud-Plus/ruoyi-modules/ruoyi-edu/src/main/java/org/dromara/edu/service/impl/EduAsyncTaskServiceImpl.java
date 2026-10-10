package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.edu.domain.EduAsyncTask;
import org.dromara.edu.domain.EduAsyncTaskRetry;
import org.dromara.edu.domain.EduDeadLetterTask;
import org.dromara.edu.domain.EduFileRef;
import org.dromara.edu.domain.bo.EduAsyncTaskBo;
import org.dromara.edu.domain.bo.EduAuditLogBo;
import org.dromara.edu.domain.bo.EduDeadLetterTaskBo;
import org.dromara.edu.domain.vo.EduAsyncTaskRetryVo;
import org.dromara.edu.domain.vo.EduAsyncTaskVo;
import org.dromara.edu.domain.vo.EduDeadLetterTaskVo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.mapper.EduAsyncTaskMapper;
import org.dromara.edu.mapper.EduAsyncTaskRetryMapper;
import org.dromara.edu.mapper.EduDeadLetterTaskMapper;
import org.dromara.edu.datascope.DataScopeContext;
import org.dromara.edu.datascope.DataScopeResolver;
import org.dromara.edu.mapper.EduFileRefMapper;
import org.dromara.edu.service.IEduAsyncTaskService;
import org.dromara.edu.service.IEduAuditService;
import org.dromara.resource.api.RemoteFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 异步任务服务层处理
 *
 * 口径：任务状态按 queued → running → succeeded / partial_failed / failed 推进，
 * **只有 queued 可取消**（REQ-IMP-034）；**failed / partial_failed 可重试**且沿用原批次号与幂等键，
 * 不产生重复数据（REQ-IMP-037 / BR-IMP-009）；**超过最大重试次数的任务进入死信**，
 * 运维查看与重放，重放原因必填并写审计（REQ-IMP-038 / NFR-MQ-03）；任务结果查询与文件下载
 * **都重新解析数据范围**，不复用发起时的判定（REQ-IMP-036 / DS-DENY-04）；
 * 默认只展示本人发起的任务（REQ-IMP-032 / BR-IMP-014）；任务记录保留 90 天（REQ-IMP-040）。
 *
 * @author Codex
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EduAsyncTaskServiceImpl implements IEduAsyncTaskService {

    /** 任务状态：排队中 */
    private static final String STATUS_QUEUED = "queued";
    /** 任务状态：执行中 */
    private static final String STATUS_RUNNING = "running";
    /** 任务状态：成功 */
    private static final String STATUS_SUCCEEDED = "succeeded";
    /** 任务状态：部分失败 */
    private static final String STATUS_PARTIAL_FAILED = "partial_failed";
    /** 任务状态：失败 */
    private static final String STATUS_FAILED = "failed";
    /** 任务状态：已取消 */
    private static final String STATUS_CANCELLED = "cancelled";
    /** 任务状态：死信 */
    private static final String STATUS_DEAD = "dead";

    /** 死信重放状态：待重放 */
    private static final String REPLAYABLE = "replayable";
    /** 死信重放状态：已重放 */
    private static final String REPLAYED = "replayed";

    /**
     * 最大重试次数。
     * 默认 3（REQ-IMP-039；RV-IMP-04 已裁决「同一用户 1 个 / 同一学校 3 个，均可配置」），
     * 后续接入配置中心时改为读配置项，本批先落常量并在 CR-090 记录。
     */
    private static final int MAX_RETRY_COUNT = 3;

    /** 短时签名链接有效期（分钟）；REQ-IMP-042 要求短时且与登录态绑定 */
    private static final int SIGNED_URL_TTL_MINUTES = 5;

    /** 原因类文案的最小长度：取消 / 重试 / 重放原因至少 5 个字 */
    private static final int REASON_MIN_LENGTH = 5;

    private final EduAsyncTaskMapper baseMapper;
    private final EduAsyncTaskRetryMapper retryMapper;
    private final EduDeadLetterTaskMapper deadLetterMapper;
    private final EduFileRefMapper fileRefMapper;
    private final IEduAuditService auditService;
    private final DataScopeResolver dataScopeResolver;

    @DubboReference
    private RemoteFileService remoteFileService;

    @Override
    public TableDataInfo<EduAsyncTaskVo> queryPageList(EduAsyncTaskBo query, PageQuery pageQuery) {
        LambdaQueryWrapper<EduAsyncTask> wrapper = new LambdaQueryWrapper<EduAsyncTask>()
            .eq(StringUtils.isNotBlank(query.getTaskType()), EduAsyncTask::getTaskType, query.getTaskType())
            .eq(StringUtils.isNotBlank(query.getTaskStatus()), EduAsyncTask::getTaskStatus, query.getTaskStatus())
            .eq(StringUtils.isNotBlank(query.getBatchNo()), EduAsyncTask::getBatchNo, query.getBatchNo())
            .like(StringUtils.isNotBlank(query.getKeyword()), EduAsyncTask::getTaskNo, query.getKeyword());
        applyOwnerScope(wrapper, query.getOwnerId());
        applyTimeRange(wrapper, query.getBeginTime(), query.getEndTime());
        wrapper.orderByDesc(EduAsyncTask::getCreateTime);
        Page<EduAsyncTaskVo> result = baseMapper.selectPageAsyncTask(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillDuration);
        return TableDataInfo.build(result);
    }

    @Override
    public EduAsyncTaskVo queryByTaskNo(String taskNo) {
        EduAsyncTask task = requireTask(taskNo);
        EduAsyncTaskVo vo = baseMapper.selectVoById(task.getTaskId());
        fillDuration(vo);
        List<EduAsyncTaskRetryVo> retryRecords = retryMapper.selectVoList(
            new LambdaQueryWrapper<EduAsyncTaskRetry>()
                .eq(EduAsyncTaskRetry::getTaskNo, taskNo)
                .orderByAsc(EduAsyncTaskRetry::getRetryNo));
        vo.setRetryRecords(retryRecords);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelAsyncTask(String taskNo, String reason) {
        EduAsyncTask task = requireTask(taskNo);
        requireReason(reason, "取消");
        if (!STATUS_QUEUED.equals(task.getTaskStatus())) {
            throw new ServiceException("只有排队中的任务可以取消，当前状态：" + task.getTaskStatus());
        }
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(task.getTaskId());
        update.setTaskStatus(STATUS_CANCELLED);
        update.setErrorMsg("取消原因：" + reason.trim());
        update.setFinishTime(DateUtils.getNowDate());
        update.setQueuePosition(null);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduAsyncTaskVo retryAsyncTask(String taskNo, String reason) {
        EduAsyncTask task = requireTask(taskNo);
        requireReason(reason, "重试");
        if (!STATUS_FAILED.equals(task.getTaskStatus()) && !STATUS_PARTIAL_FAILED.equals(task.getTaskStatus())) {
            throw new ServiceException("只有失败或部分失败的任务可以重试，当前状态：" + task.getTaskStatus());
        }
        int retried = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (retried >= MAX_RETRY_COUNT) {
            moveToDeadLetter(taskNo);
            throw new ServiceException("任务已超过最大重试次数（" + MAX_RETRY_COUNT + "），已转入死信任务列表");
        }
        EduAsyncTask update = new EduAsyncTask();
        update.setTaskId(task.getTaskId());
        update.setTaskStatus(STATUS_QUEUED);
        update.setRetryCount(retried + 1);
        update.setProgressPercent(0);
        update.setErrorMsg("重试原因：" + reason.trim());
        update.setStartTime(null);
        update.setFinishTime(null);
        baseMapper.updateById(update);
        return queryByTaskNo(taskNo);
    }

    @Override
    public EduFileRefVo resolveDownloadFile(String taskNo, String fileId) {
        EduAsyncTask task = requireTask(taskNo);
        Long parsedFileId = parseFileId(fileId);
        EduFileRef fileRef = fileRefMapper.selectOne(new LambdaQueryWrapper<EduFileRef>()
            .eq(EduFileRef::getFileId, parsedFileId));
        if (fileRef == null) {
            throw new ServiceException("文件不存在：" + fileId);
        }
        if (StringUtils.isNotBlank(fileRef.getBizId()) && !taskNo.equals(fileRef.getBizId())) {
            throw new ServiceException("文件不属于该任务，拒绝下载");
        }
        Date now = DateUtils.getNowDate();
        if (fileRef.getExpireTime() != null && fileRef.getExpireTime().before(now)) {
            throw new ServiceException("文件已过期（结果文件默认保留 7 天）");
        }
        EduFileRef update = new EduFileRef();
        update.setRefId(fileRef.getRefId());
        update.setDownloadCount((fileRef.getDownloadCount() == null ? 0 : fileRef.getDownloadCount()) + 1);
        fileRefMapper.updateById(update);

        EduFileRefVo vo = fileRefMapper.selectVoById(fileRef.getRefId());
        vo.setSignedUrl(buildSignedUrl(fileRef));
        vo.setSignedUrlExpireTime(new Date(now.getTime() + SIGNED_URL_TTL_MINUTES * 60_000L));
        writeDownloadLog(resolveDownloadSchoolId(task, fileRef), fileRef, "任务 " + taskNo);
        return vo;
    }

    @Override
    public TableDataInfo<EduDeadLetterTaskVo> queryDeadLetterPageList(EduDeadLetterTaskBo query, PageQuery pageQuery) {
        LambdaQueryWrapper<EduDeadLetterTask> wrapper = new LambdaQueryWrapper<EduDeadLetterTask>()
            .eq(StringUtils.isNotBlank(query.getTaskType()), EduDeadLetterTask::getTaskType, query.getTaskType())
            .eq(StringUtils.isNotBlank(query.getReplayStatus()), EduDeadLetterTask::getReplayStatus,
                query.getReplayStatus())
            .eq(StringUtils.isNotBlank(query.getBatchNo()), EduDeadLetterTask::getBatchNo, query.getBatchNo())
            .like(StringUtils.isNotBlank(query.getKeyword()), EduDeadLetterTask::getTaskNo, query.getKeyword())
            .like(StringUtils.isNotBlank(query.getKeywordType()), EduDeadLetterTask::getTaskType,
                query.getKeywordType());
        applyDeadLetterTimeRange(wrapper, query.getBeginTime(), query.getEndTime());
        wrapper.orderByDesc(EduDeadLetterTask::getDeadTime);
        Page<EduDeadLetterTaskVo> result = deadLetterMapper.selectPageDeadLetterTask(pageQuery.build(), wrapper);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduDeadLetterTaskVo replayDeadLetterTask(String taskNo, String reason) {
        requireReason(reason, "重放");
        EduDeadLetterTask dead = deadLetterMapper.selectOne(new LambdaQueryWrapper<EduDeadLetterTask>()
            .eq(EduDeadLetterTask::getTaskNo, taskNo));
        if (dead == null) {
            throw new ServiceException("死信任务不存在：" + taskNo);
        }
        if (!REPLAYABLE.equals(dead.getReplayStatus())) {
            throw new ServiceException("该死信任务已重放，不能重复重放");
        }
        EduAsyncTask task = baseMapper.selectOne(new LambdaQueryWrapper<EduAsyncTask>()
            .eq(EduAsyncTask::getTaskNo, taskNo));
        String replayTaskStatus = null;
        if (task != null) {
            EduAsyncTask update = new EduAsyncTask();
            update.setTaskId(task.getTaskId());
            update.setTaskStatus(STATUS_QUEUED);
            update.setRetryCount(0);
            update.setProgressPercent(0);
            update.setErrorMsg(null);
            update.setStartTime(null);
            update.setFinishTime(null);
            baseMapper.updateById(update);
            replayTaskStatus = STATUS_QUEUED;
        }
        EduDeadLetterTask update = new EduDeadLetterTask();
        update.setDeadLetterId(dead.getDeadLetterId());
        update.setReplayStatus(REPLAYED);
        update.setReplayBy(LoginHelper.getUserId());
        update.setReplayTime(DateUtils.getNowDate());
        update.setReplayReason(reason.trim());
        update.setReplayTaskStatus(replayTaskStatus);
        deadLetterMapper.updateById(update);
        return deadLetterMapper.selectVoById(dead.getDeadLetterId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRetryResult(String taskNo, Integer retryNo, String result, String errorMsg) {
        if (StringUtils.isBlank(taskNo) || retryNo == null) {
            throw new ServiceException("任务编号与重试序号不能为空");
        }
        EduAsyncTaskRetry retry = retryMapper.selectOne(new LambdaQueryWrapper<EduAsyncTaskRetry>()
            .eq(EduAsyncTaskRetry::getTaskNo, taskNo)
            .eq(EduAsyncTaskRetry::getRetryNo, retryNo));
        if (retry == null) {
            retry = new EduAsyncTaskRetry();
            retry.setTaskNo(taskNo);
            retry.setRetryNo(retryNo);
            retry.setCreateTime(DateUtils.getNowDate());
            EduAsyncTask task = baseMapper.selectOne(new LambdaQueryWrapper<EduAsyncTask>()
                .eq(EduAsyncTask::getTaskNo, taskNo));
            retry.setSchoolId(task == null ? null : task.getSchoolId());
            retry.setResult(result);
            retry.setErrorMsg(errorMsg);
            retryMapper.insert(retry);
            return;
        }
        EduAsyncTaskRetry update = new EduAsyncTaskRetry();
        update.setRetryId(retry.getRetryId());
        update.setResult(result);
        update.setErrorMsg(errorMsg);
        retryMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToDeadLetter(String taskNo) {
        EduAsyncTask task = requireTask(taskNo);
        EduDeadLetterTask exist = deadLetterMapper.selectOne(new LambdaQueryWrapper<EduDeadLetterTask>()
            .eq(EduDeadLetterTask::getTaskNo, taskNo));
        if (exist == null) {
            EduDeadLetterTask dead = new EduDeadLetterTask();
            dead.setSchoolId(task.getSchoolId());
            dead.setTaskNo(taskNo);
            dead.setTaskType(task.getTaskType());
            dead.setDeadTime(DateUtils.getNowDate());
            dead.setRetryCount(task.getRetryCount() == null ? 0 : task.getRetryCount());
            dead.setLastError(StringUtils.isBlank(task.getErrorMsg()) ? "超过最大重试次数" : task.getErrorMsg());
            dead.setBatchNo(task.getBatchNo());
            dead.setReplayStatus(REPLAYABLE);
            deadLetterMapper.insert(dead);
        }
        if (!STATUS_DEAD.equals(task.getTaskStatus())) {
            EduAsyncTask update = new EduAsyncTask();
            update.setTaskId(task.getTaskId());
            update.setTaskStatus(STATUS_DEAD);
            update.setFinishTime(DateUtils.getNowDate());
            baseMapper.updateById(update);
        }
    }

    private EduAsyncTask requireTask(String taskNo) {
        if (StringUtils.isBlank(taskNo)) {
            throw new ServiceException("任务编号不能为空");
        }
        EduAsyncTask task = baseMapper.selectOne(new LambdaQueryWrapper<EduAsyncTask>()
            .eq(EduAsyncTask::getTaskNo, taskNo));
        if (task == null) {
            throw new ServiceException("异步任务不存在：" + taskNo);
        }
        if (!LoginHelper.isSuperAdmin() && !LoginHelper.getUserId().equals(task.getOwnerId())) {
            throw new ServiceException("只能查看本人发起的异步任务");
        }
        return task;
    }

    private void applyOwnerScope(LambdaQueryWrapper<EduAsyncTask> wrapper, Long ownerId) {
        if (LoginHelper.isSuperAdmin()) {
            wrapper.eq(ownerId != null, EduAsyncTask::getOwnerId, ownerId);
            return;
        }
        Long currentUserId = LoginHelper.getUserId();
        if (ownerId != null && !ownerId.equals(currentUserId)) {
            throw new ServiceException("只能查看本人发起的异步任务");
        }
        wrapper.eq(EduAsyncTask::getOwnerId, currentUserId);
    }

    private void applyTimeRange(LambdaQueryWrapper<EduAsyncTask> wrapper, String beginTime, String endTime) {
        Date begin = StringUtils.isNotBlank(beginTime) ? DateUtils.parseDate(beginTime) : null;
        wrapper.ge(begin != null, EduAsyncTask::getCreateTime, begin);
        if (StringUtils.isNotBlank(endTime)) {
            wrapper.le(EduAsyncTask::getCreateTime, toEndOfDay(DateUtils.parseDate(endTime)));
        }
    }

    private void applyDeadLetterTimeRange(LambdaQueryWrapper<EduDeadLetterTask> wrapper, String beginTime,
                                          String endTime) {
        Date begin = StringUtils.isNotBlank(beginTime) ? DateUtils.parseDate(beginTime) : null;
        wrapper.ge(begin != null, EduDeadLetterTask::getDeadTime, begin);
        if (StringUtils.isNotBlank(endTime)) {
            wrapper.le(EduDeadLetterTask::getDeadTime, toEndOfDay(DateUtils.parseDate(endTime)));
        }
    }

    private Date toEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0
            && calendar.get(Calendar.SECOND) == 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
        }
        return calendar.getTime();
    }

    private void requireReason(String reason, String action) {
        if (StringUtils.isBlank(reason) || reason.trim().length() < REASON_MIN_LENGTH) {
            throw new ServiceException(action + "原因必填，且至少 " + REASON_MIN_LENGTH + " 个字");
        }
    }

    private Long parseFileId(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            throw new ServiceException("文件 ID 不能为空");
        }
        try {
            return Long.valueOf(fileId.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("文件 ID 格式不正确：" + fileId);
        }
    }

    /**
     * 生成短时签名下载地址。
     *
     * 由统一文件服务用对象存储预签名能力签发（REQ-IMP-042 / 045）：传 ossId 与有效期秒数，
     * 返回带签名的 GET 链接；过期后链接失效。文件不存在 / 已清理时抛业务异常。
     */
    /**
     * 下载文件的学校归属（GAP-113）：任务 → 文件引用 → 当前登录态数据范围，逐级兜底。
     *
     * 任务与文件引用在落库时都已带学校；兜底只是防止历史数据（GAP-093 之前落下的任务）缺学校。
     */
    private Long resolveDownloadSchoolId(EduAsyncTask task, EduFileRef fileRef) {
        if (task != null && task.getSchoolId() != null) {
            return task.getSchoolId();
        }
        if (fileRef.getSchoolId() != null) {
            return fileRef.getSchoolId();
        }
        try {
            DataScopeContext context = dataScopeResolver.resolve();
            return context != null && context.getSchoolIds().size() == 1
                ? context.getSchoolIds().iterator().next() : null;
        } catch (Exception e) {
            log.warn("下载审计兜底解析学校失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 写一条下载审计（`REQ-IMP-043`）：谁在什么时候下载了哪个文件。
     *
     * `edu_audit_log.school_id` 非空（GAP-113）：取不到学校就**拒绝下载**，
     * 不允许出现「下载成功但没有留痕」的情况（`REQ-IMP-043` / `DS-DENY-02`）。
     */
    private void writeDownloadLog(Long schoolId, EduFileRef fileRef, String source) {
        if (schoolId == null) {
            throw new ServiceException("下载文件缺少学校上下文，已拒绝下载（REQ-IMP-043）");
        }
        EduAuditLogBo logBo = new EduAuditLogBo();
        logBo.setActionType("export");
        logBo.setModuleCode("import_export");
        logBo.setObjectType("file");
        logBo.setObjectId(String.valueOf(fileRef.getFileId()));
        logBo.setObjectName(fileRef.getFileName());
        logBo.setSchoolId(schoolId);
        logBo.setDetail("下载文件：" + fileRef.getFileName() + "（" + source + "）");
        auditService.recordLog(logBo);
    }

    private String buildSignedUrl(EduFileRef fileRef) {
        return remoteFileService.signedDownloadUrl(
            String.valueOf(fileRef.getFileId()), SIGNED_URL_TTL_MINUTES * 60L);
    }

    private void fillDuration(EduAsyncTaskVo vo) {
        if (vo == null || vo.getStartTime() == null) {
            return;
        }
        Date end = vo.getFinishTime() != null ? vo.getFinishTime() : DateUtils.getNowDate();
        vo.setDurationSeconds((end.getTime() - vo.getStartTime().getTime()) / 1000L);
    }

}
