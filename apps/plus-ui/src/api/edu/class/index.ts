import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ClassForm, ClassQuery, ClassRosterAddForm, ClassRosterQuery, ClassRosterVO, ClassTransferForm, ClassVO } from './types';

/**
 * 查询班级花名册（PAGE-CLS-DETAIL / PAGE-CLS-MOVE 的来源名单）
 *
 * 对应 operationId `listClassRoster`（GET /edu/class/{id}/roster）。
 */
export const listClassRoster = (classId: string, query?: ClassRosterQuery): AxiosPromise<ClassRosterVO[]> => {
  return request({
    url: `/edu/class/${classId}/roster`,
    method: 'get',
    params: query
  });
};

/**
 * 添加学生到行政班（学生班级归属的唯一写入入口在班级管理，DP-01）
 *
 * 对应 operationId `addClassRoster`（POST /edu/class/{id}/roster，权限 `org.class:update`）。
 */
export const addClassRoster = (data: ClassRosterAddForm) => {
  return request({
    url: `/edu/class/${data.classId}/roster`,
    method: 'post',
    data
  });
};

/**
 * 移出学生（移出行政班，写审计且不可静默删除）
 *
 * 对应 operationId `removeClassRoster`（DELETE /edu/class/{id}/roster/{studentId}）。
 */
export const removeClassRoster = (classId: string, studentId: string, reason?: string) => {
  return request({
    url: `/edu/class/${classId}/roster/${studentId}`,
    method: 'delete',
    params: { reason }
  });
};

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
  listClassRoster,
  addClassRoster,
  removeClassRoster,
  transferClass,
  addClass,
  updateClass
};
