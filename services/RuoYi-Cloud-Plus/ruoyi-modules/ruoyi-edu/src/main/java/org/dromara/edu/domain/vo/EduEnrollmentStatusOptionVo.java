package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学籍异动动作选项（listEnrollmentStatusOption，对应前端 EnrollmentStatusOptionVO）
 *
 * 选项集合与「当前状态 → 可选新状态」的流转矩阵由 PRD 决定（BR-STU 系列）：
 * 开除在义务教育阶段不可用且后端拒绝（REQ-PRM-043）；退学 / 开除 / 死亡需校级管理员审批。
 *
 * @author Codex
 */
@Data
public class EduEnrollmentStatusOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 异动动作码，如 suspend / abroad / missing / transfer_out / withdraw */
    private String value;

    /** 展示文案，含状态流转，如「休学（在读 → 休学）」 */
    private String label;

    /** 是否需要校级管理员审批 */
    private Boolean needApproval;

    /** 是否必须指定复学 / 报到后的班级 */
    private Boolean needClass;

    /** 当前状态下不可用（如义务教育阶段的开除） */
    private Boolean disabled;

    /** 不可用原因 */
    private String disabledReason;

}
