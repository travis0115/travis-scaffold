<script lang="ts" setup>
import type { SystemMessageApi, SystemUserApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { message } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import {
  createMessage,
  getDeptTree,
  getAppUserOptions,
  getAppUserOptionsByIds,
  getMessageDetail,
  getMessageTemplatePage,
  getRoleList,
  getUserOptions,
  getUserOptionsByIds,
  updateMessage,
} from '#/api';
import { isDeptEnabled } from '#/features';

import { useFormSchema } from './data';
import {
  getMessageTemplateParamTypeLabel,
  validateMessageTemplateParamValue,
} from './param-types';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.Message>();
const templateOptions = ref<SystemMessageApi.MessageTemplate[]>([]);
const templateParamRows = ref<TemplateParamRow[]>([]);
const deptTreeData = ref<any[]>([]);
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });

type TemplateParamRow = {
  description?: string;
  key: string;
  label: string;
  required: boolean;
  type: string;
  value: string;
};
const receiverScopeFieldMap: Record<number, string> = {
  1: 'userIds',
  2: 'roleIds',
  3: 'deptIds',
};

function effectiveReceiverType(values: Record<string, any>) {
  return values.receiverType;
}

function trimDeptTree(departments: any[]): any[] {
  return departments.map((item) => ({
    ...item,
    children: item.children ? trimDeptTree(item.children) : item.children,
    deptName: typeof item.deptName === 'string' ? item.deptName.trim() : item.deptName,
  }));
}

function collectDescendantIds(node: any, ids: Set<number>) {
  node.children?.forEach((child: any) => {
    ids.add(child.id);
    collectDescendantIds(child, ids);
  });
}

function normalizeDeptIds(ids: number[] = []) {
  const selected = new Set(ids);
  const descendants = new Set<number>();
  const visit = (nodes: any[]) => {
    nodes.forEach((node) => {
      if (selected.has(node.id)) {
        collectDescendantIds(node, descendants);
      }
      if (node.children?.length) {
        visit(node.children);
      }
    });
  };
  visit(deptTreeData.value);
  return ids.filter((id) => !descendants.has(id));
}

function formatUserOption(item: SystemUserApi.UserOption) {
  const name = item.nickname || item.username;
  const suffix = item.deptName || item.mobile || item.username;
  return { label: suffix && suffix !== name ? `${name}（${suffix}）` : name, value: item.id };
}

function userSelectProps(options: SystemUserApi.UserOption[] = []) {
  return {
    class: 'w-full',
    filterOption: false,
    mode: 'multiple',
    onSearch: handleUserSearch,
    options: options.map(formatUserOption),
    placeholder: '请输入用户名/昵称/手机号搜索',
    showSearch: true,
  };
}

function updateUserOptions(options: SystemUserApi.UserOption[] = []) {
  formApi.updateSchema([
    {
      componentProps: userSelectProps(options),
      fieldName: 'userIds',
    },
  ]);
}

async function fetchUserOptionsByIds(receiverType: string, ids: number[]) {
  if (ids.length === 0) return [];
  return receiverType === 'app'
    ? await getAppUserOptionsByIds(ids)
    : await getUserOptionsByIds(ids);
}

async function handleUserSearch(keyword: string) {
  if (!keyword?.trim()) {
    updateUserOptions();
    return;
  }
  const values = await formApi.getValues();
  const receiverType = effectiveReceiverType(values);
  const options =
    receiverType === 'app'
      ? await getAppUserOptions({ keyword, limit: 20 })
      : await getUserOptions({ keyword, limit: 20 });
  updateUserOptions(options);
}

function buildChannelContents(values: Record<string, any>) {
  const channel = values.channel || 'IN_APP';
  const templateId = values.templateId;
  const templateParams = values.templateParams;
  const template = templateOptions.value.find((item) => item.id === templateId);
  const contents: SystemMessageApi.MessageChannelContent[] = [];
  if (channel === 'IN_APP') {
    contents.push({
      channel: 'IN_APP',
      content: values.inAppContent,
      jumpUrl: template?.redirectUrl,
      templateId,
      templateParams,
    });
  }
  if (channel === 'SMS') {
    contents.push({
      channel: 'SMS',
      content: values.smsContent,
      templateId,
      templateParams,
      wordCount: values.smsContent?.length || 0,
    });
  }
  if (channel === 'WECHAT_MP') {
    contents.push({
      channel: 'WECHAT_MP',
      jumpUrl: template?.redirectUrl,
      templateId,
      templateParams,
    });
  }
  if (channel === 'WECHAT_OA') {
    contents.push({
      channel: 'WECHAT_OA',
      jumpUrl: template?.redirectUrl,
      templateId,
      templateParams,
    });
  }
  return contents;
}

