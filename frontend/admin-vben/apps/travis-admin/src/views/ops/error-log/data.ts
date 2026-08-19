import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { OpsErrorLogApi } from '#/api';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { httpMethodOptions } from '#/utils/business-options';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, OPS_PERMS } from '#/utils/permissions';

export const ERROR_LOG_HANDLE_STATUS_DICT = 'ops_error_log_handle_status';
export const handleStatusOptions = getDictOptions(ERROR_LOG_HANDLE_STATUS_DICT);

export const ERROR_LOG_PLATFORM_TYPE_DICT = 'ops_error_log_platform_type';
export const platformTypeOptions = getDictOptions(ERROR_LOG_PLATFORM_TYPE_DICT);

export const useGridFormSchema = (): VbenFormSchema[] => [
  {
    component: 'Select',
    componentProps: { allowClear: true, options: handleStatusOptions },
    fieldName: 'status',
    label: '处理状态',
  },
  { component: 'Input', fieldName: 'moduleName', label: '模块名称' },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: platformTypeOptions },
    fieldName: 'platformType',
    label: '平台类型',
  },
  { component: 'Input', fieldName: 'exceptionClass', label: '异常类型' },
  { component: 'Input', fieldName: 'requestId', label: '请求 ID' },
  { component: 'Input', fieldName: 'requestUrl', label: '请求地址' },
  { component: 'Input', fieldName: 'ip', label: '请求 IP' },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: httpMethodOptions },
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
    label: '最近发生时间',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<OpsErrorLogApi.ErrorLog>,
): VxeTableGridColumns {
  return [
    {
      cellRender: {
        attrs: { dictCode: ERROR_LOG_HANDLE_STATUS_DICT },
        name: 'CellTag',
      },
      field: 'status',
      fixed: 'left',
      title: '处理状态',
      width: 100,
    },
    {
      field: 'sourceInfo',
      minWidth: 190,
      showOverflow: false,
      slots: { default: 'sourceInfo' },
      title: '来源信息',
    },
    {
      field: 'requestInfo',
      minWidth: 320,
      showOverflow: false,
      slots: { default: 'requestInfo' },
      title: '请求信息',
    },
    {
      field: 'message',
      minWidth: 260,
      showOverflow: false,
      slots: { default: 'errorMessage' },
      title: '错误消息',
    },
    { field: 'occurrenceCount', title: '发生次数', width: 100 },
    { field: 'username', title: '用户名', width: 120 },
    {
      field: 'occurrenceTime',
      minWidth: 180,
      showOverflow: false,
      slots: { default: 'occurrenceTime' },
      title: '发生时间',
    },
    {
      align: 'center',
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            { code: 'preview', text: '查看' },
            {
              code: 'edit',
              show: (row: OpsErrorLogApi.ErrorLog) => row.status === 0,
              text: '处理',
            },
            { code: 'delete', popconfirm: false },
          ],
          {
            delete: OPS_PERMS.errorLogDelete,
            edit: OPS_PERMS.errorLogHandle,
            preview: OPS_PERMS.errorLogQuery,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 200,
    },
  ];
}
