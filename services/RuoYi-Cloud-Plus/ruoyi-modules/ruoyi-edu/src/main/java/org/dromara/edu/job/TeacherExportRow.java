package org.dromara.edu.job;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 教师导出行的默认列。
 *
 * 列取自教师管理列表页（`PAGE-TCH-LIST`）：工号 / 姓名 / 性别 / 所属学校 / 教育角色 / 任教学科 /
 * 任课班级数 / 在职状态 / 联系电话。联系电话**默认掩码**（BR-TEACHER-005 / REQ-IMP-028），
 * 明文导出需要 `read_sensitive` 授权。
 *
 * @author Codex
 */
@Data
public class TeacherExportRow {

    @ExcelProperty("工号")
    private String teacherNo;

    @ExcelProperty("姓名")
    private String teacherName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("所属学校")
    private String schoolName;

    @ExcelProperty("教育角色")
    private String eduRoles;

    @ExcelProperty("任教学科")
    private String subjectNames;

    @ExcelProperty("任课班级数")
    private Integer teachingClassCount;

    @ExcelProperty("在职状态")
    private String employmentStatusName;

    @ExcelProperty("联系电话")
    private String phoneMasked;
}
