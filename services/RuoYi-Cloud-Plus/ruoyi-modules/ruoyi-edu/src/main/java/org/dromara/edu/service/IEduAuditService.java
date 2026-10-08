package org.dromara.edu.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.edu.domain.bo.EduAuditArchiveBatchBo;
import org.dromara.edu.domain.bo.EduAuditLogBo;
import org.dromara.edu.domain.vo.EduAuditArchiveBatchVo;
import org.dromara.edu.domain.vo.EduAuditLogVo;
import org.dromara.edu.domain.vo.EduAuditOperatorAccessVo;
import org.dromara.edu.domain.vo.EduAuditSecurityEventVo;
import org.dromara.edu.domain.vo.EduExportResultVo;

import java.util.List;

/**
 * 审计与操作日志服务层
 *
 * 覆盖 audit 模块全部 10 个 operationId：listOperationLog / getOperationLog / listObjectChangeLog /
 * exportOperationLog / listOperatorAccess / exportOperatorAccess / listSensitiveAccess /
 * listSecurityEvent / listArchiveBatch / searchArchivedLog。
 *
 * 另外提供两个**非契约**内部方法给其它模块写日志（`REQ-AUD-035`：关键写操作与日志同事务提交）：
 * `recordLog` 与 `recordChange`。
 *
 * @author Codex
 */
public interface IEduAuditService {

    /**
     * 操作日志分页查询（默认最近 7 天，单次跨度上限 90 天）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 日志分页结果
     */
    TableDataInfo<EduAuditLogVo> queryLogPageList(EduAuditLogBo query, PageQuery pageQuery);

    /**
     * 日志详情（含变更明细 diff）
     *
     * @param logId 日志 ID
     * @return 日志详情
     */
    EduAuditLogVo queryLogDetail(Long logId);

    /**
     * 对象变更时间线（该对象的全部变更记录）
     *
     * @param objectType 对象类型
     * @param objectId   对象标识
     * @param query      查询条件（时间范围等）
     * @return 时间线（按时间倒序）
     */
    List<EduAuditLogVo> queryObjectTimeline(String objectType, String objectId, EduAuditLogBo query);

    /**
     * 日志导出（走导入导出引擎；导出行为本身写入审计，`REQ-AUD-029` / `NFR-AUDIT-03`）
     *
     * @param query 导出筛选条件
     * @return 导出结果（异步任务编号）
     */
    EduExportResultVo exportOperationLog(EduAuditLogBo query);

    /**
     * 运营访问记录分页查询（租户侧自助查询，`REQ-AUD-015`）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 运营访问记录分页结果
     */
    TableDataInfo<EduAuditOperatorAccessVo> queryOperatorAccessPageList(EduAuditLogBo query, PageQuery pageQuery);

    /**
     * 运营访问记录导出（只含本租户数据，`REQ-AUD-016`）
     *
     * @param query 查询条件
     * @return 导出结果（异步任务编号）
     */
    EduExportResultVo exportOperatorAccess(EduAuditLogBo query);

    /**
     * 敏感数据访问记录分页查询（`REQ-AUD-008` / `REQ-AUD-012`）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 敏感数据访问记录分页结果
     */
    TableDataInfo<EduAuditLogVo> querySensitiveAccessPageList(EduAuditLogBo query, PageQuery pageQuery);

    /**
     * 登录与安全事件分页查询（`REQ-AUD-007`）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 安全事件分页结果
     */
    TableDataInfo<EduAuditSecurityEventVo> querySecurityEventPageList(EduAuditLogBo query, PageQuery pageQuery);

    /**
     * 归档批次列表（`REQ-AUD-033` / `REQ-AUD-034`）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 归档批次分页结果
     */
    TableDataInfo<EduAuditArchiveBatchVo> queryArchiveBatchPageList(EduAuditArchiveBatchBo query,
                                                                   PageQuery pageQuery);

    /**
     * 归档区间检索（归档后仍可按时间范围检索，`REQ-AUD-033`；重新解析数据范围，`DS-DENY-04`）
     *
     * @param query     查询条件
     * @param pageQuery 分页参数
     * @return 归档日志分页结果
     */
    TableDataInfo<EduAuditLogVo> searchArchivedLog(EduAuditArchiveBatchBo query, PageQuery pageQuery);

    /**
     * 【内部方法，无对应端点】写一条操作日志。
     * 幂等键 = `(request_id, object_id, action_type)`，命中则跳过不重复写（`REQ-AUD-035`）。
     *
     * @param bo 日志入参
     * @return 日志 ID；命中幂等时返回已存在记录的 ID
     */
    Long recordLog(EduAuditLogBo bo);

    /**
     * 【内部方法，无对应端点】写一条变更明细（只记发生变化且已掩码的字段，`REQ-AUD-003` / `REQ-AUD-010`）
     *
     * @param logId       日志 ID
     * @param fieldName   字段名
     * @param beforeValue 变更前值（掩码后）
     * @param afterValue  变更后值（掩码后）
     */
    void recordChange(Long logId, String fieldName, String beforeValue, String afterValue);

}
