package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduImportBatch;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 导入批次视图对象
 *
 * 导入完成后展示结果摘要：总行数、成功数、失败数、跳过数、耗时；摘要可导出（REQ-IMP-020）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduImportBatch.class)
public class EduImportBatchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long batchId;

    private String batchNo;

    private String moduleCode;

    private String templateVersion;

    private String asyncTaskNo;

    private Long sourceFileId;

    private Long resultFileId;

    private Long failedFileId;

    private Integer rowTotal;

    private Integer validCount;

    private Integer invalidCount;

    private Integer successCount;

    private Integer skippedCount;

    /** 已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 */
    private String importStatus;

    private Long operatorId;

    private Date createTime;

}
