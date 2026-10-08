package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 日志归档批次 edu_audit_archive_batch
 *
 * 在线保留窗口（默认 12 个月）到期后归档；归档可按时间范围检索（`REQ-AUD-033`）；
 * 保留期不少于 3 年，保留期内不清理（`REQ-AUD-032`）；归档动作本身写日志
 * （归档批次、范围、操作人，`REQ-AUD-034`）。
 *
 * **本表 scope 为 tenant、with_audit 为 false、soft_delete 为 false**（schema.yaml）：
 * 有 tenant_id 但**没有 school_id**，不继承 BaseEntity / TenantEntity。
 *
 * @author Codex
 */
@Data
@TableName("edu_audit_archive_batch")
public class EduAuditArchiveBatch {

    /** 归档批次 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long archiveId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 归档批次号（唯一） */
    private String archiveNo;

    /** 归档范围开始 */
    private Date rangeStart;

    /** 归档范围结束 */
    private Date rangeEnd;

    /** 归档行数 */
    private Long rowCount;

    /** 归档文件引用 */
    private Long fileId;

    /** running 进行中 / done 已完成 / failed 失败 */
    private String archiveStatus;

    /** 操作人 */
    private Long operatorId;

    /** 完成时间 */
    private Date archiveTime;

    /** 失败原因 */
    private String errorMsg;

}
