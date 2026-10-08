package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 在校记录 edu_student_enrollment
 *
 * 学校侧的学生身份。学籍状态的**唯一流转入口**是升班与学籍异动模块（DP-01）；
 * 本表只承载当前状态，历史变更在 edu_enrollment_change。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student_enrollment")
public class EduStudentEnrollment extends TenantEntity {

    /** 在校记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long enrollmentId;

    /** 学校归属 */
    private Long schoolId;

    /** 学生主体 ID（平台级，经本表两段式取数） */
    private Long studentId;

    /** 入校日期 */
    private Date enrollDate;

    /** 学籍状态（在读 / 休学 / 转入未报到 / graduated 等） */
    private String enrollmentStatus;

    /** 当前状态生效日期 */
    private Date statusEffectiveDate;

    /** 离校日期（毕业 / 转出 / 开除等终态时写入） */
    private Date leaveDate;

    /** 校区（入校时归属，参考信息） */
    private Long campusId;

    /** 入校年级 */
    private Long entryGradeId;

    /**
     * 学生联系电话（学校侧联系方式，默认掩码展示；查看全量需 read_contact 并写敏感数据访问日志）
     *
     * 落点由 GAP-090 裁决，脚本 `V6__edu_student_contact.sql` 为阶段 5 新版本增量（CR-095）。
     */
    private String studentPhone;

    /** 备注 */
    private String remark;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
