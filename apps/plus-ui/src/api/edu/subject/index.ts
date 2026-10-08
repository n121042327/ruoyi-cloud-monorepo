import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SubjectOptionVO, SubjectQuery, SubjectVO } from './types';

/**
 * 学科下拉清单（按学段过滤；各模块高频调用，接口层带缓存）
 *
 * 对应 operationId `listSubjectOption`（GET /edu/subject/option）。
 */
export const listSubjectOption = (query?: { stageCode?: string }): AxiosPromise<SubjectOptionVO[]> => {
  return request({
    url: '/edu/subject/option',
    method: 'get',
    params: query
  });
};

/**
 * 学科列表（按排序号升序）
 *
 * 对应 operationId `listSubject`（GET /edu/subject/list）。
 */
export const listSubject = (query?: SubjectQuery): AxiosPromise<SubjectVO[]> => {
  return request({
    url: '/edu/subject/list',
    method: 'get',
    params: query
  });
};

export default {
  listSubjectOption,
  listSubject
};
