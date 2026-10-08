package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 任务重试记录 edu_async_task_retry
 *
 * 每次重试一条记录，`(task_no, retry_no)` 唯一，用于判断「是否超过最大重试次数」（REQ-IMP-039）。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：只有 `create_time`，
 * 因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_async_task_retry")
public class EduAsyncTaskRetry {

    /** 重试记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long retryId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 任务编号 */
    private String taskNo;

    /** 第几次重试（从 1 开始） */
    private Integer retryNo;

    /** success / failed / timeout */
    private String result;

    /** 本次失败原因 */
    private String errorMsg;

    /** 重试时间 */
    private Date createTime;

}
