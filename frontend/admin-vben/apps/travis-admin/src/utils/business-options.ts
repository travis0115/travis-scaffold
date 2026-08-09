import { getTranslatedOptions } from './dict';

export const enableStatusOptions = getTranslatedOptions('enable_status', [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
]);

export const operationStatusOptions = getTranslatedOptions(
  'operation_status',
  [
    { label: '失败', value: 0 },
    { label: '成功', value: 1 },
  ],
);

export const menuTypeOptions = getTranslatedOptions('sys_menu_type', [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
]);

export const messageStatusOptions = getTranslatedOptions(
  'sys_message_status',
  [
    { label: '待推送', value: 0 },
    { label: '已推送', value: 1 },
    { label: '已撤回', value: 2 },
  ],
);

export const manualMessagePushTypeOptions = getTranslatedOptions(
  'sys_message_push_type',
  [
    { label: '手动推送', value: 0 },
    { label: '定时推送', value: 1 },
  ],
);

export const messageChannelOptions = getTranslatedOptions(
  'sys_message_channel',
  [{ label: '站内信', value: 'IN_APP' }],
);

export const messageTypeOptions = getTranslatedOptions('sys_message_type', [
  { label: '系统消息', value: 1 },
  { label: '系统公告', value: 2 },
  { label: '版本更新', value: 3 },
]);

export const messageReadStatusOptions = getTranslatedOptions(
  'sys_message_read_status',
  [
    { label: '未读', value: 0 },
    { label: '已读', value: 1 },
  ],
);

export const publishStatusOptions = getTranslatedOptions(
  'sys_publish_status',
  [
    { label: '草稿', value: 0 },
    { label: '已发布', value: 1 },
    { label: '已撤回', value: 2 },
  ],
);

export const editablePublishStatusOptions = getTranslatedOptions(
  'sys_publish_status',
  [
    { label: '草稿', value: 0 },
    { label: '已发布', value: 1 },
  ],
);

export const operationBusinessTypeOptions = getTranslatedOptions(
  'operation_business_type',
  [
    { label: '新增', value: 'CREATE' },
    { label: '修改', value: 'UPDATE' },
    { label: '删除', value: 'DELETE' },
    { label: '授权', value: 'GRANT' },
    { label: '上传', value: 'UPLOAD' },
    { label: '导入', value: 'IMPORT' },
    { label: '导出', value: 'EXPORT' },
    { label: '其他', value: 'OTHER' },
  ],
);

export const httpMethodOptions = getTranslatedOptions('http_method', [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'DELETE', value: 'DELETE' },
]);
