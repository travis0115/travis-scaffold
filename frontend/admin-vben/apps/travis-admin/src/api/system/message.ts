import type { AxiosProgressEvent } from '@vben/request';
import type { Recordable } from '@vben/types';

import type { FileUploadResult } from './file';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemMessageApi {
  export type Id = number | string;

  export enum WebSocketEvent {
    Deleted = 'SYSTEM_MESSAGE_DELETED',
    InboxChanged = 'SYSTEM_MESSAGE_INBOX_CHANGED',
    Published = 'SYSTEM_MESSAGE_PUBLISHED',
    Republished = 'SYSTEM_MESSAGE_REPUBLISHED',
    Revoked = 'SYSTEM_MESSAGE_REVOKED',
  }

  export interface Message {
    [key: string]: any;
    channel?: string;
    content: string;
    id: Id;
    jumpUrl?: string;
    messageType: number;
    pushType: number;
    publishTime?: string;
    receiverScope: number;
    receiverType: string;
    receiverValues?: Id[];
    remark?: string;
    sourceId?: string;
    sourceType?: string;
    status: number;
    templateId?: Id;
    templateParams?: string;
    title: string;
  }

  export interface MessageSaveRequest {
    channel: string;
    content?: string;
    jumpUrl?: string;
    publishTime?: string;
    pushType: 0 | 1;
    receiverScope: number;
    receiverType: string;
    receiverValues?: Id[];
    remark?: string;
    templateId?: Id;
    templateParams?: string;
    title?: string;
  }

  export interface MessageTemplate {
    channel: string;
    content?: string;
    contentSchema?: string;
    createTime?: string;
    id: Id;
    isBuiltin: 0 | 1;
    platformTemplateId?: string;
    redirectUrl?: string;
    remark?: string;
    status: number;
    templateCode: string;
    templateName: string;
    title?: string;
  }

  export interface MessageTemplateSaveRequest {
    channel: string;
    content: string;
    contentSchema?: string;
    platformTemplateId?: string;
    redirectUrl?: string;
    remark?: string;
    status: number;
    templateCode?: string;
    templateName: string;
    title?: string;
  }

  export interface UserMessage {
    content?: string;
    createTime: string;
    messageId: Id;
    messageType: number;
    publishTime?: string;
    readStatus: number;
    readTime?: string;
    sourceId?: string;
    sourceType?: string;
    title: string;
  }

  export interface UserMessageDetail extends UserMessage {
    content: string;
    metadata?: Record<string, any>;
  }
}

const getMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.Message>>(
    '/system/message/page',
    { params },
  );
const getMessageDetail = (id: SystemMessageApi.Id) =>
  requestClient.get<SystemMessageApi.Message>(`/system/message/${id}`);
const createMessage = (data: SystemMessageApi.MessageSaveRequest) =>
  requestClient.post('/system/message', data);
const updateMessage = (
  id: SystemMessageApi.Id,
  data: SystemMessageApi.MessageSaveRequest,
) => requestClient.put(`/system/message/${id}`, data);
const deleteMessage = (id: SystemMessageApi.Id) =>
  requestClient.delete(`/system/message/${id}`);
const pushMessage = (id: SystemMessageApi.Id) =>
  requestClient.put(`/system/message/${id}/push`);
const revokeMessage = (id: SystemMessageApi.Id) =>
  requestClient.put(`/system/message/${id}/revoke`);
const uploadMessageImage = (
  file: File,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
) => {
  const formData = new FormData();
  formData.append('file', file);
  return requestClient.post<FileUploadResult>(
    '/system/message/image/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    },
  );
};

const getMessageTemplatePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.MessageTemplate>>(
    '/system/message/template/page',
    { params },
  );
const getMessageTemplateDetail = (id: SystemMessageApi.Id) =>
  requestClient.get<SystemMessageApi.MessageTemplate>(
    `/system/message/template/${id}`,
  );
const createMessageTemplate = (
  data: SystemMessageApi.MessageTemplateSaveRequest,
) => requestClient.post('/system/message/template', data);
const updateMessageTemplate = (
  id: SystemMessageApi.Id,
  data: SystemMessageApi.MessageTemplateSaveRequest,
) => requestClient.put(`/system/message/template/${id}`, data);
const deleteMessageTemplate = (id: SystemMessageApi.Id) =>
  requestClient.delete(`/system/message/template/${id}`);

const getRecentMessages = (limit = 10) =>
  requestClient.get<SystemMessageApi.UserMessage[]>(
    '/system/message/inbox/recent',
    {
      params: { limit },
      errorMessageType: false,
    },
  );
const getInboxMessagePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemMessageApi.UserMessage>>(
    '/system/message/inbox/page',
    {
      params,
    },
  );
const getInboxMessageDetail = (id: number | string) =>
  requestClient.get<SystemMessageApi.UserMessageDetail>(
    `/system/message/inbox/${id}`,
  );
const markMessageRead = (id: number | string) =>
  requestClient.put(`/system/message/inbox/${id}/read`);
const markAllMessagesRead = () =>
  requestClient.put('/system/message/inbox/read-all');
const deleteInboxMessage = (id: number | string) =>
  requestClient.delete(`/system/message/inbox/${id}`);

export {
  createMessage,
  createMessageTemplate,
  deleteInboxMessage,
  deleteMessage,
  deleteMessageTemplate,
  getInboxMessageDetail,
  getInboxMessagePage,
  getMessageDetail,
  getMessagePage,
  getMessageTemplateDetail,
  getMessageTemplatePage,
  getRecentMessages,
  markAllMessagesRead,
  markMessageRead,
  pushMessage,
  revokeMessage,
  updateMessage,
  updateMessageTemplate,
  uploadMessageImage,
};
