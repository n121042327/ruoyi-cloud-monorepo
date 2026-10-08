import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  TeacherForm,
  TeacherQuery,
  TeacherVO,
  TeachingAssignmentCopyForm,
  TeachingAssignmentForm,
  TeachingAssignmentQuery,
  TeachingAssignmentVO
} from './types';
import type { AsyncTaskVO, ImportValidateVO } from '@/api/edu/importExport/types';

/**
 * 查询教师列表（班级列表查询区与新建表单的「班主任」下拉）
 *
 * 对应 operationId `listTeacher`（GET /edu/teacher/list）。
 */
export const listTeacher = (query?: TeacherQuery): AxiosPromise<TeacherVO[]> => {
  return request({
    url: '/edu/teacher/list',
    method: 'get',
    params: query
  });
};

/**
 * 新增教师（保存成功后自动创建登录账号）
 *
 * 对应 operationId `addTeacher`（POST /edu/teacher）。
 */
export const addTeacher = (data: TeacherForm) => {
  return request({
    url: '/edu/teacher',
    method: 'post',
    data
  });
};

/**
 * 编辑教师（工号修改需校级管理员权限并留审计，REQ-TCH-022）
 *
 * 对应 operationId `updateTeacher`（PUT /edu/teacher）。
 */
export const updateTeacher = (data: TeacherForm) => {
  return request({
    url: '/edu/teacher',
    method: 'put',
    data
  });
};

/**
 * 查询任教关系（PAGE-TCH-ASSIGN；按班级或按教师两种视角）
 *
 * 对应 operationId `listTeachingAssignment`（GET /edu/teacher/assignment/list）。
 */
export const listTeachingAssignment = (query?: TeachingAssignmentQuery): AxiosPromise<TeachingAssignmentVO[]> => {
  return request({
    url: '/edu/teacher/assignment/list',
    method: 'get',
    params: query
  });
};

/**
 * 新增 / 编辑任教关系（权限 `person.teaching_assignment:create`）
 *
 * 对应 operationId `saveTeachingAssignment`（POST /edu/teacher/assignment）。
 */
export const saveTeachingAssignment = (data: TeachingAssignmentForm) => {
  return request({
    url: '/edu/teacher/assignment',
    method: 'post',
    data
  });
};

/**
 * 批量保存任教关系（同一班级一次挂多门学科）
 *
 * 对应 operationId `batchSaveTeachingAssignment`（POST /edu/teacher/assignment/batch）。
 */
export const batchSaveTeachingAssignment = (data: TeachingAssignmentForm[]) => {
  return request({
    url: '/edu/teacher/assignment/batch',
    method: 'post',
    data
  });
};

/**
 * 删除任教关系（结束一条任教关系，写审计）
 *
 * 对应 operationId `removeTeachingAssignment`（DELETE /edu/teacher/assignment/{id}）。
 */
export const removeTeachingAssignment = (assignmentId: string, reason?: string) => {
  return request({
    url: `/edu/teacher/assignment/${assignmentId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 复制上一学年的任教关系（DIALOG-TCH-COPY）
 *
 * 对应 operationId `copyTeachingAssignment`（POST /edu/teacher/assignment/copy）。
 */
export const copyTeachingAssignment = (data: TeachingAssignmentCopyForm) => {
  return request({
    url: '/edu/teacher/assignment/copy',
    method: 'post',
    data
  });
};

/**
 * 教师资格导入校验（PAGE-TCH-IMPORT 第 2 步）
 *
 * 对应 operationId `importTeacherValidate`（POST /edu/teacher/import/validate）。
 */
export const importTeacherValidate = (file: File, data?: { termId?: string; duplicatePolicy?: string }): AxiosPromise<ImportValidateVO> => {
  const form = new FormData();
  form.append('file', file);
  Object.entries(data ?? {}).forEach(([key, value]) => {
    if (value != null && value !== '') {
      form.append(key, String(value));
    }
  });
  return request({
    url: '/edu/teacher/import/validate',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: form
  });
};

/**
 * 教师导入执行（异步，返回任务编号；同一批次重复提交不重复写入）
 *
 * 对应 operationId `importTeacherExecute`（POST /edu/teacher/import/execute）。
 */
export const importTeacherExecute = (data: { batchNo: string }): AxiosPromise<AsyncTaskVO> => {
  return request({
    url: '/edu/teacher/import/execute',
    method: 'post',
    data
  });
};

/**
 * 下载教师导入模板
 *
 * 对应 operationId `downloadTeacherImportTemplate`（GET /edu/teacher/import/template）。
 */
export const downloadTeacherImportTemplate = (): AxiosPromise<Blob> => {
  return request({
    url: '/edu/teacher/import/template',
    method: 'get',
    responseType: 'blob'
  });
};

export default {
  listTeacher,
  addTeacher,
  updateTeacher,
  listTeachingAssignment,
  saveTeachingAssignment,
  batchSaveTeachingAssignment,
  removeTeachingAssignment,
  copyTeachingAssignment,
  importTeacherValidate,
  importTeacherExecute,
  downloadTeacherImportTemplate
};
