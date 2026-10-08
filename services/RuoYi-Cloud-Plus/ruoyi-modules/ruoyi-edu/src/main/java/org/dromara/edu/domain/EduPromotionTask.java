package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 升班任务 edu_promotion_task
 *
 * 升班按学年**追加**下一学年的班级与学生关系，不改写历史（BR-PROMO-001）。
 * 同一源学期与目标学期的未结束任务唯一（REQ-PRM-005）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_promotion_task")
public class EduPromotionTask extends TenantEntity {

    /** 升班任务 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long taskId;

    /** 学校归属 */
    private Long schoolId;

    /** 任务编号（对外标识，唯一） */
    private String taskNo;

    /** 源学年学期 */
    private Long sourceTermId;

    /** 目标学年学期 */
    private Long targetTermId;

    /** 范围说明 */
    private String scopeNote;

    /** 任务状态：draft / queued / running / succeeded / partial_failed / failed / cancelled / archived */
    private String taskStatus;

    /** 涉及学生总数 */
    private Integer totalCount;

    /** 成功数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failedCount;

    /** 留级数 */
    private Integer repeatCount;

    /** 毕业数 */
    private Integer graduateCount;

    /** 关联异步任务号 */
    private String asyncTaskNo;

    /** 开始执行时间 */
    private Date startTime;

    /** 结束时间 */
    private Date finishTime;

    /** 取消原因（取消时必填） */
    private String cancelReason;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
