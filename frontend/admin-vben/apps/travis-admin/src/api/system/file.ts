import { requestClient } from '#/api/request';

/** 文件上传响应 */
export interface FileUploadResult {
  /** 文件元数据ID */
  id: number;
  /** 相对路径（用于数据库存储） */
  path: string;
  /** 完整访问URL（用于前端展示） */
  url: string;
}

/**
 * 上传文件
 * @param file 文件对象
 * @returns 文件上传结果（含path和url）
 */
export function uploadFileApi(file: File, folderId?: number) {
  const formData = new FormData();
  formData.append('file', file);
  if (folderId) formData.append('folderId', String(folderId));
  return requestClient.post<FileUploadResult>(
    '/system/file/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  );
}
