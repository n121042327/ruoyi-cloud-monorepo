package org.dromara.edu.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 死信任务业务对象
 *
 * 覆盖死信任务列表与重放两个 operationId 的入参。重放原因必填（REQ-IMP-038）。
 *
 * 死信表 `with_audit` 为 false，因此这里**不挂** `@AutoMapper`：死信记录只由系统在
 * 超过最大重试次数时写入，没有「前端表单 → 实体」的转换需求。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EduDeadLetterTaskBo extends BaseEntity {

    /** 死信记录 ID */
    private Long deadLetterId;

    /** 任务编号 */
    private String taskNo;

    /** 任务类型 */
    private String taskType;

    /** replayable 待重放 / replayed 已重放 */
    private String replayStatus;

    /** 关联批次号 */
    private String batchNo;

    /** 关键字：任务编号模糊匹配 */
    private String keyword;

    /** 关键字：任务类型模糊匹配 */
    private String keywordType;

    /** 进入死信时间范围起（含） */
    private String beginTime;

    /** 进入死信时间范围止（含） */
    private String endTime;

    /** 重放原因（必填，至少 5 个字，写审计） */
    private String reason;

}
