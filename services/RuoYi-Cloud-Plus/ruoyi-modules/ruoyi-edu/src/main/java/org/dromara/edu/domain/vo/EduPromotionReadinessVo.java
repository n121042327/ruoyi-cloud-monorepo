package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 目标学年学期的年级与班级齐备性（与前端 PromotionReadiness 对齐）
 *
 * 创建任务前的前置检查：目标学期必须有年级与班级，否则创建被阻止（REQ-PRM-009）。
 *
 * @author Codex
 */
@Data
public class EduPromotionReadinessVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** ready 可以创建 / blocked 阻塞 / warning 可创建但需确认 */
    private String level;

    private String message;

    /** 缺失的年级名称 */
    private List<String> missingGrades;

    /** 目标学期班级数 */
    private Integer classCount;

    /** 同一源 → 目标学期是否已有未结束任务 */
    private Boolean hasUnfinishedTask;

    /** 源学期在读人数 */
    private Integer enrolledCount;

}
