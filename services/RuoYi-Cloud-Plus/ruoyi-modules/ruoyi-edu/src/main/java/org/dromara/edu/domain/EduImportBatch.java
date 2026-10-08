package org.dromara.edu.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 导入批次 edu_import_batch
 *
 * 批次号是**幂等键**（BR-IMP-014）：同一批次号重复提交不得重复写入业务数据，
 * 第二次提交直接返回第一次的结果（REQ-IMP-017 / BR-IMP-002）；
 * 部分失败时**保留已成功行，不整体回滚**（REQ-IMP-019 / NFR-MQ-01）。
 *
 * **本表 with_audit 为 true、soft_delete 为 false**（schema.yaml）：继承 TenantEntity，
 * **不加** `@TableLogic delFlag`。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_import_batch")
public class EduImportBatch extends TenantEntity {

    /** 批次 ID（物理列 `id`） */
    @TableId(value = "id")
    private Long batchId;

    /** 学校归属 */
    private Long schoolId;

    /** 批次号（全局唯一，幂等键） */
    private String batchNo;

    /** 模块 */
    private String moduleCode;

    /** 使用的模板版本 */
    private String templateVersion;

    /** 关联异步任务号 */
    private String asyncTaskNo;

    /** 上传文件引用 */
    private Long sourceFileId;

    /** 结果文件引用（含学号对照表） */
    private Long resultFileId;

    /** 失败明细文件引用 */
    private Long failedFileId;

    /** 总行数 */
    private Integer rowTotal;

    /** 校验通过行数 */
    private Integer validCount;

    /** 校验失败行数 */
    private Integer invalidCount;

    /** 执行成功行数 */
    private Integer successCount;

    /** 跳过行数（幂等跳过） */
    private Integer skippedCount;

    /** 已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 */
    private String importStatus;

    /** 操作人 */
    private Long operatorId;

}
