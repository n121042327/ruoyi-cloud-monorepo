package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 失败原因分组统计
 *
 * 校验结果提供「按失败原因分组」的统计视图，便于一次性修正同类问题（REQ-IMP-007 / BR-IMP-016）。
 *
 * @author Codex
 */
@Data
public class EduImportFailGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 失败原因 */
    private String failReason;

    /** 命中行数 */
    private Integer count;

}
