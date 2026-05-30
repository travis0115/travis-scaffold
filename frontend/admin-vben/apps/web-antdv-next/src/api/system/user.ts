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
    phone?: string;
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
  return requestClient.get<{ items: SystemUserApi.SysUser[]; total: number }>(
    '/api/system/user/page',
    { params },
  );
}

/**
 * 获取用户详情
 */
async function getUserDetail(id: number) {
  return requestClient.get<SystemUserApi.SysUser>(`/api/system/user/${id}`);
}

/**
 * 新增用户
 */
async function createUser(data: Partial<SystemUserApi.SysUser>) {
  return requestClient.post('/api/system/user', data);
}

/**
 * 更新用户
 */
async function updateUser(id: number, data: Partial<SystemUserApi.SysUser>) {
  return requestClient.put(`/api/system/user/${id}`, data);
}

/**
 * 删除用户
 */
async function deleteUser(id: number) {
  return requestClient.delete(`/api/system/user/${id}`);
}

/**
 * 为用户分配角色
 */
async function assignUserRoles(data: { userId: number; roleIds: number[] }) {
  return requestClient.post('/api/system/user/roles', data);
}

export {
  assignUserRoles,
  createUser,
  deleteUser,
  getUserDetail,
  getUserPage,
  updateUser,
};
