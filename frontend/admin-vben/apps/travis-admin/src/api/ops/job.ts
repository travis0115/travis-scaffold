import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace OpsJobApi {
  export type ScheduleType = 'CRON' | 'INTERVAL' | 'ONCE';

  export interface Job {
    [key: string]: any;
    alertUserIds?: number[];
    concurrent: 0 | 1;
    createBy?: number;
    createByUsername?: string;
    cronExpression?: string;
    executeAt?: string;
    handlerName: string;
    handlerAvailable?: boolean;
    id: number;
    intervalMillis?: number;
    isBuiltin: 0 | 1;
    jobName: string;
    lastExecutionStatus?: 0 | 1 | 2;
    lastExecutionTime?: string;
    lockVersion?: number;
    misfirePolicy: number;
    nextFireTime?: string;
    params?: string;
    remark?: string;
    scheduleType: ScheduleType;
    status: 0 | 1;
  }

  export interface JobLogPage {
    [key: string]: any;
    durationMillis?: number;
    endTime?: string;
    handlerName: string;
    id: number;
    jobId: number;
    jobName: string;
    schedulerInstanceId?: string;
    startTime?: string;
    status: number;
  }

  export interface JobLogDetail extends JobLogPage {
    createTime: string;
    exceptionClass?: string;
    exceptionMessage?: string;
    fireInstanceId?: string;
    paramsSnapshot?: string;
    resultMessage?: string;
    scheduledFireTime?: string;
    stackTrace?: string;
  }

  export interface Handler {
    description: string;
    name: string;
  }

  export interface PreviewReq {
    cronExpression?: string;
    executeAt?: string;
    intervalMillis?: number;
    scheduleType: ScheduleType;
  }

  export interface UserOption {
    deptName?: string;
    id: number;
    nickname: string;
    username: string;
  }

  export interface Stats {
    averageDurationMillis: number | string;
    consecutiveFailures: number | string;
    failed: number | string;
    maxDurationMillis: number | string;
    p95DurationMillis: number | string;
    success: number | string;
    successRate: number;
    total: number | string;
    trend: Array<{
      date: string;
      failed: number | string;
      success: number | string;
    }>;
  }

  export interface Dashboard {
    enabledJobs: number;
    executions: number;
    failedExecutions: number;
    pausedJobs: number;
    successExecutions: number;
    successRate: number;
    totalJobs: number;
  }
}

const getJobPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<OpsJobApi.Job>>('/ops/job/page', { params });
const getJobDetail = (id: number) =>
  requestClient.get<OpsJobApi.Job>(`/ops/job/${id}`);
const createJob = (data: Partial<OpsJobApi.Job>) =>
  requestClient.post('/ops/job', data);
const updateJob = (id: number, data: Partial<OpsJobApi.Job>) =>
  requestClient.put(`/ops/job/${id}`, data);
const deleteJob = (id: number) => requestClient.delete(`/ops/job/${id}`);
const changeJobStatus = (id: number, status: number) =>
  requestClient.put(`/ops/job/${id}/status`, undefined, { params: { status } });
const runJob = (id: number, params?: string) =>
  requestClient.post(`/ops/job/${id}/run`, { params });
const copyJob = (id: number) => requestClient.post(`/ops/job/${id}/copy`);
const previewJob = (data: OpsJobApi.PreviewReq, count = 5) =>
  requestClient.post<string[]>('/ops/job/preview', data, {
    errorMessageType: false,
    params: { count },
  });
const getJobHandlers = (includeBuiltin = false) =>
  requestClient.get<OpsJobApi.Handler[]>('/ops/job/handlers', {
    params: { includeBuiltin },
  });
const getJobUserOptions = (params?: { keyword?: string; userIds?: string }) =>
  requestClient.get<OpsJobApi.UserOption[]>('/ops/job/user-options', {
    params,
  });
const getJobStats = (id: number) =>
  requestClient.get<OpsJobApi.Stats>(`/ops/job/${id}/stats`);
const getJobDashboard = () =>
  requestClient.get<OpsJobApi.Dashboard>('/ops/job/dashboard');
const getJobLogPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<OpsJobApi.JobLogPage>>('/ops/job-log/page', {
    params,
  });
const getJobLogDetail = (id: number) =>
  requestClient.get<OpsJobApi.JobLogDetail>(`/ops/job-log/${id}`);
const cleanJobLogs = (jobId?: number) =>
  requestClient.delete('/ops/job-log/clean', { params: { jobId } });

export {
  changeJobStatus,
  cleanJobLogs,
  copyJob,
  createJob,
  deleteJob,
  getJobDashboard,
  getJobDetail,
  getJobHandlers,
  getJobLogDetail,
  getJobLogPage,
  getJobPage,
  getJobStats,
  getJobUserOptions,
  previewJob,
  runJob,
  updateJob,
};
