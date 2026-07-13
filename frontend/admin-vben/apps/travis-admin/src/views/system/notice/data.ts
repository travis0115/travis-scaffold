import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

import { z } from '#/adapter/form';
import { uploadNoticeImage } from '#/api';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

const publishStatusOptions = getDictOptions('sys_publish_status');

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
    rules: z
      .string({ required_error: '公告标题不能为空' })
      .min(1, '公告标题不能为空')
      .max(255, '公告标题长度不能超过255个字符'),
  },
  {
    component: 'RichEditor',
    componentProps: {
      imageUpload: {
        upload: async (file: File, onProgress?: (percent: number) => void) => {
          const result = await uploadNoticeImage(file, (event) => {
            if (!event.total) return;
            onProgress?.(Math.round((event.loaded / event.total) * 100));
          });
          return { id: result.id, url: result.url };
        },
      },
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
    rules: z
      .string({ required_error: '公告内容不能为空' })
      .max(5000, '公告内容长度不能超过5000个字符')
      .refine(hasRichTextContent, '公告内容不能为空'),
  },
  {
    component: 'InputNumber',
    componentProps: { max: 9999, min: 0 },
    defaultValue: 999,
    fieldName: 'sort',
    label: '排序',
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
    fieldName: 'isPinned',
    label: '置顶',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: publishStatusOptions.filter(
        (option) => Number(option.value) !== 2,
      ),
    },
    defaultValue: 0,
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    label: '备注',
    rules: z
      .string()
      .max(255, '备注长度不能超过255个字符')
      .optional()
      .or(z.literal('')),
  },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '公告标题' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: publishStatusOptions,
    },
    fieldName: 'status',
    label: '状态',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<SystemNoticeApi.Notice>,
  onPinnedChange?: (
    isPinned: number,
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
      cellRender: {
        attrs: {
          beforeChange: onPinnedChange,
          dictCode: 'is_pinned',
        },
        name: onPinnedChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'isPinned',
      title: '置顶',
      width: 80,
    },
    { field: 'sort', title: '排序', width: 80 },
    {
      cellRender: {
        attrs: { dictCode: 'sys_publish_status' },
        name: 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: '状态',
      width: 100,
    },
    {
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: '公告',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            { code: 'preview', text: '预览' },
            {
              code: 'publish',
              show: (row: SystemNoticeApi.Notice) => row.status === 0,
              text: '发布',
            },
            {
              code: 'publish',
              show: (row: SystemNoticeApi.Notice) => row.status === 2,
              text: '重新发布',
            },
            {
              code: 'revoke',
              danger: true,
              show: (row: SystemNoticeApi.Notice) => row.status === 1,
              text: '撤回',
            },
            {
              code: 'edit',
              show: (row: SystemNoticeApi.Notice) => row.status !== 1,
            },
            {
              code: 'delete',
              show: (row: SystemNoticeApi.Notice) => row.status !== 1,
            },
          ],
          {
            delete: SYSTEM_PERMS.noticeDelete,
            edit: SYSTEM_PERMS.noticeUpdate,
            publish: SYSTEM_PERMS.noticeUpdate,
            revoke: SYSTEM_PERMS.noticeUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 210,
    },
  ];
}
