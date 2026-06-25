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

const getMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.Message>>('/system/message/page', { params });
const getMessageDetail = (id: number) =>
  requestClient.get<SystemMessageApi.Message>(`/system/message/${id}`);
const createMessage = (data: Partial<SystemMessageApi.Message>) =>
  requestClient.post('/system/message', data);
const updateMessage = (id: number, data: Partial<SystemMessageApi.Message>) =>
  requestClient.put(`/system/message/${id}`, data);
const updateMessageStatus = (id: number, status: number) =>
  requestClient.put(`/system/message/${id}/status`, undefined, { params: { status } });
const deleteMessage = (id: number) => requestClient.delete(`/system/message/${id}`);

const getRecentMessages = (limit = 10) =>
  requestClient.get<SystemMessageApi.UserMessage[]>('/system/message/inbox/recent', {
    params: { limit },
  });
const getInboxMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.UserMessage>>('/system/message/inbox/page', {
    params,
  });
const getUnreadMessageCount = () =>
  requestClient.get<{ count: number }>('/system/message/inbox/unread-count');
const markMessageRead = (id: number | string) =>
  requestClient.put(`/system/message/inbox/${id}/read`);
const markAllMessagesRead = () => requestClient.put('/system/message/inbox/read-all');
const deleteInboxMessage = (id: number | string) =>
  requestClient.delete(`/system/message/inbox/${id}`);
const clearMessages = () => requestClient.delete('/system/message/inbox/clear');

export {
  clearMessages,
  createMessage,
  deleteInboxMessage,
  deleteMessage,
  getInboxMessagePage,
  getMessageDetail,
  getMessagePage,
  getRecentMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
  updateMessage,
  updateMessageStatus,
};
