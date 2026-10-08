package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 选科变更申请 edu_stream_change_request
 *
 * 截止后的变更单，含审批轨迹。同一学生同一学期**同时只允许一条待审批**（REQ-STR-029 / BR-STREAM-007）。
 * 审批人是校级管理员，教务主任不能自审（REQ-STR-034）；通过前保持原组合不变（REQ-STR-032）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_stream_change_request")
public class EduStreamChangeRequest extends TenantEntity {

    /** 申请 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long requestId;

    /** 学校归属 */
    private Long schoolId;

    /** 申请单号（唯一） */
    private String requestNo;

    /** 学生主体 ID */
    private Long studentId;

    /** 学年学期 */
    private Long termId;

    /** 原组合（展示用文本） */
    private String beforeCombination;

    /** 新组合（展示用文本） */
    private String afterCombination;

    /** 新的首选科目 */
    private String primarySubjectCode;

    /** 新的再选科目 */
    private String secondarySubjectCodes;

    /** 草稿 / 待审批 / 已通过 / 已驳回 / 已撤销 */
    private String requestStatus;

    /** 申请原因（必填） */
    private String reason;

    /** 发起人（学生本人或班主任代发起） */
    private Long applyBy;

    /** 发起人角色（student / homeroom） */
    private String applyByRole;

    /** 提交时间 */
    private Date applyTime;

    /** 审批人（校级管理员） */
    private Long approveBy;

    /** 审批时间 */
    private Date approveTime;

    /** 审批意见（驳回必填） */
    private String approveOpinion;

    /** 撤回时间 */
    private Date cancelTime;

}
