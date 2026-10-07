import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CampusVO, SchoolVO } from './types';

/**
 * 查询学校列表（学生列表查询区的「学校」下拉，仅平台运营可切换）
 *
 * 对应 operationId `listSchool`（GET /edu/school/list）。
 */
export const listSchool = (): AxiosPromise<SchoolVO[]> => {
  return request({
    url: '/edu/school/list',
    method: 'get'
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

export default {
  listSchool,
  getCurrentSchool,
  listCampus
};
