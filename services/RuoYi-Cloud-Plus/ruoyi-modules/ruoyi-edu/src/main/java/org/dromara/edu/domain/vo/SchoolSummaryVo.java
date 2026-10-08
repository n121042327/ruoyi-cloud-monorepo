package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 学校数据摘要（getSchoolSummary）
 *
 * 用于学校详情页与开通初始化的执行结果摘要。各项计数按当前可用模块聚合，
 * 学生 / 任教关系等模块交付后逐项补齐。
 *
 * @author Codex
 */
@Data
public class SchoolSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long schoolId;

    private String schoolName;

    /** 开设学段 */
    private List<String> stageCodes;

    /** 校区数 */
    private Long campusCount;

    /** 学年数 */
    private Long academicYearCount;

    /** 学期数 */
    private Long termCount;

    /** 年级数 */
    private Long gradeCount;

    /** 班级数 */
    private Long classCount;

    /** 教师数 */
    private Long teacherCount;

    /** 学科数 */
    private Long subjectCount;

    /** 是否已完成开通初始化（学段 / 学年 / 学科都有数据即视为已初始化） */
    private Boolean initialized;

}
