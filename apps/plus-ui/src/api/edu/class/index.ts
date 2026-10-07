import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ClassForm, ClassQuery, ClassTransferForm, ClassVO } from './types';

/**
 * 查询班级列表（学生列表查询区的「班级」下拉）
 *
 * 对应 operationId `listClass`（GET /edu/class/list）。
 */
export const listClass = (query?: ClassQuery): AxiosPromise<ClassVO[]> => {
  return request({
    url: '/edu/class/list',
    method: 'get',
    params: query
  });
};

/**
 * 调班 / 批量迁学生（学生班级归属的唯一写入入口在班级管理，DP-01）
 *
 * 对应 operationId `transferClass`（POST /edu/class/roster/transfer，权限 `org.class:update`）。
 */
export const transferClass = (data: ClassTransferForm) => {
  return request({
    url: '/edu/class/roster/transfer',
    method: 'post',
    data
  });
};

/**
 * 新增班级
 *
 * 对应 operationId `addClass`（POST /edu/class）。
 */
export const addClass = (data: ClassForm) => {
  return request({
    url: '/edu/class',
    method: 'post',
    data
  });
};

/**
 * 编辑班级（学年学期与年级一经创建不可修改，REQ-CLS-022）
 *
 * 对应 operationId `updateClass`（PUT /edu/class）。
 */
export const updateClass = (data: ClassForm) => {
  return request({
    url: '/edu/class',
    method: 'put',
    data
  });
};

export default {
  listClass,
  transferClass,
  addClass,
  updateClass
};
