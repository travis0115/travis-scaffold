import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemConfigApi {
  export interface SystemConfig {
    [key: string]: any;
    configGroup?: string;
    configKey: string;
    configValue?: string;
    createTime?: string;
    id: string;
    remark?: string;
    updateTime?: string;
  }
}

/**
 * 获取系统配置分页列表
 */
async function getConfigList(params: Recordable<any>) {
  return requestClient.get('/system/config/page', { params });
}

/**
 * 获取配置详情
 */
async function getConfigDetail(id: string) {
  return requestClient.get<SystemConfigApi.SystemConfig>(
    `/system/config/${id}`,
  );
}

/**
 * 创建配置
 */
async function createConfig(
  data: Omit<SystemConfigApi.SystemConfig, 'id'>,
) {
  return requestClient.post('/system/config', data);
}

/**
 * 更新配置
 */
async function updateConfig(
  id: string,
  data: Omit<SystemConfigApi.SystemConfig, 'id'>,
) {
  return requestClient.put(`/system/config/${id}`, data);
}

/**
 * 删除配置
 */
async function deleteConfig(id: string) {
  return requestClient.delete(`/system/config/${id}`);
}

export {
  createConfig,
  deleteConfig,
  getConfigDetail,
  getConfigList,
  updateConfig,
};
