package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 选科可选科目（与前端 StreamOptionVO 对齐）
 *
 * 首选固定物理 / 历史（BV-STREAM-001），再选固定化学 / 生物 / 思想政治 / 地理（BR-STREAM-002），
 * 学校不可增减，因此本接口的值来自 edu_subject 的 stream_role 配置，按 sort_no 排序（REQ-STR-017）。
 *
 * @author Codex
 */
@Data
public class EduStreamOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<SubjectOption> primarySubjects;

    private List<SubjectOption> secondarySubjects;

    /** 再选需要选几门，固定 2 */
    private Integer secondaryRequired;

    @Data
    public static class SubjectOption implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String code;

        private String name;

    }

}
