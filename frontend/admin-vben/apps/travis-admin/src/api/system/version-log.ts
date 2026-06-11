import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemVersionLogApi {
  export interface VersionLog {
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
 * 分页查询版本日志列表
 */
async function getVersionLogPage(params: {
  pageNum: number;
  pageSize: number;
  status?: number;
  title?: string;
  version?: string;
}) {
  return requestClient.get<PageResp<SystemVersionLogApi.VersionLog>>(
    '/system/version-log/page',
    { params },
  );
}

/**
 * 获取版本日志详情
 */
async function getVersionLogDetail(id: number) {
  return requestClient.get<SystemVersionLogApi.VersionLog>(`/system/version-log/${id}`);
}

/**
 * 新增版本日志
 */
async function createVersionLog(data: Partial<SystemVersionLogApi.VersionLog>) {
  return requestClient.post('/system/version-log', data);
}

/**
 * 更新版本日志
 */
async function updateVersionLog(id: number, data: Partial<SystemVersionLogApi.VersionLog>) {
  return requestClient.put(`/system/version-log/${id}`, data);
}

/**
 * 删除版本日志
 */
async function deleteVersionLog(id: number) {
  return requestClient.delete(`/system/version-log/${id}`);
}

/**
 * 获取已发布的版本日志列表
 */
async function getPublishedVersionLogs(limit: number = 10) {
  return requestClient.get<SystemVersionLogApi.VersionLog[]>('/system/version-log/published', {
    params: { limit },
  });
}

export {
  createVersionLog,
  deleteVersionLog,
  getPublishedVersionLogs,
  getVersionLogDetail,
  getVersionLogPage,
  updateVersionLog,
};
