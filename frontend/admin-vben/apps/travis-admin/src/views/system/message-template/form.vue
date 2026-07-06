<script lang="ts" setup>
import type { SystemMessageApi } from '#/api';

import { ref } from 'vue';

import { JsonViewer, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { Button, Checkbox, Input, message, RadioGroup, Select } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import {
  createMessageTemplate,
  getMessageTemplateDetail,
  updateMessageTemplate,
} from '#/api';

import { useFormSchema } from './data';
import {
  isMessageTemplateParamType,
  messageTemplateParamTypeOptions,
} from '../message/param-types';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.MessageTemplate>();
const contentSchemaPreview = ref<Record<string, any>>();
const remark = ref('');
const status = ref(1);
const variableRows = ref<VariableRow[]>([]);
const editingVariableRows = ref<VariableRow[]>([]);
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });
const [ParamModal, paramModalApi] = useVbenModal({
  onConfirm() {
    confirmParamConfig();
  },
});

type VariableRow = {
  description?: string;
  key: string;
  label?: string;
  required: boolean;
  type: string;
};

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];
const paramKeyPattern = /^[A-Za-z][A-Za-z0-9_]*$/;
const templateParamPattern = /\{\{\s*([^{}]+?)\s*}}/g;

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

function schemaToRows(value?: string) {
  const schema = parseJsonObject(value);
  if (!schema) return [];
  return Object.entries(schema).map(([key, item]) => {
    const config = item && typeof item === 'object' && !Array.isArray(item) ? item : {};
    return {
      description: config.description ? String(config.description) : undefined,
      key,
      label: config.label ? String(config.label) : undefined,
      required: config.required !== false,
      type: config.type ? String(config.type) : 'text',
    };
  });
}

function cloneRows(rows: VariableRow[]) {
  return rows.map((row) => ({ ...row }));
}

function rowsToSchema(rows = variableRows.value) {
  return Object.fromEntries(
    rows
      .map((row) => ({
        ...row,
        key: row.key.trim(),
      }))
      .filter((row) => row.key)
      .map((row) => [
        row.key,
        {
          ...(row.label?.trim() ? { label: row.label.trim() } : {}),
          type: row.type || 'text',
          required: row.required,
          ...(row.description?.trim() ? { description: row.description.trim() } : {}),
        },
      ]),
  );
}

function updateContentSchemaPreview() {
  const schema = rowsToSchema();
  contentSchemaPreview.value = Object.keys(schema).length > 0 ? schema : undefined;
}

function setVariableRows(rows: VariableRow[]) {
  variableRows.value = rows;
  updateContentSchemaPreview();
}

function addVariableRow() {
  editingVariableRows.value.push({
    key: '',
    required: true,
    type: 'text',
  });
}

function removeVariableRow(index: number) {
  editingVariableRows.value.splice(index, 1);
}

function validateVariableRows(rows = variableRows.value) {
  if (rows.some((row) => !row.key.trim())) {
    message.error('请填写参数名');
    return false;
  }
  if (rows.some((row) => !paramKeyPattern.test(row.key.trim()))) {
    message.error('参数名格式错误，仅支持英文字母开头，包含字母、数字、下划线');
    return false;
  }
  const keys = rows.map((row) => row.key.trim());
  if (keys.length !== new Set(keys).size) {
    message.error('参数名不能重复');
    return false;
  }
  if (rows.some((row) => !isMessageTemplateParamType(row.type))) {
    message.error('参数类型不支持');
    return false;
  }
  return true;
}

function buildContentSchema() {
  const schema = rowsToSchema();
  if (Object.keys(schema).length === 0) return undefined;
  try {
    return JSON.stringify(schema, null, 2);
  } catch {
    return '{}';
  }
}

function extractTemplateParamKeys(content?: string) {
  if (!content) return [];
  const keys: string[] = [];
  for (const match of content.matchAll(templateParamPattern)) {
    const key = match[1]?.trim();
    if (!key) continue;
    if (!paramKeyPattern.test(key)) {
      message.error(`模板参数引用格式错误：${key}`);
      return undefined;
    }
    keys.push(key);
  }
  return [...new Set(keys)];
}

function validateTemplateParamUsage(content?: string) {
  const contentKeys = extractTemplateParamKeys(content);
  if (!contentKeys) return false;
  const contentKeySet = new Set(contentKeys);
  const configuredKeys = variableRows.value.map((row) => row.key.trim());
  const configuredKeySet = new Set(configuredKeys);
  const missingKeys = contentKeys.filter((key) => !configuredKeySet.has(key));
  if (missingKeys.length > 0) {
    message.error(`模板内容中的参数未配置：${missingKeys.join('、')}`);
    return false;
  }
  const extraKeys = configuredKeys.filter((key) => !contentKeySet.has(key));
  if (extraKeys.length > 0) {
    message.error(`已配置但模板内容未引用的参数：${extraKeys.join('、')}`);
    return false;
  }
  return true;
}

function openParamModal() {
  editingVariableRows.value = cloneRows(variableRows.value);
  paramModalApi.open();
}

