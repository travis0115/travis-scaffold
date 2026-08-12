import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace OpsErrorLogApi {
  export interface ErrorLog {
    [key: string]: any;
    controllerMethod: string;
    createTime: string;
    businessKey?: string;
    exceptionClass: string;
    id: number;
    ip: string;
    message?: string;
    requestId?: string;
    requestMethod: string;
    requestParams?: string;
    requestUrl: string;
    sourceName?: string;
    sourceType: string;
    stackTrace: string;
    traceId?: string;
  }
}

const getErrorLogPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<OpsErrorLogApi.ErrorLog>>('/ops/error-log/page', {
    params,
  });

export { getErrorLogPage };
