package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduAsyncTask;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 异步任务视图对象（与前端 AsyncTaskVO 对齐）
 *
 * 任务详情展示：任务类型、参数摘要、状态、进度、耗时、结果文件与失败明细入口（REQ-IMP-033）。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduAsyncTask.class)
public class EduAsyncTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private String taskNo;

    private String taskType;

    private String taskStatus;

    private Integer progressPercent;

    private Long ownerId;

    /** 发起人姓名（展示用，连接查询补） */
    private String ownerName;

    private String ownerRole;

    private String paramsSummary;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private Integer skippedCount;

    private Long resultFileId;

    private Long failedFileId;

    /** 排队位置（queued 状态展示，REQ-IMP-049） */
    private Integer queuePosition;

    private Integer retryCount;

    private String batchNo;

    private Date startTime;

    private Date finishTime;

    /** 耗时（秒），由 start_time 与 finish_time 派生 */
    private Long durationSeconds;

    private String errorMsg;

    private Date createTime;

    /** 重试记录（详情接口返回，用于判断是否已达最大重试次数） */
    private List<EduAsyncTaskRetryVo> retryRecords;

}
