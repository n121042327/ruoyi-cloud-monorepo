import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TeacherQuery, TeacherVO } from './types';

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

export default {
  listTeacher
};
