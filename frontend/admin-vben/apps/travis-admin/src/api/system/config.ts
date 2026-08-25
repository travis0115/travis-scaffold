import type { Recordable } from '@vben/types';

import type { Id, PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemConfigApi {
  export interface SystemConfig {
    [key: string]: any;
    configKey: string;
    configValue?: string;
    createTime?: string;
    id: Id;
    isBuiltin: 0 | 1;
    lockVersion?: number;
    remark?: string;
    updateTime?: string;
  }
}

/**
 * 获取系统配置分页列表
 */
async function getConfigList(params: Recordable<any>) {
  return requestClient.get<PageResp<SystemConfigApi.SystemConfig>>(
    '/system/config/page',
    { params },
  );
}

/**
 * 创建配置
 */
async function createConfig(data: Omit<SystemConfigApi.SystemConfig, 'id'>) {
  return requestClient.post('/system/config', data);
}

/**
 * 更新配置
 */
async function updateConfig(
  id: Id,
  data: Omit<SystemConfigApi.SystemConfig, 'configKey' | 'id'>,
) {
  return requestClient.put(`/system/config/${id}`, data);
}

/**
 * 删除配置
 */
async function deleteConfig(id: Id) {
  return requestClient.delete(`/system/config/${id}`);
}

export { createConfig, deleteConfig, getConfigList, updateConfig };
