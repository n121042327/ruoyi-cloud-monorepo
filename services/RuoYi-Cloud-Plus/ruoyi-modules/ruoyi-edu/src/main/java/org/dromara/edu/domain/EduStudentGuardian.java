package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * 监护人与学生关联 edu_student_guardian
 *
 * 平台级实体（scope: platform），跨租户多对多关系 + 学校侧审核。绑定上限 3（BR-ACCOUNT-018 同口径）；
 * 解绑需班主任确认（GAP-015）；审核未过可重提，但同一学生同一监护人只有一条关系记录。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student_guardian")
public class EduStudentGuardian extends BaseEntity {

    /** 关系 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long relationId;

    /** 学生主体 ID */
    private Long studentId;

    /** 监护人主体 ID */
    private Long guardianId;

    /** 关系：father / mother / other */
    private String relation;

    /** 是否主要联系人：1 是 / 0 否 */
    private String isPrimary;

    /** 绑定状态：pending 待审核 / approved 已通过 / rejected 已驳回 / unbinding 待解绑 */
    private String bindStatus;

    /** 来源：qrcode 扫码绑定 / teacher 教师录入 / import 导入 */
    private String source;

    /** 审核人（班主任） */
    private Long auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 审核意见（驳回必填） */
    private String auditOpinion;

    /** 绑定时间 */
    private Date bindTime;

    /** 解绑时间 */
    private Date unbindTime;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
