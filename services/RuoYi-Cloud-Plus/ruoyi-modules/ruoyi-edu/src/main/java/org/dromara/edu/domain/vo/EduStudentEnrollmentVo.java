package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduStudentEnrollment;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 在校记录视图对象
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduStudentEnrollment.class)
public class EduStudentEnrollmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long enrollmentId;

    private Long schoolId;

    private Long studentId;

    private Date enrollDate;

    /** 学籍状态 */
    private String enrollmentStatus;

    private Date statusEffectiveDate;

    private Date leaveDate;

    private Long campusId;

    private Long entryGradeId;

    private String remark;

}
