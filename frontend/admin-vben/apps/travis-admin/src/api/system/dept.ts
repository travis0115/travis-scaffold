import type { Id } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemDeptApi {
  export interface SysDept {
    [key: string]: any;
    id: Id;
    parentId: Id;
    deptName: string;
    sort?: number;
    leader?: string;
    mobile?: string;
    status: 0 | 1;
    createTime?: string;
    children?: SysDept[];
  }
}

/**
 * 获取部门树形列表
 */
async function getDeptTree() {
  return requestClient.get<SystemDeptApi.SysDept[]>('/system/dept/list');
}

/**
 * 获取启用部门树形列表
 */
async function getEnabledDeptTree() {
  return requestClient.get<SystemDeptApi.SysDept[]>(
    '/system/dept/list-enabled',
  );
}

/**
 * 获取部门详情
 */
async function getDeptDetail(id: Id) {
  return requestClient.get<SystemDeptApi.SysDept>(`/system/dept/${id}`);
}

/**
 * 新增部门
 */
async function createDept(data: Partial<SystemDeptApi.SysDept>) {
  return requestClient.post('/system/dept', data);
}

/**
 * 更新部门
 */
async function updateDept(id: Id, data: Partial<SystemDeptApi.SysDept>) {
  return requestClient.put(`/system/dept/${id}`, data);
}

async function updateDeptStatus(id: Id, status: 0 | 1) {
  return requestClient.put(`/system/dept/${id}/status`, undefined, {
    params: { status },
  });
}

/**
 * 删除部门
 */
async function deleteDept(id: Id) {
  return requestClient.delete(`/system/dept/${id}`);
}

export {
  createDept,
  deleteDept,
  getDeptDetail,
  getDeptTree,
  getEnabledDeptTree,
  updateDept,
  updateDeptStatus,
};
