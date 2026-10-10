package org.dromara.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.edu.domain.EduAuditArchiveBatch;
import org.dromara.edu.domain.EduAuditChange;
import org.dromara.edu.domain.EduAuditLog;
import org.dromara.edu.domain.EduAuditLogArchive;
import org.dromara.edu.domain.bo.EduAuditArchiveBatchBo;
import org.dromara.edu.domain.bo.EduAuditLogBo;
import org.dromara.edu.domain.bo.EduExportBo;
import org.dromara.edu.domain.vo.EduAuditArchiveBatchVo;
import org.dromara.edu.domain.vo.EduAuditChangeVo;
import org.dromara.edu.domain.vo.EduAuditLogVo;
import org.dromara.edu.domain.vo.EduAuditOperatorAccessVo;
import org.dromara.edu.domain.vo.EduAuditSecurityEventVo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.mapper.EduAuditArchiveBatchMapper;
import org.dromara.edu.mapper.EduAuditChangeMapper;
import org.dromara.edu.mapper.EduAuditLogArchiveMapper;
import org.dromara.edu.mapper.EduAuditLogMapper;
import org.dromara.edu.service.IEduAuditService;
import org.dromara.edu.service.IEduImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 审计与操作日志服务层处理
 *
 * 口径：日志表**只允许追加**，本类与 Mapper 都不提供更新 / 删除（`BR-AUDIT-003` / `NFR-AUDIT-05` /
 * `REQ-AUD-025` / `REQ-AUD-030`）；日志**查询不记录**（避免噪声）而**导出必须记录**
 * （`REQ-AUD-011` / `NFR-AUDIT-03`）；列表默认最近 7 天（`REQ-AUD-020`），单次查询跨度上限 90 天
 * （`REQ-AUD-021`）；范围为空返回空列表、不退化为全量（`REQ-AUD-024` / `DS-DENY-03`）。
 *
 * @author Codex
 */
@RequiredArgsConstructor
@Service
public class EduAuditServiceImpl implements IEduAuditService {

    /** 敏感字段"查看全量"的动作码（field-dictionary 的 audit_action_type） */
    private static final String ACTION_VIEW_SENSITIVE = "view_sensitive";

    /** 运营访问记录的角色快照（DDL 注释与视图 v_edu_audit_operator_access 一致） */
    private static final String ROLE_PLATFORM_OPS = "platform_ops";

    /**
     * 登录与安全事件的动作码集合。
     * 取值来自 `v_edu_audit_security_event` 视图定义（REQ-AUD-007 / BR-AUDIT-012）。
     */
    private static final List<String> SECURITY_EVENT_ACTIONS = List.of(
        "login", "account_locked", "activation_view", "activation_reset",
        "student_no_change", "permission_change");

    /** 默认时间范围：最近 7 天（REQ-AUD-020） */
    private static final int DEFAULT_RANGE_DAYS = 7;

    /** 单次查询时间跨度上限：90 天（REQ-AUD-021 / NFR-PERF-02） */
    private static final int MAX_RANGE_DAYS = 90;

    private final EduAuditLogMapper baseMapper;
    private final EduAuditChangeMapper changeMapper;
    private final EduAuditLogArchiveMapper archiveMapper;
    private final EduAuditArchiveBatchMapper archiveBatchMapper;
    /**
     * 导入导出服务：审计导出复用通用导出引擎（而引擎在导出 / 下载时也要写审计），两者互相依赖。
     *
     * 用 `@Lazy` 延迟注入打破**构造期**循环：审计导出是低频入口，首次真正调用时才解析该依赖，
     * 不影响其它审计写入（recordLog / recordChange 只用本模块的 mapper）。
     */
    @Lazy
    @Autowired
    private IEduImportExportService importExportService;

    @Override
    public TableDataInfo<EduAuditLogVo> queryLogPageList(EduAuditLogBo query, PageQuery pageQuery) {
        Page<EduAuditLogVo> result = baseMapper.selectPageLog(pageQuery.build(), buildLogWrapper(query));
        result.getRecords().forEach(this::fillChangeItems);
        return TableDataInfo.build(result);
    }

