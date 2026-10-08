package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduPromotionTask;

import java.util.List;

/**
 * 升班任务业务对象
 *
 * 覆盖升班向导 10 个 operationId 的入参：任务列表 / 详情 / 创建 / 预览 / 明细调整 /
 * 批量调整 / 校验 / 执行 / 重试 / 取消（导出类 3 个归入导入导出引擎批次）。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduPromotionTask.class, reverseConvertGenerate = false)
public class EduPromotionTaskBo extends BaseEntity {

    /** 升班任务 ID */
    private Long taskId;

    /** 学校 */
    private Long schoolId;

    /** 任务编号（对外展示，服务端生成） */
    private String taskNo;

    /** 源学年学期 */
    private Long sourceTermId;

    /** 目标学年学期 */
    private Long targetTermId;

    /** 范围说明 */
    private String scopeNote;

    /** 任务状态 */
    private String taskStatus;

    /** 关键字：任务编号 */
    private String keyword;

    /** 取消原因（cancelPromotionTask 必填，至少 5 个字） */
    private String cancelReason;

    /** 重试说明 */
    private String retryReason;

    /** 明细 ID（updatePromotionItem） */
    private Long itemId;

    /** 目标班级（明细调整） */
    private Long targetClassId;

    /** 结果类型（promote / repeat / transfer / graduate / skip） */
    private String resultType;

    /** 调整说明 */
    private String remark;

    /** 批量调整：源班级整体指定目标班级 */
    private Long sourceClassId;

    /** 批量调整：明细 ID 列表 */
    private List<Long> itemIds;

    /** 明细查询：只看某个源班级 */
    private Long filterSourceClassId;

    /** 明细查询：明细状态 */
    private String itemStatus;

}
