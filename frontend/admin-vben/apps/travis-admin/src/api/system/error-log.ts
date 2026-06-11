import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace SystemErrorLogApi {
  export interface ErrorLog {
    [key: string]: any;
    controllerMethod: string;
    createTime: string;
    exceptionClass: string;
    id: number;
    ip: string;
    message?: string;
    requestMethod: string;
    requestUrl: string;
    stackTrace: string;
  }
}

const getErrorLogPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<SystemErrorLogApi.ErrorLog>>('/system/error-log/page', { params });

export { getErrorLogPage };
