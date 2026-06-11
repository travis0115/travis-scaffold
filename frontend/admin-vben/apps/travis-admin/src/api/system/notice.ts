import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemNoticeApi {
  export interface Notice {
    [key: string]: any;
    content: string;
    id: number;
    audienceType: number;
    noticeType: number;
    publishTime?: string;
    remark?: string;
    status: number;
    targetIds?: number[];
    title: string;
  }

  export interface UserMessage {
    content: string;
    createTime: string;
    id: number;
    noticeId: number;
    noticeType: number;
    publishTime?: string;
    readStatus: number;
    readTime?: string;
    title: string;
  }
}

const getNoticePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemNoticeApi.Notice>>('/system/notice/page', { params });
const getNoticeDetail = (id: number) =>
  requestClient.get<SystemNoticeApi.Notice>(`/system/notice/${id}`);
const createNotice = (data: Partial<SystemNoticeApi.Notice>) =>
  requestClient.post('/system/notice', data);
const updateNotice = (id: number, data: Partial<SystemNoticeApi.Notice>) =>
  requestClient.put(`/system/notice/${id}`, data);
const deleteNotice = (id: number) => requestClient.delete(`/system/notice/${id}`);
const getRecentMessages = (limit = 10) =>
  requestClient.get<SystemNoticeApi.UserMessage[]>('/system/message/recent', {
    params: { limit },
  });
const getMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemNoticeApi.UserMessage>>('/system/message/page', {
    params,
  });
const getUnreadMessageCount = () =>
  requestClient.get<{ count: number }>('/system/message/unread-count');
const markMessageRead = (id: number | string) =>
  requestClient.put(`/system/message/${id}/read`);
const markAllMessagesRead = () => requestClient.put('/system/message/read-all');
const deleteMessage = (id: number | string) => requestClient.delete(`/system/message/${id}`);
const clearMessages = () => requestClient.delete('/system/message/clear');

export {
  clearMessages,
  createNotice,
  deleteMessage,
  deleteNotice,
  getMessagePage,
  getNoticeDetail,
  getNoticePage,
  getRecentMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
  updateNotice,
};
