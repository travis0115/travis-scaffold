import type { Recordable } from '@vben/types';

import { requestClient } from '#/api/request';

export namespace SystemUpdateLogApi {
  export interface UpdateLog {
    [key: string]: any;
    content?: string;
    createTime?: string;
    id: string;
    publishTime?: string;
    status: 0 | 1;
    title?: string;
    version?: string;
  }
}

/**
 * 获取更新日志分页列表
 */
async function getUpdateLogList(params: Recordable<any>) {
  return requestClient.get('/system/update-log/page', { params });
}

/**
 * 获取更新日志详情
 */
async function getUpdateLogDetail(id: string) {
  return requestClient.get<SystemUpdateLogApi.UpdateLog>(
    `/system/update-log/${id}`,
  );
}

/**
 * 创建更新日志
 */
async function createUpdateLog(
  data: Omit<SystemUpdateLogApi.UpdateLog, 'id'>,
) {
  return requestClient.post('/system/update-log', data);
}

/**
 * 更新更新日志
 */
async function updateUpdateLog(
  id: string,
  data: Omit<SystemUpdateLogApi.UpdateLog, 'id'>,
) {
  return requestClient.put(`/system/update-log/${id}`, data);
}

/**
 * 删除更新日志
 */
async function deleteUpdateLog(id: string) {
  return requestClient.delete(`/system/update-log/${id}`);
}

/**
 * 获取已发布的更新日志列表（用户查看）
 */
async function getPublishedUpdateLogs(limit?: number) {
  return requestClient.get<SystemUpdateLogApi.UpdateLog[]>(
    '/system/update-log/published',
    { params: { limit: limit ?? 20 } },
  );
}

export {
  createUpdateLog,
  deleteUpdateLog,
  getPublishedUpdateLogs,
  getUpdateLogDetail,
  getUpdateLogList,
  updateUpdateLog,
};
