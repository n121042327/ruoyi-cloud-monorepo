package org.dromara.edu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.idev.excel.FastExcel;
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
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.edu.domain.EduAsyncTask;
import org.dromara.edu.domain.EduClass;
import org.dromara.edu.domain.EduFileRef;
import org.dromara.edu.domain.EduImportBatch;
import org.dromara.edu.domain.EduImportError;
import org.dromara.edu.domain.EduImportTemplate;
import org.dromara.edu.domain.bo.EduAuditLogBo;
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
import org.dromara.edu.mapper.EduClassMapper;
import org.dromara.edu.mapper.EduFileRefMapper;
import org.dromara.edu.mapper.EduImportBatchMapper;
import org.dromara.edu.mapper.EduImportErrorMapper;
import org.dromara.edu.datascope.DataScopeContext;
import org.dromara.edu.datascope.DataScopeResolver;
import org.dromara.edu.job.EduImportContext;
import org.dromara.edu.job.EduImportFileReader;
import org.dromara.edu.job.EduImportHandler;
import org.dromara.edu.job.EduImportRow;
import org.dromara.edu.mapper.EduImportTemplateMapper;
import org.dromara.edu.service.IEduAuditService;
import org.dromara.edu.service.IEduImportExportService;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.resource.api.domain.RemoteFile;
import org.dromara.system.api.model.LoginUser;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Slf4j
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

    /**
     * 模板兜底版本号：`edu_import_template` 未登记时按此版本现场生成（GAP-119 / D-241）。
     * 模板列的权威来源是模块校验器声明的 `templateHeaders()`，与校验阶段的表头严格比对同源。
     */
    private static final String TEMPLATE_FALLBACK_VERSION = "v1";

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
    private final IEduAuditService auditService;
    private final DataScopeResolver dataScopeResolver;
    private final EduImportFileReader importFileReader;
    private final EduClassMapper classMapper;

    /** 已登记的模块导入校验器（key = 模块编码） */
    private final List<EduImportHandler> importHandlers;

    @DubboReference
    private RemoteFileService remoteFileService;

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
        appendUnregisteredModules(result, query);
        return TableDataInfo.build(result);
    }

    /**
     * 补齐「已登记校验器、但 `edu_import_template` 里还没有记录」的模块（GAP-119 / D-241）。
     *
     * 导入向导的模块下拉直接取本接口；模板表为空时用户连模块都选不到，整条导入链路卡在第一步。
     * 模板列的权威来源本来就是模块校验器（`EduImportHandler.templateHeaders()`，与表头严格比对同一份声明），
     * 因此为未落库模块合成一行「当前版本、文件按需生成」的模板行，真正的文件在下载时生成并落库。
     * 仅在未按模块过滤时合并，避免把用户明确筛选的结果污染成全集。
     */
    private void appendUnregisteredModules(Page<EduImportTemplateVo> result, EduImportTemplateBo query) {
        if (importHandlers == null || importHandlers.isEmpty() || StringUtils.isNotBlank(query.getModuleCode())) {
            return;
        }
        Set<String> registered = new HashSet<>();
        for (EduImportTemplateVo row : result.getRecords()) {
            registered.add(row.getModuleCode());
        }
        List<EduImportTemplateVo> synthesized = new ArrayList<>();
        for (EduImportHandler handler : importHandlers) {
            List<String> headers = handler.templateHeaders();
            if (StringUtils.isBlank(handler.moduleCode()) || registered.contains(handler.moduleCode())
                || headers == null || headers.isEmpty()) {
                continue;
            }
            EduImportTemplateVo vo = new EduImportTemplateVo();
            vo.setModuleCode(handler.moduleCode());
            vo.setTemplateVersion(TEMPLATE_FALLBACK_VERSION);
            vo.setColumnCount(headers.size());
            vo.setStatus(TEMPLATE_CURRENT);
            vo.setExpired(Boolean.FALSE);
            synthesized.add(vo);
        }
        if (synthesized.isEmpty()) {
            return;
        }
        List<EduImportTemplateVo> rows = new ArrayList<>(result.getRecords());
        rows.addAll(synthesized);
        rows.sort(Comparator.comparing(EduImportTemplateVo::getModuleCode,
            Comparator.nullsLast(Comparator.naturalOrder())));
        result.setRecords(rows);
        result.setTotal(result.getTotal() + synthesized.size());
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
            // GAP-119 / D-241：模板列由模块校验器声明，DB 未登记时现场生成；
            // 否则「下载模板」必然报「导入模板不存在」，导入向导第一步就走不通。
            template = generateTemplate(moduleCode, version);
        }
        if (template.getFileId() == null) {
            throw new ServiceException("模板文件尚未上传，请联系管理员：" + template.getTemplateVersion());
        }
        // 旧版本模板仍可下载，只在提示里标注已过期（REQ-IMP-003 / IMP-Q-05 已裁决）
        return buildDownload(fileRefByFileId(template.getFileId()), false, templateExpiredHint(template));
    }

    /**
     * 按模块校验器声明的表头现场生成导入模板并落库（GAP-119 / D-241）。
     *
     * 步骤：取校验器表头 → 生成只有表头行的 xlsx → 上传统一文件服务 →
     * 登记 `edu_file_ref`（file_kind = import_template）→ 登记 `edu_import_template`（状态 = 当前版本）。
     * 幂等：唯一键 `module_code + template_version`；并发首次下载时后到的一方复用先落库的行。
     */
    private EduImportTemplate generateTemplate(String moduleCode, String version) {
        EduImportHandler handler = importHandler(moduleCode);
        if (handler == null || StringUtils.isBlank(handler.moduleCode())
            || (StringUtils.isNotBlank(version) && !TEMPLATE_FALLBACK_VERSION.equals(version))) {
            throw new ServiceException("导入模板不存在：" + moduleCode
                + (StringUtils.isBlank(version) ? "" : " / " + version));
        }
        List<String> headers = handler.templateHeaders();
        if (headers == null || headers.isEmpty()) {
            throw new ServiceException("模块未声明导入模板列，无法生成模板：" + moduleCode);
        }
        Long schoolId = currentSchoolIdOrNull();
        if (schoolId == null) {
            throw new ServiceException("下载模板缺少学校上下文，已拒绝（REQ-IMP-043）");
        }
        String fileName = "import-template-" + moduleCode + "-" + TEMPLATE_FALLBACK_VERSION + ".xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        byte[] bytes = writeTemplateWorkbook(headers);
        RemoteFile uploaded = remoteFileService.upload(fileName, fileName, contentType, bytes);
        if (uploaded == null || uploaded.getOssId() == null) {
            throw new ServiceException("模板文件生成失败，请稍后重试");
        }
        EduFileRef ref = new EduFileRef();
        ref.setFileId(uploaded.getOssId());
        ref.setFileKind(FILE_KIND_TEMPLATE);
        ref.setFileName(fileName);
        ref.setStorageKey(uploaded.getUrl());
        ref.setContentType(contentType);
        ref.setFileSize((long) bytes.length);
        ref.setBizType("import_template");
        ref.setBizId(moduleCode);
        ref.setSchoolId(schoolId);
        fileRefMapper.insert(ref);

        EduImportTemplate add = new EduImportTemplate();
        add.setModuleCode(moduleCode);
        add.setTemplateVersion(TEMPLATE_FALLBACK_VERSION);
        add.setColumnCount(headers.size());
        add.setFileId(uploaded.getOssId());
        add.setStatus(TEMPLATE_CURRENT);
        try {
            templateMapper.insert(add);
        } catch (DuplicateKeyException e) {
            // 并发首次下载：另一方已落库，直接复用（本次上传的文件引用成为未使用的孤儿，不影响功能）
            EduImportTemplate existed = templateMapper.selectOne(new LambdaQueryWrapper<EduImportTemplate>()
                .eq(EduImportTemplate::getModuleCode, moduleCode)
                .eq(EduImportTemplate::getTemplateVersion, TEMPLATE_FALLBACK_VERSION));
            if (existed != null) {
                return existed;
            }
            throw e;
        }
        log.info("导入模板按校验器表头生成：{} / {}（{} 列）",
            moduleCode, TEMPLATE_FALLBACK_VERSION, headers.size());
        return add;
    }

    /**
     * 写只有表头行的 xlsx（不写示例行：示例数据会被解析成真实导入行并校验失败）。
     * 表头即模块校验器声明的 `templateHeaders()` 顺序，与 `EduImportFileReader` 的严格比对同源。
     */
    private byte[] writeTemplateWorkbook(List<String> headers) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(new ArrayList<>(headers));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            FastExcel.write(out).sheet("导入模板").doWrite(rows);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ServiceException("模板文件生成失败：" + e.getMessage());
        }
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
        // 学校归属在幂等判定之前定下来：同一份文件给不同学校导入时不能复用同一个批次（GAP-115）
        Long schoolId = resolveImportSchoolId(bo.getSchoolId());
        // 幂等：同一文件 + 同一模块 + 同一学校在 24 小时内重复校验，直接复用原批次（REQ-IMP-014 / 017）
        EduImportBatch existed = findReusableBatch(bo.getModuleCode(), sourceFileId, schoolId);
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
        batch.setSchoolId(schoolId);
        batchMapper.insert(batch);
        String guidance = runRowValidation(batch, bo, sourceFile);
        EduImportBatch reloaded = batchMapper.selectById(batch.getBatchId());
        return toValidateResult(reloaded == null ? batch : reloaded, guidance);
    }

    /**
     * 导入的学校归属（GAP-115）。
     *
     * 学校租户只有一所学校，留空即取本校；多校账号（集团 / 运营方）必须显式传入目标学校，
     * 且必须落在本人数据范围内 —— 既不许「取第一个」，也不许「留空由数据库默认」。
     * 学校归属一经确定就随批次落库，执行阶段只按批次的 school_id 取数（D-218 的同一口径）。
     */
    private Long resolveImportSchoolId(Long requested) {
        DataScopeContext context = dataScopeResolver.resolve();
        if (context == null || context.getSchoolIds().isEmpty()) {
            throw new ServiceException("导入缺少学校上下文，拒绝校验（DS-DENY-02）");
        }
        Set<Long> available = context.getSchoolIds();
        if (requested != null) {
            if (!available.contains(requested)) {
                throw new ServiceException("无权导入到该学校，请选择本人可管理的学校（DS-DENY-02）");
            }
            return requested;
        }
        if (available.size() > 1) {
            throw new ServiceException("当前账号可管理多个学校，导入需要显式指定目标学校（GAP-115）");
        }
        return available.iterator().next();
    }

    /**
     * 同步解析文件并逐行校验（REQ-IMP-005 / REQ-STU-053）。
     *
     * 模块未登记校验器时只登记批次并返回指引（不写 edu_import_error）；
     * 已登记时：取文件字节（`edu_file_ref.file_id` 即文件服务 ossId）→ 解析 → 逐行校验 →
     * 失败行写 edu_import_error → 回填批次计数。
     */
    private String runRowValidation(EduImportBatch batch, EduImportValidateBo bo, EduFileRef sourceFile) {
        EduImportHandler handler = importHandler(bo.getModuleCode());
        if (handler == null) {
            return "批次已登记；模块「" + bo.getModuleCode() + "」的行级校验规则尚未落地（GAP-094a），"
                + "规则落地前不会写 edu_import_error。若模板声明行数超过 " + MAX_ROW_COUNT
                + " 行，请拆分为多个批次后重新上传（REQ-IMP-051）。";
        }
        byte[] bytes = readImportFileBytes(sourceFile);
        List<EduImportRow> rows = importFileReader.read(bytes, sourceFile.getFileName(), handler.templateHeaders());
        EduImportContext context = new EduImportContext();
        context.setBatchNo(batch.getBatchNo());
        context.setSchoolId(batch.getSchoolId());
        context.setModuleCode(bo.getModuleCode());
        context.setTermId(bo.getTermId());
        context.setTargetClassId(bo.getTargetClassId());
        context.setStrategy(bo.getStrategy());
        context.setFileId(bo.getFileId());
        context.setFileName(sourceFile.getFileName());
        handler.validateRows(context, rows);
        int invalid = 0;
        for (EduImportRow row : rows) {
            if (StringUtils.isBlank(row.getFailReason())) {
                continue;
            }
            invalid++;
            EduImportError error = new EduImportError();
            error.setSchoolId(batch.getSchoolId());
            error.setBatchNo(batch.getBatchNo());
            error.setRowNo(row.getRowNo());
            error.setResult("invalid");
            error.setFailReason(StringUtils.substring(row.getFailReason(), 0, 500));
            error.setObjectName(row.getCells().get("姓名"));
            error.setRawData(JsonUtils.toJsonString(row.getCells()));
            errorMapper.insert(error);
        }
        EduImportBatch update = new EduImportBatch();
        update.setBatchId(batch.getBatchId());
        update.setRowTotal(rows.size());
        update.setValidCount(rows.size() - invalid);
        update.setInvalidCount(invalid);
        batchMapper.updateById(update);
        return "校验完成：共 " + rows.size() + " 行，可执行 " + (rows.size() - invalid)
            + " 行，失败 " + invalid + " 行。失败行可下载明细逐条修正后作为新批次重新上传（REQ-STU-060）。";
    }

    /** 取文件字节：edu_file_ref.file_id 是文件服务 ossId，先换 url 再取内容（NFR-DATA-03） */
    private byte[] readImportFileBytes(EduFileRef sourceFile) {
        String url = remoteFileService.selectUrlByIds(String.valueOf(sourceFile.getFileId()));
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("文件不存在或已清理，请重新上传");
        }
        return remoteFileService.downloadByUrl(url.split(",")[0].trim());
    }

    private EduImportHandler importHandler(String moduleCode) {
        if (importHandlers == null || StringUtils.isBlank(moduleCode)) {
            return null;
        }
        for (EduImportHandler handler : importHandlers) {
            if (moduleCode.equals(handler.moduleCode())) {
                return handler;
            }
        }
        return null;
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
        // 细粒度数据范围（年级主任看本年级 / 班主任看本班 / 任课教师看本人任教班级）必须在**请求线程**解析：
        // 后台执行没有登录态，数据权限插件不会生效（D-218 / GAP-114）。范围随任务落库，执行阶段按它过滤。
        DataScopeContext exportScope = dataScopeResolver.resolve();
        // 学校归属必须在请求线程定下来：文件引用与下载审计都要求学校非空（GAP-113 / REQ-IMP-043）
        task.setSchoolId(resolveExportSchoolId(exportScope));
        task.setParamsSummary(buildExportParamsSummary(bo, format, plainText, exportScope));
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

    /** 复用批次必须限定在同一学校：多校账号把同一份文件导入不同学校时，不能复用彼此的结果（GAP-115） */
    private EduImportBatch findReusableBatch(String moduleCode, Long sourceFileId, Long schoolId) {
        Date freshLine = new Date(System.currentTimeMillis() - VALIDATE_VALID_HOURS * 3600_000L);
        return batchMapper.selectOne(new LambdaQueryWrapper<EduImportBatch>()
            .eq(EduImportBatch::getModuleCode, moduleCode)
            .eq(EduImportBatch::getSourceFileId, sourceFileId)
            .eq(EduImportBatch::getSchoolId, schoolId)
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
     * 导出任务的学校归属（GAP-113）。
     *
     * `edu_file_ref.school_id` 与 `edu_audit_log.school_id` 都要求非空，而后台执行没有登录态 ——
     * 学校必须在请求线程定下来随任务落库（与 D-218 同一口径）：
     * 数据范围内唯一学校直接用；多所学校时无法判定，明确拒绝而不是落空；
     * 只有班级范围（班主任 / 任课教师）时按班级反推所属学校。
     */
    private Long resolveExportSchoolId(DataScopeContext scope) {
        if (scope == null) {
            throw new ServiceException("导出缺少数据范围上下文，拒绝导出（DS-DENY-01）");
        }
        if (scope.getSchoolIds().size() == 1) {
            return scope.getSchoolIds().iterator().next();
        }
        if (scope.getSchoolIds().size() > 1) {
            throw new ServiceException("当前账号可管理多个学校，导出需要显式指定学校（GAP-113）");
        }
        return schoolIdByClasses(scope);
    }

    /** 只有班级范围（班主任 / 任课教师）时，按班级反推学校；取不到或跨多所学校都拒绝 */
    private Long schoolIdByClasses(DataScopeContext scope) {
        Set<Long> classIds = new HashSet<>(scope.getClassIds());
        classIds.addAll(scope.getTeachingClassIds());
        if (classIds.isEmpty()) {
            throw new ServiceException("导出缺少学校上下文，拒绝导出（DS-DENY-02）");
        }
        Set<Long> schoolIds = new HashSet<>();
        for (EduClass clazz : classMapper.selectByIds(classIds)) {
            if (clazz.getSchoolId() != null) {
                schoolIds.add(clazz.getSchoolId());
            }
        }
        if (schoolIds.size() != 1) {
            throw new ServiceException("导出范围跨多所学校或学校信息缺失，拒绝导出（DS-DENY-02）");
        }
        return schoolIds.iterator().next();
    }

    /**
     * 写一条下载审计（`REQ-IMP-043`）。
     *
     * `edu_audit_log.school_id` 非空（GAP-113）：文件引用上没有学校时用当前登录态的数据范围兜底；
     * 仍取不到就**拒绝下载** —— 没有留痕的下载不允许发生（`REQ-IMP-043` / `DS-DENY-02`）。
     */
    private void writeDownloadLog(EduFileRef fileRef) {
        Long schoolId = fileRef.getSchoolId() == null ? currentSchoolIdOrNull() : fileRef.getSchoolId();
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
        logBo.setDetail("下载文件：" + fileRef.getFileName());
        auditService.recordLog(logBo);
    }

    /** 当前登录态数据范围内唯一学校；没有或多所都返回 null（下载审计兜底，不做「取第一个」） */
    private Long currentSchoolIdOrNull() {
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
        // 真实短时签名链接由统一文件服务签发（REQ-IMP-042 / 045）
        vo.setSignedUrl(remoteFileService.signedDownloadUrl(
            String.valueOf(fileRef.getFileId()), SIGNED_URL_TTL_MINUTES * 60L));
        vo.setSignedUrlExpireTime(new Date(now.getTime() + SIGNED_URL_TTL_MINUTES * 60_000L));
        vo.setExpired(expired);
        vo.setHint(hint);
        writeDownloadLog(fileRef);
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

    private String buildExportParamsSummary(EduExportBo bo, String format, boolean plainText,
                                            DataScopeContext scope) {
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
        // 附加筛选条件由调用方以 JSON 文本透传（如 teaching_assignment 的 teacherId）；
        // 看起来是 JSON 对象时直接内嵌，否则按字符串转义，保证 params_summary 始终是合法 JSON。
        String filters = StringUtils.trimToEmpty(bo.getFilters());
        if (StringUtils.isNotBlank(filters)) {
            if (filters.startsWith("{") && filters.endsWith("}")) {
                sb.append(",\"filters\":").append(filters);
            } else {
                sb.append(",\"filters\":\"").append(filters.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        appendScope(sb, "gradeIds", scope == null ? null : scope.getGradeIds());
        appendScope(sb, "classIds", scope == null ? null : scope.getClassIds());
        return sb.append('}').toString();
    }

    /** 把数据范围里的 id 集合写进任务参数（空集合不写；不写 = 本校全量，与既有口径一致） */
    private void appendScope(StringBuilder sb, String key, java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        StringBuilder list = new StringBuilder();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            if (list.length() > 0) {
                list.append(',');
            }
            list.append(id);
        }
        if (list.length() == 0) {
            return;
        }
        sb.append(",\"").append(key).append("\":[").append(list).append(']');
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
