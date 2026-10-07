/**
 * 审计与操作日志模块的接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 audit 模块；
 * 日志表只允许追加写入，接口层不提供更新与删除方法（审计 PRD 第 8 节接口层要求 4）。
 */

/** 操作日志列表行 */
export interface OperationLogVO {
  logId: string;
  /** 操作时间 */
  operateTime: string;
  /** 操作人 */
  operator: string;
  /** 操作人角色快照 */
  operatorRole?: string;
  /** 租户与学校 */
  tenantId?: string;
  schoolId?: string;
  /** 对象类型与对象标识 */
  objectType?: string;
  objectId?: string;
  /** 操作类型：新增 / 修改 / 删除 / 导入 / 执行 / 审批 / 授权 / 状态变更 */
  actionType?: string;
  /** 执行结果 */
  result?: string;
  /** 来源 IP */
  sourceIp?: string;
  /** 请求标识 */
  requestId?: string;
  /** 批次号（批量操作与导入执行用） */
  batchNo?: string;
}

/** 操作日志查询参数 */
export interface OperationLogQuery extends Partial<PageQuery> {
  /** 时间范围（开始 / 结束），默认最近 7 天 */
  beginTime?: string;
  endTime?: string;
  operator?: string;
  objectType?: string;
  objectId?: string;
  actionType?: string;
  /** 执行结果：success / failed */
  result?: string;
  batchNo?: string;
  /** 关键字：对象标识 / 操作人 / 请求标识 */
  keyword?: string;
  /** 数据范围（由后端解析，前端只传上下文） */
  tenantId?: string;
  schoolId?: string;
}
