import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { GradeForm, GradeLeaderForm, GradeLeaderVO, GradeQuery, GradeVO } from './types';

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

/**
 * 查询年级详情
 *
 * 对应 operationId `getGrade`（GET /edu/grade/{id}）。
 */
export const getGrade = (gradeId: string): AxiosPromise<GradeVO> => {
  return request({
    url: `/edu/grade/${gradeId}`,
    method: 'get'
  });
};

/**
 * 删除年级（有班级或在读学生时后端拒绝）
 *
 * 对应 operationId `removeGrade`（DELETE /edu/grade/{id}）。
 */
export const removeGrade = (gradeId: string, reason?: string) => {
  return request({
    url: `/edu/grade/${gradeId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 年级归档（归档后不允许新增班级，只读保留）
 *
 * 对应 operationId `archiveGrade`（POST /edu/grade/{id}/archive）。
 */
export const archiveGrade = (gradeId: string, reason: string) => {
  return request({
    url: `/edu/grade/${gradeId}/archive`,
    method: 'post',
    params: { reason }
  });
};

/**
 * 查询年级主任任职（DS-05）
 *
 * 对应 operationId `listGradeLeader`（GET /edu/grade/{id}/leader）。
 */
export const listGradeLeader = (gradeId: string, termId?: string): AxiosPromise<GradeLeaderVO[]> => {
  return request({
    url: `/edu/grade/${gradeId}/leader`,
    method: 'get',
    params: { termId }
  });
};

/**
 * 指定 / 变更年级主任任职（DS-05 的唯一写入入口）
 *
 * 对应 operationId `saveGradeLeader`（POST /edu/grade/{id}/leader）。
 */
export const saveGradeLeader = (gradeId: string, data: GradeLeaderForm) => {
  return request({
    url: `/edu/grade/${gradeId}/leader`,
    method: 'post',
    data
  });
};

/**
 * 年级主任离任（置 status=0，不物理删除）
 *
 * 对应 operationId `removeGradeLeader`（DELETE /edu/grade/{id}/leader/{leaderId}）。
 */
export const removeGradeLeader = (gradeId: string, leaderId: string, reason?: string) => {
  return request({
    url: `/edu/grade/${gradeId}/leader/${leaderId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 年级升班只读视图（「学段内序号 +1」的参考，升班唯一执行入口在升班模块）
 *
 * 对应 operationId `getGradePromotionView`（GET /edu/grade/promotion-view）。
 */
export const getGradePromotionView = (query?: { schoolId?: string; termId?: string }): AxiosPromise<GradeVO[]> => {
  return request({
    url: '/edu/grade/promotion-view',
    method: 'get',
    params: query
  });
};

/**
 * 年级列表导出（统一走导出引擎）
 *
 * 对应 operationId `exportGrade`（POST /edu/grade/export）。
 */
export const exportGrade = (data?: Record<string, unknown>) => {
  return request({
    url: '/edu/grade/export',
    method: 'post',
    data: data ?? {}
  });
};

export default {
  listGrade,
  addGrade,
  updateGrade,
  batchAddGrade
};
