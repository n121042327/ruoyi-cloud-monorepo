import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TeacherForm, TeacherQuery, TeacherVO } from './types';

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

export default {
  listTeacher,
  addTeacher,
  updateTeacher
};
