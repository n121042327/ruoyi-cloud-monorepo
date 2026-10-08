package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 异步任务 edu_async_task
 *
 * 任务状态按 queued → running → succeeded / partial_failed / failed 推进（REQ-IMP-034）；
 * 只有 queued 可取消，failed / partial_failed 可重试且沿用原幂等键（REQ-IMP-037）；
 * 默认只展示本人发起的任务（REQ-IMP-032）。
 *
 * **本表 with_audit 为 true、soft_delete 为 false**（schema.yaml）：继承 TenantEntity 拿到
 * 租户键与审计列，但**不加** `@TableLogic delFlag`（表里没有 del_flag 列）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_async_task")
public class EduAsyncTask extends TenantEntity {

    /** 任务 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long taskId;

    /** 学校归属 */
    private Long schoolId;

    /** 任务编号（全局唯一，幂等键） */
    private String taskNo;

    /** 任务类型：import / export / promotion / teaching_class / archive */
    private String taskType;

    /** 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 死信 */
    private String taskStatus;

    /** 进度百分比（0—100，仅 running 状态有意义） */
    private Integer progressPercent;

    /** 发起人 */
    private Long ownerId;

    /** 发起人角色快照 */
    private String ownerRole;

    /** 参数摘要（模块 / 模板 / 筛选条件等），物理列为 json，按文本读写 */
    private String paramsSummary;

    /** 总数 */
    private Integer totalCount;

    /** 成功数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failedCount;

    /** 跳过数 */
    private Integer skippedCount;

    /** 结果文件引用 */
    private Long resultFileId;

    /** 失败明细文件引用 */
    private Long failedFileId;

    /** 排队位置（queue 状态时展示，REQ-IMP-049） */
    private Integer queuePosition;

    /** 已重试次数 */
    private Integer retryCount;

    /** 关联批次号（导入 / 升班类任务） */
    private String batchNo;

    /** 开始执行时间 */
    private Date startTime;

    /** 结束时间 */
    private Date finishTime;

    /** 失败原因 */
    private String errorMsg;

}
