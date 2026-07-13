import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { z } from '#/adapter/form';
import { uploadMessageImage } from '#/api';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';
import { messageChannelOptions } from '#/views/system/message/data';

const requiredString = (message: string) =>
  z.string({ required_error: message }).min(1, message);

const optionalString = (max: number, message: string) =>
  z.string().max(max, message).optional().or(z.literal(''));

const externalChannels = ['SMS', 'WECHAT_MP', 'WECHAT_OA'];
const titleChannels = ['IN_APP', 'WECHAT_MP', 'WECHAT_OA'];
const redirectChannels = ['WECHAT_MP', 'WECHAT_OA'];

function isExternalChannel(channel?: string) {
  return channel ? externalChannels.includes(channel) : false;
}

function needsTitle(channel?: string) {
  return channel ? titleChannels.includes(channel) : false;
}

function needsRedirect(channel?: string) {
  return channel ? redirectChannels.includes(channel) : false;
}

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

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];

export const useFormSchema = (
  onInAppContentBlur?: () => void,
): VbenFormSchema[] => [
  {
    component: 'Input',
    fieldName: 'templateCode',
    label: '模板编码',
    rules: requiredString('模板编码不能为空').max(
      64,
      '模板编码长度不能超过64个字符',
    ),
  },
  {
    component: 'Input',
    fieldName: 'templateName',
    label: '模板名称',
    rules: requiredString('模板名称不能为空').max(
      100,
      '模板名称长度不能超过100个字符',
    ),
  },
  {
    component: 'Select',
    componentProps: { options: messageChannelOptions },
    fieldName: 'channel',
    label: '推送通道',
    rules: requiredString('推送通道不能为空').max(
      32,
      '推送通道长度不能超过32个字符',
    ),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => isExternalChannel(values.channel),
      triggerFields: ['channel'],
    },
    fieldName: 'platformTemplateId',
    label: '平台模板ID',
    rules: requiredString('平台模板ID不能为空').max(
      128,
      '平台模板ID长度不能超过128个字符',
    ),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => needsRedirect(values.channel),
      triggerFields: ['channel'],
    },
    fieldName: 'redirectUrl',
    label: '跳转地址',
    rules: optionalString(500, '跳转地址长度不能超过500个字符'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => needsTitle(values.channel),
      triggerFields: ['channel'],
    },
    fieldName: 'title',
    label: '模板标题',
    rules: requiredString('模板标题不能为空').max(
      255,
      '模板标题长度不能超过255个字符',
    ),
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
      onBlur: onInAppContentBlur,
      placeholder:
        '请输入模板内容，可使用 {{name}} 格式引用模板参数，例如 {{nickname}}',
    },
    dependencies: {
      show: (values) => values.channel === 'IN_APP',
      triggerFields: ['channel'],
    },
    fieldName: 'inAppContent',
    formFieldProps: {
      validateOnBlur: false,
      validateOnChange: false,
      validateOnInput: false,
      validateOnModelUpdate: false,
    },
    label: '模板内容',
    rules: z
      .string({ required_error: '模板内容不能为空' })
      .max(5000, '模板内容长度不能超过5000个字符')
      .refine(hasRichTextContent, '模板内容不能为空'),
  },
  {
    component: 'Textarea',
    componentProps: { rows: 5 },
    dependencies: {
      show: (values) => isExternalChannel(values.channel),
      triggerFields: ['channel'],
    },
    fieldName: 'content',
    label: '模板内容',
    rules: requiredString('模板内容不能为空').max(
      5000,
      '模板内容长度不能超过5000个字符',
    ),
  },
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
  const isBuiltin = (
    row: Pick<SystemMessageApi.MessageTemplate, 'isBuiltin'>,
  ) => row.isBuiltin === 1;

  return [
    { field: 'templateCode', minWidth: 180, title: '模板编码' },
    { field: 'templateName', minWidth: 180, title: '模板名称' },
    {
      cellRender: {
        attrs: { dictCode: 'sys_message_channel' },
        name: 'CellTag',
      },
      field: 'channel',
      title: '推送通道',
      width: 130,
    },
    {
      cellRender: {
        attrs: { dictCode: 'sys_config_type' },
        name: 'CellTag',
      },
      field: 'isBuiltin',
      title: '模板类型',
      width: 110,
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
    {
      field: 'createTime',
      formatter: 'formatDateTime',
      title: '创建时间',
      width: 180,
    },
    {
      cellRender: {
        attrs: {
          nameField: 'templateName',
          nameTitle: '消息模板',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            'edit',
            {
              code: 'delete',
              show: (row: SystemMessageApi.MessageTemplate) => !isBuiltin(row),
            },
          ],
          {
            delete: SYSTEM_PERMS.messageTemplateDelete,
            edit: SYSTEM_PERMS.messageTemplateUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 130,
    },
  ];
}
