import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TermVO } from './types';

/**
 * 查询学期列表
 *
 * 对应 operationId `listTerm`（GET /edu/term/list）。
 */
export const listTerm = (query?: { schoolId?: string; academicYearId?: string }): AxiosPromise<TermVO[]> => {
  return request({
    url: '/edu/term/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询当前学年学期（各模块默认学期上下文的权威来源）
 *
 * 对应 operationId `getCurrentTerm`（GET /edu/term/current）。
 */
export const getCurrentTerm = (): AxiosPromise<TermVO> => {
  return request({
    url: '/edu/term/current',
    method: 'get'
  });
};

export default {
  listTerm,
  getCurrentTerm
};
