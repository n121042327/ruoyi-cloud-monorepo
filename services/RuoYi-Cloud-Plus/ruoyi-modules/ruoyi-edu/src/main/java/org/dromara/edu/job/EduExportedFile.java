package org.dromara.edu.job;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 模块导出器产出的文件。
 *
 * @author Codex
 */
@Data
@AllArgsConstructor
public class EduExportedFile {

    /** 文件字节 */
    private byte[] bytes;

    /** 文件名（含扩展名） */
    private String fileName;

    /** MIME 类型 */
    private String contentType;

    /** 数据行数 */
    private int rowCount;
}
