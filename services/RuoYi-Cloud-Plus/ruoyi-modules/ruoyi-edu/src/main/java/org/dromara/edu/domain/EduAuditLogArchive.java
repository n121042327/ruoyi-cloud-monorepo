package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 归档操作日志 edu_audit_log_archive
 *
 * 与在线表 `edu_audit_log` **同构**（schema.yaml 的 `derived_tables`：`CREATE TABLE ... LIKE`）；
 * 归档 = 按时间范围把行从在线表搬进本表，并写 `edu_audit_archive_batch`；
 * 归档后仍可按时间范围检索（`REQ-AUD-033` / `BR-AUDIT-011`），保留期内不得清理（`REQ-AUD-032`）。
 *
 * **本表与在线表同构**：with_audit 为 false、soft_delete 为 false，不继承 BaseEntity / TenantEntity。
 *
 * @author Codex
 */
@Data
@TableName("edu_audit_log_archive")
public class EduAuditLogArchive {

    /** 日志 ID（物理列 `id`，与在线表同构） */
    @TableId(value = "id")
    private Long logId;

    /** 租户隔离键 */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 操作类型 */
    private String actionType;

    /** 所属模块 */
    private String moduleCode;

    /** 对象类型 */
    private String objectType;

    /** 对象标识 */
    private String objectId;

    /** 对象名称 */
    private String objectName;

    /** 操作人 */
    private Long operatorId;

    /** 操作人角色快照 */
    private String operatorRole;

    /** 来源 IP */
    private String clientIp;

    /** 请求标识 */
    private String requestId;

    /** 批次号 */
    private String batchNo;

    /** success / failed */
    private String actionResult;

    /** web / api / job / mq */
    private String source;

    /** 说明 */
    private String detail;

    /** 记录时间（分区键） */
    private Date logTime;

}
