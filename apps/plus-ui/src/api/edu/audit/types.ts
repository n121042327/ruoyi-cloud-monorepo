/**
 * 审计与操作日志模块的接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 audit 模块；
 * 日志表只允许追加写入，接口层不提供更新与删除方法（审计 PRD 第 8 节接口层要求 4）。
 */

/** 变更明细（edu_audit_change：只记录发生变化的字段） */
export interface AuditChangeVO {
  fieldName: string;
  fieldLabel?: string;
  beforeValue?: string;
  afterValue?: string;
}

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
  /** 变更明细（仅详情接口返回，diff 形式） */
  changes?: AuditChangeVO[];
  /** 用途说明（敏感字段全量查看时必填，REQ-AUD-008） */
  purpose?: string;
  /** 被访问的敏感字段名（敏感数据访问记录用） */
  fieldName?: string;
  /** 访问方式：掩码展示 / 揭示全量 / 明文导出（REQ-AUD-009） */
  accessType?: string;
  /** 安全事件：事件类型 / 账号 / 说明 */
  eventType?: string;
  account?: string;
  detail?: string;
  /** 归档批次：归档范围 / 行数 / 状态 / 归档时间 */
  archiveRange?: string;
  rowCount?: number;
  archiveTime?: string;
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
