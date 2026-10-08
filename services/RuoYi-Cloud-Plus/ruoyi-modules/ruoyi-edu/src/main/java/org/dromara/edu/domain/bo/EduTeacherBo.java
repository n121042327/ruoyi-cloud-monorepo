package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduTeacher;

/**
 * 教师业务对象
 *
 * 查询参数与教师 PRD 6.1 的查询区对齐（学校 / 任教年级 / 任教班级 / 任教学科 / 教育角色 /
 * 在职状态 / 关键字）；写入字段与 schema.yaml 的 edu_teacher 列一一对应。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduTeacher.class, reverseConvertGenerate = false)
public class EduTeacherBo extends BaseEntity {

    /** 教师 ID */
    private Long teacherId;

    /** 学校 */
    private Long schoolId;

    /** 工号（学校租户内唯一） */
    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    /** 姓名 */
    @NotBlank(message = "教师姓名不能为空")
    private String teacherName;

    /** 性别 */
    private String gender;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 入职日期（YYYY-MM-DD） */
    private String hireDate;

    /** 在职状态 */
    private String employmentStatus;

    /** 离职 / 调离生效日期（YYYY-MM-DD） */
    private String leaveDate;

    /** 关联系统账号 */
    private Long userId;

    /** 备注 */
    private String remark;

    /** 登录名（新增教师时用于创建账号；不落 edu_teacher） */
    private String loginName;

    /** 初始密码（新增教师时用于创建账号；不落 edu_teacher） */
    private String password;

    /** 任教年级（列表筛选，经任教关系关联） */
    private Long gradeId;

    /** 任教班级（列表筛选） */
    private Long classId;

    /** 任教学科（列表筛选） */
    private Long subjectId;

    /** 教育角色（列表筛选，来自 edu_user_role） */
    private String eduRole;

    /** 关键字：工号 / 姓名 / 电话 */
    private String keyword;

    /** 离职 / 调离原因（leaveTeacher 必填，至少 5 个字） */
    private String reason;

}