function toFormValues(detail: SystemMessageApi.Message) {
  const channelContents = detail.channelContents || [];
  const contentMap = new Map(channelContents.map((item) => [item.channel, item]));
  const channel = detail.channel || channelContents[0]?.channel || 'IN_APP';
  const channelContent = contentMap.get(channel);
  return {
    ...detail,
    channel,
    templateId: channelContent?.templateId,
    inAppContent: contentMap.get('IN_APP')?.content || detail.content,
    templateParams:
      contentMap.get('IN_APP')?.templateParams ||
      contentMap.get('SMS')?.templateParams ||
      contentMap.get('WECHAT_MP')?.templateParams ||
      contentMap.get('WECHAT_OA')?.templateParams,
    smsContent: contentMap.get('SMS')?.content,
  };
}

function cleanFormOnlyFields(data: Record<string, any>) {
  [
    'deptIds',
    'inAppContent',
    'roleIds',
    'smsContent',
    'templateId',
    'templateParams',
    'userIds',
  ].forEach((key) => delete data[key]);
}

function parseJsonObject(value?: string) {
  if (!value) return undefined;
  try {
    const parsed = JSON.parse(value);
    return parsed !== null && typeof parsed === 'object' && !Array.isArray(parsed)
      ? (parsed as Record<string, any>)
      : undefined;
  } catch {
    return undefined;
  }
}

function schemaToParamRows(contentSchema?: string, values?: Record<string, any>) {
  const schema = parseJsonObject(contentSchema);
  if (!schema) return [];
  return Object.entries(schema).map(([key, item]) => {
    const config = item && typeof item === 'object' && !Array.isArray(item) ? item : {};
    return {
      description: config.description ? String(config.description) : undefined,
      key,
      label: config.label ? String(config.label) : key,
      required: config.required !== false,
      type: config.type ? String(config.type) : 'text',
      value: values?.[key] === undefined || values?.[key] === null ? '' : String(values[key]),
    };
  });
}

function updateTemplateParamRows(templateId?: number, params?: string) {
  const template = templateOptions.value.find((item) => item.id === templateId);
  templateParamRows.value = template
    ? schemaToParamRows(template.contentSchema, parseJsonObject(params))
    : [];
}

function buildTemplateParams() {
  const params = Object.fromEntries(
    templateParamRows.value.map((row) => [row.key, row.value ?? '']),
  );
  return JSON.stringify(params, null, 2);
}

function validateTemplateParams() {
  const missingRows = templateParamRows.value.filter(
    (row) => row.required && !String(row.value ?? '').trim(),
  );
  if (missingRows.length > 0) {
    message.error(`请填写模板参数：${missingRows.map((row) => row.label).join('、')}`);
    return false;
  }
  const invalidRow = templateParamRows.value.find((row) => {
    const result = validateMessageTemplateParamValue(row.type, String(row.value ?? '').trim());
    return result !== true;
  });
  if (!invalidRow) return true;
  const result = validateMessageTemplateParamValue(
    invalidRow.type,
    String(invalidRow.value ?? '').trim(),
  );
  message.error(`${invalidRow.label}：${result}`);
  return false;
}

async function loadTemplates() {
  const page = await getMessageTemplatePage({
    pageNum: 1,
    pageSize: 100,
    status: 1,
  });
  templateOptions.value = page.records;
  formApi.updateSchema([
    {
      componentProps: {
        allowClear: true,
        filterOption: (input: string, option: any) =>
          String(option?.label ?? '').toLowerCase().includes(input.toLowerCase()),
        onChange: handleTemplateChange,
        options: page.records.map((item) => ({
          label: `${item.templateName}（${item.templateCode}）`,
          value: item.id,
        })),
        placeholder: '可选择模板快速填充内容',
        showSearch: true,
      },
      fieldName: 'templateId',
    },
  ]);
}

