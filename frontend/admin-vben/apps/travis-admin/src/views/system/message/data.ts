import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { h } from 'vue';

import { Button } from 'antdv-next';

import { z } from '#/adapter/form';
import { uploadMessageImage } from '#/api';
import { isDeptEnabled } from '#/features';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

const requiredNumber = (message: string) =>
  z
    .number({ invalid_type_error: message, required_error: message })
    .finite(message);

const requiredIdList = (message: string) =>
  z
    .array(z.union([z.number(), z.string().regex(/^-?\d+$/)]), {
      invalid_type_error: message,
      required_error: message,
    })
    .min(1, message);

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

export const messageStatusOptions = getDictOptions('sys_message_status');

export const messagePushTypeOptions = getDictOptions('sys_message_push_type');

export const messageChannelOptions = getDictOptions('sys_message_channel');

export const messageReceiverTypeOptions = [
  { label: '后台账号', value: 'admin' },
  { label: '客户端用户', value: 'app' },
];

function getReceiverScopeOptions(receiverType?: string) {
  const options = [
    { label: '全部用户', value: 0 },
    { label: '指定用户', value: 1 },
  ];
  if (receiverType === 'admin') {
    options.push({ label: '指定角色', value: 2 });
    if (isDeptEnabled()) {
      options.push({ label: '指定部门', value: 3 });
    }
  }
  return options;
}

export const useFormSchema = (
  onReceiverTypeChange?: (value: unknown) => void,
  onChannelChange?: (value: unknown) => void,
  onSelectTemplate?: () => void,
  onSelectUsers?: () => void,
): VbenFormSchema[] => [
  {
    component: 'Select',
    componentProps: {
      onChange: onChannelChange,
      options: messageChannelOptions,
    },
    defaultValue: 'IN_APP',
    fieldName: 'channel',
    hideRequiredMark: true,
    label: '推送通道',
    rules: z
      .string({ required_error: '请选择推送通道' })
      .min(1, '请选择推送通道'),
  },
  {
    component: 'Input',
    componentProps: {
      placeholder: '请选择消息模板',
      readonly: true,
    },
    fieldName: 'templateName',
    label: '消息模板',
    renderComponentContent: () => ({
      addonAfter: () =>
        h(
          Button,
          { onClick: onSelectTemplate, size: 'small', type: 'link' },
          () => '选择',
        ),
    }),
  },
  {
    component: 'Input',
    fieldName: 'templateId',
    hide: true,
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      onChange: onReceiverTypeChange,
      optionType: 'button',
      options: messageReceiverTypeOptions,
    },
    defaultValue: 'admin',
    fieldName: 'receiverType',
    hideRequiredMark: true,
    label: '接收端',
    rules: z
      .string({ required_error: '接收端不能为空' })
      .min(1, '接收端不能为空'),
  },
  {
    component: 'RadioGroup',
    componentProps: (values) => ({
      buttonStyle: 'solid',
      optionType: 'button',
      options: getReceiverScopeOptions(values.receiverType),
    }),
    defaultValue: 0,
    fieldName: 'receiverScope',
    hideRequiredMark: true,
    label: '接收范围',
    rules: requiredNumber('接收范围不能为空').refine(
      (value) => [0, 1, 2, 3].includes(Number(value)),
      '接收范围错误',
    ),
  },
  {
    component: 'Input',
    componentProps: {
      class: 'w-full',
      placeholder: '请选择用户',
      readonly: true,
    },
    dependencies: {
      show: (values) => values.receiverScope === 1,
      triggerFields: ['receiverScope'],
    },
    fieldName: 'userDisplay',
    formFieldProps: {
      validateOnBlur: false,
      validateOnChange: false,
      validateOnInput: false,
      validateOnModelUpdate: false,
    },
    label: '接收用户',
    renderComponentContent: () => ({
      addonAfter: () =>
        h(
          Button,
          { onClick: onSelectUsers, size: 'small', type: 'link' },
          () => '选择',
        ),
    }),
    rules: z.string().min(1, '请选择接收用户'),
  },
  {
    component: 'Input',
    fieldName: 'userIds',
    hide: true,
  },
  {
    component: 'Select',
    componentProps: {
      class: 'w-full',
      mode: 'multiple',
      optionFilterProp: 'label',
      options: [],
      placeholder: '请选择角色',
      showSearch: true,
    },
    dependencies: {
      show: (values) =>
        values.receiverType === 'admin' && values.receiverScope === 2,
      triggerFields: ['receiverType', 'receiverScope'],
    },
    fieldName: 'roleIds',
    label: '接收角色',
    rules: requiredIdList('请选择接收角色'),
  },
  {
    component: 'TreeSelect',
    componentProps: {
      allowClear: true,
      fieldNames: { children: 'children', label: 'deptName', value: 'id' },
      multiple: true,
      placeholder: '请选择部门',
      showCheckedStrategy: 'SHOW_PARENT',
      treeData: [],
      treeCheckable: true,
      treeCheckStrictly: false,
    },
    dependencies: {
      show: (values) =>
        isDeptEnabled() &&
        values.receiverType === 'admin' &&
        values.receiverScope === 3,
      triggerFields: ['receiverType', 'receiverScope'],
    },
    fieldName: 'deptIds',
    label: '接收部门',
    rules: requiredIdList('请选择接收部门'),
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: messagePushTypeOptions,
    },
    defaultValue: 0,
    fieldName: 'pushType',
    hideRequiredMark: true,
    label: '推送方式',
    rules: requiredNumber('推送方式不能为空'),
  },
  {
    component: 'DatePicker',
    componentProps: {
      format: 'YYYY-MM-DD HH:mm',
      showTime: { format: 'HH:mm' },
      valueFormat: 'YYYY-MM-DD HH:mm',
    },
    dependencies: {
      show: (values) => values.pushType === 1,
      triggerFields: ['pushType'],
    },
    fieldName: 'publishTime',
    label: '推送时间',
    rules: z
      .string({ required_error: '推送时间不能为空' })
      .min(1, '推送时间不能为空'),
  },
  {
    component: 'Input',
    dependencies: {
      disabled: (values) => Boolean(values.templateId),
      triggerFields: ['templateId'],
    },
    fieldName: 'title',
    label: '消息标题',
    rules: z
      .string({ required_error: '消息标题不能为空' })
      .min(1, '消息标题不能为空')
      .max(255, '消息标题长度不能超过255个字符'),
  },
  {
    component: 'RichEditor',
    componentProps: {
      imageUpload: {
        upload: async (file: File, onProgress?: (percent: number) => void) => {
          const result = await uploadMessageImage(file, (event) => {
            if (!event.total) return;
            onProgress?.(Math.round((event.loaded / event.total) * 100));
          });
          return { id: result.id, url: result.url };
        },
      },
      maxHeight: 420,
      minHeight: 240,
    },
    dependencies: {
      componentProps: (values) => ({ editable: !values.templateId }),
      triggerFields: ['templateId'],
    },
    fieldName: 'inAppContent',
    formFieldProps: {
      validateOnBlur: false,
      validateOnChange: false,
      validateOnInput: false,
      validateOnModelUpdate: false,
    },
    label: '消息内容',
    rules: z
      .string({ required_error: '站内信内容不能为空' })
      .max(5000, '站内信内容长度不能超过5000个字符')
      .refine(hasRichTextContent, '站内信内容不能为空'),
  },
  { component: 'Textarea', fieldName: 'remark', label: '备注' },
];

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '消息标题' },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: messagePushTypeOptions },
    fieldName: 'pushType',
    label: '推送方式',
  },
  {
    component: 'Select',
    componentProps: { allowClear: true, options: messageStatusOptions },
    fieldName: 'status',
    label: '状态',
  },
];

