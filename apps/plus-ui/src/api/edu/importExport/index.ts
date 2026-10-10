import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AsyncTaskVO, EduExportResultVO, EduFileRefVO, ImportTemplateVO, ImportValidateForm, ImportValidateVO, OssUploadVO } from './types';

/**
 * 查询模板清单与当前版本
 *
 * 对应 operationId `listImportTemplate`（GET /edu/import/template）。
 */
export const listImportTemplate = (): AxiosPromise<ImportTemplateVO[]> => {
  return request({
    url: '/edu/import/template',
    method: 'get'
  });
};

/**
 * 模板下载（按模块）：后端返回文件引用，页面用 signedUrl 触发下载
 *
 * 对应 operationId `downloadImportTemplate`（GET /edu/import/template/{module}，返回 `R<EduFileRefVo>`）。
 */
export const downloadImportTemplate = (module: string, version?: string): AxiosPromise<EduFileRefVO> => {
  return request({
    url: `/edu/import/template/${module}`,
    method: 'get',
    params: { version }
  });
};

/**
 * 上传导入文件到统一文件服务（POST /resource/oss/upload）
 *
 * 导入引擎只收文件引用（`EduImportValidateBo.fileId`），不接收二进制（REQ-IMP-041 / NFR-DATA-03）：
 * 先上传拿到 `ossId`，再作为 `validateImportFile` 的 `fileId` 提交。
 */
export const uploadImportFile = (file: File): AxiosPromise<OssUploadVO> => {
  const form = new FormData();
  form.append('file', file);
  return request({
    url: '/resource/oss/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: form
  });
};

/**
 * 上传后同步校验（≤ 5000 行 / 10 MB；校验阶段不写业务数据）
 *
 * 对应 operationId `validateImportFile`（POST /edu/import/validate，requestBody 为 application/json）。
 */
export const validateImportFile = (data: ImportValidateForm): AxiosPromise<ImportValidateVO> => {
  return request({
    url: '/edu/import/validate',
    method: 'post',
    data
  });
};

/**
 * 确认执行（异步，返回任务编号；同一批次号重复提交不重复写入）
 *
 * 对应 operationId `executeImport`（POST /edu/import/execute）。
 */
export const executeImport = (data: { batchNo: string }): AxiosPromise<AsyncTaskVO> => {
  return request({
    url: '/edu/import/execute',
    method: 'post',
    data
  });
};

/**
 * 失败行明细下载：返回文件引用（含原始行号与失败原因）
 *
 * 对应 operationId `downloadImportFailedRows`（GET /edu/import/{batchNo}/failed-rows）。
 */
export const downloadImportFailedRows = (batchNo: string): AxiosPromise<EduFileRefVO> => {
  return request({
    url: `/edu/import/${batchNo}/failed-rows`,
    method: 'get'
  });
};

/**
 * 结果摘要与学号对照表下载：返回文件引用
 *
 * 对应 operationId `downloadImportResult`（GET /edu/import/{batchNo}/result）。
 */
export const downloadImportResult = (batchNo: string): AxiosPromise<EduFileRefVO> => {
  return request({
    url: `/edu/import/${batchNo}/result`,
    method: 'get'
  });
};

/**
 * 通用导出（返回 EduExportResultVo：同步带 file 引用，超过行数上限转异步任务，REQ-IMP-002 / GAP-086）
 *
 * 对应 operationId `exportData`（POST /edu/export）。
 */
export const exportData = (data: {
  /** 导出模块编码（EduExportBo.moduleCode）：student / teacher / class / class_roster / grade / promotion / stream … */
  moduleCode: string;
  termId?: string;
  classId?: string;
  gradeId?: string;
  studentId?: string;
  /** 需要导出的列；留空表示按模块默认列 */
  columns?: string[];
  /** 附加筛选条件（JSON 字符串透传） */
  filters?: string;
  format?: string;
  plainText?: boolean;
}): AxiosPromise<EduExportResultVO> => {
  return request({
    url: '/edu/export',
    method: 'post',
    data
  });
};