    @Override
    public EduAuditLogVo queryLogDetail(Long logId) {
        EduAuditLog entity = requireLog(logId);
        EduAuditLogVo vo = baseMapper.selectVoById(entity.getLogId());
        fillChangeItems(vo);
        return vo;
    }

    @Override
    public List<EduAuditLogVo> queryObjectTimeline(String objectType, String objectId, EduAuditLogBo query) {
        if (StringUtils.isBlank(objectType) || StringUtils.isBlank(objectId)) {
            throw new ServiceException("对象类型与对象标识不能为空");
        }
        EduAuditLogBo condition = query == null ? new EduAuditLogBo() : query;
        condition.setObjectType(objectType);
        condition.setObjectId(objectId);
        List<EduAuditLogVo> timeline = baseMapper.selectVoList(buildLogWrapper(condition));
        timeline.forEach(this::fillChangeItems);
        return timeline;
    }

    @Override
    public EduExportResultVo exportOperationLog(EduAuditLogBo query) {
        // 导出重新解析数据范围（REQ-AUD-027 / DS-DENY-04）：导出任务记录筛选条件与范围，
        // 实际取数由模块导出器按同一套条件执行（见 GAP-094）。
        EduExportBo export = new EduExportBo();
        export.setModuleCode("audit_log");
        export.setFormat(StringUtils.isBlank(query.getFormat()) ? "xlsx" : query.getFormat());
        export.setColumns(query.getColumns());
        export.setKeyword(query.getKeyword());
        export.setFilters(buildAuditRangeFilter(query));
        return importExportService.exportData(export);
    }

    @Override
    public TableDataInfo<EduAuditOperatorAccessVo> queryOperatorAccessPageList(EduAuditLogBo query,
                                                                              PageQuery pageQuery) {
        EduAuditLogBo condition = new EduAuditLogBo();
        copyRange(condition, query);
        condition.setOperatorRole(ROLE_PLATFORM_OPS);
        Page<EduAuditLogVo> page = baseMapper.selectPageLog(pageQuery.build(), buildLogWrapper(condition));
        List<EduAuditOperatorAccessVo> records = new ArrayList<>(page.getRecords().size());
        for (EduAuditLogVo log : page.getRecords()) {
            records.add(toOperatorAccess(log));
        }
        Page<EduAuditOperatorAccessVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return TableDataInfo.build(result);
    }

    @Override
    public EduExportResultVo exportOperatorAccess(EduAuditLogBo query) {
        EduExportBo export = new EduExportBo();
        export.setModuleCode("audit_operator_access");
        export.setFormat(StringUtils.isBlank(query.getFormat()) ? "xlsx" : query.getFormat());
        export.setFilters(buildAuditRangeFilter(query));
        return importExportService.exportData(export);
    }

