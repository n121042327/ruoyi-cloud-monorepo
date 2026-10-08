package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志 edu_audit_log
 *
 * **只允许追加，任何业务代码不得更新或删除**（`BR-AUDIT-003` / `NFR-AUDIT-05` / `REQ-AUD-030`）；
 * 关键写操作与日志在同一事务内提交（`REQ-AUD-035` / `BR-AUDIT-010`）；
 * 幂等键 = `(request_id, object_id, action_type)`（`uk_audit_idempotent`）。
 * 日志本身不得包含敏感字段明文，只记字段名与掩码后的值（`REQ-AUD-010` / `BR-AUDIT-007`）。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：没有 create_by / update_time /
 * del_flag，记录时间列是 `log_time`，因此**不继承** BaseEntity / TenantEntity，
 * 只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_audit_log")
public class EduAuditLog {

    /** 日志 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long logId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** create / update / delete / import / export / approve / grant / login / status_change */
    private String actionType;

    /** 所属模块 */
    private String moduleCode;

    /** 对象类型（student / class / teacher / stream 等） */
    private String objectType;

    /** 对象标识 */
    private String objectId;

    /** 对象名称（便于阅读，如 高一 (1) 班） */
    private String objectName;

    /** 操作人；平台运营访问时为运营账号 */
    private Long operatorId;

    /** 操作人角色快照 */
    private String operatorRole;

    /** 来源 IP */
    private String clientIp;

    /** 请求标识（用于幂等写入与排障） */
    private String requestId;

    /** 批次号（批量操作共用） */
    private String batchNo;

    /** success / failed */
    private String actionResult;

    /** web / api / job / mq */
    private String source;

    /** 说明（驳回意见、失败原因等） */
    private String detail;

    /** 记录时间 */
    private Date logTime;

}
