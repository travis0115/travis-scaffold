<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { SystemMessageApi, SystemUserApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { Divider } from 'antdv-next';

import { useVbenForm, z } from '#/adapter/form';
import {
  createMessage,
  getAppUserOptionsByIds,
  getDeptTree,
  getMessageDetail,
  getMessageTemplateDetail,
  getRoleList,
  getUserOptionsByIds,
  updateMessage,
} from '#/api';
import { isDeptEnabled } from '#/features';

import { useFormSchema } from './data';
import TemplateSelectorModalComponent from './modules/template-selector-modal.vue';
import UserSelectorModalComponent from './modules/user-selector-modal.vue';
import {
  getMessageTemplateParamTypeLabel,
  validateMessageTemplateParamValue,
} from './param-types';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.Message>();
const templateOptions = ref<SystemMessageApi.MessageTemplate[]>([]);
const templateParamRows = ref<TemplateParamRow[]>([]);
const selectedTemplateId = ref<number | string>();
const deptTreeData = ref<any[]>([]);
const selectedUserIds = ref<Array<number | string>>([]);
const selectedUserOptions = ref<SystemUserApi.UserOption[]>([]);
const paramFormOptions = {
  handleValuesChange: () => {
    const values = paramFormApi.form.values;
    templateParamRows.value = templateParamRows.value.map((row) => ({
      ...row,
      value:
        values[row.key] === undefined || values[row.key] === null
          ? ''
          : String(values[row.key]),
    }));
  },
  schema: [] as VbenFormSchema[],
  showDefaultActions: false,
};
const [Form, formApi] = useVbenForm({
  schema: useFormSchema(
    handleReceiverTypeChange,
    handleChannelChange,
    openTemplateSelector,
    openUserSelector,
  ),
  showDefaultActions: false,
});
const [ParamForm, paramFormApi] = useVbenForm(paramFormOptions);
const [TemplateSelectorModal, templateSelectorModalApi] = useVbenModal({
  connectedComponent: TemplateSelectorModalComponent,
  zIndex: 2200,
});
const [UserSelectorModal, userSelectorModalApi] = useVbenModal({
  connectedComponent: UserSelectorModalComponent,
  zIndex: 2200,
});

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

function trimDeptTree(departments: any[]): any[] {
  return departments.map((item) => ({
    ...item,
    children: item.children ? trimDeptTree(item.children) : item.children,
    deptName:
      typeof item.deptName === 'string' ? item.deptName.trim() : item.deptName,
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

function formatUserOption(item: SystemUserApi.UserOption, receiverType: string) {
  const name = item.nickname || item.username;
  const suffix =
    receiverType === 'app'
      ? item.username || item.mobile
      : item.username;
  return {
    label: suffix && suffix !== name ? `${name}（${suffix}）` : name,
    value: item.id,
  };
}

async function setSelectedUsers(
  options: SystemUserApi.UserOption[],
  selectedIds?: Array<number | string>,
) {
  const values = await formApi.getValues();
  selectedUserOptions.value = options;
  const finalIds = selectedIds ?? options.map((item) => item.id);
  selectedUserIds.value = [...finalIds];
  const display =
    options.length === finalIds.length
      ? options
          .map((item) => formatUserOption(item, values.receiverType).label)
          .join('、')
      : `已选择 ${finalIds.length} 个用户`;
  await formApi.setValues(
    {
      userDisplay: display,
      userIds: finalIds,
    },
    true,
    false,
  );
  formApi.form.setFieldError('userDisplay', undefined);
}

async function openUserSelector() {
  const values = await formApi.getValues();
  formApi.form.setFieldError('userDisplay', undefined);
  userSelectorModalApi
    .setData({
      receiverType: values.receiverType,
      selectedIds: selectedUserIds.value,
      selectedOptions: selectedUserOptions.value,
    })
    .open();
}

async function handleReceiverTypeChange(_value: unknown) {
  const values = await formApi.getValues();
  selectedUserIds.value = [];
  selectedUserOptions.value = [];
  await formApi.setValues(
    {
      receiverScope: [0, 1].includes(values.receiverScope)
        ? values.receiverScope
        : 0,
      userDisplay: '',
      userIds: [],
    },
    true,
    false,
  );
  formApi.form.setFieldError('userDisplay', undefined);
}

function toFormValues(detail: SystemMessageApi.Message) {
  const channel = detail.channel || 'IN_APP';
  return {
    ...detail,
    channel,
    inAppContent: channel === 'IN_APP' ? detail.content : undefined,
    plainContent: channel === 'IN_APP' ? undefined : detail.content,
    publishTime:
      typeof detail.publishTime === 'string'
        ? detail.publishTime.slice(0, 16)
        : detail.publishTime,
  };
}

function cleanFormOnlyFields(data: Record<string, any>) {
  [
    'deptIds',
    'inAppContent',
    'plainContent',
    'roleIds',
    'templateName',
    'userDisplay',
    'userIds',
  ].forEach((key) => delete data[key]);
}

function parseJsonObject(value?: string) {
  if (!value) return undefined;
  try {
    const parsed = JSON.parse(value);
    return parsed !== null &&
      typeof parsed === 'object' &&
      !Array.isArray(parsed)
      ? (parsed as Record<string, any>)
      : undefined;
  } catch {
    return undefined;
  }
}

function schemaToParamRows(
  contentSchema?: string,
  values?: Record<string, any>,
) {
  const schema = parseJsonObject(contentSchema);
  if (!schema) return [];
  return Object.entries(schema).map(([key, item]) => {
    const config =
      item && typeof item === 'object' && !Array.isArray(item) ? item : {};
    return {
      description: config.description ? String(config.description) : undefined,
      key,
      label: config.label ? String(config.label) : key,
      required: config.required !== false,
      type: config.type ? String(config.type) : 'text',
      value:
        values?.[key] === undefined || values?.[key] === null
          ? ''
          : String(values[key]),
    };
  });
}

function updateTemplateParamRows(
  templateId?: number | string,
  params?: string,
) {
  selectedTemplateId.value = templateId;
  const template = templateOptions.value.find(
    (item) => String(item.id) === String(templateId),
  );
  templateParamRows.value = template
    ? schemaToParamRows(template.contentSchema, parseJsonObject(params))
    : [];
  updateParamFormSchema(templateParamRows.value);
}

function updateParamFormSchema(rows: TemplateParamRow[]) {
  paramFormApi.setState({
    schema: rows.map((row) => ({
      component: getTemplateParamComponent(row.type),
      componentProps: getTemplateParamComponentProps(row),
      description: row.description,
      fieldName: row.key,
      label: row.label,
      rules: createTemplateParamRule(row),
    })),
  });
  void nextTick(async () => {
    await paramFormApi.setValues(
      Object.fromEntries(rows.map((row) => [row.key, row.value])),
    );
    rows
      .filter((row) => row.type === 'date' || row.type === 'datetime')
      .forEach((row) => {
        const component = paramFormApi.getFieldComponentRef<any>(row.key);
        const root =
          component instanceof HTMLElement ? component : component?.$el;
        const input = root?.querySelector?.('input') as
          | HTMLInputElement
          | undefined;
        if (input) input.readOnly = true;
      });
  });
}

function getTemplateParamComponent(type: string) {
  if (type === 'number' || type === 'amount') return 'InputNumber';
  if (type === 'date' || type === 'datetime') return 'DatePicker';
  return 'Input';
}

function getTemplateParamComponentProps(row: TemplateParamRow) {
  const placeholder = `请输入${row.label}（${getMessageTemplateParamTypeLabel(row.type)}）`;
  const preventManualDateInput = (event: Event) => event.preventDefault();
  switch (row.type) {
    case 'amount': {
      return {
        class: 'w-full',
        placeholder,
        precision: 2,
        step: '0.01',
        stringMode: true,
      };
    }
    case 'number': {
      return { class: 'w-full', placeholder, stringMode: true };
    }
    case 'date': {
      return {
        class: 'w-full',
        inputReadOnly: true,
        onKeyDown: preventManualDateInput,
        onPaste: preventManualDateInput,
        placeholder,
        readonly: true,
        valueFormat: 'YYYY-MM-DD',
      };
    }
    case 'datetime': {
      return {
        class: 'w-full',
        inputReadOnly: true,
        onKeyDown: preventManualDateInput,
        onPaste: preventManualDateInput,
        placeholder,
        readonly: true,
        showTime: true,
        valueFormat: BACKEND_DATETIME_FORMAT,
      };
    }
    case 'email': {
      return { placeholder, type: 'email' };
    }
    case 'mobile': {
      return { inputmode: 'numeric', maxlength: 11, placeholder, type: 'tel' };
    }
    case 'url': {
      return { placeholder, type: 'url' };
    }
    default: {
      return { placeholder };
    }
  }
}

function createTemplateParamRule(row: TemplateParamRow) {
  return z.preprocess(
    (value) => (value === undefined || value === null ? '' : String(value)),
    z.string().superRefine((value, context) => {
      const paramValue =
        value === undefined || value === null ? '' : String(value).trim();
      if (row.required && !paramValue) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          message: `请输入${row.label}`,
        });
        return;
      }
      const result = validateMessageTemplateParamValue(row.type, paramValue);
      if (result !== true) {
        context.addIssue({ code: z.ZodIssueCode.custom, message: result });
      }
    }),
  );
}

function escapeHtml(value: string) {
  return value.replace(/[&<>'"]/g, (character) => {
    return (
      { '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[
        character
      ] || character
    );
  });
}

const templatePreviewContent = computed(() => {
  const template = templateOptions.value.find(
    (item) => String(item.id) === String(selectedTemplateId.value),
  );
  if (!template?.content) return '';
  const params = Object.fromEntries(
    templateParamRows.value.map((row) => [row.key, row.value]),
  );
  return template.content.replace(/\{\{\s*([^{}]+?)\s*}}/g, (_, key) =>
    escapeHtml(String(params[key.trim()] ?? '')),
  );
});

const templatePreviewTitle = computed(() => {
  const template = templateOptions.value.find(
    (item) => String(item.id) === String(selectedTemplateId.value),
  );
  return template?.title || template?.templateName || '';
});

function buildTemplateParams() {
  const params = Object.fromEntries(
    templateParamRows.value.map((row) => [row.key, row.value ?? '']),
  );
  return JSON.stringify(params, null, 2);
}

async function buildTemplateParamsForSubmit() {
  const values = await paramFormApi.getValues();
  return JSON.stringify(
    Object.fromEntries(
      templateParamRows.value.map((row) => [row.key, values[row.key] ?? '']),
    ),
    null,
    2,
  );
}

async function validateTemplateParams() {
  let valid = true;
  templateParamRows.value.forEach((row) => {
    const value = row.value.trim();
    let error: string | undefined;
    if (row.required && !value) {
      error = `请输入${row.label}`;
    } else {
      const result = validateMessageTemplateParamValue(row.type, value);
      if (result !== true) error = result;
    }
    paramFormApi.form.setFieldError(row.key, error);
    if (error) valid = false;
  });
  return valid;
}

async function openTemplateSelector() {
  const values = await formApi.getValues();
  templateSelectorModalApi
    .setData({ channel: values.channel, selectedId: selectedTemplateId.value })
    .open();
}

async function handleChannelChange(value: unknown) {
  const channel = typeof value === 'string' ? value : 'IN_APP';
  templateOptions.value = [];
  updateTemplateParamRows();
  await formApi.setFieldValue('templateId', undefined, false);
  await nextTick();
  await formApi.setValues(
    {
      inAppContent: undefined,
      jumpUrl: undefined,
      plainContent: undefined,
      templateId: undefined,
      templateName: '',
      templateParams: undefined,
      title: undefined,
    },
    true,
    false,
  );
  formApi.form.setFieldError('inAppContent', undefined);
  formApi.form.setFieldError('plainContent', undefined);
  formApi.form.setFieldError('templateId', undefined);
  formApi.form.setFieldError('templateName', undefined);
  formApi.form.setFieldError('title', undefined);
  await formApi.setFieldValue('channel', channel, false);
}

async function handleTemplateSelected(
  template?: SystemMessageApi.MessageTemplate,
) {
  if (!template) {
    templateOptions.value = [];
    updateTemplateParamRows();
    await formApi.setFieldValue('templateId', undefined, false);
    await nextTick();
    await formApi.setValues(
      {
        inAppContent: undefined,
        jumpUrl: undefined,
        plainContent: undefined,
        templateId: undefined,
        templateName: undefined,
        templateParams: undefined,
        title: undefined,
      },
      true,
      false,
    );
    return;
  }
  templateOptions.value = [template];
  updateTemplateParamRows(template.id);
  await formApi.setFieldValue('templateId', template.id, false);
  await nextTick();
  await formApi.setValues(
    {
      inAppContent:
        template.channel === 'IN_APP' ? template.content : undefined,
      jumpUrl: template.redirectUrl,
      plainContent:
        template.channel === 'IN_APP' ? undefined : template.content,
      templateId: template.id,
      templateName: `${template.templateName}（${template.templateCode}）`,
      templateParams: buildTemplateParams(),
      title: template.title || template.templateName,
    },
    true,
    false,
  );
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    if (values.channel !== 'IN_APP' && !selectedTemplateId.value) {
      formApi.form.setFieldError(
        'templateName',
        '短信和微信通道必须选择消息模板',
      );
      return;
    }
    formApi.form.setFieldError('templateName', undefined);
    if (selectedTemplateId.value) {
      const paramValid = await validateTemplateParams();
      if (!paramValid) return;
      values.templateId = selectedTemplateId.value;
      values.templateParams = await buildTemplateParamsForSubmit();
    } else {
      values.templateId = undefined;
      values.templateParams = undefined;
    }
    const receiverType = values.receiverType;
    const selectedReceiverScope = Number(values.receiverScope);
    const receiverScope =
      receiverType === 'app' && ![0, 1].includes(selectedReceiverScope)
        ? 0
        : selectedReceiverScope;
    const receiverField = receiverScopeFieldMap[receiverScope];
    let receiverValues: number[] = [];
    if (receiverField === 'deptIds') {
      receiverValues = normalizeDeptIds(values.deptIds);
    } else if (receiverField) {
      receiverValues = values[receiverField] || [];
    }
    const data: Record<string, any> = {
      ...values,
      content:
        values.channel === 'IN_APP'
          ? values.inAppContent
          : values.plainContent,
      receiverScope,
      receiverType,
      receiverValues: receiverField ? receiverValues : [],
    };
    if (values.pushType === 0) {
      delete data.publishTime;
    } else if (typeof data.publishTime === 'string' && data.publishTime.length === 16) {
      data.publishTime = `${data.publishTime}:00`;
    }
    cleanFormOnlyFields(data);
    await (formData.value?.id
      ? updateMessage(formData.value.id, data)
      : createMessage(data));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    selectedUserIds.value = [];
    selectedUserOptions.value = [];
    const data = drawerApi.getData<SystemMessageApi.Message>();
    formApi.resetForm();
    formData.value = data;
    formApi.updateSchema([
      {
        fieldName: 'status',
        hide: Boolean(data?.id),
      },
    ]);
    const [roles, departments] = await Promise.all([
      getRoleList(),
      isDeptEnabled() ? getDeptTree() : Promise.resolve([]),
    ]);
    deptTreeData.value = trimDeptTree(departments);
    formApi.updateSchema([
      {
        componentProps: {
          optionFilterProp: 'label',
          options: roles.map((item) => ({
            label: item.roleName,
            value: item.id,
          })),
          showSearch: true,
        },
        fieldName: 'roleIds',
      },
      {
        componentProps: { treeData: deptTreeData.value },
        fieldName: 'deptIds',
      },
    ]);
    if (data?.id) {
      const detail = await getMessageDetail(data.id);
      const receiverField = receiverScopeFieldMap[detail.receiverScope];
      const template = detail.templateId
        ? await getMessageTemplateDetail(detail.templateId)
        : undefined;
      if (template) templateOptions.value = [template];
      await formApi.setValues({
        ...toFormValues(detail),
        templateName: template
          ? `${template.templateName}（${template.templateCode}）`
          : undefined,
        ...(receiverField ? { [receiverField]: detail.receiverValues } : {}),
      });
      if (receiverField === 'userIds' && detail.receiverValues?.length) {
        const options =
          detail.receiverType === 'app'
            ? await getAppUserOptionsByIds(detail.receiverValues)
            : await getUserOptionsByIds(detail.receiverValues);
        await setSelectedUsers(options);
      }
      updateTemplateParamRows(detail.templateId, detail.templateParams);
      if (template && detail.templateId) {
        await nextTick();
        await formApi.setValues(
          {
            templateId: detail.templateId,
            templateName: `${template.templateName}（${template.templateCode}）`,
            templateParams: detail.templateParams,
          },
          true,
          false,
        );
      }
    } else {
      formData.value = undefined;
      templateParamRows.value = [];
      selectedTemplateId.value = undefined;
      await formApi.setValues({
        channel: 'IN_APP',
        pushType: 0,
        receiverScope: 0,
        receiverType: 'admin',
      });
    }
  },
});
</script>

<template>
  <TemplateSelectorModal @success="handleTemplateSelected" />
  <UserSelectorModal @success="setSelectedUsers" />
  <Drawer
    class="w-full max-w-220"
    :title="formData?.id ? '编辑消息推送' : '新增消息推送'"
  >
    <Form />
    <div v-if="templateParamRows.length > 0" class="mt-2">
      <Divider>模板参数</Divider>
      <ParamForm />
    </div>
    <div v-if="selectedTemplateId" class="mt-2">
      <Divider>消息预览</Divider>
      <div class="px-3 py-2">
        <p class="text-base font-semibold">{{ templatePreviewTitle }}</p>
        <div
          class="prose mt-3 max-w-none text-sm leading-6"
          v-html="templatePreviewContent"
        ></div>
      </div>
    </div>
  </Drawer>
</template>
