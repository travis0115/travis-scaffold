import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemMessageApi {
  export interface Message {
    [key: string]: any;
    audienceType: number;
    channels?: string;
    content: string;
    id: number;
    messageType: number;
    publishTime?: string;
    remark?: string;
    sourceId?: string;
    sourceType?: string;
    status: number;
    targetIds?: number[];
    title: string;
  }

  export interface UserMessage {
    content: string;
    createTime: string;
    id: number;
    messageId: number;
    messageType: number;
    publishTime?: string;
    readStatus: number;
    readTime?: string;
    title: string;
  }
}

const getMessagePushPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.Message>>('/system/message/push/page', { params });
const getMessagePushDetail = (id: number) =>
  requestClient.get<SystemMessageApi.Message>(`/system/message/push/${id}`);
const createMessagePush = (data: Partial<SystemMessageApi.Message>) =>
  requestClient.post('/system/message/push', data);
const updateMessagePush = (id: number, data: Partial<SystemMessageApi.Message>) =>
  requestClient.put(`/system/message/push/${id}`, data);
const updateMessagePushStatus = (id: number, status: number) =>
  requestClient.put(`/system/message/push/${id}/status`, undefined, { params: { status } });
const deleteMessagePush = (id: number) => requestClient.delete(`/system/message/push/${id}`);

const getRecentMessages = (limit = 10) =>
  requestClient.get<SystemMessageApi.UserMessage[]>('/system/message/recent', {
    params: { limit },
  });
const getMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.UserMessage>>('/system/message/page', {
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
  createMessagePush,
  deleteMessage,
  deleteMessagePush,
  getMessagePage,
  getMessagePushDetail,
  getMessagePushPage,
  getRecentMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
  updateMessagePush,
  updateMessagePushStatus,
};
