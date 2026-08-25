import type { AxiosProgressEvent } from '@vben/request';

import type { Id } from '#/api/types';

import { requestClient } from '#/api/request';

/** 文件上传响应 */
export interface FileUploadResult {
  /** 文件元数据ID */
  id: Id;
  /** 相对路径（用于数据库存储） */
  path: string;
  /** 当前访问URL（用于前端展示） */
  url: string;
}

export const UPLOAD_FILE_MAX_SIZE_MB =
  Number(import.meta.env.VITE_UPLOAD_FILE_MAX_SIZE) || 20;

export const UPLOAD_FILE_MAX_SIZE_BYTES = UPLOAD_FILE_MAX_SIZE_MB * 1024 * 1024;

function formatUploadFileMaxSize(size: number) {
  if (size >= 1) return String(Math.floor(size));
  if (size >= 0.01) return size.toFixed(2);
  return '0.01';
}

export const UPLOAD_FILE_MAX_SIZE_TEXT = formatUploadFileMaxSize(
  UPLOAD_FILE_MAX_SIZE_MB,
);

/**
 * 上传文件
 * @param file 文件对象
 * @returns 文件上传结果（含文件ID、path和当前url）
 */
export function uploadFileApi(
  file: File,
  folderId?: Id,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
) {
  const formData = new FormData();
  formData.append('file', file);
  if (folderId) formData.append('folderId', String(folderId));
  return requestClient.post<FileUploadResult>('/system/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress,
  });
}
