package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTeachingClassMember;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 教学班成员视图对象（复用前端 ClassRosterVO 的字段名）
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTeachingClassMember.class)
public class EduTeachingClassMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private Long teachingClassId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String gender;

    /** 学籍状态（来自在校记录） */
    private String enrollmentStatus;

    /** 行政班（教学班成员可跨行政班，BR-STU-004） */
    private String currentClassName;

    /** generate / manual */
    private String source;

    private String generateTaskNo;

    private Date joinDate;

    private Date leaveDate;

    /** 1 在班 / 0 已离开 */
    private String status;

}
