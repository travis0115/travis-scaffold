import { requestClient } from '#/api/request';

/**
 * 上传文件
 * @param file 文件对象
 * @returns 文件访问URL
 */
export function uploadFileApi(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return requestClient.post<string>('/api/admin/system/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
