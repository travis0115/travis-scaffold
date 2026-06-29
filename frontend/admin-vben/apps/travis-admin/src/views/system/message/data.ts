import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { z } from '#/adapter/form';
import { uploadMessageImage } from '#/api';
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

export const messageChannelOptions = [
  { label: '站内信', value: 'IN_APP' },
  { label: 'App推送', value: 'APP_PUSH' },
  { label: '短信', value: 'SMS' },
  { label: '微信小程序', value: 'WECHAT_MP' },
  { label: '抖音小程序', value: 'DOUYIN_MP' },
];

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

export const messageSourceTypeOptions = [
  { label: '后台人工推送', value: 'MANUAL' },
  { label: '系统触发', value: 'SYSTEM' },
  { label: '订单', value: 'ORDER' },
  { label: '用户', value: 'USER' },
  { label: '定时任务', value: 'OPS_JOB' },
  { label: '运营活动', value: 'ACTIVITY' },
];

export const messageReceiverTypeOptions = [
  { label: '管理后台用户', value: 'admin' },
  { label: '前端用户', value: 'user' },
];

export const messageReceiverScopeOptions = [
  { label: '全部用户', value: 0 },
  { label: '指定用户', value: 1 },
  { label: '指定角色', value: 2 },
  { label: '指定部门', value: 3 },
];

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
      options: [
        { label: '系统消息', value: 1 },
        { label: '业务消息', value: 2 },
      ],
    },
    defaultValue: 1,
    fieldName: 'messageType',
    label: '消息类型',
    rules: requiredNumber('消息类型不能为空'),
  },
  {
    component: 'Select',
    componentProps: { options: messageSourceTypeOptions },
    defaultValue: 'MANUAL',
    fieldName: 'sourceType',
    label: '来源类型',
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
    component: 'CheckboxGroup',
    componentProps: { options: messageChannelOptions },
    defaultValue: ['IN_APP'],
    fieldName: 'channels',
    label: '推送通道',
    rules: z.array(z.string()).min(1, '请至少选择一个推送通道'),
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
    label: '接收端',
    rules: z
      .string({ required_error: '接收端不能为空' })
      .min(1, '接收端不能为空'),
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      optionType: 'button',
      options: messageReceiverScopeOptions,
    },
    defaultValue: 0,
    fieldName: 'receiverScope',
    label: '接收范围',
    rules: requiredNumber('接收范围不能为空').refine(
      (value) => [0, 1, 2, 3].includes(value),
      '接收范围错误',
    ),
  },
  {
    component: 'Select',
    componentProps: { class: 'w-full', mode: 'multiple', options: [], placeholder: '请选择用户' },
    dependencies: { show: (values) => values.receiverScope === 1, triggerFields: ['receiverScope'] },
    fieldName: 'userIds',
    label: '接收用户',
    rules: requiredIdList('请选择接收用户'),
  },
  {
    component: 'Select',
    componentProps: { class: 'w-full', mode: 'multiple', options: [], placeholder: '请选择角色' },
    dependencies: { show: (values) => values.receiverScope === 2, triggerFields: ['receiverScope'] },
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
      treeData: [],
    },
    dependencies: { show: (values) => values.receiverScope === 3, triggerFields: ['receiverScope'] },
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
    label: '推送方式',
    rules: requiredNumber('推送方式不能为空'),
  },
  {
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
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
    dependencies: { show: (values) => values.channels?.includes('IN_APP'), triggerFields: ['channels'] },
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
    component: 'Input',
    dependencies: { show: (values) => values.channels?.includes('APP_PUSH'), triggerFields: ['channels'] },
    fieldName: 'appPushTitle',
    label: 'App主标题',
    rules: optionalString(255, 'App主标题长度不能超过255个字符'),
  },
  {
    component: 'Input',
    dependencies: { show: (values) => values.channels?.includes('APP_PUSH'), triggerFields: ['channels'] },
    fieldName: 'appPushSubtitle',
    label: 'App副标题',
    rules: optionalString(255, 'App副标题长度不能超过255个字符'),
  },
  {
    component: 'Input',
    dependencies: { show: (values) => values.channels?.includes('APP_PUSH'), triggerFields: ['channels'] },
    fieldName: 'appPushImageUrl',
    label: 'App大图URL',
    rules: optionalString(500, 'App大图URL长度不能超过500个字符'),
  },
  {
    component: 'Input',
    dependencies: { show: (values) => values.channels?.includes('APP_PUSH'), triggerFields: ['channels'] },
    fieldName: 'appPushJumpUrl',
    label: 'App跳转链接',
    rules: optionalString(500, 'App跳转链接长度不能超过500个字符'),
  },
  {
    component: 'Textarea',
    componentProps: { rows: 4 },
    dependencies: { show: (values) => values.channels?.includes('SMS'), triggerFields: ['channels'] },
    fieldName: 'smsContent',
    label: '短信内容',
    rules: optionalString(5000, '短信内容长度不能超过5000个字符'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => values.channels?.includes('WECHAT_MP') || values.channels?.includes('DOUYIN_MP'),
      triggerFields: ['channels'],
    },
    fieldName: 'miniProgramTemplateParams',
    label: '小程序参数',
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
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '系统消息' : '业务消息'),
      title: '类型',
      width: 100,
    },
    {
      field: 'receiverType',
      formatter: ({ cellValue }: any) =>
        cellValue === 'user' ? '前端用户' : '管理后台用户',
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
