package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 学生资料变更申请查询对象
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduStudentFieldChangeBo extends BaseEntity {

    /** 学生主体 ID */
    private Long studentId;

    /** 字段名 */
    private String fieldName;

    /** 状态：待审核 / 已通过 / 已驳回 / 已撤销 */
    private String status;

}
