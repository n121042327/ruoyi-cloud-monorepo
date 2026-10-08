package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 导入行结果（失败与跳过明细） edu_import_error
 *
 * 校验与执行阶段逐行结果，`(batch_no, row_no)` 唯一；失败原因要能直接指导修正
 * （原始行号 + 原因，REQ-IMP-006 / BR-IMP-016）；失败行可下载为 CSV（REQ-IMP-008）。
 *
 * **本表 with_audit 为 false、soft_delete 为 false**（schema.yaml）：只有 `create_time`，
 * 因此**不继承** BaseEntity / TenantEntity，只保留 tenantId 与 schoolId 字段。
 *
 * @author Codex
 */
@Data
@TableName("edu_import_error")
public class EduImportError {

    /** 行结果 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long errorId;

    /** 租户隔离键（由租户拦截器写入） */
    private String tenantId;

    /** 学校归属 */
    private Long schoolId;

    /** 批次号 */
    private String batchNo;

    /** 行号（Excel 行号，含表头偏移） */
    private Integer rowNo;

    /** invalid 校验失败 / failed 执行失败 / skipped 跳过 */
    private String result;

    /** 失败或跳过原因 */
    private String failReason;

    /** 对象标识（姓名 / 学号等） */
    private String objectName;

    /** 原始行数据（用于对照修正），物理列为 json，按文本读写 */
    private String rawData;

    /** 创建时间 */
    private Date createTime;

}
