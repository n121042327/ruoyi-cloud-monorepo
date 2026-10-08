package org.dromara.edu.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.edu.domain.EduAuditLog;

/**
 * 操作日志业务对象
 *
 * 覆盖 `listOperationLog` / `getOperationLog` / `listObjectChangeLog` / `exportOperationLog` 的入参：
 * 支持按**时间范围、操作人、对象类型、对象标识、操作类型、批次号**筛选（`REQ-AUD-018`），
 * 关键字检索对象标识 / 操作人 / 请求标识（`REQ-AUD-019`）；
 * **默认时间范围为最近 7 天**（`REQ-AUD-020`），单次查询跨度上限默认 90 天，超出提示收窄（`REQ-AUD-021`）。
 *
 * 时间用字符串接收（`yyyy-MM-dd` 或 `yyyy-MM-dd HH:mm:ss`），服务层用 `DateUtils.parseDate` 解析。
 *
 * @author Codex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = EduAuditLog.class, reverseConvertGenerate = false)
public class EduAuditLogBo extends BaseEntity {

    /** 日志 ID（详情 / 时间线入口） */
    private Long logId;

    /** 学校归属（平台运营可按学校过滤；非平台范围由数据范围收窄） */
    private Long schoolId;

    /** 操作类型：create / update / delete / import / export / approve / grant / login / status_change */
    private String actionType;

    /** 所属模块 */
    private String moduleCode;

    /** 对象类型 */
    private String objectType;

    /** 对象标识 */
    private String objectId;

    /** 对象名称 */
    private String objectName;

    /** 操作人 */
    private Long operatorId;

    /** 操作人角色快照（运营访问记录固定传 platform_ops） */
    private String operatorRole;

    /** success / failed */
    private String actionResult;

    /** web / api / job / mq */
    private String source;

    /** 批次号 */
    private String batchNo;

    /** 请求标识 */
    private String requestId;

    /** 关键字：对象标识 / 对象名称 / 请求标识模糊匹配（REQ-AUD-019） */
    private String keyword;

    /** 说明（驳回意见、失败原因、敏感访问用途等），写入 `edu_audit_log.detail` */
    private String detail;

    /** 时间范围起（含），留空时按默认最近 7 天 */
    private String beginTime;

    /** 时间范围止（含） */
    private String endTime;

    /** 导出格式：xlsx（默认）/ csv（REQ-AUD-026） */
    private String format;

    /** 导出列（REQ-AUD-026 复用导出引擎的列选择） */
    private java.util.List<String> columns;

}