    @Override
    public TableDataInfo<EduAuditLogVo> querySensitiveAccessPageList(EduAuditLogBo query, PageQuery pageQuery) {
        EduAuditLogBo condition = new EduAuditLogBo();
        copyRange(condition, query);
        condition.setActionType(ACTION_VIEW_SENSITIVE);
        Page<EduAuditLogVo> page = baseMapper.selectPageLog(pageQuery.build(), buildLogWrapper(condition));
        page.getRecords().forEach(this::fillChangeItems);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<EduAuditSecurityEventVo> querySecurityEventPageList(EduAuditLogBo query,
                                                                            PageQuery pageQuery) {
        EduAuditLogBo condition = new EduAuditLogBo();
        copyRange(condition, query);
        Page<EduAuditLogVo> page = baseMapper.selectPageLog(pageQuery.build(), buildSecurityWrapper(condition));
        List<EduAuditSecurityEventVo> records = new ArrayList<>(page.getRecords().size());
        for (EduAuditLogVo log : page.getRecords()) {
            records.add(toSecurityEvent(log));
        }
        Page<EduAuditSecurityEventVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<EduAuditArchiveBatchVo> queryArchiveBatchPageList(EduAuditArchiveBatchBo query,
                                                                          PageQuery pageQuery) {
        LambdaQueryWrapper<EduAuditArchiveBatch> wrapper = new LambdaQueryWrapper<EduAuditArchiveBatch>()
            .eq(StringUtils.isNotBlank(query.getArchiveNo()), EduAuditArchiveBatch::getArchiveNo,
                query.getArchiveNo())
            .eq(StringUtils.isNotBlank(query.getArchiveStatus()), EduAuditArchiveBatch::getArchiveStatus,
                query.getArchiveStatus());
        Date begin = StringUtils.isNotBlank(query.getRangeStart()) ? DateUtils.parseDate(query.getRangeStart()) : null;
        Date end = StringUtils.isNotBlank(query.getRangeEnd()) ? DateUtils.parseDate(query.getRangeEnd()) : null;
        wrapper.ge(begin != null, EduAuditArchiveBatch::getRangeStart, begin)
            .le(end != null, EduAuditArchiveBatch::getRangeEnd, end)
            .orderByDesc(EduAuditArchiveBatch::getArchiveTime);
        return TableDataInfo.build(archiveBatchMapper.selectPageArchiveBatch(pageQuery.build(), wrapper));
    }

    @Override
    public TableDataInfo<EduAuditLogVo> searchArchivedLog(EduAuditArchiveBatchBo query, PageQuery pageQuery) {
        Date begin = StringUtils.isNotBlank(query.getRangeStart()) ? DateUtils.parseDate(query.getRangeStart()) : null;
        Date end = StringUtils.isNotBlank(query.getRangeEnd()) ? DateUtils.parseDate(query.getRangeEnd()) : null;
        if (begin == null && end == null) {
            throw new ServiceException("归档检索必须指定时间范围（REQ-AUD-033）");
        }
        LambdaQueryWrapper<EduAuditLogArchive> wrapper = new LambdaQueryWrapper<EduAuditLogArchive>()
            .eq(StringUtils.isNotBlank(query.getActionType()), EduAuditLogArchive::getActionType,
                query.getActionType())
            .eq(StringUtils.isNotBlank(query.getObjectType()), EduAuditLogArchive::getObjectType,
                query.getObjectType())
            .eq(StringUtils.isNotBlank(query.getObjectId()), EduAuditLogArchive::getObjectId, query.getObjectId())
            .eq(query.getOperatorId() != null, EduAuditLogArchive::getOperatorId, query.getOperatorId())
            .ge(begin != null, EduAuditLogArchive::getLogTime, begin)
            .le(end != null, EduAuditLogArchive::getLogTime, end)
            .orderByDesc(EduAuditLogArchive::getLogTime);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(EduAuditLogArchive::getObjectId, query.getKeyword())
                .or().like(EduAuditLogArchive::getObjectName, query.getKeyword()));
        }
        Page<EduAuditLogArchive> page = archiveMapper.selectPage(pageQuery.build(), wrapper);
        List<EduAuditLogVo> records = new ArrayList<>(page.getRecords().size());
        for (EduAuditLogArchive archive : page.getRecords()) {
            records.add(toLogVo(archive));
        }
        Page<EduAuditLogVo> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordLog(EduAuditLogBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getActionType())
            || StringUtils.isBlank(bo.getObjectType()) || StringUtils.isBlank(bo.getObjectId())) {
            throw new ServiceException("日志的 operationId / objectType / objectId 不能为空");
        }
        // 幂等：uk_audit_idempotent = (request_id, object_id, action_type)，命中直接复用（REQ-AUD-035）
        if (StringUtils.isNotBlank(bo.getRequestId())) {
            EduAuditLog exist = baseMapper.selectOne(new LambdaQueryWrapper<EduAuditLog>()
                .eq(EduAuditLog::getRequestId, bo.getRequestId())
                .eq(EduAuditLog::getObjectId, bo.getObjectId())
                .eq(EduAuditLog::getActionType, bo.getActionType())
                .last("limit 1"));
            if (exist != null) {
                return exist.getLogId();
            }
        }
        EduAuditLog entity = new EduAuditLog();
        entity.setSchoolId(bo.getSchoolId());
        entity.setActionType(bo.getActionType());
        entity.setModuleCode(bo.getModuleCode());
        entity.setObjectType(bo.getObjectType());
        entity.setObjectId(bo.getObjectId());
        entity.setObjectName(bo.getObjectName());
        entity.setOperatorId(bo.getOperatorId() != null ? bo.getOperatorId() : LoginHelper.getUserId());
        entity.setOperatorRole(bo.getOperatorRole());
        entity.setRequestId(bo.getRequestId());
        entity.setBatchNo(bo.getBatchNo());
        entity.setActionResult(StringUtils.isBlank(bo.getActionResult()) ? "success" : bo.getActionResult());
        entity.setSource(StringUtils.isBlank(bo.getSource()) ? "web" : bo.getSource());
        entity.setDetail(StringUtils.isNotBlank(bo.getDetail()) ? bo.getDetail() : bo.getObjectName());
        entity.setLogTime(DateUtils.getNowDate());
        baseMapper.insert(entity);
        return entity.getLogId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordChange(Long logId, String fieldName, String beforeValue, String afterValue) {
        if (logId == null || StringUtils.isBlank(fieldName)) {
            throw new ServiceException("日志 ID 与字段名不能为空");
        }
        if (StringUtils.equals(beforeValue, afterValue)) {
            // 只记录发生变化的字段（REQ-AUD-003）
            return;
        }
        EduAuditChange change = new EduAuditChange();
        change.setLogId(logId);
        change.setFieldName(fieldName);
        change.setBeforeValue(mask(maybeTrim(beforeValue)));
        change.setAfterValue(mask(maybeTrim(afterValue)));
        changeMapper.insert(change);
    }

