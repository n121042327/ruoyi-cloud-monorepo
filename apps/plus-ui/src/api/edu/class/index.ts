import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  ClassForm,
  ClassMergeForm,
  ClassQuery,
  ClassRosterAddForm,
  ClassRosterQuery,
  ClassRosterVO,
  ClassTransferForm,
  ClassVO,
  TeachingClassQuery,
  TeachingClassVO
} from './types';

/**
 * 班级合并（源班级并入目标班级；源班级置为已停用，不物理删除）
 *
 * 对应 operationId `mergeClass`（POST /edu/class/merge，权限 `org.class:update`）。
 */
export const mergeClass = (data: ClassMergeForm) => {
  return request({
    url: '/edu/class/merge',
    method: 'post',
    data
  });
};

/**
 * 查询教学班列表（行政班与教学班是两套独立关系）
 *
 * 对应 operationId `listTeachingClass`（GET /edu/teaching-class/list）。
 */
export const listTeachingClass = (query?: TeachingClassQuery): AxiosPromise<TeachingClassVO[]> => {
  return request({
    url: '/edu/teaching-class/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询教学班详情（详情抽屉用）
 *
 * 对应 operationId `getTeachingClass`（GET /edu/teaching-class/{id}）。
 */
export const getTeachingClass = (classId: string): AxiosPromise<TeachingClassVO> => {
  return request({
    url: `/edu/teaching-class/${classId}`,
    method: 'get'
  });
};

/**
 * 停用教学班（必填原因，写审计）
 *
 * 对应 operationId `disableTeachingClass`（POST /edu/teaching-class/{id}/disable）。
 */
export const disableTeachingClass = (classId: string, reason: string) => {
  return request({
    url: `/edu/teaching-class/${classId}/disable`,
    method: 'post',
    data: { reason }
  });
};

/**
 * 查询教学班成员清单（详情抽屉的成员区块）
 *
 * 对应 operationId `listTeachingClassRoster`（GET /edu/teaching-class/{id}/roster）。
 */
export const listTeachingClassRoster = (classId: string): AxiosPromise<ClassRosterVO[]> => {
  return request({
    url: `/edu/teaching-class/${classId}/roster`,
    method: 'get'
  });
};

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
  mergeClass,
  listClassRoster,
  addClassRoster,
  removeClassRoster,
  listTeachingClass,
  getTeachingClass,
  disableTeachingClass,
  listTeachingClassRoster,
  transferClass,
  addClass,
  updateClass
};
