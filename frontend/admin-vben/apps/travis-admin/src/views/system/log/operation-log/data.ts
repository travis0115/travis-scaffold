import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import {
  httpMethodOptions,
  operationBusinessTypeOptions,
  operationStatusOptions,
} from '#/utils/business-options';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'username', label: '操作用户' },
    { component: 'Input', fieldName: 'module', label: '操作模块' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: operationBusinessTypeOptions,
      },
      fieldName: 'businessType',
      label: '业务类型',
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: httpMethodOptions,
      },
      fieldName: 'requestMethod',
      label: '请求方式',
    },
    { component: 'Input', fieldName: 'requestUrl', label: '请求地址' },
    { component: 'Input', fieldName: 'requestId', label: '请求 ID' },
    { component: 'Input', fieldName: 'ip', label: '操作 IP' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: operationStatusOptions,
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
    {
      field: 'operationInfo',
      minWidth: 180,
      slots: { default: 'operationInfo' },
      showOverflow: false,
      title: '操作模块',
    },
    {
      cellRender: {
        attrs: { dictCode: 'operation_business_type' },
        name: 'CellTag',
      },
      field: 'businessType',
      title: '业务类型',
      width: 110,
    },
    {
      cellRender: { attrs: { dictCode: 'http_method' }, name: 'CellTag' },
      field: 'requestMethod',
      title: '请求方式',
      width: 100,
    },
    {
      field: 'requestInfo',
      minWidth: 260,
      slots: { default: 'requestInfo' },
      showOverflow: false,
      title: '请求信息',
    },
    {
      field: 'deviceInfo',
      minWidth: 150,
      slots: { default: 'deviceInfo' },
      showOverflow: false,
      title: '设备信息',
    },
    {
      field: 'ip',
      minWidth: 180,
      slots: { default: 'ipInfo' },
      showOverflow: false,
      title: '操作 IP',
    },
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
