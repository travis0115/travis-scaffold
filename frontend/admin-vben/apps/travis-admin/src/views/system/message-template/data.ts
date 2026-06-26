import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { z } from '#/adapter/form';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';
import { messageChannelOptions } from '#/views/system/message/data';

const requiredString = (message: string) =>
  z.string({ required_error: message }).min(1, message);

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];

export const useFormSchema = (): VbenFormSchema[] => [
  {
    component: 'Input',
    fieldName: 'templateCode',
    label: '模板编码',
    rules: requiredString('模板编码不能为空').max(64, '模板编码长度不能超过64个字符'),
  },
  {
    component: 'Input',
    fieldName: 'templateName',
    label: '模板名称',
    rules: requiredString('模板名称不能为空').max(100, '模板名称长度不能超过100个字符'),
  },
  {
    component: 'Select',
    componentProps: { options: messageChannelOptions },
    fieldName: 'channel',
    label: '推送通道',
    rules: requiredString('推送通道不能为空'),
  },
  {
    component: 'Input',
    fieldName: 'platformTemplateId',
    label: '平台模板ID',
  },
  {
    component: 'Textarea',
    componentProps: { rows: 4 },
    fieldName: 'contentSchema',
    label: '字段结构',
  },
  {
    component: 'Textarea',
    componentProps: { rows: 5 },
    fieldName: 'content',
    label: '模板内容',
  },
  {
    component: 'Input',
    fieldName: 'pagePath',
    label: '页面路径',
  },
  {
    component: 'Input',
    fieldName: 'jumpUrl',
    label: '跳转链接',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: statusOptions,
    },
    defaultValue: 1,
    fieldName: 'status',
    label: '状态',
    rules: z.number({ invalid_type_error: '状态不能为空', required_error: '状态不能为空' }),
  },
  { component: 'Textarea', fieldName: 'remark', label: '备注' },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'templateCode', label: '模板编码' },
  { component: 'Input', fieldName: 'templateName', label: '模板名称' },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: messageChannelOptions },
    fieldName: 'channel',
    label: '推送通道',
  },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: statusOptions },
    fieldName: 'status',
    label: '状态',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<SystemMessageApi.MessageTemplate>,
): VxeTableGridColumns<SystemMessageApi.MessageTemplate> {
  return [
    { field: 'templateCode', minWidth: 180, title: '模板编码' },
    { field: 'templateName', minWidth: 180, title: '模板名称' },
    {
      field: 'channel',
      formatter: ({ cellValue }: any) =>
        messageChannelOptions.find((item) => item.value === cellValue)?.label || cellValue,
      title: '推送通道',
      width: 130,
    },
    { field: 'platformTemplateId', title: '平台模板ID', width: 180 },
    {
      cellRender: {
        attrs: { dictCode: 'sys_status' },
        name: 'CellTag',
      },
      field: 'status',
      title: '状态',
      width: 90,
    },
    { field: 'createTime', formatter: 'formatDateTime', title: '创建时间', width: 180 },
    {
      cellRender: {
        attrs: {
          nameField: 'templateName',
          nameTitle: '消息模板',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(['edit', 'delete'], {
          delete: SYSTEM_PERMS.messageTemplateDelete,
          edit: SYSTEM_PERMS.messageTemplateUpdate,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 130,
    },
  ];
}
