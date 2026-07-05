import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { z } from '#/adapter/form';
import { uploadMessageImage } from '#/api';
import { isDeptEnabled } from '#/features';
import { getDictLabel, getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

const requiredNumber = (message: string) =>
  z.number({ invalid_type_error: message, required_error: message });

const requiredIdList = (message: string) =>
  z
    .array(z.number(), { invalid_type_error: message, required_error: message })
    .min(1, message);

const optionalString = (max: number, message: string) =>
  z.string().max(max, message).optional().or(z.literal(''));

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

export const messageChannelOptions = getDictOptions('sys_message_channel');

export const messageStatusOptions = [
  { label: '待推送', value: 0 },
  { label: '定时推送', value: 1 },
  { label: '已推送', value: 2 },
  { label: '已撤回', value: 3 },
];

export const messagePushTypeOptions = [
  { label: '手动推送', value: 0 },
  { label: '定时推送', value: 1 },
];

export const messageSourceTypeOptions = getDictOptions('sys_message_source_type');
export const messageTypeOptions = getDictOptions('sys_message_type');

export const messageReceiverTypeOptions = [
  { label: '后台账号', value: 'admin' },
  { label: '客户端用户', value: 'app' },
];

const externalChannels = ['SMS', 'WECHAT_MP', 'WECHAT_OA'];

function isExternalChannel(channel?: string) {
  return channel ? externalChannels.includes(channel) : false;
}

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

export const useFormSchema = (): VbenFormSchema[] => [
  {
    component: 'Input',
    fieldName: 'title',
    label: '消息标题',
    rules: z
      .string({ required_error: '消息标题不能为空' })
      .min(1, '消息标题不能为空')
      .max(255, '消息标题长度不能超过255个字符'),
  },
  {
    component: 'Select',
    componentProps: {
      options: messageTypeOptions,
    },
    defaultValue: 1,
    fieldName: 'messageType',
    hideRequiredMark: true,
    label: '消息类型',
    rules: requiredNumber('消息类型不能为空'),
  },
  {
    component: 'Input',
    componentProps: { placeholder: '业务来源ID，可为空' },
    dependencies: {
      show: (values) => values.sourceType && values.sourceType !== 'MANUAL',
      triggerFields: ['sourceType'],
    },
    fieldName: 'sourceId',
    label: '来源ID',
  },
  {
    component: 'Select',
    componentProps: { options: messageChannelOptions },
    defaultValue: 'IN_APP',
    fieldName: 'channel',
    hideRequiredMark: true,
    label: '推送通道',
    rules: z
      .string({ required_error: '请选择推送通道' })
      .min(1, '请选择推送通道'),
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: [],
      placeholder: '可选择模板快速填充内容',
      showSearch: true,
    },
    fieldName: 'templateId',
    label: '消息模板',
  },
  {
    component: 'Switch',
    defaultValue: false,
    dependencies: {
      show: (values) => isExternalChannel(values.channel),
      triggerFields: ['channel'],
    },
    fieldName: 'enableInboxCopy',
    label: '同步站内信副本',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
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
      (value) => [0, 1, 2, 3].includes(value),
      '接收范围错误',
    ),
  },
  {
    component: 'Select',
    componentProps: {
      class: 'w-full',
      filterOption: false,
      mode: 'multiple',
      options: [],
      placeholder: '请输入用户名/昵称/手机号搜索',
      showSearch: true,
    },
    dependencies: { show: (values) => values.receiverScope === 1, triggerFields: ['receiverScope'] },
    fieldName: 'userIds',
    label: '接收用户',
    rules: requiredIdList('请选择接收用户'),
  },
  {
    component: 'Select',
    componentProps: { class: 'w-full', mode: 'multiple', options: [], placeholder: '请选择角色' },
    dependencies: {
      show: (values) =>
        values.receiverType === 'admin' &&
        values.receiverScope === 2,
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
      showTime: true,
      valueFormat: BACKEND_DATETIME_FORMAT,
    },
    dependencies: { show: (values) => values.pushType === 1, triggerFields: ['pushType'] },
    fieldName: 'publishTime',
    label: '发布时间',
    rules: z
      .string({ required_error: '发布时间不能为空' })
      .min(1, '发布时间不能为空'),
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
    dependencies: { show: (values) => values.channel === 'IN_APP', triggerFields: ['channel'] },
    fieldName: 'inAppContent',
    formFieldProps: {
      validateOnBlur: false,
      validateOnChange: false,
      validateOnInput: false,
      validateOnModelUpdate: false,
    },
    label: '站内信内容',
    rules: z
      .string({ required_error: '站内信内容不能为空' })
      .max(5000, '站内信内容长度不能超过5000个字符')
      .refine(hasRichTextContent, '站内信内容不能为空'),
  },
  {
    component: 'Textarea',
    componentProps: { rows: 4 },
    dependencies: { show: (values) => values.channel === 'SMS', triggerFields: ['channel'] },
    fieldName: 'smsContent',
    label: '短信内容',
    rules: optionalString(5000, '短信内容长度不能超过5000个字符'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => values.channel === 'WECHAT_MP' || values.channel === 'WECHAT_OA',
      triggerFields: ['channel'],
    },
    fieldName: 'miniProgramTemplateParams',
    label: '微信模板参数',
    rules: optionalString(4000, '模板参数长度不能超过4000个字符'),
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
      field: 'messageType',
      formatter: ({ cellValue }: any) => getDictLabel('sys_message_type', cellValue),
      title: '类型',
      width: 100,
    },
    {
      field: 'receiverType',
      formatter: ({ cellValue }: any) =>
        cellValue === 'app' ? '客户端用户' : '后台账号',
      title: '接收端',
      width: 120,
    },
    {
      field: 'receiverScope',
      formatter: ({ cellValue }: any) => ['全部用户', '指定用户', '指定角色', '指定部门'][cellValue] ?? '-',
      title: '接收范围',
      width: 110,
    },
    {
      field: 'pushType',
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '定时推送' : '手动推送'),
      title: '推送方式',
      width: 110,
    },
    { field: 'publishTime', formatter: 'formatDateTime', title: '发布时间', width: 180 },
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
        options: filterAccessOptions([
          {
            code: 'push',
            show: (row: SystemMessageApi.Message) => row.status === 0,
            text: '推送',
          },
          {
            code: 'push',
            show: (row: SystemMessageApi.Message) => row.status === 1,
            text: '手动推送',
          },
          {
            code: 'revoke',
            danger: true,
            show: (row: SystemMessageApi.Message) => row.status === 2,
            text: '撤回',
          },
          {
            code: 'edit',
            show: (row: SystemMessageApi.Message) => row.status !== 2,
          },
          'delete',
        ], {
          delete: SYSTEM_PERMS.messageDelete,
          edit: SYSTEM_PERMS.messageUpdate,
          push: SYSTEM_PERMS.messageUpdate,
          revoke: SYSTEM_PERMS.messageUpdate,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 210,
    },
  ];
}
