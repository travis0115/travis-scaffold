import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace OpsErrorLogApi {
  export type HandleStatus = 0 | 1 | 2;

  export interface ErrorLog {
    [key: string]: any;
    controllerMethod: string;
    createTime: string;
    businessKey?: string;
    applicationName?: string;
    applicationVersion?: string;
    fingerprint?: string;
    firstOccurrenceTime: string;
    handleRemark?: string;
    handledBy?: number;
    handledByUsername?: string;
    handledTime?: string;
    exceptionClass: string;
    id: number;
    instanceName?: string;
    ip: string;
    lastOccurrenceTime: string;
    message?: string;
    moduleName?: string;
    occurrences?: Occurrence[];
    occurrenceCount: number;
    platformType: 'ADMIN' | 'APP' | 'SYSTEM';
    requestId?: string;
    requestMethod: string;
    requestParams?: string;
    requestUrl: string;
    sourceName?: string;
    sourceType: string;
    stackTrace: string;
    status: HandleStatus;
    traceId?: string;
    userId?: number;
    username?: string;
  }

  export interface Occurrence {
    applicationName?: string;
    applicationVersion?: string;
    controllerMethod?: string;
    id: number;
    instanceName?: string;
    ip?: string;
    message?: string;
    occurredTime: string;
    requestId?: string;
    requestMethod?: string;
    requestParams?: string;
    requestUrl?: string;
    stackTrace?: string;
    traceId?: string;
    userId?: number;
    username?: string;
  }
}

const getErrorLogPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<OpsErrorLogApi.ErrorLog>>('/ops/error-log/page', {
    params,
  });

const getErrorLogDetail = (id: number | string) =>
  requestClient.get<OpsErrorLogApi.ErrorLog>(`/ops/error-log/${id}`);

const handleErrorLog = (id: number, data: { remark?: string; status: 1 | 2 }) =>
  requestClient.put(`/ops/error-log/${id}/handle`, data);

const handleAllPendingErrorLogs = (data: { remark?: string; status: 1 | 2 }) =>
  requestClient.put<number>('/ops/error-log/handle-all', data);

const deleteErrorLog = (id: number) =>
  requestClient.delete(`/ops/error-log/${id}`);

export {
  deleteErrorLog,
  getErrorLogDetail,
  getErrorLogPage,
  handleAllPendingErrorLogs,
  handleErrorLog,
};
