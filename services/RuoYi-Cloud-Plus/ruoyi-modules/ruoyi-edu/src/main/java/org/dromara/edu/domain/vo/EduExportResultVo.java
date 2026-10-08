package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出结果视图对象（exportData 与各模块导出端点）
 *
 * 导出行数 ≤ 2000 时同步下载（`async=false` + `file` 签名链接），
 * 超过 2000 行转异步任务（`async=true` + `taskNo`）（REQ-IMP-029 / NFR-PERF-05）。
 *
 * @author Codex
 */
@Data
public class EduExportResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** true 表示已转异步任务，false 表示同步返回文件 */
    private Boolean async;

    /** 异步任务的编号（async=true 时返回） */
    private String taskNo;

    /** 同步导出的文件（async=false 时返回短时签名链接描述） */
    private EduFileRefVo file;

    /** 本次导出命中的行数（范围为空时为 0，导出空文件并提示，DS-DENY-03） */
    private Integer rowCount;

    /** xlsx（默认）/ csv */
    private String format;

    /** 是否含敏感字段明文（REQ-IMP-028） */
    private Boolean plainText;

    /** 提示：范围为空、已达同步上限或需拆分等（DS-DENY-03 / REQ-IMP-051） */
    private String guidance;

}
