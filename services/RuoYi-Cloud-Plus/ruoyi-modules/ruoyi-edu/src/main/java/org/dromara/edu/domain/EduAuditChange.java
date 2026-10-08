package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 日志变更明细 edu_audit_change
 *
 * **只记录发生变化的字段**（`REQ-AUD-003`），只记录字段名与变更前后值（`REQ-AUD-010`）；
 * `(log_id, field_name)` 唯一。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：不继承 BaseEntity /
 * TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_audit_change")
public class EduAuditChange {

    /** 明细 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long changeId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 操作日志 ID */
    private Long logId;

    /** 字段名 */
    private String fieldName;

    /** 变更前值（敏感字段掩码） */
    private String beforeValue;

    /** 变更后值（敏感字段掩码） */
    private String afterValue;

}
