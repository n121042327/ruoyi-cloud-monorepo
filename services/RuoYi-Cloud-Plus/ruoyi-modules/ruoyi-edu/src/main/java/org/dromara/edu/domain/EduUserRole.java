package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 用户教育角色（学校级）edu_user_role
 *
 * 只承载与具体班级、年级无关的学校级角色（校领导 / 教务主任）。年级主任在 edu_grade_leader，
 * 班主任在 edu_class.head_teacher_id，都不写本表（DP-01 / DP-02）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_user_role")
public class EduUserRole extends TenantEntity {

    /** 角色记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long userRoleId;

    /** 学校归属 */
    private Long schoolId;

    /** 系统用户 */
    private Long userId;

    /** 教师（如该用户是教师） */
    private Long teacherId;

    /** 学校级教育角色（如 school_leader / academic_director） */
    private String eduRole;

    /** 1 启用 / 0 停用 */
    private String status;

    /** 任职开始日期 */
    private Date startDate;

    /** 任职结束日期 */
    private Date endDate;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
