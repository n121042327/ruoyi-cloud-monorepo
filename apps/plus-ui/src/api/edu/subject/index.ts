import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SubjectForm, SubjectOptionVO, SubjectQuery, SubjectReferenceVO, SubjectVO } from './types';

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

/**
 * 查询学科详情
 *
 * 对应 operationId `getSubject`（GET /edu/subject/{id}）。
 */
export const getSubject = (subjectId: string): AxiosPromise<SubjectVO> => {
  return request({
    url: `/edu/subject/${subjectId}`,
    method: 'get'
  });
};

/**
 * 新增学科（编码校内唯一，BR-SUBJECT-001）
 *
 * 对应 operationId `addSubject`（POST /edu/subject）。
 */
export const addSubject = (data: SubjectForm) => {
  return request({
    url: '/edu/subject',
    method: 'post',
    data
  });
};

/**
 * 编辑学科（学科编码一经创建不可修改）
 *
 * 对应 operationId `updateSubject`（PUT /edu/subject）。
 */
export const updateSubject = (data: SubjectForm) => {
  return request({
    url: '/edu/subject',
    method: 'put',
    data
  });
};

/**
 * 删除学科（删除前检查三类引用，有引用时后端拒绝，只允许停用）
 *
 * 对应 operationId `removeSubject`（DELETE /edu/subject/{id}）。
 */
export const removeSubject = (subjectId: string, reason?: string) => {
  return request({
    url: `/edu/subject/${subjectId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 按学段批量初始化默认学科清单（REQ-SUB-011）
 *
 * 对应 operationId `batchInitSubject`（POST /edu/subject/batch-init）。
 */
export const batchInitSubject = (initStageCodes: string[]) => {
  return request({
    url: '/edu/subject/batch-init',
    method: 'post',
    data: { initStageCodes }
  });
};

/**
 * 保存学科启用学段（多选，REQ-SUB-014）
 *
 * 对应 operationId `saveSubjectStage`（POST /edu/subject/{id}/stage）。
 */
export const saveSubjectStage = (subjectId: string, stageCodes: string[]) => {
  return request({
    url: `/edu/subject/${subjectId}/stage`,
    method: 'post',
    data: { stageCodes }
  });
};

/**
 * 保存选科角色（primary 首选 / secondary 再选 / none 不参与，REQ-SUB-015）
 *
 * 对应 operationId `saveSubjectStreamRole`（POST /edu/subject/{id}/stream-role）。
 */
export const saveSubjectStreamRole = (subjectId: string, data: { streamEnabled: string; streamRole: string }) => {
  return request({
    url: `/edu/subject/${subjectId}/stream-role`,
    method: 'post',
    data
  });
};

/**
 * 停用学科（有引用时只能停用，BR-SUBJECT-006）
 *
 * 对应 operationId `disableSubject`（POST /edu/subject/{id}/disable）。
 */
export const disableSubject = (subjectId: string, reason: string) => {
  return request({
    url: `/edu/subject/${subjectId}/disable`,
    method: 'post',
    params: { reason }
  });
};

/**
 * 启用学科
 *
 * 对应 operationId `enableSubject`（POST /edu/subject/{id}/enable）。
 */
export const enableSubject = (subjectId: string, reason?: string) => {
  return request({
    url: `/edu/subject/${subjectId}/enable`,
    method: 'post',
    params: { reason }
  });
};

/**
 * 学科引用检查（任教关系 / 教学班 / 学生选科）
 *
 * 对应 operationId `checkSubjectReference`（GET /edu/subject/{id}/reference）。
 */
export const checkSubjectReference = (subjectId: string): AxiosPromise<SubjectReferenceVO> => {
  return request({
    url: `/edu/subject/${subjectId}/reference`,
    method: 'get'
  });
};

export default {
  listSubjectOption,
  listSubject,
  getSubject,
  addSubject,
  updateSubject,
  removeSubject,
  batchInitSubject,
  saveSubjectStage,
  saveSubjectStreamRole,
  disableSubject,
  enableSubject,
  checkSubjectReference
};
