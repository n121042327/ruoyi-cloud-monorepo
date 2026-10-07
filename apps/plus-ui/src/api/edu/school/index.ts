import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CampusVO, SchoolVO } from './types';

/** 学校列表查询参数 */
export interface SchoolQuery extends Partial<PageQuery> {
  /** 集团 */
  parentTenantId?: string;
  schoolName?: string;
  schoolCode?: string;
  /** 办学类型 */
  schoolType?: string;
  status?: string;
  stageCode?: string;
  /** 关键字：学校名称 / 学校编码 */
  keyword?: string;
}

/**
 * 查询学校列表（学生列表查询区的「学校」下拉，仅平台运营可切换）
 *
 * 对应 operationId `listSchool`（GET /edu/school/list）。
 */
export const listSchool = (query?: SchoolQuery): AxiosPromise<SchoolVO[]> => {
  return request({
    url: '/edu/school/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询当前学校（学校侧角色锁定本校）
 *
 * 对应 operationId `getCurrentSchool`（GET /edu/school/current）。
 */
export const getCurrentSchool = (): AxiosPromise<SchoolVO> => {
  return request({
    url: '/edu/school/current',
    method: 'get'
  });
};

/**
 * 查询校区列表（班级列表查询区的「校区」下拉；校区不参与数据权限判定）
 *
 * 对应 operationId `listCampus`（GET /edu/school/{id}/campus）。
 */
export const listCampus = (schoolId: string): AxiosPromise<CampusVO[]> => {
  return request({
    url: `/edu/school/${schoolId}/campus`,
    method: 'get'
  });
};

/**
 * 停用学校（需二次确认并填写原因；停用后该校租户下所有人员登录与写操作被拒绝，历史数据保留）
 *
 * 对应 operationId `disableSchool`（POST /edu/school/{id}/disable）。
 */
export const disableSchool = (schoolId: string, reason: string) => {
  return request({
    url: `/edu/school/${schoolId}/disable`,
    method: 'post',
    data: { reason }
  });
};

/**
 * 启用学校
 *
 * 对应 operationId `enableSchool`（POST /edu/school/{id}/enable）。
 */
export const enableSchool = (schoolId: string) => {
  return request({
    url: `/edu/school/${schoolId}/enable`,
    method: 'post'
  });
};

export default {
  listSchool,
  getCurrentSchool,
  listCampus,
  disableSchool,
  enableSchool
};
