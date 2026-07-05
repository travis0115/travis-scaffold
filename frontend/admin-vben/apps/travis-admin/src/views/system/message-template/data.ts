import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { z } from '#/adapter/form';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';
import { messageChannelOptions } from '#/views/system/message/data';

const requiredString = (message: string) =>
  z.string({ required_error: message }).min(1, message);

const optionalString = (max: number, message: string) =>
  z.string().max(max, message).optional().or(z.literal(''));

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
    rules: requiredString('推送通道不能为空').max(32, '推送通道长度不能超过32个字符'),
  },
  {
    component: 'Input',
    fieldName: 'templateType',
    label: '模板分类',
    rules: optionalString(64, '模板分类长度不能超过64个字符'),
  },
  {
    component: 'Input',
    fieldName: 'title',
    label: '模板标题',
    rules: optionalString(255, '模板标题长度不能超过255个字符'),
  },
  {
    component: 'Input',
    fieldName: 'platformTemplateId',
    label: '平台模板ID',
    rules: optionalString(128, '平台模板ID长度不能超过128个字符'),
  },
  {
    component: 'Textarea',
    componentProps: { rows: 4 },
    fieldName: 'contentSchema',
    label: '字段结构',
    rules: optionalString(4000, '字段结构长度不能超过4000个字符'),
  },
  {
    component: 'Textarea',
    componentProps: { rows: 5 },
    fieldName: 'content',
    label: '模板内容',
    rules: optionalString(5000, '模板内容长度不能超过5000个字符'),
  },
  {
    component: 'Input',
    fieldName: 'redirectUrl',
    label: '跳转地址',
    rules: optionalString(500, '跳转地址长度不能超过500个字符'),
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
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    label: '备注',
    rules: optionalString(255, '备注长度不能超过255个字符'),
  },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'templateCode', label: '模板编码' },
  { component: 'Input', fieldName: 'templateName', label: '模板名称' },
  { component: 'Input', fieldName: 'templateType', label: '模板分类' },
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
    { field: 'templateType', title: '模板分类', width: 130 },
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
        attrs: { dictCode: 'enable_status' },
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
