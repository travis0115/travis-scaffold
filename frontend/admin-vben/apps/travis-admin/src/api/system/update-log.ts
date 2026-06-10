import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemUpdateLogApi {
  export interface UpdateLog {
    [key: string]: any;
    id: number;
    version: string;
    title: string;
    content: string;
    publishTime?: string;
    status: 0 | 1;
    createTime?: string;
    createBy?: number;
  }
}

/**
 * 分页查询更新日志列表
 */
async function getUpdateLogPage(params: {
  version?: string;
  title?: string;
  pageNum: number;
  pageSize: number;
  status?: number;
}) {
  return requestClient.get<PageResp<SystemUpdateLogApi.UpdateLog>>(
    '/system/update-log/page',
    { params },
  );
}

/**
 * 获取更新日志详情
 */
async function getUpdateLogDetail(id: number) {
  return requestClient.get<SystemUpdateLogApi.UpdateLog>(`/system/update-log/${id}`);
}

/**
 * 新增更新日志
 */
async function createUpdateLog(data: Partial<SystemUpdateLogApi.UpdateLog>) {
  return requestClient.post('/system/update-log', data);
}

/**
 * 更新更新日志
 */
async function updateUpdateLog(id: number, data: Partial<SystemUpdateLogApi.UpdateLog>) {
  return requestClient.put(`/system/update-log/${id}`, data);
}

/**
 * 删除更新日志
 */
async function deleteUpdateLog(id: number) {
  return requestClient.delete(`/system/update-log/${id}`);
}

/**
 * 获取已发布的更新日志列表
 */
async function getPublishedUpdateLogs(limit: number = 10) {
  return requestClient.get<SystemUpdateLogApi.UpdateLog[]>('/system/update-log/published', {
    params: { limit },
  });
}

export {
  createUpdateLog,
  deleteUpdateLog,
  getPublishedUpdateLogs,
  getUpdateLogDetail,
  getUpdateLogPage,
  updateUpdateLog,
};
