package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 导入校验结果视图对象（validateImportFile）
 *
 * 校验阶段**不写业务数据**，校验结果保留有效期默认 24 小时，过期后需重新上传
 * （REQ-IMP-014 / BR-IMP-008）；**任一行越权时整批拒绝执行并列出越权行号**（REQ-IMP-012 / DS-DENY-06）。
 *
 * @author Codex
 */
@Data
public class EduImportValidateResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 批次号（执行阶段的幂等键） */
    private String batchNo;

    private String moduleCode;

    private String templateVersion;

    /** 已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 */
    private String importStatus;

    private Integer rowTotal;

    private Integer validCount;

    private Integer invalidCount;

    /** 校验结果有效期（默认 24 小时，REQ-IMP-014） */
    private Date validateExpireTime;

    /** 失败行明细（分页查询用 queryBatchRowPageList） */
    private List<EduImportErrorVo> invalidRows;

    /** 按失败原因分组的统计（REQ-IMP-007） */
    private List<EduImportFailGroupVo> failGroups;

    /** 指引：超 5000 行时的拆分指引，或本次校验的注意事项（REQ-IMP-051） */
    private String guidance;

}
