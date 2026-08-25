import type { AxiosProgressEvent, RequestClientConfig } from '@vben/request';
import type { UserInfo } from '@vben/types';

import type { FileUploadResult } from '../system/file';

import { requestClient } from '#/api/request';

/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/system/auth/user-info');
}

/**
 * 修改个人资料
 */
export async function updateProfileApi(
  data: {
    email?: string;
    mobile?: string;
    nickname?: string;
  },
  config?: RequestClientConfig,
) {
  return requestClient.put('/system/user/profile', data, config);
}

/**
 * 更新头像
 */
export async function updateAvatarApi(
  avatarFileId: number | string,
  config?: RequestClientConfig,
) {
  return requestClient.put('/system/user/avatar', undefined, {
    ...config,
    params: { ...config?.params, avatarFileId },
  });
}

export function uploadAvatarApi(
  file: File,
  onUploadProgress?: (progressEvent: AxiosProgressEvent) => void,
) {
  const formData = new FormData();
  formData.append('file', file);
  return requestClient.post<FileUploadResult>(
    '/system/user/avatar/upload',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    },
  );
}

/**
 * 修改密码
 */
export async function changePasswordApi(data: {
  newPassword: string;
  oldPassword: string;
}) {
  return requestClient.put('/system/user/change-password', data);
}
