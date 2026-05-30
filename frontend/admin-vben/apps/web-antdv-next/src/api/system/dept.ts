import { requestClient } from '#/api/request';

export namespace SystemDeptApi {
  export interface SysDept {
    [key: string]: any;
    id: number;
    parentId: number;
    deptName: string;
    sort?: number;
    leader?: string;
    phone?: string;
    status: 0 | 1;
    createTime?: string;
    children?: SysDept[];
  }
}

/**
 * 获取部门树形列表
 */
async function getDeptTree() {
  return requestClient.get<SystemDeptApi.SysDept[]>(
    '/api/system/dept/list',
  );
}

/**
 * 获取部门详情
 */
async function getDeptDetail(id: number) {
  return requestClient.get<SystemDeptApi.SysDept>(`/api/system/dept/${id}`);
}

/**
 * 新增部门
 */
async function createDept(data: Partial<SystemDeptApi.SysDept>) {
  return requestClient.post('/api/system/dept', data);
}

/**
 * 更新部门
 */
async function updateDept(id: number, data: Partial<SystemDeptApi.SysDept>) {
  return requestClient.put(`/api/system/dept/${id}`, data);
}

/**
 * 删除部门
 */
async function deleteDept(id: number) {
  return requestClient.delete(`/api/system/dept/${id}`);
}

export { createDept, deleteDept, getDeptDetail, getDeptTree, updateDept };
