package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 学籍异动记录查询对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduEnrollmentChangeBo extends BaseEntity {

    /** 学生主体 ID */
    private Long studentId;

    /** 学校 */
    private Long schoolId;

    /** 异动类型 */
    private String changeType;

    /** 异动记录 ID（审批用） */
    private Long changeId;

    /** 学年学期（异动历史页查询与展示） */
    private Long termId;

    /** 年级（异动历史页查询与展示） */
    private Long gradeId;

    /** 生效日期（登记时必填） */
    private String effectiveDate;

    /** 原因（登记时必填，至少 5 个字） */
    private String reason;

    /** 审批结果：true 通过 / false 驳回 */
    private Boolean approved;

    /** 审批意见（驳回必填） */
    private String approveOpinion;

    /** 关键字：学号 / 姓名 */
    private String keyword;

    /** 生效日期（查询起点） */
    private String effectiveDateFrom;

    /** 生效日期（查询终点） */
    private String effectiveDateTo;

    /** 审批状态 */
    private String approvalStatus;

}
