import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AsyncTaskVO, ImportTemplateVO, ImportValidateVO } from './types';

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
 * 下载模板（按模块）
 *
 * 对应 operationId `downloadImportTemplate`（GET /edu/import/template/{module}）。
 */
export const downloadImportTemplate = (module: string): AxiosPromise<Blob> => {
  return request({
    url: `/edu/import/template/${module}`,
    method: 'get',
    responseType: 'blob'
  });
};

/**
 * 上传并同步校验（≤ 5000 行 / 10 MB；校验阶段不写业务数据）
 *
 * 对应 operationId `validateImportFile`（POST /edu/import/validate）。
 */
export const validateImportFile = (
  file: File,
  data: { module: string; termId?: string; duplicatePolicy?: string }
): AxiosPromise<ImportValidateVO> => {
  const form = new FormData();
  form.append('file', file);
  Object.entries(data).forEach(([key, value]) => {
    if (value != null && value !== '') {
      form.append(key, String(value));
    }
  });
  return request({
    url: '/edu/import/validate',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: form
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
 * 下载失败行明细（CSV，含原始行号与失败原因）
 *
 * 对应 operationId `downloadImportFailedRows`（GET /edu/import/{batchNo}/failed-rows）。
 */
export const downloadImportFailedRows = (batchNo: string): AxiosPromise<Blob> => {
  return request({
    url: `/edu/import/${batchNo}/failed-rows`,
    method: 'get',
    responseType: 'blob'
  });
};

/**
 * 下载结果摘要与学号对照表
 *
 * 对应 operationId `downloadImportResult`（GET /edu/import/{batchNo}/result）。
 */
export const downloadImportResult = (batchNo: string): AxiosPromise<Blob> => {
  return request({
    url: `/edu/import/${batchNo}/result`,
    method: 'get',
    responseType: 'blob'
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

export default {
  listImportTemplate,
  downloadImportTemplate,
  validateImportFile,
  executeImport,
  downloadImportFailedRows,
  downloadImportResult,
  getAsyncTask
};
