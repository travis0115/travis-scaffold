import { requestClient } from '#/api/request';

export namespace SystemMenuApi {
  /** 菜单类型选项 */
  export const MenuTypeOptions = [
    { color: 'processing', label: '目录', value: 0 },
    { color: 'default', label: '菜单', value: 1 },
    { color: 'error', label: '按钮', value: 2 },
  ] as const;

  export interface SysMenu {
    [key: string]: any;
    id: number;
    parentId: number;
    name: string;
    path?: string;
    component?: string;
    perms?: string;
    menuType: 0 | 1 | 2;
    icon?: string;
    sort?: number;
    status: 0 | 1;
    createTime?: string;
    children?: SysMenu[];
  }
}

/**
 * 获取菜单树形列表
 */
async function getMenuTree() {
  return requestClient.get<SystemMenuApi.SysMenu[]>(
    '/api/system/menu/list',
  );
}

/**
 * 获取菜单详情
 */
async function getMenuDetail(id: number) {
  return requestClient.get<SystemMenuApi.SysMenu>(`/api/system/menu/${id}`);
}

/**
 * 新增菜单
 */
async function createMenu(data: Partial<SystemMenuApi.SysMenu>) {
  return requestClient.post('/api/system/menu', data);
}

/**
 * 更新菜单
 */
async function updateMenu(id: number, data: Partial<SystemMenuApi.SysMenu>) {
  return requestClient.put(`/api/system/menu/${id}`, data);
}

/**
 * 删除菜单
 */
async function deleteMenu(id: number) {
  return requestClient.delete(`/api/system/menu/${id}`);
}

export { createMenu, deleteMenu, getMenuDetail, getMenuTree, updateMenu };
