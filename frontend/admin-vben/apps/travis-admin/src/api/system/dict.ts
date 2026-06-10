import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemDictApi {
  export interface SysDict {
    [key: string]: any;
    id: number;
    dictName: string;
    dictType: string;
    status: 0 | 1;
    remark?: string;
    createTime?: string;
  }

  export interface SysDictItem {
    [key: string]: any;
    id: number;
    dictId: number;
    label: string;
    value: string;
    sort?: number;
    status: 0 | 1;
    remark?: string;
  }
}

/**
 * 获取字典树形数据（字典类型 + 子数据项）
 */
async function getDictTree() {
  return requestClient.get<SystemDictApi.SysDict[]>('/system/dict/tree');
}

/**
 * 分页查询字典类型列表
 */
async function getDictPage(params: {
  dictName?: string;
  dictType?: string;
  pageNum: number;
  pageSize: number;
  status?: number;
}) {
  return requestClient.get<PageResp<SystemDictApi.SysDict>>(
    '/system/dict/page',
    { params },
  );
}

/**
 * 获取字典类型详情
 */
async function getDictDetail(id: number) {
  return requestClient.get<SystemDictApi.SysDict>(`/system/dict/${id}`);
}

/**
 * 新增字典类型
 */
async function createDict(data: Partial<SystemDictApi.SysDict>) {
  return requestClient.post('/system/dict', data);
}

/**
 * 更新字典类型
 */
async function updateDict(id: number, data: Partial<SystemDictApi.SysDict>) {
  return requestClient.put(`/system/dict/${id}`, data);
}

/**
 * 删除字典类型
 */
async function deleteDict(id: number) {
  return requestClient.delete(`/system/dict/${id}`);
}

/**
 * 查询指定字典类型下的所有数据项
 */
async function getDictItems(dictId: number) {
  return requestClient.get<SystemDictApi.SysDictItem[]>(
    `/system/dict/items/${dictId}`
  );
}

/**
 * 新增字典数据项
 */
async function createDictItem(data: Partial<SystemDictApi.SysDictItem>) {
  return requestClient.post('/system/dict/item', data);
}

/**
 * 更新字典数据项
 */
async function updateDictItem(
  id: number,
  data: Partial<SystemDictApi.SysDictItem>,
) {
  return requestClient.put(`/system/dict/item/${id}`, data);
}

/**
 * 删除字典数据项
 */
async function deleteDictItem(id: number) {
  return requestClient.delete(`/system/dict/item/${id}`);
}

export {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  getDictDetail,
  getDictItems,
  getDictPage,
  getDictTree,
  updateDict,
  updateDictItem,
};
