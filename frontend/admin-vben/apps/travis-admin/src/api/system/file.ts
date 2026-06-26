import type { AxiosProgressEvent } from '@vben/request';

import { requestClient } from '#/api/request';

/** 文件上传响应 */
export interface FileUploadResult {
  /** 文件元数据ID */
  id: number;
  /** 相对路径（用于数据库存储） */
  path: string;
  /** 当前访问URL（用于前端展示） */
  url: string;
}

export const FILE_FOLDER_IDS = {
  MANUAL_UPLOAD: 2,
  AVATAR: 3,
  NOTICE: 4,
  VERSION: 5,
} as const;

/**
 * 上传文件
 * @param file 文件对象
 * @returns 文件上传结果（含文件ID、path和当前url）
 */
export function uploadFileApi(
  file: File,
  folderId?: number | string,
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
