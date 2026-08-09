import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { z } from '#/adapter/form';
import {
  messageReadStatusOptions,
  messageTypeOptions,
} from '#/utils/business-options';

export const useGridFormSchema = (): VbenFormSchema[] => [
  {
    component: 'Input',
    fieldName: 'title',
    label: '消息标题',
    rules: z.string().max(255, '消息标题长度不能超过255个字符').optional(),
  },
  {
    component: 'RangePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD' },
    fieldName: 'publishDateRange',
    label: '发布日期',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: messageTypeOptions,
    },
    fieldName: 'messageType',
    label: '消息类型',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: messageReadStatusOptions,
    },
    fieldName: 'readStatus',
    label: '阅读状态',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<SystemMessageApi.UserMessage>,
): VxeTableGridColumns<SystemMessageApi.UserMessage> {
  return [
    {
      align: 'center',
      field: 'title',
      minWidth: 260,
      slots: { default: 'title' },
      title: '消息标题',
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_type' },
        name: 'CellTag',
      },
      field: 'messageType',
      title: '类型',
      width: 90,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_read_status' },
        name: 'CellTag',
      },
      field: 'readStatus',
      title: '状态',
      width: 90,
    },
    { field: 'publishTime', formatter: 'formatDateTime', title: '发布时间', width: 180 },
    {
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: '消息',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          { code: 'preview', text: '预览' },
          { code: 'read', show: (row: SystemMessageApi.UserMessage) => row.readStatus === 0, text: '已读' },
          'delete',
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 210,
    },
  ];
}
