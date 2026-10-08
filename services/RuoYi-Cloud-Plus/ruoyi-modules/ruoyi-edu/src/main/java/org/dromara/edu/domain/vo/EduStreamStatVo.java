package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 组合分布统计（与前端 StreamStatVO 对齐）
 *
 * 图表与明细表**同源**：都来自本接口的同一次查询结果（见 docs 的 charts 规则）。
 *
 * @author Codex
 */
@Data
public class EduStreamStatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long termId;

    private String termName;

    /** 已选科人数 */
    private Integer selectedCount;

    /** 未选科人数 */
    private Integer unselectedCount;

    /** 首选分布 */
    private List<PrimaryRow> primaryDistribution;

    /** 组合明细 */
    private List<CombinationRow> combinations;

    /** 学科选择人数 */
    private List<SubjectRow> subjects;

    @Data
    public static class PrimaryRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String subjectName;

        private Integer memberCount;

        private String ratio;

    }

    @Data
    public static class CombinationRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String subjectCombination;

        private String primarySubjectName;

        private List<String> secondarySubjectNames;

        private Integer memberCount;

        private String ratio;

    }

    @Data
    public static class SubjectRow implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String subjectName;

        /** 首选 / 再选 */
        private String streamRole;

        private Integer memberCount;

        private String ratio;

        private String ratioBase;

    }

}