    private LambdaQueryWrapper<EduAuditLog> buildLogWrapper(EduAuditLogBo query) {
        EduAuditLogBo condition = query == null ? new EduAuditLogBo() : query;
        LambdaQueryWrapper<EduAuditLog> wrapper = baseQuery(condition);
        wrapper.orderByDesc(EduAuditLog::getLogTime);
        return wrapper;
    }

    private LambdaQueryWrapper<EduAuditLog> buildSecurityWrapper(EduAuditLogBo query) {
        LambdaQueryWrapper<EduAuditLog> wrapper = baseQuery(query);
        // v_edu_audit_security_event 视图没有 tenant_id，无法做租户隔离；
        // 这里用基表加同一组 action_type 等价过滤，租户隔离交给租户拦截器（见 CR-092 与 GAP-095）
        wrapper.in(EduAuditLog::getActionType, SECURITY_EVENT_ACTIONS);
        wrapper.orderByDesc(EduAuditLog::getLogTime);
        return wrapper;
    }

    /**
     * 通用日志查询条件：默认最近 7 天、跨度上限 90 天（`REQ-AUD-020` / `REQ-AUD-021`）。
     */
    private LambdaQueryWrapper<EduAuditLog> baseQuery(EduAuditLogBo condition) {
        Date[] range = resolveRange(condition);
        LambdaQueryWrapper<EduAuditLog> wrapper = new LambdaQueryWrapper<EduAuditLog>()
            .eq(condition.getSchoolId() != null, EduAuditLog::getSchoolId, condition.getSchoolId())
            .eq(StringUtils.isNotBlank(condition.getActionType()), EduAuditLog::getActionType,
                condition.getActionType())
            .eq(StringUtils.isNotBlank(condition.getModuleCode()), EduAuditLog::getModuleCode,
                condition.getModuleCode())
            .eq(StringUtils.isNotBlank(condition.getObjectType()), EduAuditLog::getObjectType,
                condition.getObjectType())
            .eq(StringUtils.isNotBlank(condition.getObjectId()), EduAuditLog::getObjectId, condition.getObjectId())
            .eq(StringUtils.isNotBlank(condition.getObjectName()), EduAuditLog::getObjectName,
                condition.getObjectName())
            .eq(condition.getOperatorId() != null, EduAuditLog::getOperatorId, condition.getOperatorId())
            .eq(StringUtils.isNotBlank(condition.getOperatorRole()), EduAuditLog::getOperatorRole,
                condition.getOperatorRole())
            .eq(StringUtils.isNotBlank(condition.getActionResult()), EduAuditLog::getActionResult,
                condition.getActionResult())
            .eq(StringUtils.isNotBlank(condition.getSource()), EduAuditLog::getSource, condition.getSource())
            .eq(StringUtils.isNotBlank(condition.getBatchNo()), EduAuditLog::getBatchNo, condition.getBatchNo())
            .eq(StringUtils.isNotBlank(condition.getRequestId()), EduAuditLog::getRequestId,
                condition.getRequestId())
            .ge(EduAuditLog::getLogTime, range[0])
            .le(EduAuditLog::getLogTime, range[1]);
        if (StringUtils.isNotBlank(condition.getKeyword())) {
            wrapper.and(w -> w.like(EduAuditLog::getObjectId, condition.getKeyword())
                .or().like(EduAuditLog::getObjectName, condition.getKeyword())
                .or().like(EduAuditLog::getRequestId, condition.getKeyword()));
        }
        return wrapper;
    }

