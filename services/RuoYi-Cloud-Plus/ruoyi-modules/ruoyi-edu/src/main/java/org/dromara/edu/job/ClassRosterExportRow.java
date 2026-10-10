package org.dromara.edu.job;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 编班表（班级花名册）导出行的默认列。
 *
 * 列取自班级详情花名册（`PAGE-CLS-DETAIL`）：学号 / 姓名 / 性别 / 学籍状态 / 加入日期 / 监护人 /
 * 联系电话，另加「班级」列 —— 编班表导出可能覆盖全校多个班，没有班级列无法看出归属
 * （导入模板的「目标班级」列与之对应）。联系电话默认掩码（REQ-AUD-009）。
 *
 * @author Codex
 */
@Data
public class ClassRosterExportRow {

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("学籍状态")
    private String enrollmentStatusName;

    @ExcelProperty("加入日期")
    private String joinDate;

    @ExcelProperty("监护人")
    private String guardianName;

    @ExcelProperty("联系电话")
    private String guardianPhone;
}