async function handleTemplateChange(templateId?: number) {
  const template = templateOptions.value.find((item) => item.id === templateId);
  updateTemplateParamRows(templateId);
  if (!template) {
    await formApi.setValues({ templateParams: undefined });
    return;
  }
  await formApi.setValues({
    channel: template.channel,
    inAppContent: template.channel === 'IN_APP' ? template.content : undefined,
    smsContent: template.channel === 'SMS' ? template.content : undefined,
    templateParams: buildTemplateParams(),
    title: template.title || template.templateName,
  });
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    if (values.templateId && !validateTemplateParams()) return;
    values.templateParams = values.templateId ? buildTemplateParams() : undefined;
    const channel = values.channel || 'IN_APP';
    const receiverType = values.receiverType;
    const receiverScope =
      receiverType === 'app' && ![0, 1].includes(values.receiverScope)
        ? 0
        : values.receiverScope;
    const receiverField = receiverScopeFieldMap[receiverScope];
    let receiverValues: number[] = [];
    if (receiverField === 'deptIds') {
      receiverValues = normalizeDeptIds(values.deptIds);
    } else if (receiverField) {
      receiverValues = values[receiverField] || [];
    }
    const data: Record<string, any> = {
      ...values,
      channel,
      channelContents: buildChannelContents(values),
      content: values.inAppContent || values.smsContent || values.title,
      enableInboxCopy: channel === 'IN_APP' || Boolean(values.enableInboxCopy),
      receiverScope,
      receiverType,
      receiverValues: receiverField ? receiverValues : [],
      sourceType: 'MANUAL',
    };
    if (values.pushType === 0) {
      delete data.publishTime;
    }
    cleanFormOnlyFields(data);
    await (formData.value?.id ? updateMessage(formData.value.id, data) : createMessage(data));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemMessageApi.Message>();
    formApi.resetForm();
    formData.value = data;
    formApi.updateSchema([
      {
        fieldName: 'status',
        hide: Boolean(data?.id),
      },
    ]);
    updateUserOptions();
    const [roles, departments] = await Promise.all([
      getRoleList(),
      isDeptEnabled() ? getDeptTree() : Promise.resolve([]),
      loadTemplates(),
    ]);
    deptTreeData.value = trimDeptTree(departments);
    formApi.updateSchema([
      {
        componentProps: { options: roles.map((item) => ({ label: item.roleName, value: item.id })) },
        fieldName: 'roleIds',
      },
      { componentProps: { treeData: deptTreeData.value }, fieldName: 'deptIds' },
    ]);
    if (data?.id) {
      const detail = await getMessageDetail(data.id);
      const receiverField = receiverScopeFieldMap[detail.receiverScope];
      if (receiverField === 'userIds' && detail.receiverValues?.length) {
        updateUserOptions(
          await fetchUserOptionsByIds(
            effectiveReceiverType(detail),
            detail.receiverValues,
          ),
        );
      }
      await formApi.setValues({
        ...toFormValues(detail),
        ...(receiverField ? { [receiverField]: detail.receiverValues } : {}),
      });
      const values = await formApi.getValues();
      updateTemplateParamRows(values.templateId, values.templateParams);
    } else {
      formData.value = undefined;
      templateParamRows.value = [];
      await formApi.setValues({
        channel: 'IN_APP',
        enableInboxCopy: true,
        pushType: 0,
        receiverScope: 0,
        receiverType: 'admin',
        sourceType: 'MANUAL',
      });
    }
  },
});
</script>

<template>
  <Drawer class="w-full max-w-220" :title="formData?.id ? '编辑消息推送' : '新增消息推送'">
    <Form />
    <div v-if="templateParamRows.length > 0" class="mt-4 rounded border p-3">
      <div class="mb-3 text-sm font-medium">模板参数</div>
      <div class="grid gap-3">
        <div
          v-for="row in templateParamRows"
          :key="row.key"
          class="grid gap-2 md:grid-cols-[160px_1fr]"
        >
          <div class="pt-1.5 text-sm text-gray-600">
            <span>{{ row.label }}</span>
            <span v-if="row.required" class="ml-1 text-red-500">*</span>
          </div>
          <div>
            <input
              v-model="row.value"
              class="w-full rounded border px-3 py-1.5 text-sm"
              :placeholder="`请输入${row.label}（${getMessageTemplateParamTypeLabel(row.type)}）`"
            />
            <div v-if="row.description" class="mt-1 text-xs text-gray-400">
              {{ row.description }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </Drawer>
</template>
