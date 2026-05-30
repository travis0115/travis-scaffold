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
 * 分页查询字典类型列表
 */
async function getDictPage(params: {
  dictName?: string;
  dictType?: string;
  pageNum: number;
  pageSize: number;
  status?: number;
}) {
  return requestClient.get<SystemDictApi.SysDict>('/api/system/dict/page', {
    params,
  });
}

/**
 * 获取字典类型详情
 */
async function getDictDetail(id: number) {
  return requestClient.get<SystemDictApi.SysDict>(`/api/system/dict/${id}`);
}

/**
 * 新增字典类型
 */
async function createDict(data: Partial<SystemDictApi.SysDict>) {
  return requestClient.post('/api/system/dict', data);
}

/**
 * 更新字典类型
 */
async function updateDict(id: number, data: Partial<SystemDictApi.SysDict>) {
  return requestClient.put(`/api/system/dict/${id}`, data);
}

/**
 * 删除字典类型
 */
async function deleteDict(id: number) {
  return requestClient.delete(`/api/system/dict/${id}`);
}

/**
 * 查询指定字典类型下的所有数据项
 */
async function getDictItems(dictId: number) {
  return requestClient.get<SystemDictApi.SysDictItem[]>(
    `/api/system/dict/items/${dictId}`,
  );
}

/**
 * 新增字典数据项
 */
async function createDictItem(data: Partial<SystemDictApi.SysDictItem>) {
  return requestClient.post('/api/system/dict/item', data);
}

/**
 * 更新字典数据项
 */
async function updateDictItem(
  id: number,
  data: Partial<SystemDictApi.SysDictItem>,
) {
  return requestClient.put(`/api/system/dict/item/${id}`, data);
}

/**
 * 删除字典数据项
 */
async function deleteDictItem(id: number) {
  return requestClient.delete(`/api/system/dict/item/${id}`);
}

export {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  getDictDetail,
  getDictItems,
  getDictPage,
  updateDict,
  updateDictItem,
};
