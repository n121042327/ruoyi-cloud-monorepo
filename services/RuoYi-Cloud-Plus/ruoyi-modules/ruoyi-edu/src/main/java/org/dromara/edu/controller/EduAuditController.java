package org.dromara.edu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.edu.domain.bo.EduAuditArchiveBatchBo;
import org.dromara.edu.domain.bo.EduAuditLogBo;
import org.dromara.edu.domain.vo.EduAuditArchiveBatchVo;
import org.dromara.edu.domain.vo.EduAuditLogVo;
import org.dromara.edu.domain.vo.EduAuditOperatorAccessVo;
import org.dromara.edu.domain.vo.EduAuditSecurityEventVo;
import org.dromara.edu.domain.vo.EduExportResultVo;
import org.dromara.edu.service.IEduAuditService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审计与操作日志
 *
 * 覆盖 audit 模块全部 10 个 operationId：listOperationLog / getOperationLog / listObjectChangeLog /
 * exportOperationLog / listOperatorAccess / exportOperatorAccess / listSensitiveAccess /
 * listSecurityEvent / listArchiveBatch / searchArchivedLog。
 *
 * 口径：日志表**只允许追加**，本控制器**不提供任何更新与删除入口**
 * （`BR-AUDIT-003` / `NFR-AUDIT-05` / `REQ-AUD-025`）；日志**查询不记录**（避免噪声）而
 * **导出必须记录**（`REQ-AUD-011` / `NFR-AUDIT-03`）；列表默认最近 7 天、单次跨度上限 90 天
 * （`REQ-AUD-020` / `REQ-AUD-021`）；日志查询受数据范围约束，范围为空返回空列表
 * （`REQ-AUD-024` / `DS-DENY-03`）。
 *
 * 权限依据 `docs/10-prd/05-permission-matrix.yaml`：查询用 `audit.log` 的 `read`，
 * 导出用 `audit.log` 的 `export`。
 *
 * @author Codex
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/edu/audit")
public class EduAuditController extends BaseController {

    private final IEduAuditService auditService;

    /** 操作日志分页查询（按时间范围、操作人、对象、操作类型、批次号筛选） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/log/list")
    public TableDataInfo<EduAuditLogVo> listOperationLog(EduAuditLogBo log, PageQuery pageQuery) {
        return auditService.queryLogPageList(log, pageQuery);
    }

    /** 日志详情（含变更明细 diff）；不提供修改与删除 */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/log/{logId}")
    public R<EduAuditLogVo> getOperationLog(@PathVariable Long logId) {
        return R.ok(auditService.queryLogDetail(logId));
    }

    /** 对象变更时间线（该对象的全部变更记录，REQ-AUD-023） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/object/{objectType}/{objectId}/timeline")
    public R<List<EduAuditLogVo>> listObjectChangeLog(@PathVariable String objectType,
                                                      @PathVariable String objectId,
                                                      EduAuditLogBo log) {
        return R.ok(auditService.queryObjectTimeline(objectType, objectId, log));
    }

    /** 日志导出（导出行为本身写入审计，REQ-AUD-029 / NFR-AUDIT-03） */
    @SaCheckPermission("audit.log:export")
    @Log(title = "审计日志", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/log/export")
    public R<EduExportResultVo> exportOperationLog(@RequestBody EduAuditLogBo log) {
        return R.ok(auditService.exportOperationLog(log));
    }

    /** 运营访问记录（租户侧自助查询，REQ-AUD-013 ~ 015） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/operator-access/list")
    public TableDataInfo<EduAuditOperatorAccessVo> listOperatorAccess(EduAuditLogBo log, PageQuery pageQuery) {
        return auditService.queryOperatorAccessPageList(log, pageQuery);
    }

    /** 运营访问记录导出（只含本租户数据，REQ-AUD-016） */
    @SaCheckPermission("audit.log:export")
    @Log(title = "运营访问记录", businessType = BusinessType.EXPORT)
    @RepeatSubmit()
    @PostMapping("/operator-access/export")
    public R<EduExportResultVo> exportOperatorAccess(@RequestBody EduAuditLogBo log) {
        return R.ok(auditService.exportOperatorAccess(log));
    }

    /** 敏感数据访问记录（查看全量才算，掩码展示不记，REQ-AUD-008 / 009 / 012） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/sensitive-access/list")
    public TableDataInfo<EduAuditLogVo> listSensitiveAccess(EduAuditLogBo log, PageQuery pageQuery) {
        return auditService.querySensitiveAccessPageList(log, pageQuery);
    }

    /** 登录与安全事件（REQ-AUD-007 / BR-AUDIT-012） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/security-event/list")
    public TableDataInfo<EduAuditSecurityEventVo> listSecurityEvent(EduAuditLogBo log, PageQuery pageQuery) {
        return auditService.querySecurityEventPageList(log, pageQuery);
    }

    /** 归档批次列表（REQ-AUD-033 / 034） */
    @SaCheckPermission("audit.log:read")
    @GetMapping("/archive/list")
    public TableDataInfo<EduAuditArchiveBatchVo> listArchiveBatch(EduAuditArchiveBatchBo archive,
                                                                 PageQuery pageQuery) {
        return auditService.queryArchiveBatchPageList(archive, pageQuery);
    }

    /** 归档区间检索（归档后仍可按时间范围检索，REQ-AUD-033） */
    @SaCheckPermission("audit.log:read")
    @PostMapping("/archive/search")
    public TableDataInfo<EduAuditLogVo> searchArchivedLog(@RequestBody EduAuditArchiveBatchBo archive,
                                                          PageQuery pageQuery) {
        return auditService.searchArchivedLog(archive, pageQuery);
    }

}
