import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  EnrollmentChangeForm,
  EnrollmentStatusOptionVO,
  GuardianForm,
  GuardianVO,
  StudentChangeLogVO,
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
  changeEnrollmentStatus
};
