import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OperationLogQuery, OperationLogVO } from './types';

/**
 * 操作日志分页查询（默认按时间倒序，默认最近 7 天）
 *
 * 对应 operationId `listOperationLog`（GET /edu/audit/log/list）。
 */
export const listOperationLog = (query?: OperationLogQuery): AxiosPromise<OperationLogVO[]> => {
  return request({
    url: '/edu/audit/log/list',
    method: 'get',
    params: query
  });
};

/**
 * 操作日志详情（含变更明细 diff）
 *
 * 对应 operationId `getOperationLog`（GET /edu/audit/log/{id}）。
 */
export const getOperationLog = (logId: string): AxiosPromise<OperationLogVO> => {
  return request({
    url: `/edu/audit/log/${logId}`,
    method: 'get'
  });
};

/**
 * 对象变更时间线：该对象的全部变更记录，按时间展示（REQ-AUD-023）
 *
 * 对应 operationId `listObjectChangeLog`（GET /edu/audit/object/{objectType}/{objectId}/timeline）。
 */
export const listObjectChangeLog = (objectType: string, objectId: string): AxiosPromise<OperationLogVO[]> => {
  return request({
    url: `/edu/audit/object/${objectType}/${objectId}/timeline`,
    method: 'get'
  });
};

/**
 * 运营访问记录（租户侧自助查询：平台运营对本租户数据的访问留痕，REQ-AUD-013 / 015）
 *
 * 对应 operationId `listOperatorAccess`（GET /edu/audit/operator-access/list）。
 */
export const listOperatorAccess = (query?: OperationLogQuery): AxiosPromise<OperationLogVO[]> => {
  return request({
    url: '/edu/audit/operator-access/list',
    method: 'get',
    params: query
  });
};

/**
 * 敏感数据访问记录（查看人 / 对象 / 字段 / 时间 / 用途说明，REQ-AUD-008 / 012）
 *
 * 对应 operationId `listSensitiveAccess`（GET /edu/audit/sensitive-access/list）。
 */
export const listSensitiveAccess = (query?: OperationLogQuery): AxiosPromise<OperationLogVO[]> => {
  return request({
    url: '/edu/audit/sensitive-access/list',
    method: 'get',
    params: query
  });
};

export default {
  listOperationLog,
  getOperationLog,
  listObjectChangeLog,
  listOperatorAccess,
  listSensitiveAccess
};
