package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduAsyncTask;

/**
 * 异步任务业务对象
 *
 * 覆盖异步任务中心 5 个 operationId 的入参：任务列表 / 任务详情 / 取消 / 重试 / 结果文件下载。
 * 单位约定：不是列表查询条件的字段（如原因）只用于写接口，不要写进分页 wrapper。
 *
 * 时间范围用字符串接收（`yyyy-MM-dd` 或 `yyyy-MM-dd HH:mm:ss`），服务层用
 * `DateUtils.parseDate` 解析，避免请求参数直接绑定 `java.util.Date` 的时区与格式歧义。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduAsyncTask.class, reverseConvertGenerate = false)
public class EduAsyncTaskBo extends BaseEntity {

    /** 任务 ID */
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 任务类型：import / export / promotion / teaching_class / archive */
    private String taskType;

    /** 任务状态：queued / running / succeeded / partial_failed / failed / cancelled */
    private String taskStatus;

    /**
     * 发起人过滤。
     * 默认只查本人发起的任务（REQ-IMP-032）；平台运营（super_admin / DS-01）可传具体 ownerId 或留空查全平台。
     */
    private Long ownerId;

    /** 关联批次号 */
    private String batchNo;

    /** 关键字：任务编号模糊匹配 */
    private String keyword;

    /** 创建时间范围起（含） */
    private String beginTime;

    /** 创建时间范围止（含） */
    private String endTime;

    /** 取消 / 重试 / 重放原因（写接口必填，至少 5 个字） */
    private String reason;

}
