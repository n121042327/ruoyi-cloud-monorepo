package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 学生资料变更申请 edu_student_field_change
 *
 * 字段级可编辑性矩阵的落点：非班主任角色（如家长、学生本人）修改关键字段时走申请。
 * 同一学生同一字段同时只允许一条待审核（GAP-018）——由数据库生成列 `pending_guard` + 唯一键强制。
 *
 * 注意：`pending_guard` 是 MySQL 生成列（stored generated），**不是可写字段**，
 * 因此本实体不映射该列，避免 MyBatis-Plus 生成 insert/update 语句时写生成列而报错。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_student_field_change")
public class EduStudentFieldChange extends TenantEntity {

    /** 申请 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long fieldChangeId;

    /** 学校归属 */
    private Long schoolId;

    /** 学生主体 ID */
    private Long studentId;

    /** 申请修改的字段名 */
    private String fieldName;

    /** 原值（敏感字段需掩码） */
    private String oldValue;

    /** 申请值 */
    private String newValue;

    /** 申请人 */
    private Long applyByUserId;

    /** 申请原因 */
    private String applyReason;

    /** 待审核 / 已通过 / 已驳回 / 已撤销 */
    private String status;

    /** 审核人（班主任 / 教务主任） */
    private Long auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 逻辑删除标记 */
    @TableLogic
    private String delFlag;

}
