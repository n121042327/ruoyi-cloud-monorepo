package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 学校开设学段 edu_school_stage
 *
 * 学校实际开设的学段；未开设的学段在学科与年级配置里置灰（REQ-SUB-026 同口径）。
 * 本表没有 del_flag（soft_delete 为 false）：停开设写 status='0'。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_school_stage")
public class EduSchoolStage extends TenantEntity {

    /** 记录 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long schoolStageId;

    /** 学校归属 */
    private Long schoolId;

    /** 学段 */
    private String stageCode;

    /** 1 开设 / 0 停开 */
    private String status;

}
