package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 导入行结果查询入参
 *
 * 校验结果分页展示，区分成功行与失败行，失败行给出原始行号与失败原因（REQ-IMP-006）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduImportErrorBo extends BaseEntity {

    /** 批次号 */
    private String batchNo;

    /** invalid 校验失败 / failed 执行失败 / skipped 跳过 */
    private String result;

    /** 关键字：对象标识或失败原因模糊匹配 */
    private String keyword;

}
