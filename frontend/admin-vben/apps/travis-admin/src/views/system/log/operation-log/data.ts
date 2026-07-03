import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { getDictOptions } from '#/utils/dict';

const BUSINESS_TYPE_OPTIONS = [
  { label: '新增', value: 'CREATE' },
  { label: '修改', value: 'UPDATE' },
  { label: '删除', value: 'DELETE' },
  { label: '授权', value: 'GRANT' },
  { label: '上传', value: 'UPLOAD' },
  { label: '导入', value: 'IMPORT' },
  { label: '导出', value: 'EXPORT' },
  { label: '其他', value: 'OTHER' },
];

function formatIp(ip?: string, location?: string) {
  if (!ip && !location) {
    return '-';
  }
  return `${ip || '-'}${location ? `（${location}）` : ''}`;
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'username', label: '操作用户' },
    { component: 'Input', fieldName: 'module', label: '操作模块' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: BUSINESS_TYPE_OPTIONS,
      },
      fieldName: 'businessType',
      label: '业务类型',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions('http_method'),
      },
      fieldName: 'requestMethod',
      label: '请求方式',
    },
    { component: 'Input', fieldName: 'requestUrl', label: '请求地址' },
    { component: 'Input', fieldName: 'ip', label: '操作 IP' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions('operation_status'),
      },
      fieldName: 'status',
      label: '操作状态',
    },
    {
      component: 'RangePicker',
      componentProps: {
        showTime: true,
        valueFormat: BACKEND_DATETIME_FORMAT,
      },
      fieldName: 'operationTimeRange',
      label: '操作时间',
    },
  ];
}

export function useColumns(): VxeTableGridColumns<SystemOperationLogApi.OperationLog> {
  return [
    { field: 'username', title: '操作用户', width: 130 },
    { field: 'module', title: '操作模块', width: 140 },
    { field: 'businessType', title: '业务类型', width: 110 },
    { field: 'description', minWidth: 180, title: '操作描述' },
    {
      cellRender: { attrs: { dictCode: 'http_method' }, name: 'CellTag' },
      field: 'requestMethod',
      title: '请求方式',
      width: 100,
    },
    { field: 'requestUrl', minWidth: 220, title: '请求地址' },
    {
      field: 'ip',
      formatter: ({ row }) => formatIp(row.ip, row.location),
      minWidth: 180,
      title: '操作 IP',
    },
    { field: 'browser', title: '浏览器', width: 130 },
    { field: 'os', title: '操作系统', width: 130 },
    { field: 'requestId', title: '请求 ID', width: 180 },
    {
      field: 'createTime',
      formatter: 'formatDateTime',
      sortable: true,
      title: '操作时间',
      width: 180,
    },
    {
      field: 'duration',
      fixed: 'right',
      sortable: true,
      title: '耗时(ms)',
      width: 100,
    },
    {
      cellRender: {
        attrs: { dictCode: 'operation_status' },
        name: 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: '操作状态',
      width: 100,
    },
  ];
}
