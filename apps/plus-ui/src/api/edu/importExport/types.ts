/**
 * 导入导出与异步任务模块的接口类型。
 *
 * 契约来源：docs/40-detailed-design/api/openapi.yaml 的 import-export 模块 + 后端 VO
 * （EduImportTemplateVo / EduImportValidateResultVo / EduImportErrorVo / EduFileRefVo /
 * EduAsyncTaskVo / EduExportResultVo）；字段名一律与后端 VO 一致，不自行改名。
 * 导入模板与校验规则由业务模块声明，引擎统一在本模块（REQ-IMP-002 / GAP-086 裁决 A）。
 */

/** 导入模板（EduImportTemplateVo） */
export interface ImportTemplateVO {
  templateId?: string;
  /** 模块编码，如 student / teacher / class_roster（下划线风格） */
  moduleCode: string;
  /** 模板版本号；字段字典变更必须升版本（BR-IMP-007） */
  templateVersion: string;
  columnCount?: number;
  /** 模板文件引用 */
  fileId?: string;
  expireTime?: string;
  /** 1 当前版本 / 0 历史版本 */
  status?: string;
  /** 模板过期：仍可下载，但页面要给强提示（REQ-IMP-003） */
  expired?: boolean;
}

/** 校验结果行（EduImportErrorVo） */
export interface ImportErrorVO {
  errorId?: string;
  batchNo?: string;
  rowNo: number;
  /** success / failed */
  result?: string;
  /** 失败原因 */
  failReason?: string;
  objectName?: string;
  rawData?: string;
  createTime?: string;
}

/** 校验结果汇总（EduImportValidateResultVo） */
export interface ImportValidateVO {
  /** 批次号（幂等键） */
  batchNo: string;
  moduleCode?: string;
  templateVersion?: string;
  /** 校验后的批次状态 */
  importStatus?: string;
  /** 文件总行数 */
  rowTotal?: number;
  /** 可执行行数 */
  validCount?: number;
  /** 失败行数 */
  invalidCount?: number;
  validateExpireTime?: string;
  /** 失败行明细（分页展示，REQ-IMP-006） */
  invalidRows?: ImportErrorVO[];
  /** 按失败原因聚合 */
  failGroups?: Array<Record<string, unknown>>;
  guidance?: string;
}

/** 导入校验入参（EduImportValidateBo）：文件先上传到统一文件服务，这里只传引用（REQ-IMP-041） */
export interface ImportValidateForm {
  /** 模块编码，如 student / teacher / class_roster */
  moduleCode: string;
  /** 统一文件服务返回的 ossId */
  fileId: string;
  fileName?: string;
  templateVersion?: string;
  termId?: string;
  targetClassId?: string;
  /** 已存在数据的处理策略：skip 跳过 / overwrite 覆盖 / fail 记失败 */
  strategy?: string;
  /**
   * 目标学校（GAP-115）：学校租户留空取本校；集团 / 运营方可管理多所学校，必须显式指定，
   * 且必须是本人数据范围内的学校（后端 `resolveImportSchoolId` 校验）。
   */
  schoolId?: string;
}

/** 统一文件服务上传结果（POST /resource/oss/upload 返回 SysOssUploadVo） */
export interface OssUploadVO {
  /** 文件引用，作为 validateImportFile 的 fileId */
  ossId: string;
  url?: string;
  fileName?: string;
}

/** 文件引用（EduFileRefVo）：模板 / 失败明细 / 结果文件的下载落点 */
export interface EduFileRefVO {
  refId?: string;
  fileId?: string;
  fileKind?: string;
  fileName?: string;
  contentType?: string;
  fileSize?: number;
  bizType?: string;
  bizId?: string;
  expireTime?: string;
  downloadCount?: number;
  /** 短时签名下载地址（由统一文件服务签发，REQ-IMP-042） */
  signedUrl?: string;
  signedUrlExpireTime?: string;
  /** 结果文件过期后拒绝下载；模板过期仍可下载但会给强提示（BR-IMP-013） */
  expired?: boolean;
  /** 下载提示文案（模板版本过期时的强提示等） */
  hint?: string;
}

/** 导出结果（EduExportResultVo）：同步返回文件引用，超过行数上限则转异步任务 */
export interface EduExportResultVO {
  async?: boolean;
  taskNo?: string;
  file?: EduFileRefVO;
  rowCount?: number;
  format?: string;
  plainText?: boolean;
  guidance?: string;
}

/** 异步任务（EduAsyncTaskVo） */
export interface AsyncTaskVO {
  taskNo: string;
  taskId?: string;
  /** import / export */
  taskType?: string;
  /** queued / running / succeeded / partial_failed / failed / cancelled */
  taskStatus?: string;
  progressPercent?: number;
  ownerId?: string;
  ownerName?: string;
  ownerRole?: string;
  paramsSummary?: string;
  totalCount?: number;
  successCount?: number;
  failedCount?: number;
  skippedCount?: number;
  resultFileId?: string;
  failedFileId?: string;
  queuePosition?: number;
  retryCount?: number;
  batchNo?: string;
  startTime?: string;
  finishTime?: string;
  durationSeconds?: number;
  errorMsg?: string;
  createTime?: string;
}
