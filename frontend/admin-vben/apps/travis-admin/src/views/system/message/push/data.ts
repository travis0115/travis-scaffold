import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';

import { z } from '#/adapter/form';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export const useFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '消息标题', rules: z.string().min(1) },
  {
    component: 'Select',
    componentProps: { options: [{ label: '系统消息', value: 1 }, { label: '业务消息', value: 2 }] },
    defaultValue: 1,
    fieldName: 'messageType',
    label: '消息类型',
  },
  { component: 'Textarea', fieldName: 'content', label: '消息内容', rules: z.string().min(1) },
  {
    component: 'Input',
    componentProps: { placeholder: '例如 SYSTEM、OPS_JOB' },
    fieldName: 'sourceType',
    label: '来源类型',
  },
  {
    component: 'Input',
    componentProps: { placeholder: '业务来源ID，可为空' },
    fieldName: 'sourceId',
    label: '来源ID',
  },
  {
    component: 'Select',
    componentProps: { disabled: true, options: [{ label: '站内信', value: 'IN_APP' }] },
    defaultValue: 'IN_APP',
    fieldName: 'channels',
    label: '推送通道',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      options: [
        { label: '全部用户', value: 0 },
        { label: '指定用户', value: 1 },
        { label: '指定角色', value: 2 },
        { label: '指定部门', value: 3 },
      ],
    },
    defaultValue: 0,
    fieldName: 'audienceType',
    label: '接收范围',
  },
  {
    component: 'Select',
    componentProps: { class: 'w-full', mode: 'multiple', options: [], placeholder: '请选择用户' },
    dependencies: { show: (values) => values.audienceType === 1, triggerFields: ['audienceType'] },
    fieldName: 'userIds',
    label: '接收用户',
  },
  {
    component: 'Select',
    componentProps: { class: 'w-full', mode: 'multiple', options: [], placeholder: '请选择角色' },
    dependencies: { show: (values) => values.audienceType === 2, triggerFields: ['audienceType'] },
    fieldName: 'roleIds',
    label: '接收角色',
  },
  {
    component: 'TreeSelect',
    componentProps: {
      allowClear: true,
      fieldNames: { children: 'children', label: 'deptName', value: 'id' },
      multiple: true,
      placeholder: '请选择部门',
      treeData: [],
    },
    dependencies: { show: (values) => values.audienceType === 3, triggerFields: ['audienceType'] },
    fieldName: 'deptIds',
    label: '接收部门',
  },
  {
    component: 'RadioGroup',
    componentProps: { options: [{ label: '草稿', value: 0 }, { label: '发布', value: 1 }] },
    defaultValue: 0,
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    fieldName: 'publishTime',
    label: '发布时间',
  },
  { component: 'Textarea', fieldName: 'remark', label: '备注' },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '消息标题' },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: [{ label: '草稿', value: 0 }, { label: '已发布', value: 1 }] },
    fieldName: 'status',
    label: '状态',
  },
];

export function useColumns<T>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: number, row: T) => Promise<boolean>,
): VxeTableGridColumns {
  return [
    { field: 'title', minWidth: 220, title: '消息标题' },
    {
      field: 'messageType',
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '系统消息' : '业务消息'),
      title: '类型',
      width: 100,
    },
    {
      field: 'audienceType',
      formatter: ({ cellValue }: any) => ['全部用户', '指定用户', '指定角色', '指定部门'][cellValue] ?? '-',
      title: '接收范围',
      width: 110,
    },
    { field: 'publishTime', formatter: 'formatDateTime', title: '发布时间', width: 180 },
    {
      cellRender: {
        attrs: {
          beforeChange: onStatusChange,
        },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
        options: [{ label: '草稿', value: 0 }, { label: '已发布', value: 1 }],
      },
      field: 'status',
      fixed: 'right',
      title: '状态',
      width: 100,
    },
    {
      cellRender: {
        attrs: { nameField: 'title', onClick: onActionClick },
        name: 'CellOperation',
        options: filterAccessOptions(['edit', 'delete'], {
          delete: SYSTEM_PERMS.messageDelete,
          edit: SYSTEM_PERMS.messageUpdate,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 130,
    },
  ];
}
