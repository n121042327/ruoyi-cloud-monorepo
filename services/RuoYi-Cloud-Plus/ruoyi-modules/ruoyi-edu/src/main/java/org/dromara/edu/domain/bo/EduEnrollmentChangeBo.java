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

    /** 生效日期（查询起点） */
    private String effectiveDateFrom;

    /** 生效日期（查询终点） */
    private String effectiveDateTo;

    /** 审批状态 */
    private String approvalStatus;

}
