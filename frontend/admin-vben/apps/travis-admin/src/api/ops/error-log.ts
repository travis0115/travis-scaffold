import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace OpsErrorLogApi {
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
  requestClient.get<PageResp<OpsErrorLogApi.ErrorLog>>('/ops/error-log/page', {
    params,
  });

export { getErrorLogPage };