/**
 * 查询异步任务详情（进度与结果文件）
 *
 * 对应 operationId `getAsyncTask`（GET /edu/async-task/{taskNo}）。
 */
export const getAsyncTask = (taskNo: string): AxiosPromise<AsyncTaskVO> => {
  return request({
    url: `/edu/async-task/${taskNo}`,
    method: 'get'
  });
};

/**
 * 异步任务列表（默认只显示本人发起的任务，REQ-IMP-032）
 *
 * 对应 operationId `listAsyncTask`（GET /edu/async-task/list）。
 */
export const listAsyncTask = (query?: Record<string, unknown>): AxiosPromise<AsyncTaskVO[]> => {
  return request({
    url: '/edu/async-task/list',
    method: 'get',
    params: query
  });
};

/**
 * 取消排队中的任务（queued 状态可取消，REQ-IMP-034）
 *
 * 对应 operationId `cancelAsyncTask`（POST /edu/async-task/{taskNo}/cancel）。
 */
export const cancelAsyncTask = (taskNo: string) => {
  return request({
    url: `/edu/async-task/${taskNo}/cancel`,
    method: 'post'
  });
};

/**
 * 重试失败 / 部分失败的任务（沿用原批次号与幂等键，REQ-IMP-037）
 *
 * 对应 operationId `retryAsyncTask`（POST /edu/async-task/{taskNo}/retry）。
 */
export const retryAsyncTask = (taskNo: string) => {
  return request({
    url: `/edu/async-task/${taskNo}/retry`,
    method: 'post'
  });
};

/**
 * 任务结果文件下载：后端返回短时签名链接（REQ-IMP-042）
 *
 * 对应 operationId `downloadTaskResult`（GET /edu/async-task/{taskNo}/file/{fileId}）。
 */
export const downloadTaskResult = (taskNo: string, fileId: string): AxiosPromise<EduFileRefVO> => {
  return request({
    url: `/edu/async-task/${taskNo}/file/${fileId}`,
    method: 'get'
  });
};

/**
 * 死信任务列表（超过最大重试次数的任务，供运维查看，REQ-IMP-038）
 *
 * 对应 operationId `listDeadLetterTask`（GET /edu/async-task/dead-letter）。
 */
export const listDeadLetterTask = (query?: Record<string, unknown>): AxiosPromise<AsyncTaskVO[]> => {
  return request({
    url: '/edu/async-task/dead-letter',
    method: 'get',
    params: query
  });
};

/**
 * 死信重放（重放动作写入审计，REQ-IMP-038）
 *
 * 对应 operationId `replayDeadLetterTask`（POST /edu/async-task/dead-letter/{taskNo}/replay）。
 */
export const replayDeadLetterTask = (taskNo: string) => {
  return request({
    url: `/edu/async-task/dead-letter/${taskNo}/replay`,
    method: 'post'
  });
};

/**
 * 校验 / 执行结果按行分页查询（REQ-IMP-006 要求分页展示）
 *
 * 对应 operationId `listImportRows`（GET /edu/import/{batchNo}/rows）。
 */
export const listImportRows = (batchNo: string, query?: { result?: string; keyword?: string; pageNum?: number; pageSize?: number }) => {
  return request({
    url: `/edu/import/${batchNo}/rows`,
    method: 'get',
    params: query
  });
};

export default {
  listImportTemplate,
  uploadImportFile,
  downloadImportTemplate,
  validateImportFile,
  executeImport,
  downloadImportFailedRows,
  downloadImportResult,
  exportData,
  getAsyncTask,
  listAsyncTask,
  cancelAsyncTask,
  retryAsyncTask,
  downloadTaskResult,
  listDeadLetterTask,
  replayDeadLetterTask
};