export function useColumns<T>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridColumns<SystemMessageApi.Message> {
  return [
    { field: 'title', minWidth: 220, title: '消息标题' },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_type' },
        name: 'CellTag',
      },
      field: 'messageType',
      title: '消息类型',
      width: 110,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_receiver_type' },
        name: 'CellTag',
      },
      field: 'receiverType',
      title: '接收端',
      width: 120,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_receiver_scope' },
        name: 'CellTag',
      },
      field: 'receiverScope',
      title: '接收范围',
      width: 110,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_push_type' },
        name: 'CellTag',
      },
      field: 'pushType',
      title: '推送方式',
      width: 110,
    },
    {
      field: 'publishTime',
      formatter: 'formatDateTime',
      title: '推送时间',
      width: 180,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_status' },
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
          nameTitle: '消息',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            {
              code: 'push',
              show: (row: SystemMessageApi.Message) =>
                row.sourceType === 'MANUAL' && row.status === 0,
              text: '推送',
            },
            {
              code: 'push',
              show: (row: SystemMessageApi.Message) =>
                row.sourceType === 'MANUAL' && row.status === 2,
              text: '重新推送',
            },
            {
              code: 'preview',
              show: (row: SystemMessageApi.Message) => row.status === 1,
              text: '预览',
            },
            {
              code: 'revoke',
              danger: true,
              show: (row: SystemMessageApi.Message) =>
                row.sourceType === 'MANUAL' && row.status === 1,
              text: '撤回',
            },
            {
              code: 'edit',
              show: (row: SystemMessageApi.Message) =>
                row.sourceType === 'MANUAL' &&
                (row.status === 0 || row.status === 2),
            },
            {
              code: 'delete',
              show: (row: SystemMessageApi.Message) =>
                row.sourceType === 'MANUAL' && row.status !== 1,
            },
          ],
          {
            delete: SYSTEM_PERMS.messageDelete,
            edit: SYSTEM_PERMS.messageUpdate,
            push: SYSTEM_PERMS.messageUpdate,
            revoke: SYSTEM_PERMS.messageUpdate,
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
