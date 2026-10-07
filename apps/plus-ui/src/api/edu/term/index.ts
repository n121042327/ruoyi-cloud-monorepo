import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AcademicYearQuery, AcademicYearVO, TermVO } from './types';

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

/**
 * 查询学年列表（按学校维度分组，按学年编码倒序）
 *
 * 对应 operationId `listAcademicYear`（GET /edu/term/year/list）。
 */
export const listAcademicYear = (query?: AcademicYearQuery): AxiosPromise<AcademicYearVO[]> => {
  return request({
    url: '/edu/term/year/list',
    method: 'get',
    params: query
  });
};

/**
 * 设为当前学年学期（作用于学期级别，同一学校唯一；已结束年份不得设为当前）
 *
 * 对应 operationId `setCurrentTerm`（POST /edu/term/{id}/set-current）。
 */
export const setCurrentTerm = (termId: string) => {
  return request({
    url: `/edu/term/${termId}/set-current`,
    method: 'post'
  });
};

export default {
  listTerm,
  getCurrentTerm,
  listAcademicYear,
  setCurrentTerm
};
