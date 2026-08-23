import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemLoginLogApi {
  export interface Dashboard {
    todayLoginUsers: number;
  }

  export interface LoginLog {
    browser?: string;
    id: string;
    ip?: string;
    location?: string;
    loginTime?: string;
    message?: string;
    os?: string;
    status: 0 | 1;
    username?: string;
  }
}

/**
 * 获取登录日志分页列表
 */
async function getLoginLogList(params: Recordable<any>) {
  return requestClient.get<PageResp<SystemLoginLogApi.LoginLog>>(
    '/system/login-log/page',
    { params },
  );
}

async function getLoginDashboard() {
  return requestClient.get<SystemLoginLogApi.Dashboard>(
    '/system/login-log/dashboard',
  );
}

export { getLoginDashboard, getLoginLogList };
