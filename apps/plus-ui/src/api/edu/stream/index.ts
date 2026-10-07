import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { MyStreamForm, MyStreamVO, StreamConfigForm, StreamConfigVO, StreamHistoryVO, StreamOptionVO, UnselectedStudentVO } from './types';

/**
 * 查询选科配置（开放期与逾期审批口径）
 *
 * 对应 operationId `getStreamConfig`（GET /edu/stream/config）。
 */
export const getStreamConfig = (query?: { termId?: string }): AxiosPromise<StreamConfigVO> => {
  return request({
    url: '/edu/stream/config',
    method: 'get',
    params: query
  });
};

/**
 * 保存选科配置（变更写审计，NFR-AUDIT-01）
 *
 * 对应 operationId `saveStreamConfig`（PUT /edu/stream/config，权限 `stream.config:update`）。
 */
export const saveStreamConfig = (data: StreamConfigForm) => {
  return request({
    url: '/edu/stream/config',
    method: 'put',
    data
  });
};

/**
 * 查询选科可选科目（首选固定 2 门、再选固定 4 门，学校不可增减）
 *
 * 对应 operationId `getStreamOption`（GET /edu/stream/option）。
 */
export const getStreamOption = (): AxiosPromise<StreamOptionVO> => {
  return request({
    url: '/edu/stream/option',
    method: 'get'
  });
};

/**
 * 查询我的选科结果
 *
 * 对应 operationId `getMyStream`（GET /edu/stream/my）。
 */
export const getMyStream = (query?: { termId?: string; studentId?: string }): AxiosPromise<MyStreamVO> => {
  return request({
    url: '/edu/stream/my',
    method: 'get',
    params: query
  });
};

/**
 * 提交我的选科（首次提交；截止后转为变更申请，BR-STREAM-005）
 *
 * 对应 operationId `submitMyStream`（POST /edu/stream/my，权限 `stream.selection:update`）。
 */
export const submitMyStream = (data: MyStreamForm) => {
  return request({
    url: '/edu/stream/my',
    method: 'post',
    data
  });
};

/**
 * 更新我的选科（变更；截止后进入审批待办）
 *
 * 对应 operationId `updateMyStream`（PUT /edu/stream/my，权限 `stream.selection:update`）。
 */
export const updateMyStream = (data: MyStreamForm) => {
  return request({
    url: '/edu/stream/my',
    method: 'put',
    data
  });
};

/**
 * 查询未选科学生清单（按年级 / 班级分组，用于催办）
 *
 * 对应 operationId `listUnselectedStudent`（GET /edu/stream/unselected）。
 */
export const listUnselectedStudent = (query?: Record<string, unknown>): AxiosPromise<UnselectedStudentVO[]> => {
  return request({
    url: '/edu/stream/unselected',
    method: 'get',
    params: query
  });
};

/**
 * 查询我的选科历史
 *
 * 对应 operationId `listStreamHistory`（GET /edu/stream/history）。
 */
export const listStreamHistory = (query?: { studentId?: string; termId?: string }): AxiosPromise<StreamHistoryVO[]> => {
  return request({
    url: '/edu/stream/history',
    method: 'get',
    params: query
  });
};

export default {
  getStreamConfig,
  saveStreamConfig,
  getStreamOption,
  getMyStream,
  submitMyStream,
  updateMyStream,
  listUnselectedStudent,
  listStreamHistory
};
