package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 死信任务 edu_dead_letter_task
 *
 * 超过最大重试次数的任务进入死信，提供运维查看与重放（REQ-IMP-038）。
 * 重放沿用原批次号与幂等键，重放原因必填并写审计；记录只追加，不提供删除入口。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：没有 create_by /
 * update_time / del_flag，因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_dead_letter_task")
public class EduDeadLetterTask {

    /** 死信记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long deadLetterId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 任务编号（唯一） */
    private String taskNo;

    /** 任务类型 */
    private String taskType;

    /** 进入死信时间 */
    private Date deadTime;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最后一次错误 */
    private String lastError;

    /** 原批次号 */
    private String batchNo;

    /** replayable 待重放 / replayed 已重放 */
    private String replayStatus;

    /** 重放人 */
    private Long replayBy;

    /** 重放时间 */
    private Date replayTime;

    /** 重放原因（必填，写审计） */
    private String replayReason;

    /** 重放后的执行结果，用于追溯 */
    private String replayTaskStatus;

}
