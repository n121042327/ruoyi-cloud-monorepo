import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  EnrollmentChangeForm,
  EnrollmentStatusOptionVO,
  GuardianForm,
  GuardianVO,
  StudentChangeLogVO,
  StudentEnrollmentVO,
  StudentForm,
  StudentQuery,
  StudentVO
} from './types';
import { parseStrEmpty } from '@/utils/ruoyi';

/**
 * 查询学生列表
 *
 * 对应 operationId `listStudent`（GET /edu/student/list）。
 */
export const listStudent = (query: StudentQuery): AxiosPromise<StudentVO[]> => {
  return request({
    url: '/edu/student/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询学生详情
 *
 * 对应 operationId `getStudent`（GET /edu/student/{id}）。
 */
export const getStudent = (studentId?: string): AxiosPromise<StudentVO> => {
  return request({
    url: '/edu/student/' + parseStrEmpty(studentId),
    method: 'get'
  });
};

/**
 * 新增学生
 *
 * 对应 operationId `addStudent`（POST /edu/student）。学号由后端统一发号，前端不提交。
 */
export const addStudent = (data: StudentForm) => {
  return request({
    url: '/edu/student',
    method: 'post',
    data
  });
};

/**
 * 修改学生
 *
 * 对应 operationId `updateStudent`（PUT /edu/student）。
 */
export const updateStudent = (data: StudentForm) => {
  return request({
    url: '/edu/student',
    method: 'put',
    data
  });
};

/**
 * 查询学生监护人列表
 *
 * 对应 operationId `listStudentGuardian`（GET /edu/student/{id}/guardian）。
 */
export const listStudentGuardian = (studentId: string): AxiosPromise<GuardianVO[]> => {
  return request({
    url: `/edu/student/${studentId}/guardian`,
    method: 'get'
  });
};

/**
 * 新增 / 修改监护人（班主任为唯一写入口）
 *
 * 对应 operationId `saveStudentGuardian`（POST /edu/student/{id}/guardian）。
 */
export const saveStudentGuardian = (studentId: string, data: GuardianForm) => {
  return request({
    url: `/edu/student/${studentId}/guardian`,
    method: 'post',
    data
  });
};

/**
 * 查询学生变更记录（只读，来自审计模块）
 *
 * 对应 operationId `listStudentChangeLog`（GET /edu/student/{id}/change-log）。
 */
export const listStudentChangeLog = (studentId: string): AxiosPromise<StudentChangeLogVO[]> => {
  return request({
    url: `/edu/student/${studentId}/change-log`,
    method: 'get'
  });
};

/**
 * 查看完整证件号（需 `person.student:read_sensitive`，写敏感数据访问日志）
 *
 * 对应 operationId `viewStudentIdCard`（GET /edu/student/{id}/id-card）。
 */
export const viewStudentIdCard = (studentId: string): AxiosPromise<{ idCardNo: string }> => {
  return request({
    url: `/edu/student/${studentId}/id-card`,
    method: 'get'
  });
};

/**
 * 查看完整联系电话（需 `person.student_contact` 的 `read_contact`，写敏感数据访问日志）
 *
 * 对应 operationId `viewStudentPhone`（GET /edu/student/{id}/phone，CR-046 补登记）。
 */
export const viewStudentPhone = (studentId: string): AxiosPromise<{ studentPhone: string }> => {
  return request({
    url: `/edu/student/${studentId}/phone`,
    method: 'get'
  });
};

/**
 * 解绑监护人（提交解绑申请，需班主任确认；同一字段同时只允许一条待审核）
 *
 * 对应 operationId `unbindStudentGuardian`（POST /edu/student/{id}/guardian/{guardianId}/unbind）。
 */
export const unbindStudentGuardian = (studentId: string, guardianId: string) => {
  return request({
    url: `/edu/student/${studentId}/guardian/${guardianId}/unbind`,
    method: 'post'
  });
};

/**
 * 上传 / 更换学生照片（单张，走统一文件服务）
 *
 * 对应 operationId `uploadStudentPhoto`（POST /edu/student/{id}/photo）。
 */
export const uploadStudentPhoto = (studentId: string, file: File) => {
  const data = new FormData();
  data.append('file', file);
  return request({
    url: `/edu/student/${studentId}/photo`,
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data
  });
};

/**
 * 查看照片原图（需 `person.student:read_sensitive`，写敏感数据访问日志）
 *
 * 对应 operationId `getStudentPhoto`（GET /edu/student/{id}/photo，返回二进制图片）。
 */
export const getStudentPhoto = (studentId: string): AxiosPromise<Blob> => {
  return request({
    url: `/edu/student/${studentId}/photo`,
    method: 'get',
    responseType: 'blob'
  });
};

/**
 * 当前状态可执行的异动（选项随状态变化；终态没有出口）
 *
 * 对应 operationId `listEnrollmentStatusOption`（GET /edu/student/{id}/status-options）。
 */
export const listEnrollmentStatusOption = (studentId: string): AxiosPromise<EnrollmentStatusOptionVO[]> => {
  return request({
    url: `/edu/student/${studentId}/status-options`,
    method: 'get'
  });
};

/**
 * 学籍异动（原状态 → 新状态，写审计）
 *
 * 对应 operationId `changeEnrollmentStatus`（POST /edu/student/{id}/enrollment-change）。
 */
export const changeEnrollmentStatus = (studentId: string, data: EnrollmentChangeForm) => {
  return request({
    url: `/edu/student/${studentId}/enrollment-change`,
    method: 'post',
    data
  });
};

/**
 * 变更学号（学号是导入 / 导出对照表的键，需单独申请并写审计）
 *
 * 对应 operationId `updateStudentNo`（PUT /edu/student/{id}/student-no）。
 */
export const updateStudentNo = (studentId: string, studentNo: string, reason?: string) => {
  return request({
    url: `/edu/student/${studentId}/student-no`,
    method: 'put',
    params: { studentNo, reason }
  });
};

/**
 * 逻辑删除学生（有在读关系或异动记录时后端拒绝，REQ-STU-075 / REQ-STU-078）
 *
 * 对应 operationId `removeStudent`（DELETE /edu/student/{id}）；原因随请求提交并写审计。
 */
export const removeStudent = (studentId: string, reason?: string) => {
  return request({
    url: `/edu/student/${studentId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 重置学生登录账号密码
 *
 * 对应 operationId `resetStudentPassword`（POST /edu/student/{id}/reset-password）。
 */
export const resetStudentPassword = (studentId: string, password?: string) => {
  return request({
    url: `/edu/student/${studentId}/reset-password`,
    method: 'post',
    data: password ? { password } : {}
  });
};

/**
 * 查询学生在校记录（PAGE-STU-DETAIL 的学籍信息段）
 *
 * 对应 operationId `getStudentEnrollment`（GET /edu/student/{id}/enrollment）。
 */
export const getStudentEnrollment = (studentId: string): AxiosPromise<StudentEnrollmentVO> => {
  return request({
    url: `/edu/student/${studentId}/enrollment`,
    method: 'get'
  });
};

/**
 * 学生列表导出（统一走导出引擎，导出前重新解析数据范围）
 *
 * 对应 operationId `exportStudent`（POST /edu/student/export）。
 */
export const exportStudent = (data?: Record<string, unknown>) => {
  return request({
    url: '/edu/student/export',
    method: 'post',
    data: data ?? {}
  });
};

/**
 * 查询学生激活码（PAGE-STU-DETAIL / 激活码弹窗）
 *
 * 对应 operationId `getActivationCode`（GET /edu/student/{id}/activation-code）。
 */
export const getActivationCode = (studentId: string) => {
  return request({
    url: `/edu/student/${studentId}/activation-code`,
    method: 'get'
  });
};

/**
 * 打印激活单（批量，返回导出文件）
 *
 * 对应 operationId `printActivationSlip`（POST /edu/student/activation-slip/print）。
 */
export const printActivationSlip = (studentIds: string[]) => {
  return request({
    url: '/edu/student/activation-slip/print',
    method: 'post',
    data: { studentIds }
  });
};

/**
 * 激活学生账号（激活码校验通过后置为已激活）
 *
 * 对应 operationId `activateStudent`（POST /edu/student/{id}/activate）。
 */
export const activateStudent = (studentId: string) => {
  return request({
    url: `/edu/student/${studentId}/activate`,
    method: 'post'
  });
};

/**
 * 导出激活码（批量）
 *
 * 对应 operationId `exportActivationCode`（POST /edu/student/activation-code/export）。
 */
export const exportActivationCode = (studentIds: string[]) => {
  return request({
    url: '/edu/student/activation-code/export',
    method: 'post',
    data: { studentIds }
  });
};

export default {
  listStudent,
  getStudent,
  addStudent,
  updateStudent,
  listStudentGuardian,
  saveStudentGuardian,
  listStudentChangeLog,
  viewStudentIdCard,
  viewStudentPhone,
  unbindStudentGuardian,
  uploadStudentPhoto,
  getStudentPhoto,
  listEnrollmentStatusOption,
  changeEnrollmentStatus,
  removeStudent
};
