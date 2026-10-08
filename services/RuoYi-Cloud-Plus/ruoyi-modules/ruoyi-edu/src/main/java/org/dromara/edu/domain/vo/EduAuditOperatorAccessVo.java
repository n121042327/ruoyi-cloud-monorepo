package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 运营访问记录视图对象（与 `v_edu_audit_operator_access` 的列对齐）
 *
 * 平台运营的任何数据访问都要形成运营访问记录并**归属到被访问的租户**
 * （`REQ-AUD-013` / `BR-AUDIT-005`）；租户侧可自助查询与导出**只含本租户**的记录
 * （`REQ-AUD-015` / `REQ-AUD-016` / `SCN-AUDIT-02` / `SCN-AUDIT-03`）。
 *
 * 注意：视图 `v_edu_audit_operator_access` 没有 `tenant_id` 时无法做租户隔离，
 * 因此本 VO 由 `edu_audit_log` 基表按 `operator_role = 'platform_ops'` 等价过滤后投影，
 * 详见 CR-092 与 GAP-095。
 *
 * @author Codex
 */
@Data
public class EduAuditOperatorAccessVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 日志 ID（对应视图的 id） */
    private Long logId;

    private Date logTime;

    /** 运营账号 */
    private Long operatorId;

    private String objectType;

    private String objectId;

    /** 访问动作（查看 / 导出） */
    private String actionType;

    /** 用途说明（对应视图的 detail AS purpose） */
    private String purpose;

    private String clientIp;

    private Long schoolId;

}
