package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 学科与学段启用 edu_subject_stage
 *
 * 同一学科同一学段一条记录；(subject_id, stage_code) 唯一。未开设的学段（edu_school_stage.status=0）
 * 不允许启用（REQ-SUB-026 同口径）。本表没有 del_flag：停用写 status='0'。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_subject_stage")
public class EduSubjectStage extends TenantEntity {

    /** 记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long subjectStageId;

    /** 学校归属 */
    private Long schoolId;

    /** 学科 */
    private Long subjectId;

    /** 学段 */
    private String stageCode;

    /** 1 启用 / 0 停用 */
    private String status;

}
