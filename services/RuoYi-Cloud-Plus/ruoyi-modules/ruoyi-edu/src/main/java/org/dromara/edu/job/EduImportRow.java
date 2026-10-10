package org.dromara.edu.job;

import lombok.Data;

import java.util.Map;

/**
 * 导入文件的一行。
 *
 * `cells` 以**表头名**为键（模板列顺序固定，REQ-STU-052）；`failReason` 非空表示该行校验失败。
 *
 * @author Codex
 */
@Data
public class EduImportRow {

    /** 文件里的物理行号（含表头，第 1 行是表头） */
    private int rowNo;

    /** 表头名 → 单元格文本 */
    private Map<String, String> cells;

    /** 失败原因（多条用「；」连接；空表示通过） */
    private String failReason;
}
