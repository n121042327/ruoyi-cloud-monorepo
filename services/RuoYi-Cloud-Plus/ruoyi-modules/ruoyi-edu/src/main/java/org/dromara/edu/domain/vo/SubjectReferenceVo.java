package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学科引用检查结果（checkSubjectReference）
 *
 * 删除前检查三类引用：任教关系 / 教学班 / 学生选科；有引用时只允许停用。
 * 教学班与学生选科两类要等对应模块交付后填充，本批先接任教关系（edu_teaching_assignment 已交付）。
 *
 * @author Codex
 */
@Data
public class SubjectReferenceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任教关系数 */
    private Long teachingAssignmentCount;

    /** 教学班引用数（教学班模块交付后填充） */
    private Long teachingClassCount;

    /** 学生选科人数（选科模块交付后填充） */
    private Long studentStreamCount;

    /** 是否有引用：有引用时只允许停用 */
    private Boolean referenced;

}
