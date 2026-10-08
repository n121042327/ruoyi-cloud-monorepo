import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CampusVO, SchoolForm, SchoolVO } from './types';

/** 学校列表查询参数 */
export interface SchoolQuery extends Partial<PageQuery> {
  /** 集团 */
  parentTenantId?: string;
  schoolName?: string;
  schoolCode?: string;
  /** 办学类型 */
  schoolType?: string;
  /** 状态：active 正常 / disabled 已停用（后端查询参数名为 schoolStatus） */
  schoolStatus?: string;
  /** 学段：原型有该筛选项，但 listSchool 的后端查询暂不支持按学段过滤（见 GAP-101），当前传参不生效 */
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
 * 新建学校（学校与租户一一对应，BR-ORG-002）
 *
 * 对应 operationId `addSchool`（POST /edu/school），需 `org.school:create`。
 */
export const addSchool = (data: SchoolForm): AxiosPromise<void> => {
  return request({
    url: '/edu/school',
    method: 'post',
    data
  });
};

/**
 * 编辑学校（学校与租户的绑定关系不可修改，REQ-SCH-022）
 *
 * 对应 operationId `updateSchool`（PUT /edu/school），需 `org.school:update`。
 */
export const updateSchool = (data: SchoolForm): AxiosPromise<void> => {
  return request({
    url: '/edu/school',
    method: 'put',
    data
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

/**
 * 停用 / 删除校区（已被班级引用的校区不允许删除，只允许停用，REQ-SCH-030）
 *
 * 对应 operationId `removeCampus`（DELETE /edu/school/campus/{id}）。
 */
export const removeCampus = (campusId: string, reason?: string) => {
  return request({
    url: `/edu/school/campus/${campusId}`,
    method: 'delete',
    params: { reason }
  });
};

/**
 * 开通初始化（一键完成学年学期、学科模板、基础角色初始化；幂等）
 *
 * 对应 operationId `initSchoolBaseline`（POST /edu/school/{id}/init，REQ-SCH-019 / 045）。
 */
export const initSchoolBaseline = (
  schoolId: string,
  data: { stageCodes: string[]; academicYearCode: string; startDate: string; endDate: string }
) => {
  return request({
    url: `/edu/school/${schoolId}/init`,
    method: 'post',
    data
  });
};

export default {
  listSchool,
  addSchool,
  updateSchool,
  getCurrentSchool,
  listCampus,
  disableSchool,
  enableSchool,
  removeCampus,
  initSchoolBaseline
};
