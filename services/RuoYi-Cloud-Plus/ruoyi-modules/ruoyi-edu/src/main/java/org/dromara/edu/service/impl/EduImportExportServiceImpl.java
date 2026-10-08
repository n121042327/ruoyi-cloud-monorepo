package org.dromara.edu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.edu.domain.EduAsyncTask;
import org.dromara.edu.domain.EduFileRef;
import org.dromara.edu.domain.EduImportBatch;
import org.dromara.edu.domain.EduImportError;
import org.dromara.edu.domain.EduImportTemplate;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.bo.EduImportErrorBo;
import org.dromara.edu.domain.bo.EduImportExecuteBo;
import org.dromara.edu.domain.bo.EduImportTemplateBo;
import org.dromara.edu.domain.bo.EduImportValidateBo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.domain.vo.EduFileRefVo;
import org.dromara.edu.domain.vo.EduImportErrorVo;
import org.dromara.edu.domain.vo.EduImportExecuteResultVo;
import org.dromara.edu.domain.vo.EduImportTemplateVo;
import org.dromara.edu.domain.vo.EduImportValidateResultVo;
import org.dromara.edu.mapper.EduAsyncTaskMapper;
import org.dromara.edu.mapper.EduFileRefMapper;
import org.dromara.edu.mapper.EduImportBatchMapper;
import org.dromara.edu.mapper.EduImportErrorMapper;
import org.dromara.edu.mapper.EduImportTemplateMapper;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.system.api.model.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 导入导出引擎服务层处理
 *
 * 口径：导入向导固定四步「下载模板 → 上传文件 → 校验结果 → 确认执行」（REQ-IMP-001 / BR-IMP-001）；
 * **校验阶段不写业务数据**，校验结果保留 24 小时（REQ-IMP-014 / BR-IMP-008）；
 * 批次号是**幂等键**，同一批次号重复提交直接返回第一次结果（REQ-IMP-016 / 017 / BR-IMP-002 / 009）；
 * **部分失败保留已成功行，不整体回滚**（REQ-IMP-019 / NFR-MQ-01）；
 * 导出行数 ≤ 2000 同步下载、超过转异步任务（REQ-IMP-029 / NFR-PERF-05）；
 * 敏感字段默认掩码，明文导出需 `read_sensitive`（REQ-IMP-028 / BR-IMP-012）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduImportExportServiceImpl implements IEduImportExportService {

    /** 模板状态：当前版本 / 历史版本 */
    private static final String TEMPLATE_CURRENT = "1";

    /** 批次状态：已校验（校验阶段不写业务数据） */
    private static final String BATCH_VALIDATED = "validated";
    /** 批次状态：排队中 */
    private static final String BATCH_QUEUED = "queued";
    /** 批次状态：已完成 */
    private static final String BATCH_COMPLETED = "completed";
    /** 批次状态：部分失败 */
    private static final String BATCH_PARTIAL_FAILED = "partial_failed";

    /** 异步任务状态：排队中 / 导出类型 */
    private static final String TASK_QUEUED = "queued";
    private static final String TASK_TYPE_IMPORT = "import";
    private static final String TASK_TYPE_EXPORT = "export";

    /** 文件类型：失败明细 / 导出结果 / 导入模板 */
    private static final String FILE_KIND_FAILED_ROWS = "failed_rows";
    private static final String FILE_KIND_EXPORT_RESULT = "export_result";
    private static final String FILE_KIND_TEMPLATE = "import_template";

    /** 批次文件下载类型 */
    private static final String KIND_FAILED_ROWS = "failed_rows";
    private static final String KIND_RESULT = "result";

    /** 校验结果有效期（小时）：过期后需重新上传（REQ-IMP-014） */
    private static final int VALIDATE_VALID_HOURS = 24;

    /** 单文件大小上限 10 MB（REQ-IMP-004 / BR-IMP-010） */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    /** 单文件行数上限 5000（REQ-IMP-004 / REQ-IMP-051 / BR-IMP-010） */
    private static final int MAX_ROW_COUNT = 5000;

    /** 同步导出上限 2000 行，超过转异步（REQ-IMP-029 / NFR-PERF-05） */
    private static final int SYNC_EXPORT_ROW_LIMIT = 2000;

    /** 短时签名链接有效期（分钟），与任务结果文件下载保持一致（REQ-IMP-042） */
    private static final int SIGNED_URL_TTL_MINUTES = 5;

    /**
     * 明文导出所需的敏感字段权限资源。
     * 资源编码取自 `docs/10-prd/05-permission-matrix.yaml` 的资源清单，
     * **未登记的模块一律拒绝明文导出**，不回退为「放行」。
     */
    private static final Map<String, String> SENSITIVE_RESOURCES = new HashMap<>();

    static {
        SENSITIVE_RESOURCES.put("student", "person.student");
        SENSITIVE_RESOURCES.put("activation_code", "person.student");
        SENSITIVE_RESOURCES.put("teacher", "person.teacher");
        SENSITIVE_RESOURCES.put("class", "org.class");
        SENSITIVE_RESOURCES.put("class_roster", "org.class");
        SENSITIVE_RESOURCES.put("grade", "org.grade");
        SENSITIVE_RESOURCES.put("school", "org.school");
        SENSITIVE_RESOURCES.put("promotion", "promotion.batch");
        SENSITIVE_RESOURCES.put("stream", "stream.selection");
    }

    private final EduImportTemplateMapper templateMapper;
    private final EduImportBatchMapper batchMapper;
    private final EduImportErrorMapper errorMapper;
    private final EduFileRefMapper fileRefMapper;
    private final EduAsyncTaskMapper asyncTaskMapper;

    @Override
    public TableDataInfo<EduImportTemplateVo> queryTemplatePageList(EduImportTemplateBo query, PageQuery pageQuery) {
        LambdaQueryWrapper<EduImportTemplate> wrapper = new LambdaQueryWrapper<EduImportTemplate>()
            .eq(StringUtils.isNotBlank(query.getModuleCode()), EduImportTemplate::getModuleCode,
                query.getModuleCode())
            .eq(StringUtils.isNotBlank(query.getTemplateVersion()), EduImportTemplate::getTemplateVersion,
                query.getTemplateVersion())
            .eq(StringUtils.isNotBlank(query.getStatus()), EduImportTemplate::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(EduImportTemplate::getModuleCode, query.getKeyword())
                .or().like(EduImportTemplate::getTemplateVersion, query.getKeyword()));
        }
        wrapper.orderByAsc(EduImportTemplate::getModuleCode)
            .orderByDesc(EduImportTemplate::getTemplateVersion);
        Page<EduImportTemplateVo> result = templateMapper.selectPageTemplate(pageQuery.build(), wrapper);
        result.getRecords().forEach(this::fillTemplateExpired);
        return TableDataInfo.build(result);
    }

    @Override
    public EduFileRefVo resolveTemplateDownload(String moduleCode, String version) {
        if (StringUtils.isBlank(moduleCode)) {
            throw new ServiceException("请选择导入模块");
        }
        EduImportTemplate template = StringUtils.isBlank(version)
            ? templateMapper.selectOne(new LambdaQueryWrapper<EduImportTemplate>()
                .eq(EduImportTemplate::getModuleCode, moduleCode)
                .eq(EduImportTemplate::getStatus, TEMPLATE_CURRENT))
            : templateMapper.selectOne(new LambdaQueryWrapper<EduImportTemplate>()
                .eq(EduImportTemplate::getModuleCode, moduleCode)
                .eq(EduImportTemplate::getTemplateVersion, version));
        if (template == null) {
            throw new ServiceException("导入模板不存在：" + moduleCode + (StringUtils.isBlank(version) ? ""
                : " / " + version));
        }
        if (template.getFileId() == null) {
            throw new ServiceException("模板文件尚未上传，请联系管理员：" + template.getTemplateVersion());
        }
        // 旧版本模板仍可下载，只在提示里标注已过期（REQ-IMP-003 / IMP-Q-05 已裁决）
        return buildDownload(fileRefByFileId(template.getFileId()), false, templateExpiredHint(template));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduImportValidateResultVo validateImportFile(EduImportValidateBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getModuleCode())) {
            throw new ServiceException("请选择导入模块");
        }
        EduImportTemplate template = resolveTemplate(bo.getModuleCode(), bo.getTemplateVersion());
        Long sourceFileId = parseId(bo.getFileId(), "文件 ID");
        EduFileRef sourceFile = fileRefByFileId(sourceFileId);
        // 上传限制：单文件 ≤ 10 MB（REQ-IMP-004）；行数上限在解析阶段校验（需要文件内容）
        if (sourceFile.getFileSize() != null && sourceFile.getFileSize() > MAX_FILE_SIZE) {
            throw new ServiceException("单个文件不得超过 10 MB（REQ-IMP-004），当前：" + sourceFile.getFileSize());
        }
        // 幂等：同一文件 + 同一模块在 24 小时内重复校验，直接复用原批次（REQ-IMP-014 / 017）
        EduImportBatch existed = findReusableBatch(bo.getModuleCode(), sourceFileId);
        if (existed != null) {
            return toValidateResult(existed, "该文件已在 24 小时内校验过，复用原批次结果（校验结果有效期 "
                + VALIDATE_VALID_HOURS + " 小时）");
        }
        EduImportBatch batch = new EduImportBatch();
        batch.setBatchNo(generateNo("IMP"));
        batch.setModuleCode(bo.getModuleCode());
        batch.setTemplateVersion(template.getTemplateVersion());
        batch.setSourceFileId(sourceFileId);
        batch.setRowTotal(0);
        batch.setValidCount(0);
        batch.setInvalidCount(0);
        batch.setSuccessCount(0);
        batch.setSkippedCount(0);
        batch.setImportStatus(BATCH_VALIDATED);
        batch.setOperatorId(LoginHelper.getUserId());
        batchMapper.insert(batch);
        String guidance = "批次已登记；行级校验（必填 / 格式 / 枚举 / 引用完整性 / 唯一性 / 数据范围）"
            + "由文件解析器读取文件后逐行写 edu_import_error，见 CR-091 与 GAP-094。"
            + "若模板声明行数超过 " + MAX_ROW_COUNT + " 行，请拆分为多个批次后重新上传（REQ-IMP-051）。";
        return toValidateResult(batch, guidance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduImportExecuteResultVo executeImport(EduImportExecuteBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getBatchNo())) {
            throw new ServiceException("批次号不能为空");
        }
        EduImportBatch batch = requireBatch(bo.getBatchNo());
        EduImportExecuteResultVo vo = new EduImportExecuteResultVo();
        vo.setBatchNo(batch.getBatchNo());
        vo.setRowTotal(batch.getRowTotal());
        // 幂等：已排队 / 执行中 / 已完成 / 部分失败的批次再次提交，直接返回第一次的任务（REQ-IMP-017）
        if (StringUtils.isNotBlank(batch.getAsyncTaskNo())
            && !BATCH_VALIDATED.equals(batch.getImportStatus())) {
            vo.setTaskNo(batch.getAsyncTaskNo());
            vo.setImportStatus(batch.getImportStatus());
            vo.setCreatedNewTask(false);
            return vo;
        }
        EduAsyncTask task = new EduAsyncTask();
        task.setTaskNo(generateNo("TASK"));
        task.setTaskType(TASK_TYPE_IMPORT);
        task.setTaskStatus(TASK_QUEUED);
        task.setProgressPercent(0);
        task.setOwnerId(LoginHelper.getUserId());
        task.setOwnerRole(currentRoleSnapshot());
        task.setBatchNo(batch.getBatchNo());
        task.setParamsSummary(buildImportParamsSummary(batch, bo));
        task.setSchoolId(batch.getSchoolId());
        task.setTotalCount(batch.getRowTotal());
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setSkippedCount(0);
        task.setRetryCount(0);
        asyncTaskMapper.insert(task);

        EduImportBatch update = new EduImportBatch();
        update.setBatchId(batch.getBatchId());
        update.setAsyncTaskNo(task.getTaskNo());
        update.setImportStatus(BATCH_QUEUED);
        batchMapper.updateById(update);

        vo.setTaskNo(task.getTaskNo());
        vo.setImportStatus(BATCH_QUEUED);
        vo.setCreatedNewTask(true);
        return vo;
    }

    @Override
    public EduFileRefVo resolveBatchFile(String batchNo, String kind) {
        EduImportBatch batch = requireBatch(batchNo);
        Long fileId;
        if (KIND_FAILED_ROWS.equals(kind)) {
            fileId = batch.getFailedFileId();
            if (fileId == null) {
                throw new ServiceException("本批次没有失败行，无法下载失败明细");
            }
        } else if (KIND_RESULT.equals(kind)) {
            fileId = batch.getResultFileId();
            if (fileId == null) {
                throw new ServiceException("结果文件尚未生成，请稍后在异步任务中心查看进度");
            }
        } else {
            throw new ServiceException("不支持的文件类型：" + kind);
        }
        // 结果文件过期后下载一律拒绝（BR-IMP-013 / REQ-IMP-031），文件默认保留 7 天
        return buildDownload(fileRefByFileId(fileId), true, null);
    }

    @Override
    public TableDataInfo<EduImportErrorVo> queryBatchRowPageList(EduImportErrorBo query, PageQuery pageQuery) {
        if (query == null || StringUtils.isBlank(query.getBatchNo())) {
            throw new ServiceException("批次号不能为空");
        }
        requireBatch(query.getBatchNo());
        LambdaQueryWrapper<EduImportError> wrapper = new LambdaQueryWrapper<EduImportError>()
            .eq(EduImportError::getBatchNo, query.getBatchNo())
            .eq(StringUtils.isNotBlank(query.getResult()), EduImportError::getResult, query.getResult());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(EduImportError::getObjectName, query.getKeyword())
                .or().like(EduImportError::getFailReason, query.getKeyword()));
        }
        wrapper.orderByAsc(EduImportError::getRowNo);
        return TableDataInfo.build(errorMapper.selectPageError(pageQuery.build(), wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EduExportResultVo exportData(EduExportBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getModuleCode())) {
            throw new ServiceException("请选择导出模块");
        }
        String format = StringUtils.isBlank(bo.getFormat()) ? "xlsx" : bo.getFormat().trim().toLowerCase();
        if (!"xlsx".equals(format) && !"csv".equals(format)) {
            throw new ServiceException("导出格式只支持 xlsx 与 csv（REQ-IMP-026）");
        }
        boolean plainText = Boolean.TRUE.equals(bo.getPlainText());
        if (plainText) {
            requireSensitivePermission(bo.getModuleCode());
        }
        // 导出前重新解析数据范围，不复用列表页判定（REQ-IMP-025 / DS-DENY-04）。
        // 数据范围由 DataScopeResolver 在控制器 / 服务入口注入，本服务只负责把范围条件随任务落库。
        EduAsyncTask task = new EduAsyncTask();
        task.setTaskNo(generateNo("EXP"));
        task.setTaskType(TASK_TYPE_EXPORT);
        task.setTaskStatus(TASK_QUEUED);
        task.setProgressPercent(0);
        task.setOwnerId(LoginHelper.getUserId());
        task.setOwnerRole(currentRoleSnapshot());
        task.setParamsSummary(buildExportParamsSummary(bo, format, plainText));
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setSkippedCount(0);
        task.setRetryCount(0);
        asyncTaskMapper.insert(task);

        EduExportResultVo vo = new EduExportResultVo();
        vo.setAsync(true);
        vo.setTaskNo(task.getTaskNo());
        vo.setFormat(format);
        vo.setPlainText(plainText);
        vo.setRowCount(0);
        vo.setGuidance("已创建导出任务；实际取数与文件生成由模块导出器执行，行数 ≤ "
            + SYNC_EXPORT_ROW_LIMIT + " 时导出器直接返回文件，超过则走异步结果文件（REQ-IMP-029）。"
            + "筛选条件、行数、耗时、导出人与是否含明文都会写审计（REQ-IMP-030）。");
        return vo;
    }

    private void requireSensitivePermission(String moduleCode) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        String resource = SENSITIVE_RESOURCES.get(moduleCode);
        if (resource == null) {
            throw new ServiceException("模块「" + moduleCode + "」未登记敏感字段权限资源，禁止明文导出");
        }
        if (!StpUtil.hasPermission(resource + ":read_sensitive")) {
            throw new ServiceException("没有敏感字段读取权限，不能导出明文（REQ-IMP-028 / BR-IMP-012）");
        }
    }

    /**
     * 发起人角色快照（写入 `edu_async_task.owner_role`，列长 30）。
     * 按 PRD 7.3 的要求记录「操作发生时的角色快照」，不随角色变更而改变。
     */
    private String currentRoleSnapshot() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || loginUser.getRoles() == null || loginUser.getRoles().isEmpty()) {
            return LoginHelper.isSuperAdmin() ? "super_admin" : null;
        }
        String roleKey = loginUser.getRoles().get(0).getRoleKey();
        if (StringUtils.isBlank(roleKey)) {
            return null;
        }
        return roleKey.length() > 30 ? roleKey.substring(0, 30) : roleKey;
    }

    private EduImportTemplate resolveTemplate(String moduleCode, String version) {
        EduImportTemplate template = StringUtils.isBlank(version)
            ? templateMapper.selectOne(new LambdaQueryWrapper<EduImportTemplate>()
                .eq(EduImportTemplate::getModuleCode, moduleCode)
                .eq(EduImportTemplate::getStatus, TEMPLATE_CURRENT))
            : templateMapper.selectOne(new LambdaQueryWrapper<EduImportTemplate>()
                .eq(EduImportTemplate::getModuleCode, moduleCode)
                .eq(EduImportTemplate::getTemplateVersion, version));
        if (template == null) {
            throw new ServiceException("导入模板不存在：" + moduleCode);
        }
        return template;
    }

    private EduImportBatch findReusableBatch(String moduleCode, Long sourceFileId) {
        Date freshLine = new Date(System.currentTimeMillis() - VALIDATE_VALID_HOURS * 3600_000L);
        return batchMapper.selectOne(new LambdaQueryWrapper<EduImportBatch>()
            .eq(EduImportBatch::getModuleCode, moduleCode)
            .eq(EduImportBatch::getSourceFileId, sourceFileId)
            .ge(EduImportBatch::getCreateTime, freshLine)
            .orderByDesc(EduImportBatch::getBatchId)
            .last("limit 1"));
    }

    private EduImportBatch requireBatch(String batchNo) {
        if (StringUtils.isBlank(batchNo)) {
            throw new ServiceException("批次号不能为空");
        }
        EduImportBatch batch = batchMapper.selectOne(new LambdaQueryWrapper<EduImportBatch>()
            .eq(EduImportBatch::getBatchNo, batchNo));
        if (batch == null) {
            throw new ServiceException("导入批次不存在：" + batchNo);
        }
        if (!LoginHelper.isSuperAdmin() && !LoginHelper.getUserId().equals(batch.getOperatorId())) {
            throw new ServiceException("只能查看本人发起的导入批次");
        }
        return batch;
    }

    private EduFileRef fileRefByFileId(Long fileId) {
        EduFileRef fileRef = fileRefMapper.selectOne(new LambdaQueryWrapper<EduFileRef>()
            .eq(EduFileRef::getFileId, fileId));
        if (fileRef == null) {
            throw new ServiceException("文件不存在或已清理：" + fileId);
        }
        return fileRef;
    }

    /**
     * 组装下载视图。
     *
     * @param fileRef       文件引用
     * @param rejectExpired true 时过期直接拒绝（结果文件，BR-IMP-013 / REQ-IMP-031）；
     *                      false 时过期仍可下载但给出强提示（模板，REQ-IMP-003）
     * @param hint          额外提示文案
     */
    private EduFileRefVo buildDownload(EduFileRef fileRef, boolean rejectExpired, String hint) {
        Date now = DateUtils.getNowDate();
        boolean expired = fileRef.getExpireTime() != null && fileRef.getExpireTime().before(now);
        if (expired && rejectExpired) {
            throw new ServiceException("文件已过期（结果文件默认保留 7 天，REQ-IMP-031）");
        }
        EduFileRef update = new EduFileRef();
        update.setRefId(fileRef.getRefId());
        update.setDownloadCount((fileRef.getDownloadCount() == null ? 0 : fileRef.getDownloadCount()) + 1);
        fileRefMapper.updateById(update);

        EduFileRefVo vo = fileRefMapper.selectVoById(fileRef.getRefId());
        vo.setSignedUrl("/edu/file/download/" + fileRef.getFileId() + "?key=" + fileRef.getStorageKey());
        vo.setSignedUrlExpireTime(new Date(now.getTime() + SIGNED_URL_TTL_MINUTES * 60_000L));
        vo.setExpired(expired);
        vo.setHint(hint);
        return vo;
    }

    private String templateExpiredHint(EduImportTemplate template) {
        if (template.getExpireTime() == null || template.getExpireTime().after(DateUtils.getNowDate())) {
            return null;
        }
        return "该模板版本（" + template.getTemplateVersion()
            + "）已过期，仍可下载但字段可能已变更，请优先下载当前版本（REQ-IMP-003）";
    }

    private void fillTemplateExpired(EduImportTemplateVo vo) {
        if (vo == null || vo.getExpireTime() == null) {
            return;
        }
        vo.setExpired(vo.getExpireTime().before(DateUtils.getNowDate()));
    }

    private EduImportValidateResultVo toValidateResult(EduImportBatch batch, String guidance) {
        EduImportValidateResultVo vo = new EduImportValidateResultVo();
        vo.setBatchNo(batch.getBatchNo());
        vo.setModuleCode(batch.getModuleCode());
        vo.setTemplateVersion(batch.getTemplateVersion());
        vo.setImportStatus(batch.getImportStatus());
        vo.setRowTotal(batch.getRowTotal());
        vo.setValidCount(batch.getValidCount());
        vo.setInvalidCount(batch.getInvalidCount());
        vo.setValidateExpireTime(new Date(System.currentTimeMillis() + VALIDATE_VALID_HOURS * 3600_000L));
        List<EduImportErrorVo> invalidRows = errorMapper.selectList(new LambdaQueryWrapper<EduImportError>()
                .eq(EduImportError::getBatchNo, batch.getBatchNo())
                .eq(EduImportError::getResult, "invalid")
                .orderByAsc(EduImportError::getRowNo))
            .stream()
            .map(this::toErrorVo)
            .toList();
        vo.setInvalidRows(invalidRows);
        vo.setFailGroups(groupByFailReason(invalidRows));
        vo.setGuidance(guidance);
        return vo;
    }

    private List<org.dromara.edu.domain.vo.EduImportFailGroupVo> groupByFailReason(List<EduImportErrorVo> rows) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (EduImportErrorVo row : rows) {
            String key = StringUtils.isBlank(row.getFailReason()) ? "未说明原因" : row.getFailReason();
            counter.merge(key, 1, Integer::sum);
        }
        List<org.dromara.edu.domain.vo.EduImportFailGroupVo> groups = new ArrayList<>(counter.size());
        counter.forEach((reason, count) -> {
            org.dromara.edu.domain.vo.EduImportFailGroupVo group =
                new org.dromara.edu.domain.vo.EduImportFailGroupVo();
            group.setFailReason(reason);
            group.setCount(count);
            groups.add(group);
        });
        return groups;
    }

    private EduImportErrorVo toErrorVo(EduImportError entity) {
        EduImportErrorVo vo = new EduImportErrorVo();
        vo.setErrorId(entity.getErrorId());
        vo.setBatchNo(entity.getBatchNo());
        vo.setRowNo(entity.getRowNo());
        vo.setResult(entity.getResult());
        vo.setFailReason(entity.getFailReason());
        vo.setObjectName(entity.getObjectName());
        vo.setRawData(entity.getRawData());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String buildImportParamsSummary(EduImportBatch batch, EduImportExecuteBo bo) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"moduleCode\":\"").append(batch.getModuleCode()).append('"');
        sb.append(",\"templateVersion\":\"").append(batch.getTemplateVersion()).append('"');
        sb.append(",\"batchNo\":\"").append(batch.getBatchNo()).append('"');
        sb.append(",\"strategy\":\"").append(StringUtils.isBlank(bo.getStrategy()) ? "skip" : bo.getStrategy())
            .append('"');
        if (StringUtils.isNotBlank(bo.getFailedRowAction())) {
            sb.append(",\"failedRowAction\":\"").append(bo.getFailedRowAction()).append('"');
        }
        return sb.append('}').toString();
    }

    private String buildExportParamsSummary(EduExportBo bo, String format, boolean plainText) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"moduleCode\":\"").append(bo.getModuleCode()).append('"');
        sb.append(",\"format\":\"").append(format).append('"');
        sb.append(",\"plainText\":").append(plainText);
        if (bo.getTermId() != null) {
            sb.append(",\"termId\":").append(bo.getTermId());
        }
        if (bo.getGradeId() != null) {
            sb.append(",\"gradeId\":").append(bo.getGradeId());
        }
        if (bo.getClassId() != null) {
            sb.append(",\"classId\":").append(bo.getClassId());
        }
        if (bo.getStudentId() != null) {
            sb.append(",\"studentId\":").append(bo.getStudentId());
        }
        if (StringUtils.isNotBlank(bo.getKeyword())) {
            sb.append(",\"keyword\":\"").append(bo.getKeyword()).append('"');
        }
        if (bo.getColumns() != null && !bo.getColumns().isEmpty()) {
            sb.append(",\"columns\":").append(bo.getColumns().size());
        }
        return sb.append('}').toString();
    }

    /** 生成业务编号：前缀-yyyyMMdd-####，与升班 / 选科 / 转学单的编号口径一致 */
    private String generateNo(String prefix) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return prefix + "-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private Long parseId(String value, String label) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(label + "不能为空");
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new ServiceException(label + "格式不正确：" + value);
        }
    }

    /**
     * 未使用的常量保留：批次终态与同步阈值在导出器 / 解析器批次会用到，
     * 提前落下避免两处以不同字面量表达同一状态（REQ-IMP-019 / 029）。
     */
    @SuppressWarnings("unused")
    private static final String[] RESERVED_BATCH_STATUS = {BATCH_COMPLETED, BATCH_PARTIAL_FAILED};

    @SuppressWarnings("unused")
    private static final String[] RESERVED_FILE_KINDS = {FILE_KIND_FAILED_ROWS, FILE_KIND_EXPORT_RESULT,
        FILE_KIND_TEMPLATE};

}
