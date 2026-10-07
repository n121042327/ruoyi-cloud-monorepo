/**
 * 导入导出与异步任务模块的接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 import-export 模块；
 * 各业务模块的导入模板与校验规则由业务模块声明，引擎统一在本模块（REQ-IMP-002 / GAP-086 裁决 A）。
 */

/** 导入模板 */
export interface ImportTemplateVO {
  /** 模块编码，如 student / teacher / classRoster */
  module: string;
  moduleName?: string;
  /** 模板版本号；字段字典变更必须升版本（BR-IMP-007） */
  version: string;
  /** 列定义（列名 / 是否必填 / 格式说明） */
  columns?: Array<{ name: string; required: boolean; note?: string }>;
  /** 旧版本模板下载时的过期提示 */
  expired?: boolean;
}

/** 校验结果行 */
export interface ImportErrorVO {
  rowNo: number;
  objectName?: string;
  errorMsg: string;
}

/** 校验结果汇总 */
export interface ImportValidateVO {
  /** 批次号（幂等键） */
  batchNo: string;
  templateVersion?: string;
  totalCount: number;
  validCount: number;
  invalidCount: number;
  errors?: ImportErrorVO[];
}

/** 异步任务 */
export interface AsyncTaskVO {
  taskNo: string;
  taskType?: string;
  /** queued / running / succeeded / partial_failed / failed */
  status?: string;
  progress?: number;
  totalCount?: number;
  successCount?: number;
  failedCount?: number;
  resultFileId?: string;
  createTime?: string;
}
