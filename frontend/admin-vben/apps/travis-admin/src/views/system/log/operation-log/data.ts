import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { component: 'Input', fieldName: 'username', label: '操作用户' },
    { component: 'Input', fieldName: 'module', label: '操作模块' },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '成功', value: 1 },
          { label: '失败', value: 0 },
        ],
      },
      fieldName: 'status',
      label: '操作状态',
    },
  ];
}

export function useColumns(): VxeTableGridColumns<SystemOperationLogApi.OperationLog> {
  return [
    { field: 'username', title: '操作用户', width: 130 },
    { field: 'module', title: '操作模块', width: 140 },
    { field: 'description', minWidth: 180, title: '操作描述' },
    { field: 'requestMethod', title: '请求方式', width: 100 },
    { field: 'requestUrl', minWidth: 220, title: '请求地址' },
    { field: 'ip', title: '操作 IP', width: 140 },
    { field: 'duration', title: '耗时(ms)', width: 100 },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      formatter: ({ cellValue }) => (cellValue === 1 ? '成功' : '失败'),
      title: '操作状态',
      width: 100,
    },
    {
      field: 'createTime',
      formatter: 'formatDateTime',
      title: '操作时间',
      width: 180,
    },
  ];
}
