package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 班级成员关系（花名册）edu_class_member
 *
 * 本表只承载**行政班**花名册（classType 恒为 administrative）；教学班成员在 edu_teaching_class_member
 * —— 两套独立关系分别落表（BR-CLASS-001）。关系追加式：离开时写 leaveDate 并置 status，
 * 不物理删除（BR-PROMO-012 同口径）。
 *
 * 本表没有 del_flag（schema.yaml 的 soft_delete 为 false），因此不继承带 @TableLogic 的字段。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_class_member")
public class EduClassMember extends TenantEntity {

    /** 成员关系 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long memberId;

    /** 学校归属 */
    private Long schoolId;

    /** 学年学期 */
    private Long termId;

    /** 行政班 */
    private Long classId;

    /** 恒为 administrative（教学班成员在 edu_teaching_class_member） */
    private String classType;

    /** 学生主体 ID（平台级，经本表两段式取数） */
    private Long studentId;

    /** 在校记录（学校侧身份） */
    private Long studentEnrollmentId;

    /** 加入日期 */
    private Date joinDate;

    /** 离开日期 */
    private Date leaveDate;

    /** 1 在班 / 0 已离开 */
    private String status;

    /** 性别快照（花名册排序与展示） */
    private String genderSnapshot;

}
