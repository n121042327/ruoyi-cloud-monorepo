package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStudentFieldChange;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学生资料变更申请视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStudentFieldChange.class)
public class EduStudentFieldChangeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long fieldChangeId;

    private Long studentId;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private Long applyByUserId;

    private String applyReason;

    /** 待审核 / 已通过 / 已驳回 / 已撤销 */
    private String status;

    private Long auditBy;

    private Date auditTime;

    private String auditOpinion;

}
