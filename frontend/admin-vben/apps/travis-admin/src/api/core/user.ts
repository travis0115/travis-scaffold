import type { RequestClientConfig } from '@vben/request';
import type { UserInfo } from '@vben/types';

import { requestClient } from '#/api/request';

/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/api/admin/system/auth/user-info');
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
  return requestClient.put('/api/admin/system/user/profile', data, config);
}

/**
 * 更新头像
 */
export async function updateAvatarApi(
  data: {
    avatar: string;
  },
  config?: RequestClientConfig,
) {
  return requestClient.put('/api/admin/system/user/avatar', data, config);
}

/**
 * 修改密码
 */
export async function changePasswordApi(data: {
  newPassword: string;
  oldPassword: string;
}) {
  return requestClient.put('/api/admin/system/user/change-password', data);
}
