package org.dromara.edu.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导入执行结果视图对象（executeImport）
 *
 * 用户确认执行后返回任务编号（REQ-IMP-015 / BR-IMP-003）；同一批次号重复提交
 * 直接返回第一次的结果（REQ-IMP-017 / BR-IMP-002）。
 *
 * @author Codex
 */
@Data
public class EduImportExecuteResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String batchNo;

    /** 异步任务编号，用于跳转任务详情与结果下载 */
    private String taskNo;

    /** 已校验 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 */
    private String importStatus;

    /** 本次是否新建任务；false 表示命中幂等、复用已有任务 */
    private Boolean createdNewTask;

    private Integer rowTotal;

}
