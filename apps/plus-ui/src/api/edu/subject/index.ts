import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SubjectOptionVO } from './types';

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

export default {
  listSubjectOption
};
