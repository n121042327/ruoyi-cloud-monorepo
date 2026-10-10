package org.dromara.edu.job;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 任教关系导出行的默认列。
 *
 * 列以 `edu_teaching_assignment` 的实际列（schema.yaml）为准：term_id / teacher_id / subject_id /
 * class_type / class_id / status —— 表里没有周课时列，因此不导出该列。
 *
 * @author Codex
 */
@Data
public class TeachingAssignmentExportRow {

    @ExcelProperty("学年学期")
    private String termName;

    @ExcelProperty("学科")
    private String subjectName;

    @ExcelProperty("教师")
    private String teacherName;

    @ExcelProperty("工号")
    private String teacherNo;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("班级类型")
    private String classType;

    @ExcelProperty("状态")
    private String status;
}
