import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  TeacherForm,
  TeacherQuery,
  TeacherRoleForm,
  TeacherRoleVO,
  TeacherVO,
  TeachingAssignmentCopyForm,
  TeachingAssignmentForm,
  TeachingAssignmentQuery,
  TeachingAssignmentVO
} from './types';
import type { AsyncTaskVO, EduFileRefVO, ImportValidateVO } from '@/api/edu/importExport/types';

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
export const importTeacherValidate = (data: {
  fileId: string;
  fileName?: string;
  termId?: string;
  strategy?: string;
  /** 目标学校（GAP-115）：学校租户留空取本校；多校账号必选 */
  schoolId?: string;
}): AxiosPromise<ImportValidateVO> => {
  return request({
    url: '/edu/teacher/import/validate',
    method: 'post',
    data
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
 * 教师导入模板下载：返回文件引用（含 signedUrl）
 *
 * 对应 operationId `downloadTeacherImportTemplate`（GET /edu/teacher/import/template）。
 */
export const downloadTeacherImportTemplate = (): AxiosPromise<EduFileRefVO> => {
  return request({
    url: '/edu/teacher/import/template',
    method: 'get'
  });
};

/**
 * 查询教师详情（教师详情抽屉 PAGE-TCH-DETAIL）
 *
 * 对应 operationId `getTeacher`（GET /edu/teacher/{id}）。
 */
export const getTeacher = (teacherId: string): AxiosPromise<TeacherVO> => {
  return request({
    url: `/edu/teacher/${teacherId}`,
    method: 'get'
  });
};

/**
 * 变更工号（编码是导入 / 导出对照表的键，需单独申请并写审计）
 *
 * 对应 operationId `updateTeacherNo`（PUT /edu/teacher/{id}/teacher-no）。
 */
export const updateTeacherNo = (teacherId: string, teacherNo: string, reason?: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/teacher-no`,
    method: 'put',
    params: { teacherNo, reason }
  });
};

/**
 * 离职与调离登记（登记后教师不可新增任教关系，历史关系保留）
 *
 * 对应 operationId `leaveTeacher`（POST /edu/teacher/{id}/leave）。
 */
export const leaveTeacher = (teacherId: string, data: { employmentStatus: string; leaveDate: string; reason?: string }) => {
  return request({
    url: `/edu/teacher/${teacherId}/leave`,
    method: 'post',
    data
  });
};

/**
 * 撤销离职与调离登记（DIALOG-TCH-REVOKE，需填原因并写审计）
 *
 * 对应 operationId `revokeTeacherLeave`（POST /edu/teacher/{id}/leave/revoke）。
 */
export const revokeTeacherLeave = (teacherId: string, reason: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/leave/revoke`,
    method: 'post',
    params: { reason }
  });
};

/**
 * 查询教师的教育角色
 *
 * 对应 operationId `listTeacherRole`（GET /edu/teacher/{id}/role）。
 */
export const listTeacherRole = (teacherId: string): AxiosPromise<TeacherRoleVO[]> => {
  return request({
    url: `/edu/teacher/${teacherId}/role`,
    method: 'get'
  });
};

/**
 * 分配教育角色（PAGE-TCH-ROLE）
 *
 * 对应 operationId `assignTeacherRole`（POST /edu/teacher/{id}/role）。
 */
export const assignTeacherRole = (teacherId: string, data: TeacherRoleForm) => {
  return request({
    url: `/edu/teacher/${teacherId}/role`,
    method: 'post',
    data
  });
};

/**
 * 移除教育角色
 *
 * 对应 operationId `removeTeacherRole`（DELETE /edu/teacher/{id}/role/{userRoleId}）。
 */
export const removeTeacherRole = (teacherId: string, userRoleId: string, reason?: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/role/${userRoleId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 教师列表导出（统一走导出引擎）
 *
 * 对应 operationId `exportTeacher`（POST /edu/teacher/export）。
 */
export const exportTeacher = (data?: Record<string, unknown>) => {
  return request({
    url: '/edu/teacher/export',
    method: 'post',
    data: data ?? {}
  });
};

/**
 * 重置教师账号密码
 *
 * 对应 operationId `resetTeacherPassword`（POST /edu/teacher/{id}/reset-password）。
 */
export const resetTeacherPassword = (teacherId: string, password?: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/reset-password`,
    method: 'post',
    data: password ? { password } : {}
  });
};

/**
 * 停用教师账号（不影响在职状态与任教关系）
 *
 * 对应 operationId `disableTeacherAccount`（POST /edu/teacher/{id}/account/disable）。
 */
export const disableTeacherAccount = (teacherId: string, reason?: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/account/disable`,
    method: 'post',
    params: { reason }
  });
};

/**
 * 启用教师账号
 *
 * 对应 operationId `enableTeacherAccount`（POST /edu/teacher/{id}/account/enable）。
 */
export const enableTeacherAccount = (teacherId: string, reason?: string) => {
  return request({
    url: `/edu/teacher/${teacherId}/account/enable`,
    method: 'post',
    params: { reason }
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