function confirmParamConfig() {
  if (!validateVariableRows(editingVariableRows.value)) return;
  setVariableRows(cloneRows(editingVariableRows.value));
  paramModalApi.close();
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    if (!validateVariableRows()) return;
    if (remark.value.length > 255) {
      message.error('备注长度不能超过255个字符');
      return;
    }
    const values = await formApi.getValues();
    values.contentSchema = buildContentSchema();
    values.remark = remark.value;
    values.status = status.value;
    if (values.channel === 'IN_APP') {
      values.content = values.inAppContent;
      values.platformTemplateId = undefined;
      values.redirectUrl = undefined;
    }
    if (values.channel === 'SMS') {
      values.title = undefined;
      values.redirectUrl = undefined;
    }
    if (!['WECHAT_MP', 'WECHAT_OA'].includes(values.channel)) {
      values.redirectUrl = undefined;
    }
    if (!validateTemplateParamUsage(values.content)) return;
    delete values.inAppContent;
    await (formData.value?.id
      ? updateMessageTemplate(formData.value.id, values)
      : createMessageTemplate(values));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemMessageApi.MessageTemplate>();
    formApi.resetForm();
    if (data?.id) {
      const detail = await getMessageTemplateDetail(data.id);
      formData.value = detail;
      remark.value = detail.remark || '';
      status.value = detail.status ?? 1;
      setVariableRows(schemaToRows(detail.contentSchema));
      await formApi.setValues({
        ...detail,
        inAppContent: detail.channel === 'IN_APP' ? detail.content : undefined,
      });
    } else {
      formData.value = undefined;
      remark.value = '';
      status.value = 1;
      setVariableRows([]);
      await formApi.setValues({ channel: 'IN_APP', status: 1 });
    }
  },
});
</script>

<template>
  <Drawer class="w-full max-w-220" :title="formData?.id ? '编辑消息模板' : '新增消息模板'">
    <Form />
    <div class="relative flex flex-row items-start pb-4">
      <div class="mr-2 flex w-[100px] shrink-0 justify-end pt-1 text-sm font-medium leading-6">
        模板参数
      </div>
      <div class="min-w-0 flex-auto overflow-hidden p-px">
        <div class="w-full rounded border p-3">
          <div class="mb-2 flex justify-end">
            <Button size="small" type="primary" @click="openParamModal">参数配置</Button>
          </div>
          <JsonViewer
            v-if="contentSchemaPreview"
            :boxed="false"
            :copyable="true"
            :expand-depth="3"
            :value="contentSchemaPreview"
          />
          <div v-else class="py-6 text-center text-sm text-gray-400">
            暂无参数配置
          </div>
        </div>
      </div>
    </div>
    <div class="relative flex flex-row items-start pb-4">
      <div class="mr-2 flex w-[100px] shrink-0 justify-end pt-1 text-sm font-medium leading-6">
        备注
      </div>
      <div class="min-w-0 flex-auto overflow-hidden p-px">
        <textarea
          v-model="remark"
          class="min-h-20 w-full rounded border px-3 py-2 text-sm"
          maxlength="255"
        />
      </div>
    </div>
    <div class="relative flex flex-row items-center pb-4">
      <div class="mr-2 flex w-[100px] shrink-0 justify-end text-sm font-medium leading-6">
        状态
      </div>
      <div class="min-w-0 flex-auto overflow-hidden p-px">
        <RadioGroup
          v-model:value="status"
          button-style="solid"
          option-type="button"
          :options="statusOptions"
        />
      </div>
    </div>
    <ParamModal class="h-[620px] w-[760px]" content-class="min-h-0">
      <template #title>
        <div class="flex w-full items-center justify-between pr-6">
          <span>参数配置</span>
          <Button size="small" type="primary" @click="addVariableRow">
            <Plus class="size-4" />
            新增参数
          </Button>
        </div>
      </template>
      <div class="h-full rounded border p-3">
        <div class="h-full overflow-x-auto overflow-y-scroll [scrollbar-gutter:stable]">
          <table class="w-full min-w-180 table-fixed text-left text-sm">
            <colgroup>
              <col style="width: 18%" />
              <col style="width: 18%" />
              <col style="width: 18%" />
              <col style="width: 10%" />
              <col style="width: 24%" />
              <col style="width: 12%" />
            </colgroup>
            <thead class="text-xs text-gray-500">
              <tr>
                <th class="px-2 py-2">参数名</th>
                <th class="px-2 py-2">显示名称</th>
                <th class="px-2 py-2">类型</th>
                <th class="px-2 py-2 text-center">必填</th>
                <th class="px-2 py-2">说明</th>
                <th class="px-2 py-2 text-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="editingVariableRows.length === 0">
                <td class="px-2 py-6 text-center text-gray-400" colspan="6">
                  暂无参数
                </td>
              </tr>
              <tr v-for="(row, index) in editingVariableRows" :key="index" class="border-t">
                <td class="px-2 py-2">
                  <Input
                    v-model:value="row.key"
                    class="w-full"
                    placeholder="请输入参数名"
                    size="small"
                  />
                </td>
                <td class="px-2 py-2">
                  <Input
                    v-model:value="row.label"
                    class="w-full"
                    placeholder="请输入显示名称"
                    size="small"
                  />
                </td>
                <td class="px-2 py-2">
                  <Select
                    v-model:value="row.type"
                    class="w-full"
                    :options="messageTemplateParamTypeOptions"
                    size="small"
                  />
                </td>
                <td class="px-2 py-2 text-center">
                  <Checkbox v-model:checked="row.required" />
                </td>
                <td class="px-2 py-2">
                  <Input
                    v-model:value="row.description"
                    class="w-full"
                    placeholder="请输入说明"
                    size="small"
                  />
                </td>
                <td class="px-2 py-2 text-center">
                  <Button danger size="small" type="link" @click="removeVariableRow(index)">
                    删除
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </ParamModal>
  </Drawer>
</template>
