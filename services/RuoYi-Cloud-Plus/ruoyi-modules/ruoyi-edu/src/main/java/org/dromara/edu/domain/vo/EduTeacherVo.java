package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduTeacher;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 教师视图对象
 *
 * 主键序列化为字符串；联系电话默认掩码（敏感字段）。eduRoles / subjectNames / teachingClassCount
 * 为聚合展示字段，由自定义 SQL 或 edu_user_role / edu_teaching_assignment 组装填充。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduTeacher.class)
public class EduTeacherVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long teacherId;

    private Long schoolId;

    private String schoolName;

    private String teacherNo;

    private String teacherName;

    private String gender;

    /** 联系电话（掩码） */
    private String phone;

    private String email;

    private Date hireDate;

    /** active 在职 / resigned 离职 / transferred 调离 */
    private String employmentStatus;

    private Date leaveDate;

    private Long userId;

    /** 教育角色（多个以顿号分隔，来自 edu_user_role） */
    private String eduRoles;

    /** 任教学科（多个以顿号分隔，来自 edu_teaching_assignment + edu_subject） */
    private String subjectNames;

    /** 任课班级数（来自 edu_teaching_assignment） */
    private Integer teachingClassCount;

    private String remark;

    private Date updateTime;

}
