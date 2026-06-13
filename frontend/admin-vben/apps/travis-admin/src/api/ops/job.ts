import type { Recordable } from '@vben/types';

import type { PageResp } from '#/api/types';

import { requestClient } from '#/api/request';

export namespace OpsJobApi {
  export type ScheduleType = 'CRON' | 'INTERVAL' | 'ONCE';

  export interface Job {
    [key: string]: any;
    alertUserIds?: number[];
    concurrent: 0 | 1;
    cronExpression?: string;
    dailyEndTime?: string;
    dailyStartTime?: string;
    excludedDates?: string[];
    excludedWeekdays?: number[];
    executeAt?: string;
    handlerName: string;
    handlerAvailable?: boolean;
    id: number;
    intervalMillis?: number;
    jobName: string;
    logRetentionDays: number;
    misfirePolicy: number;
    nextFireTime?: string;
    ownerUserId?: number;
    ownerUsername?: string;
    paramSchema?: string;
    params?: string;
    priority: number;
    remark?: string;
    scheduleType: ScheduleType;
    status: 0 | 1;
  }

  export interface JobLog {
    [key: string]: any;
    alertStatus: number;
    createTime: string;
    durationMillis?: number;
    endTime?: string;
    exceptionClass?: string;
    exceptionMessage?: string;
    fireInstanceId?: string;
    handlerName: string;
    id: number;
    jobId: number;
    jobName: string;
    paramsSnapshot?: string;
    scheduledFireTime?: string;
    schedulerInstanceId?: string;
    stackTrace?: string;
    startTime?: string;
    status: number;
  }

  export interface UserOption {
    deptName?: string;
    id: number;
    nickname: string;
    username: string;
  }

  export interface Stats {
    averageDurationMillis: number;
    consecutiveFailures: number;
    failed: number;
    maxDurationMillis: number;
    p95DurationMillis: number;
    success: number;
    successRate: number;
    total: number;
    trend: Array<{ date: string; failed: number; success: number }>;
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
const previewJob = (data: Partial<OpsJobApi.Job>, count = 5) =>
  requestClient.post<string[]>('/ops/job/preview', data, { params: { count } });
const getJobHandlers = () => requestClient.get<string[]>('/ops/job/handlers');
const getJobUserOptions = (params?: { keyword?: string; userIds?: string }) =>
  requestClient.get<OpsJobApi.UserOption[]>('/ops/job/user-options', {
    params,
  });
const exportJobs = () => requestClient.get<OpsJobApi.Job[]>('/ops/job/export');
const importJobs = (data: Partial<OpsJobApi.Job>[]) =>
  requestClient.post('/ops/job/import', data);
const getJobStats = (id: number) =>
  requestClient.get<OpsJobApi.Stats>(`/ops/job/${id}/stats`);
const getJobDashboard = () =>
  requestClient.get<OpsJobApi.Dashboard>('/ops/job/dashboard');
const getJobLogPage = (params: Recordable<any>) =>
  requestClient.get<PageResp<OpsJobApi.JobLog>>('/ops/job-log/page', {
    params,
  });
const getJobLogDetail = (id: number) =>
  requestClient.get<OpsJobApi.JobLog>(`/ops/job-log/${id}`);
const exportJobLogs = (params: Recordable<any>) =>
  requestClient.get<OpsJobApi.JobLog[]>('/ops/job-log/export', { params });
const cleanJobLogs = (jobId?: number) =>
  requestClient.delete('/ops/job-log/clean', { params: { jobId } });

export {
  changeJobStatus,
  cleanJobLogs,
  copyJob,
  createJob,
  deleteJob,
  exportJobLogs,
  exportJobs,
  getJobDashboard,
  getJobDetail,
  getJobHandlers,
  getJobLogDetail,
  getJobLogPage,
  getJobPage,
  getJobStats,
  getJobUserOptions,
  importJobs,
  previewJob,
  runJob,
  updateJob,
};
