import type { Recordable } from '@vben/types';
import type { AxiosProgressEvent } from '@vben/request';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

import type { FileUploadResult } from './file';

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
const getPublishedNoticePage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemNoticeApi.Notice>>('/system/notice/published', {
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
const uploadNoticeImage = (
  file: File,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
) => {
  const formData = new FormData();
  formData.append('file', file);
  return requestClient.post<FileUploadResult>(
    '/system/notice/image/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    },
  );
};

export {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticePage,
  getPublishedNoticePage,
  updateNotice,
  updateNoticeStatus,
  uploadNoticeImage,
};
