import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { StudentForm, StudentQuery, StudentVO } from './types';
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

export default {
  listStudent,
  getStudent,
  addStudent,
  updateStudent
};
