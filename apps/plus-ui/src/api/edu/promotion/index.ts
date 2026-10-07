import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { TransferForm, TransferOrderVO } from './types';

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

export default {
  listTransfer,
  addTransfer,
  cancelTransfer
};
