import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { GuardianForm, GuardianVO, StudentChangeLogVO, StudentForm, StudentQuery, StudentVO } from './types';
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

export default {
  listStudent,
  getStudent,
  addStudent,
  updateStudent,
  listStudentGuardian,
  saveStudentGuardian,
  listStudentChangeLog,
  viewStudentIdCard,
  viewStudentPhone
};
