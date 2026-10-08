import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { GradeForm, GradeQuery, GradeVO } from './types';

/**
 * 查询年级列表（学生列表查询区的「年级」下拉）
 *
 * 对应 operationId `listGrade`（GET /edu/grade/list）。
 */
export const listGrade = (query?: GradeQuery): AxiosPromise<GradeVO[]> => {
  return request({
    url: '/edu/grade/list',
    method: 'get',
    params: query
  });
};

/**
 * 新增年级
 *
 * 对应 operationId `addGrade`（POST /edu/grade）。
 */
export const addGrade = (data: GradeForm) => {
  return request({
    url: '/edu/grade',
    method: 'post',
    data
  });
};

/**
 * 编辑年级（学段与学段内序号一经创建不可修改，REQ-GRD-018）
 *
 * 对应 operationId `updateGrade`（PUT /edu/grade）。
 */
export const updateGrade = (data: GradeForm) => {
  return request({
    url: '/edu/grade',
    method: 'put',
    data
  });
};

/**
 * 按学段批量生成年级（选择学段与入学年份，一次生成该学段全部年级）
 *
 * 对应 operationId `batchAddGrade`（POST /edu/grade/batch）。
 */
export const batchAddGrade = (data: { schoolId: string; stageCode: string; enrollYear: string }) => {
  return request({
    url: '/edu/grade/batch',
    method: 'post',
    data
  });
};

export default {
  listGrade,
  addGrade,
  updateGrade,
  batchAddGrade
};
