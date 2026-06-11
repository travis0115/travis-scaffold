import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'exceptionClass', label: '异常类型' },
  { component: 'Input', fieldName: 'requestUrl', label: '请求地址' },
];

export const useColumns = (): VxeTableGridColumns => [
  { field: 'requestMethod', title: '请求方法', width: 100 },
  { field: 'requestUrl', minWidth: 220, title: '请求地址' },
  { field: 'exceptionClass', minWidth: 220, title: '异常类型' },
  { field: 'message', minWidth: 240, title: '异常消息' },
  { field: 'ip', title: 'IP', width: 140 },
  { field: 'createTime', formatter: 'formatDateTime', title: '发生时间', width: 180 },
];
