import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemRoleApi {
  export interface SysRole {
    [key: string]: any;
    id: number;
    roleName: string;
    roleCode: string;
    remark?: string;
    modifiable: 0 | 1;
    status: 0 | 1;
    menuIds?: number[];
    createTime?: string;
  }
}

/**
 * 分页查询角色列表
 */
async function getRolePage(params: Recordable<any>) {
  return requestClient.get<PageResp<SystemRoleApi.SysRole>>(
    '/system/role/page',
    { params },
  );
}

/**
 * 获取角色详情
 */
async function getRoleDetail(id: number) {
  return requestClient.get<SystemRoleApi.SysRole>(`/system/role/${id}`);
}

/**
 * 新增角色
 */
async function createRole(data: Partial<SystemRoleApi.SysRole>) {
  return requestClient.post('/system/role', data);
}

/**
 * 更新角色
 */
async function updateRole(id: number, data: Partial<SystemRoleApi.SysRole>) {
  return requestClient.put(`/system/role/${id}`, data);
}

/**
 * 删除角色
 */
async function deleteRole(id: number) {
  return requestClient.delete(`/system/role/${id}`);
}

/**
 * 为角色分配菜单权限
 */
async function assignRoleMenus(data: { menuIds: number[]; roleId: number }) {
  return requestClient.post('/system/role/menus', data);
}

/**
 * 获取所有启用角色列表（不分页）
 */
async function getRoleList() {
  return requestClient.get<SystemRoleApi.SysRole[]>('/system/role/list');
}

export {
  assignRoleMenus,
  createRole,
  deleteRole,
  getRoleDetail,
  getRoleList,
  getRolePage,
  updateRole,
};
