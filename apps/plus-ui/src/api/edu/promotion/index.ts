import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  EnrollmentChangeQuery,
  EnrollmentChangeVO,
  PromotionBatchAdjustForm,
  PromotionItemAdjustForm,
  PromotionItemQuery,
  PromotionItemVO,
  PromotionTaskForm,
  PromotionTaskQuery,
  PromotionTaskVO,
  TransferAcceptForm,
  TransferForm,
  TransferOrderVO
} from './types';

/**
 * 查询升班任务列表（按学校维度，升班任务不跨校共享，BR-DATA-018）
 *
 * 对应 operationId `listPromotionTask`（GET /edu/promotion/task/list）。
 */
export const listPromotionTask = (query?: PromotionTaskQuery): AxiosPromise<PromotionTaskVO[]> => {
  return request({
    url: '/edu/promotion/task/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询升班任务详情（进入预览 / 校验 / 结果页时加载任务上下文）
 *
 * 对应 operationId `getPromotionTask`（GET /edu/promotion/task/{id}）。
 */
export const getPromotionTask = (taskId: string): AxiosPromise<PromotionTaskVO> => {
  return request({
    url: `/edu/promotion/task/${taskId}`,
    method: 'get'
  });
};

/**
 * 新建升班任务（创建后状态为草稿，可修改范围后重新生成预览，REQ-PRM-011 / 012）
 *
 * 对应 operationId `addPromotionTask`（POST /edu/promotion/task）。
 */
export const addPromotionTask = (data: PromotionTaskForm): AxiosPromise<PromotionTaskVO> => {
  return request({
    url: '/edu/promotion/task',
    method: 'post',
    data
  });
};

/**
 * 取消升班任务（取消后保留已完成部分，不做整批回滚，REQ-PRM-036 / 058）
 *
 * 对应 operationId `cancelPromotionTask`（POST /edu/promotion/task/{id}/cancel）。
 */
export const cancelPromotionTask = (taskId: string, reason: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/cancel`,
    method: 'post',
    data: { reason }
  });
};

/**
 * 重试失败项 / 继续执行剩余项（只处理失败项，已成功记录不重复执行，REQ-PRM-032）
 *
 * 对应 operationId `retryPromotionTask`（POST /edu/promotion/task/{id}/retry）。
 */
export const retryPromotionTask = (taskId: string, reason?: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/retry`,
    method: 'post',
    data: { reason }
  });
};

/**
 * 导出升班任务台账（年级主任导出限本人负责年级）
 *
 * 对应 operationId `exportPromotionTask`（GET /edu/promotion/task/export）。
 */
export const exportPromotionTask = (query?: PromotionTaskQuery) => {
  return request({
    url: '/edu/promotion/task/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  });
};

/**
 * 导出升班结果（成功清单 / 失败清单 / 留级清单 / 毕业清单，REQ-PRM-034）
 *
 * 对应 operationId `exportPromotionResult`（GET /edu/promotion/task/{id}/result/export）。
 */
export const exportPromotionResult = (taskId: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/result/export`,
    method: 'get',
    responseType: 'blob'
  });
};

/**
 * 生成升班预览（不写入任何学生数据，REQ-PRM-020；重新预览会覆盖旧明细，REQ-PRM-021）
 *
 * 对应 operationId `previewPromotionTask`（POST /edu/promotion/task/{id}/preview）。
 */
export const previewPromotionTask = (taskId: string): AxiosPromise<PromotionTaskVO> => {
  return request({
    url: `/edu/promotion/task/${taskId}/preview`,
    method: 'post'
  });
};

/**
 * 查询升班明细（预览 / 校验 / 结果共用）
 *
 * 对应 operationId `getPromotionTask`（GET /edu/promotion/task/{id}，明细随详情返回）。
 */
export const listPromotionItem = (taskId: string, query?: PromotionItemQuery): AxiosPromise<PromotionItemVO[]> => {
  return request({
    url: `/edu/promotion/task/${taskId}`,
    method: 'get',
    params: { ...query, withItems: true }
  });
};

/**
 * 逐条调整升班去向（PAGE-PRM-ADJUST）
 *
 * 对应 operationId `updatePromotionItem`（POST /edu/promotion/task/{id}/item）。
 */
export const updatePromotionItem = (taskId: string, data: PromotionItemAdjustForm) => {
  return request({
    url: `/edu/promotion/task/${taskId}/item`,
    method: 'post',
    data
  });
};

/**
 * 按源班级批量指定目标班级（REQ-PRM-018）
 *
 * 对应 operationId `batchUpdatePromotionItem`（POST /edu/promotion/task/{id}/item/batch）。
 */
export const batchUpdatePromotionItem = (taskId: string, data: PromotionBatchAdjustForm) => {
  return request({
    url: `/edu/promotion/task/${taskId}/item/batch`,
    method: 'post',
    data
  });
};

/**
 * 升班校验（校验通过才允许执行，REQ-PRM-027）
 *
 * 对应 operationId `validatePromotionTask`（POST /edu/promotion/task/{id}/validate）。
 */
export const validatePromotionTask = (taskId: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/validate`,
    method: 'post'
  });
};

/**
 * 执行升班（异步进行，按学年追加不改写历史）
 *
 * 对应 operationId `executePromotionTask`（POST /edu/promotion/task/{id}/execute）。
 */
export const executePromotionTask = (taskId: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/execute`,
    method: 'post'
  });
};

/**
 * 导出升班预览（预览阶段明细，REQ-PRM-019）
 *
 * 对应 operationId `exportPromotionPreview`（GET /edu/promotion/task/{id}/preview/export）。
 */
export const exportPromotionPreview = (taskId: string) => {
  return request({
    url: `/edu/promotion/task/${taskId}/preview/export`,
    method: 'get',
    responseType: 'blob'
  });
};

/**
 * 查询转学单列表（转入校待接收清单）
 *
 * 对应 operationId `listTransfer`（GET /edu/enrollment/transfer/list）。
 */
export const listTransfer = (query?: Partial<PageQuery>): AxiosPromise<TransferOrderVO[]> => {
  return request({
    url: '/edu/enrollment/transfer/list',
    method: 'get',
    params: query
  });
};

/**
 * 发起转出（跨校转学的唯一发起入口，DP-01 / GAP-085 裁决 A）
 *
 * 对应 operationId `addTransfer`（POST /edu/enrollment/transfer，权限 `enrollment.transfer:create`）。
 */
export const addTransfer = (data: TransferForm): AxiosPromise<TransferOrderVO> => {
  return request({
    url: '/edu/enrollment/transfer',
    method: 'post',
    data
  });
};

/**
 * 撤销接收 / 撤销申请（转入校撤销接收或转出校撤销未接收的申请）
 *
 * 对应 operationId `cancelTransfer`（POST /edu/enrollment/transfer/{id}/cancel）。
 */
export const cancelTransfer = (transferId: string) => {
  return request({
    url: `/edu/enrollment/transfer/${transferId}/cancel`,
    method: 'post'
  });
};

/**
 * 转入校接收（接收动作本身即审批，接收前不计入转入校任何在读数）
 *
 * 对应 operationId `acceptTransfer`（POST /edu/enrollment/transfer/{id}/accept）。
 */
export const acceptTransfer = (data: TransferAcceptForm): AxiosPromise<TransferOrderVO> => {
  return request({
    url: `/edu/enrollment/transfer/${data.transferId}/accept`,
    method: 'post',
    data
  });
};

/**
 * 办理报到（学生到校报到，状态由「转入未报到」转为「在读」，可同时指定班级）
 *
 * 对应 operationId `checkInTransfer`（POST /edu/enrollment/transfer/{id}/check-in）。
 */
export const checkInTransfer = (transferId: string, data?: { toClassId?: string }): AxiosPromise<TransferOrderVO> => {
  return request({
    url: `/edu/enrollment/transfer/${transferId}/check-in`,
    method: 'post',
    data
  });
};

/**
 * 学籍异动记录（异动历史）
 *
 * 对应 operationId `listEnrollmentChange`（GET /edu/enrollment/change/list）。
 */
export const listEnrollmentChange = (query?: EnrollmentChangeQuery): AxiosPromise<EnrollmentChangeVO[]> => {
  return request({
    url: '/edu/enrollment/change/list',
    method: 'get',
    params: query
  });
};

export default {
  listPromotionTask,
  getPromotionTask,
  addPromotionTask,
  cancelPromotionTask,
  retryPromotionTask,
  exportPromotionTask,
  exportPromotionResult,
  previewPromotionTask,
  listPromotionItem,
  updatePromotionItem,
  batchUpdatePromotionItem,
  validatePromotionTask,
  executePromotionTask,
  exportPromotionPreview,
  listTransfer,
  addTransfer,
  cancelTransfer,
  acceptTransfer,
  checkInTransfer,
  listEnrollmentChange
};
