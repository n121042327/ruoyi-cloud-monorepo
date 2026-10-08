package org.dromara.edu.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.edu.domain.EduPromotionTask;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 升班任务视图对象（与前端 PromotionTaskVO 对齐）
 *
 * 前端的状态码是 draft / previewed / validating / running / succeeded / partial_failed / failed /
 * cancelled；数据库枚举是草稿 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 已归档。
 * `status` 是前端用的状态码，`taskStatus` 是数据库原值，两者由服务层映射。
 *
 * @author Codex
 */
@Data
@AutoMapper(target = EduPromotionTask.class)
public class EduPromotionTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private String taskNo;

    private Long schoolId;

    private Long sourceTermId;

    private String sourceTermName;

    private Long targetTermId;

    private String targetTermName;

    /** 前端状态码：draft / previewed / validating / running / succeeded / partial_failed / failed / cancelled */
    private String status;

    /** 数据库原值：草稿 / 排队中 / 执行中 / 已完成 / 部分失败 / 失败 / 已取消 / 已归档 */
    private String taskStatus;

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private Integer repeatCount;

    private Integer graduateCount;

    private String scopeNote;

    private String failReason;

    private String cancelReason;

    private String asyncTaskNo;

    private Date startTime;

    private Date finishTime;

    private String createBy;

    private Date createTime;

    /** 明细（详情接口带出；列表接口为空） */
    private List<EduPromotionItemVo> items;

}
