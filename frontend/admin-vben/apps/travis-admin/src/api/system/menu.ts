import { requestClient } from '#/api/request';

export namespace SystemMenuApi {
  /** 菜单类型选项 */
  export const MenuTypeOptions = [
    { color: 'processing', label: '目录', value: 0 },
    { color: 'default', label: '菜单', value: 1 },
    { color: 'error', label: '按钮', value: 2 },
    { color: 'success', label: '内嵌', value: 3 },
    { color: 'warning', label: '外链', value: 4 },
  ] as const;

  export interface SysMenu {
    [key: string]: any;
    id: number;
    parentId: number;
    menuName: string;
    path?: string;
    component?: string;
    perms?: string;
    menuType: 0 | 1 | 2 | 3 | 4;
    icon?: string;
    sort?: number;
    status: 0 | 1;
    meta?: string;
    createTime?: string;
    children?: SysMenu[];
  }
}

/**
 * 获取菜单树形列表
 */
async function getMenuTree() {
  return requestClient.get<SystemMenuApi.SysMenu[]>(
    '/system/menu/list',
  );
}

/**
 * 获取菜单详情
 */
async function getMenuDetail(id: number) {
  return requestClient.get<SystemMenuApi.SysMenu>(`/system/menu/${id}`);
}

/**
 * 新增菜单
 */
async function createMenu(data: Partial<SystemMenuApi.SysMenu>) {
  return requestClient.post('/system/menu', data);
}

/**
 * 更新菜单
 */
async function updateMenu(id: number, data: Partial<SystemMenuApi.SysMenu>) {
  return requestClient.put(`/system/menu/${id}`, data);
}

/**
 * 删除菜单
 */
async function deleteMenu(id: number) {
  return requestClient.delete(`/system/menu/${id}`);
}

/**
 * 上移菜单
 */
async function moveUpMenu(id: number) {
  return requestClient.put(`/system/menu/${id}/move-up`);
}

/**
 * 下移菜单
 */
async function moveDownMenu(id: number) {
  return requestClient.put(`/system/menu/${id}/move-down`);
}

export {
  createMenu,
  deleteMenu,
  getMenuDetail,
  getMenuTree,
  moveDownMenu,
  moveUpMenu,
  updateMenu,
};
