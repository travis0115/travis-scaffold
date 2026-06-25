import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

import { z } from '#/adapter/form';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

function hasRichTextContent(value?: string) {
  if (!value) return false;
  return (
    value
      .replaceAll('&nbsp;', ' ')
      .replaceAll(/<br\s*\/?>/gi, '')
      .replaceAll(/<[^>]*>/g, '')
      .trim().length > 0
  );
}

export const useFormSchema = (): VbenFormSchema[] => [
  {
    component: 'Input',
    fieldName: 'title',
    label: '公告标题',
    rules: z.string().min(1),
  },
  {
    component: 'RichEditor',
    componentProps: {
      maxHeight: 520,
      minHeight: 280,
    },
    fieldName: 'content',
    formFieldProps: {
      validateOnBlur: false,
      validateOnChange: false,
      validateOnInput: false,
      validateOnModelUpdate: false,
    },
    label: '公告内容',
    rules: z.string().refine(hasRichTextContent, '公告内容不能为空'),
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: [
        { label: '草稿', value: 0 },
        { label: '发布', value: 1 },
      ],
    },
    defaultValue: 0,
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'DatePicker',
    componentProps: { showTime: true, valueFormat: 'YYYY-MM-DD HH:mm:ss' },
    fieldName: 'publishTime',
    label: '发布时间',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: [
        { label: '否', value: 0 },
        { label: '是', value: 1 },
      ],
    },
    defaultValue: 0,
    fieldName: 'pinned',
    label: '置顶',
  },
  {
    component: 'InputNumber',
    defaultValue: 0,
    fieldName: 'sort',
    label: '排序',
  },
  { component: 'Textarea', fieldName: 'remark', label: '备注' },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '公告标题' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: [
        { label: '草稿', value: 0 },
        { label: '已发布', value: 1 },
      ],
    },
    fieldName: 'status',
    label: '状态',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<SystemNoticeApi.Notice>,
  onStatusChange?: (
    newStatus: number,
    row: SystemNoticeApi.Notice,
  ) => Promise<boolean>,
): VxeTableGridColumns<SystemNoticeApi.Notice> {
  return [
    {
      field: 'title',
      minWidth: 220,
      slots: { default: 'title' },
      title: '公告标题',
    },
    {
      field: 'publishTime',
      formatter: 'formatDateTime',
      title: '发布时间',
      width: 180,
    },
    {
      field: 'pinned',
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '是' : '否'),
      title: '置顶',
      width: 80,
    },
    { field: 'sort', title: '排序', width: 80 },
    {
      cellRender: {
        attrs: {
          beforeChange: onStatusChange,
        },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
        options: [
          { label: '草稿', value: 0 },
          { label: '已发布', value: 1 },
        ],
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
        options: filterAccessOptions(
          [{ code: 'preview', text: '预览' }, 'edit', 'delete'],
          {
            delete: SYSTEM_PERMS.noticeDelete,
            edit: SYSTEM_PERMS.noticeUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 190,
    },
  ];
}
