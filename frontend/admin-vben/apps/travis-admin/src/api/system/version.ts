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
    '/system/version/page',
    { params },
  );
}

/**
 * 获取版本日志详情
 */
async function getVersionLogDetail(id: number) {
  return requestClient.get<SystemVersionLogApi.VersionLog>(`/system/version/${id}`);
}

/**
 * 新增版本日志
 */
async function createVersionLog(data: Partial<SystemVersionLogApi.VersionLog>) {
  return requestClient.post('/system/version', data);
}

/**
 * 更新版本日志
 */
async function updateVersionLog(id: number, data: Partial<SystemVersionLogApi.VersionLog>) {
  return requestClient.put(`/system/version/${id}`, data);
}

async function updateVersionLogStatus(id: number, status: 0 | 1) {
  return requestClient.put(`/system/version/${id}/status`, undefined, {
    params: { status },
  });
}

/**
 * 删除版本日志
 */
async function deleteVersionLog(id: number) {
  return requestClient.delete(`/system/version/${id}`);
}

/**
 * 分页获取已发布的版本日志列表
 */
async function getPublishedVersionLogs(params: {
  pageNum: number;
  pageSize: number;
}) {
  return requestClient.get<PageResp<SystemVersionLogApi.VersionLog>>(
    '/system/version/published',
    { params },
  );
}

export {
  createVersionLog,
  deleteVersionLog,
  getPublishedVersionLogs,
  getVersionLogDetail,
  getVersionLogPage,
  updateVersionLog,
  updateVersionLogStatus,
};
