package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 导入执行入参（executeImport）
 *
 * 用户确认执行后返回任务编号，执行阶段异步进行，界面不阻塞（REQ-IMP-015 / BR-IMP-003）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduImportExecuteBo extends BaseEntity {

    /** 批次号（幂等键，REQ-IMP-016 / 017） */
    private String batchNo;

    /** 已存在数据的处理策略：skip 跳过 / overwrite 覆盖 / fail 记失败 */
    private String strategy;

    /** 失败行的处理方式：fix_first 全部修正后重新上传 / continue 忽略失败行继续执行 */
    private String failedRowAction;

}
