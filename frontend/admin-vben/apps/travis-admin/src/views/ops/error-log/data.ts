import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { httpMethodOptions } from '#/utils/business-options';

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'exceptionClass', label: '异常类型' },
  { component: 'Input', fieldName: 'requestUrl', label: '请求地址' },
  { component: 'Input', fieldName: 'ip', label: '请求 IP' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: httpMethodOptions,
    },
    fieldName: 'requestMethod',
    label: '请求方法',
  },
  {
    component: 'RangePicker',
    componentProps: {
      showTime: true,
      valueFormat: BACKEND_DATETIME_FORMAT,
    },
    fieldName: 'exceptionTimeRange',
    label: '异常时间',
  },
];

export const useColumns = (): VxeTableGridColumns => [
  { field: 'sourceType', title: '来源', width: 110 },
  { field: 'sourceName', minWidth: 220, title: '来源名称' },
  { field: 'requestMethod', title: '请求方法', width: 100 },
  { field: 'requestUrl', minWidth: 220, title: '请求地址' },
  { field: 'requestId', minWidth: 180, title: '请求 ID' },
  { field: 'exceptionClass', minWidth: 220, title: '异常类型' },
  { field: 'message', minWidth: 240, title: '异常消息' },
  { field: 'ip', title: 'IP', width: 140 },
  {
    field: 'createTime',
    formatter: 'formatDateTime',
    title: '发生时间',
    width: 180,
  },
];