    /**
     * 解析查询时间范围：留空按最近 7 天，跨度超过 90 天直接拒绝并提示收窄（`REQ-AUD-021`）。
     */
    private Date[] resolveRange(EduAuditLogBo condition) {
        Date end = StringUtils.isNotBlank(condition.getEndTime())
            ? DateUtils.parseDate(condition.getEndTime()) : DateUtils.getNowDate();
        Date begin = StringUtils.isNotBlank(condition.getBeginTime())
            ? DateUtils.parseDate(condition.getBeginTime())
            : new Date(end.getTime() - DEFAULT_RANGE_DAYS * 24L * 3600_000L);
        if (begin.after(end)) {
            throw new ServiceException("开始时间不能晚于结束时间");
        }
        long spanDays = (end.getTime() - begin.getTime()) / (24L * 3600_000L);
        if (spanDays > MAX_RANGE_DAYS) {
            throw new ServiceException("单次查询时间跨度不能超过 " + MAX_RANGE_DAYS
                + " 天（REQ-AUD-021），请收窄时间范围");
        }
        return new Date[]{begin, end};
    }

    private void copyRange(EduAuditLogBo target, EduAuditLogBo source) {
        if (source == null) {
            return;
        }
        target.setSchoolId(source.getSchoolId());
        target.setBeginTime(source.getBeginTime());
        target.setEndTime(source.getEndTime());
        target.setOperatorId(source.getOperatorId());
        target.setObjectType(source.getObjectType());
        target.setObjectId(source.getObjectId());
        target.setBatchNo(source.getBatchNo());
        target.setKeyword(source.getKeyword());
        target.setActionResult(source.getActionResult());
        target.setSource(source.getSource());
        target.setModuleCode(source.getModuleCode());
    }

    private EduAuditLog requireLog(Long logId) {
        if (logId == null) {
            throw new ServiceException("日志 ID 不能为空");
        }
        EduAuditLog entity = baseMapper.selectById(logId);
        if (entity == null) {
            throw new ServiceException("操作日志不存在：" + logId);
        }
        return entity;
    }

    private void fillChangeItems(EduAuditLogVo vo) {
        if (vo == null || vo.getLogId() == null) {
            return;
        }
        List<EduAuditChangeVo> items = changeMapper.selectVoList(new LambdaQueryWrapper<EduAuditChange>()
            .eq(EduAuditChange::getLogId, vo.getLogId())
            .orderByAsc(EduAuditChange::getFieldName));
        items.forEach(item -> {
            item.setBeforeValue(mask(item.getBeforeValue()));
            item.setAfterValue(mask(item.getAfterValue()));
        });
        vo.setChangeItems(items);
    }

