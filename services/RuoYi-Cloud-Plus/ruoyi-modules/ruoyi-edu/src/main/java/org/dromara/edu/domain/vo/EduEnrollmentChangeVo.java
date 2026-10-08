package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduEnrollmentChange;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学籍异动记录视图对象（对应前端 StudentChangeLogVO 的字段）
 *
 * changeType / changeTag / summary / operatorName / changeTime 是给学生详情抽屉「变更记录」用的展示字段，
 * 服务层把「异动类型 + 前后状态 + 原因」拼成 summary。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduEnrollmentChange.class)
public class EduEnrollmentChangeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long changeId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    /** 学年学期 */
    private Long termId;

    /** 年级 */
    private Long gradeId;

    /** 异动类型（与 changeType 同值，供前端 EnrollmentChangeVO 使用） */
    private String type;

    /** 异动类型 */
    private String changeType;

    /** 变更标签（如「在读 → 休学」） */
    private String changeTag;

    /** 变更摘要（含前后值与原因） */
    private String summary;

    /** 操作人姓名 */
    private String operatorName;

    private Long operator;

    /** 变更时间 */
    private Date changeTime;

    /** 操作时间（edu_enrollment_change.operate_time 原值） */
    private Date operateTime;

    /** 学校归属 */
    private Long schoolId;

    private String beforeStatus;

    private String afterStatus;

    private Date effectiveDate;

    private String reason;

    private String approvalStatus;

}
