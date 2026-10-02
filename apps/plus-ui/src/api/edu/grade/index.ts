import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { GradeQuery, GradeVO } from './types';

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

export default {
  listGrade
};