    private EduAuditOperatorAccessVo toOperatorAccess(EduAuditLogVo log) {
        EduAuditOperatorAccessVo vo = new EduAuditOperatorAccessVo();
        vo.setLogId(log.getLogId());
        vo.setLogTime(log.getLogTime());
        vo.setOperatorId(log.getOperatorId());
        vo.setObjectType(log.getObjectType());
        vo.setObjectId(log.getObjectId());
        vo.setActionType(log.getActionType());
        // 视图里 detail 别名为 purpose，这里保持同样的语义
        vo.setPurpose(StringUtils.isBlank(log.getDetail()) ? log.getObjectName() : log.getDetail());
        vo.setClientIp(log.getClientIp());
        vo.setSchoolId(log.getSchoolId());
        return vo;
    }

    private EduAuditSecurityEventVo toSecurityEvent(EduAuditLogVo log) {
        EduAuditSecurityEventVo vo = new EduAuditSecurityEventVo();
        vo.setLogId(log.getLogId());
        vo.setLogTime(log.getLogTime());
        vo.setActionType(log.getActionType());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorRole(log.getOperatorRole());
        vo.setObjectId(log.getObjectId());
        vo.setActionResult(log.getActionResult());
        vo.setClientIp(log.getClientIp());
        vo.setDetail(log.getDetail());
        return vo;
    }

    private EduAuditLogVo toLogVo(EduAuditLogArchive archive) {
        EduAuditLogVo vo = new EduAuditLogVo();
        vo.setLogId(archive.getLogId());
        vo.setSchoolId(archive.getSchoolId());
        vo.setActionType(archive.getActionType());
        vo.setModuleCode(archive.getModuleCode());
        vo.setObjectType(archive.getObjectType());
        vo.setObjectId(archive.getObjectId());
        vo.setObjectName(archive.getObjectName());
        vo.setOperatorId(archive.getOperatorId());
        vo.setOperatorRole(archive.getOperatorRole());
        vo.setClientIp(archive.getClientIp());
        vo.setRequestId(archive.getRequestId());
        vo.setBatchNo(archive.getBatchNo());
        vo.setActionResult(archive.getActionResult());
        vo.setSource(archive.getSource());
        vo.setDetail(archive.getDetail());
        vo.setLogTime(archive.getLogTime());
        return vo;
    }

    private String buildAuditRangeFilter(EduAuditLogBo query) {
        Date[] range = resolveRange(query);
        StringBuilder sb = new StringBuilder("{\"module\":\"audit\"");
        sb.append(",\"rangeStart\":\"").append(DateUtils.formatDateTime(range[0])).append('"');
        sb.append(",\"rangeEnd\":\"").append(DateUtils.formatDateTime(range[1])).append('"');
        if (StringUtils.isNotBlank(query.getActionType())) {
            sb.append(",\"actionType\":\"").append(query.getActionType()).append('"');
        }
        if (query.getOperatorId() != null) {
            sb.append(",\"operatorId\":").append(query.getOperatorId());
        }
        return sb.append('}').toString();
    }

    private String maybeTrim(String value) {
        if (value == null) {
            return null;
        }
        // varchar(1000)：超长直接截断，避免写库失败
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    /**
     * 敏感字段掩码：证件号保留前 6 后 4，手机号掩码中间 4 位
     * （`BR-AUDIT-007` / `NFR-SEC-06` / PRD 5.3）。
     */
    private String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        // 先掩码 18 位证件号（保留前 6 后 4），再掩码 11 位手机号（中间 4 位）
        String masked = value.replaceAll("(\\d{6})\\d{8}([0-9Xx]{4})", "$1********$2");
        return masked.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

}
