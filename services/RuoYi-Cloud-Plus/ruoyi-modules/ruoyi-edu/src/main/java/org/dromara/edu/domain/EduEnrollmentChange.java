package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 学籍异动记录 edu_enrollment_change
 *
 * 追加式，不更新不删除（BR-PROMO-012）。学籍状态的**唯一流转入口**（DP-01）：
 * 开除在义务教育阶段不可用且后端拒绝（REQ-PRM-043）；退学 / 开除 / 死亡需校级管理员审批。
 *
 * 本表没有 del_flag（soft_delete 为 false），不加 @TableLogic。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_enrollment_change")
public class EduEnrollmentChange extends TenantEntity {

    /** 异动记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long changeId;

    /** 学校归属 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 在校记录 */
    private Long schoolRecordId;

    /** 异动类型（休学 / 复学 / 转学 / 退学 / 开除 / 出国 / 失踪 / 死亡 / 转入未报到 / 报到 / 升班） */
    private String changeType;

    /** 变更前状态 */
    private String beforeStatus;

    /** 变更后状态 */
    private String afterStatus;

    /** 生效日期 */
    private Date effectiveDate;

    /** 原因（必填） */
    private String reason;

    /** 需审批的异动：待审批 / 已通过 / 已驳回 */
    private String approvalStatus;

    /** 审批人（校级管理员） */
    private Long approveBy;

    /** 审批时间 */
    private Date approveTime;

    /** 审批意见 */
    private String approveOpinion;

    /** 操作人 */
    private Long operator;

    /** 操作时间 */
    private Date operateTime;

}
