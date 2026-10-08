package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 教师主体 edu_teacher
 *
 * 教师主体带所属学校（schoolId）。跨校任教不复制教师记录：由 edu_teaching_assignment（带任教学校
 * schoolId）与 edu_user_role 表达，避免「同一教师多行」被误认为重复（BR-TEACHER-001）。
 * 账号信息（login_name / last_login_time）在 sys_user，本表不重复存。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_teacher")
public class EduTeacher extends TenantEntity {

    /** 教师 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long teacherId;

    /** 学校归属（学校租户内唯一工号的前提） */
    private Long schoolId;

    /** 工号（学校租户内唯一，BR-TEACHER-007） */
    private String teacherNo;

    /** 姓名 */
    private String teacherName;

    /** 性别 */
    private String gender;

    /** 联系电话（敏感字段，默认掩码展示） */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 入职日期 */
    private Date hireDate;

    /** 在职状态：active 在职 / resigned 离职 / transferred 调离 */
    private String employmentStatus;

    /** 离职或调离生效日期 */
    private Date leaveDate;

    /** 关联系统账号（sys_user.user_id） */
    private Long userId;

    /** 备注 */
    private String remark;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
