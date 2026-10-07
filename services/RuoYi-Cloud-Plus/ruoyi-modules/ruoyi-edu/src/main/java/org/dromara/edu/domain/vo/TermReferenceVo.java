package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学年 / 学期的引用检查结果（checkTermReference）
 *
 * 删除或归档前必须检查四类引用（REQ-TERM-034）：班级、任教关系、学生班级关系、学生选科。
 * 本批已接前两类中的班级与花名册（educ_class / edu_class_member 已交付）；
 * 任教关系数（edu_teaching_assignment）与选科人数（edu_student_stream）分别在教师模块与选科模块
 * 交付后填入，届时本 VO 字段不变、只是不再为 null。
 *
 * @author Codex
 */
@Data
public class TermReferenceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 班级数 */
    private Long classCount;

    /** 任教关系数（教师模块交付后填充） */
    private Long teachingRelationCount;

    /** 花名册人数（在读在班） */
    private Long rosterCount;

    /** 选科人数（选科模块交付后填充） */
    private Long subjectChoiceCount;

    /** 是否已产生引用：有引用时只允许归档、不允许删除（REQ-TERM-028） */
    private Boolean referenced;

}
