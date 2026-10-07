import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { EnrollmentChangeQuery, EnrollmentChangeVO, TransferAcceptForm, TransferForm, TransferOrderVO } from './types';

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
  listTransfer,
  addTransfer,
  cancelTransfer,
  acceptTransfer,
  checkInTransfer,
  listEnrollmentChange
};
