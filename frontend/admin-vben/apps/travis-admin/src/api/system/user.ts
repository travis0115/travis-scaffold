import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemUserApi {
  export interface SysUser {
    [key: string]: any;
    id: number;
    username: string;
    nickname: string;
    avatar?: string;
    email?: string;
    mobile?: string;
    deptId?: number;
    deptName?: string;
    status: 0 | 1;
    roleIds?: number[];
    roleNames?: string[];
    createTime?: string;
  }
}

/**
 * 分页查询用户列表
 */
async function getUserPage(params: Recordable<any>) {
  return requestClient.get<{ records: SystemUserApi.SysUser[]; total: number }>(
    '/api/admin/system/user/page',
    { params },
  );
}

/**
 * 获取用户详情
 */
async function getUserDetail(id: number) {
  return requestClient.get<SystemUserApi.SysUser>(`/api/admin/system/user/${id}`);
}

/**
 * 新增用户
 */
async function createUser(data: Partial<SystemUserApi.SysUser>) {
  return requestClient.post('/api/admin/system/user', data);
}

/**
 * 更新用户
 */
async function updateUser(id: number, data: Partial<SystemUserApi.SysUser>) {
  return requestClient.put(`/api/admin/system/user/${id}`, data);
}

/**
 * 删除用户
 */
async function deleteUser(id: number) {
  return requestClient.delete(`/api/admin/system/user/${id}`);
}

/**
 * 为用户分配角色
 */
async function assignUserRoles(data: { roleIds: number[]; userId: number }) {
  return requestClient.post('/api/admin/system/user/roles', data);
}

/**
 * 重置用户密码（可选指定新密码，不指定则自动生成随机密码）
 * @returns 最终使用的密码（明文）
 */
async function resetUserPassword(id: number, newPassword?: string) {
  return requestClient.put<string>(
    `/api/admin/system/user/${id}/reset-password`,
    {
      newPassword: newPassword || undefined,
    },
  );
}

export {
  assignUserRoles,
  createUser,
  deleteUser,
  getUserDetail,
  getUserPage,
  resetUserPassword,
  updateUser,
};
