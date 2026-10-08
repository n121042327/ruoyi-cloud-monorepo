package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按组合生成教学班的预览与核对结果（与前端 TeachingClassGeneratePreviewVO 对齐）
 *
 * 预览只读不写（REQ-STR-056）；执行按唯一键幂等（REQ-STR-057）；
 * 核对差异只提示不自动修正（REQ-STR-060）。
 *
 * @author Codex
 */
@Data
public class EduTeachingClassGenerateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long termId;

    private String termName;

    private Long gradeId;

    private String gradeName;

    /** combination 按完整组合 / subject 按单学科 */
    private String generateMode;

    /** 预计处理的教学班数 */
    private Integer planCount;

    /** 生成批次号（幂等键） */
    private String batchNo;

    private List<PlanRow> rows;

    private List<CheckRow> checkRows;

    @Data
    public static class PlanRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String subjectCombination;

        private String className;

        private Integer memberCount;

        /** 是否已存在同组合教学班 */
        private Boolean existing;

        /** 将执行的动作：新建 / 增量并入 / 跳过 */
        private String action;

    }

    @Data
    public static class CheckRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String className;

        private Integer memberCount;

        private Integer statCount;

        private Integer diff;

        /** 一致 / 存在差异 */
        private String result;

    }

}
