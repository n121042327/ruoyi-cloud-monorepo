package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduStudentEnrollment;

/**
 * 在校记录业务对象
 *
 * 同时承载 changeEnrollmentStatus 的入参（异动类型 / 生效日期 / 复学报到的班级 / 原因）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduStudentEnrollment.class, reverseConvertGenerate = false)
public class EduStudentEnrollmentBo extends BaseEntity {

    /** 在校记录 ID */
    private Long enrollmentId;

    /** 学校 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 入校日期 */
    private String enrollDate;

    /** 学籍状态 */
    private String enrollmentStatus;

    /** 当前状态生效日期 */
    private String statusEffectiveDate;

    /** 离校日期 */
    private String leaveDate;

    /** 校区 */
    private Long campusId;

    /** 入校年级 */
    private Long entryGradeId;

    /** 备注 */
    private String remark;

    /** 异动类型（changeType，必填，如 suspend / resume / abroad / missing / transfer_out / withdraw / expel / graduate） */
    private String changeType;

    /** 生效日期（effective_date，必填） */
    private String effectiveDate;

    /** 复学 / 报到后要落到的班级（条件必填，仅记录在异动原因摘要里，班级关系仍由班级管理写） */
    private Long classId;

    /** 异动原因（必填，至少 5 个字） */
    private String reason;

}
