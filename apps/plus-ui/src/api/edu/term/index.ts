import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AcademicYearForm, AcademicYearQuery, AcademicYearReference, AcademicYearVO, TermForm, TermVO } from './types';

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
 * 查询学年详情（编辑弹窗回填使用）
 *
 * 对应 operationId `getAcademicYear`（GET /edu/term/year/{id}）。
 */
export const getAcademicYear = (academicYearId: string): AxiosPromise<AcademicYearVO> => {
  return request({
    url: `/edu/term/year/${academicYearId}`,
    method: 'get'
  });
};

/**
 * 新建学年（同步创建默认学期结构，REQ-TERM-012）
 *
 * 对应 operationId `addAcademicYear`（POST /edu/term/year）。
 */
export const addAcademicYear = (data: AcademicYearForm) => {
  return request({
    url: '/edu/term/year',
    method: 'post',
    data
  });
};

/**
 * 编辑学年
 *
 * 对应 operationId `updateAcademicYear`（PUT /edu/term/year）。
 */
export const updateAcademicYear = (data: AcademicYearForm) => {
  return request({
    url: '/edu/term/year',
    method: 'put',
    data
  });
};

/**
 * 学年引用检查（归档前展示班级 / 任教关系 / 花名册 / 选科引用，REQ-TERM-029 / 034）
 *
 * 对应 operationId `checkTermReference`（GET /edu/term/{id}/reference）。
 */
export const checkTermReference = (academicYearId: string): AxiosPromise<AcademicYearReference> => {
  return request({
    url: `/edu/term/${academicYearId}/reference`,
    method: 'get'
  });
};

/**
 * 归档学年（有引用时只允许归档不删除；归档后移出新建业务可选列表，REQ-TERM-028 / 030）
 *
 * 对应 operationId `archiveAcademicYear`（POST /edu/term/year/{id}/archive）。
 */
export const archiveAcademicYear = (academicYearId: string, reason: string) => {
  return request({
    url: `/edu/term/year/${academicYearId}/archive`,
    method: 'post',
    data: { reason }
  });
};

/**
 * 撤销归档（误操作纠正，需租户管理员，REQ-TERM-033）
 *
 * 对应 operationId `revokeArchiveAcademicYear`（POST /edu/term/year/{id}/archive/revoke）。
 */
export const revokeArchiveAcademicYear = (academicYearId: string, reason: string) => {
  return request({
    url: `/edu/term/year/${academicYearId}/archive/revoke`,
    method: 'post',
    data: { reason }
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

/**
 * 删除学期（已被班级 / 任教关系 / 花名册引用的学期不允许删除，REQ-TERM-019）
 *
 * 对应 operationId `removeTerm`（DELETE /edu/term/{id}）。
 */
export const removeTerm = (termId: string, reason?: string) => {
  return request({
    url: `/edu/term/${termId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 新建 / 编辑学期（有 termId 为编辑）
 *
 * 对应 operationId `saveTerm`（POST /edu/term）。
 */
export const saveTerm = (data: TermForm) => {
  return request({
    url: '/edu/term',
    method: 'post',
    data
  });
};

export default {
  listTerm,
  getCurrentTerm,
  listAcademicYear,
  getAcademicYear,
  addAcademicYear,
  updateAcademicYear,
  checkTermReference,
  archiveAcademicYear,
  revokeArchiveAcademicYear,
  setCurrentTerm,
  removeTerm
};
