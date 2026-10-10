package org.dromara.edu.job;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生导出行的默认列。
 *
 * 列取自学生管理列表页（`PAGE-STU-LIST`）的非敏感列：学号 / 姓名 / 性别 / 入学年份 / 学段 / 年级 / 班级 / 学籍状态。
 * 联系电话、证件号等敏感列不在默认列内 —— 明文导出需要 `read_sensitive` 授权（REQ-IMP-028 / BR-IMP-012），
 * 与「明文导出」那一批一起做。
 *
 * @author Codex
 */
@Data
public class StudentExportRow {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("入学年份")
    private Integer enrollYear;

    @ExcelProperty("学段")
    private String stageName;

    @ExcelProperty("年级")
    private String gradeName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("学籍状态")
    private String enrollmentStatusName;
}
