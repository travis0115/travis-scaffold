import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemNoticeApi {
  export interface Notice {
    content: string;
    createTime?: string;
    id: number;
    isPinned?: number;
    publishTime?: string;
    remark?: string;
    sort?: number;
    status: number;
    title: string;
  }
}

const getNoticePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemNoticeApi.Notice>>('/system/notice/page', {
    params,
  });
const getNoticeDetail = (id: number) =>
  requestClient.get<SystemNoticeApi.Notice>(`/system/notice/${id}`);
const createNotice = (data: Partial<SystemNoticeApi.Notice>) =>
  requestClient.post('/system/notice', data);
const updateNotice = (id: number, data: Partial<SystemNoticeApi.Notice>) =>
  requestClient.put(`/system/notice/${id}`, data);
const updateNoticeStatus = (id: number, status: number) =>
  requestClient.put(`/system/notice/${id}/status`, undefined, {
    params: { status },
  });
const deleteNotice = (id: number) =>
  requestClient.delete(`/system/notice/${id}`);

export {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticePage,
  updateNotice,
  updateNoticeStatus,
};
