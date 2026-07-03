import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemOperationLogApi {
  export interface OperationLog {
    createTime?: string;
    description?: string;
    businessType?: string;
    duration?: number;
    errorMsg?: string;
    id: string;
    ip?: string;
    location?: string;
    method?: string;
    module?: string;
    browser?: string;
    os?: string;
    requestId?: string;
    requestMethod?: string;
    requestParams?: string;
    requestUrl?: string;
    responseResult?: string;
    status: 0 | 1;
    userAgent?: string;
    userId?: string;
    username?: string;
  }
}

async function getOperationLogPage(params: Recordable<any>) {
  return requestClient.get<PageResp<SystemOperationLogApi.OperationLog>>(
    '/system/operation-log/page',
    { params },
  );
}

export { getOperationLogPage };
