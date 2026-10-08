package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录与安全事件视图对象（与 `v_edu_audit_security_event` 的列对齐）
 *
 * 记录登录失败、账号锁定、激活码查看与重置、学号变更、权限变更（`REQ-AUD-007` / `BR-AUDIT-012`）。
 *
 * 注意：视图 `v_edu_audit_security_event` 没有 `tenant_id` 列，无法做租户隔离，
 * 因此本 VO 由 `edu_audit_log` 基表按同一组 `action_type` 等价过滤后投影，
 * 详见 CR-092 与 GAP-095。
 *
 * @author Codex
 */
@Data
public class EduAuditSecurityEventVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志 ID（对应视图的 id） */
    private Long logId;

    private Date logTime;

    private String actionType;

    private Long operatorId;

    private String operatorRole;

    private String objectId;

    private String actionResult;

    private String clientIp;

    private String detail;

}
