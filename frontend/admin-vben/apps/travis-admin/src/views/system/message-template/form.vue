<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { ref } from 'vue';

import { JsonViewer, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import {
  Button,
  Checkbox,
  Input,
  message,
  RadioGroup,
  Select,
} from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createMessageTemplate,
  getMessageTemplateDetail,
  updateMessageTemplate,
} from '#/api';

import {
  isMessageTemplateParamType,
  MESSAGE_TEMPLATE_PARAM_EXPRESSION_PATTERN,
  MESSAGE_TEMPLATE_PARAM_KEY_PATTERN,
  messageTemplateParamTypeOptions,
} from '../message/param-types';
import { useFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.MessageTemplate>();
const contentSchemaPreview = ref<Record<string, any>>();
const remark = ref('');
const status = ref(1);
const variableRows = ref<VariableRow[]>([]);
const editingVariableRows = ref<VariableRow[]>([]);
const [Form, formApi] = useVbenForm({
  schema: useFormSchema(syncTemplateParamsFromContent),
  showDefaultActions: false,
});
const [ParamModal, paramModalApi] = useVbenModal({
  onConfirm() {
    confirmParamConfig();
  },
  async onOpened() {
    await paramGridApi.grid.loadData(editingVariableRows.value);
  },
});

type VariableRow = {
  _rowId: number;
  description?: string;
  key: string;
  label?: string;
  required: boolean;
  type: string;
};
let variableRowSequence = 0;

const [ParamGrid, paramGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'key', minWidth: 150, slots: { default: 'key' }, title: '参数名' },
      {
        field: 'label',
        minWidth: 150,
        slots: { default: 'label' },
        title: '显示名称',
      },
      { field: 'type', minWidth: 140, slots: { default: 'type' }, title: '类型' },
      {
        align: 'center',
        field: 'required',
        slots: { default: 'required' },
        title: '必填',
        width: 80,
      },
      {
        field: 'description',
        minWidth: 180,
        slots: { default: 'description' },
        title: '说明',
      },
      {
        align: 'center',
        field: 'action',
        fixed: 'right',
        slots: { default: 'action' },
        title: '操作',
        width: 80,
      },
    ],
    height: '100%',
    pagerConfig: false as unknown as VxeTableGridOptions<VariableRow>['pagerConfig'],
    rowConfig: { keyField: '_rowId' },
    toolbarConfig: { enabled: false },
  } as VxeTableGridOptions<VariableRow>,
});

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];
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

function schemaToRows(value?: string) {
  const schema = parseJsonObject(value);
  if (!schema) return [];
  return Object.entries(schema).map(([key, item]) => {
    const config =
      item && typeof item === 'object' && !Array.isArray(item) ? item : {};
    return {
      _rowId: ++variableRowSequence,
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
          label: row.label?.trim() ?? '',
          type: row.type || 'text',
          required: row.required,
          ...(row.description?.trim()
            ? { description: row.description.trim() }
            : {}),
        },
      ]),
  );
}

function updateContentSchemaPreview() {
  const schema = rowsToSchema();
  contentSchemaPreview.value =
    Object.keys(schema).length > 0 ? schema : undefined;
}

function setVariableRows(rows: VariableRow[]) {
  variableRows.value = rows.map((row) => ({
    ...row,
    _rowId: row._rowId || ++variableRowSequence,
  }));
  updateContentSchemaPreview();
}

async function addVariableRow() {
  const row = {
    _rowId: ++variableRowSequence,
    key: '',
    label: '',
    required: true,
    type: 'text',
  };
  editingVariableRows.value.push(row);
  await paramGridApi.grid.insertAt(row, -1);
}

async function removeVariableRow(row: VariableRow) {
  editingVariableRows.value = editingVariableRows.value.filter(
    (item) => item !== row,
  );
  await paramGridApi.grid.remove(row);
}

function validateVariableRows(rows = variableRows.value) {
  if (rows.some((row) => !row.key.trim())) {
    message.error('请填写参数名');
    return false;
  }
  if (
    rows.some((row) => !MESSAGE_TEMPLATE_PARAM_KEY_PATTERN.test(row.key.trim()))
  ) {
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
  if (rows.some((row) => (row.label?.trim().length ?? 0) > 100)) {
    message.error('参数显示名称长度不能超过100个字符');
    return false;
  }
  if (rows.some((row) => (row.description?.trim().length ?? 0) > 255)) {
    message.error('参数说明长度不能超过255个字符');
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

function extractTemplateParamKeys(...contents: Array<string | undefined>) {
  const keys: string[] = [];
  for (const content of contents) {
    if (!content) continue;
    for (const match of content.matchAll(
      MESSAGE_TEMPLATE_PARAM_EXPRESSION_PATTERN,
    )) {
      const key = match[1]?.trim();
      if (!key) {
        message.error('模板参数引用格式错误：参数名不能为空');
        return undefined;
      }
      if (!MESSAGE_TEMPLATE_PARAM_KEY_PATTERN.test(key)) {
        message.error(`模板参数引用格式错误：${key}`);
        return undefined;
      }
      keys.push(key);
    }
  }
  return [...new Set(keys)];
}

async function syncTemplateParamsFromContent() {
  const values = await formApi.getValues();
  const content =
    values.channel === 'IN_APP' ? values.inAppContent : values.content;
  const keys = extractTemplateParamKeys(
    values.title,
    content,
    values.redirectUrl,
  );
  if (!keys) return;
  const existingRows = new Map(
    variableRows.value.map((row) => [row.key.trim(), row]),
  );
  setVariableRows(
    keys.map((key) => {
      const existingRow = existingRows.get(key);
      return existingRow
        ? { ...existingRow }
        : {
            _rowId: ++variableRowSequence,
            key,
            label: '',
            required: true,
            type: 'text',
          };
    }),
  );
}

function validateTemplateParamUsage(...contents: Array<string | undefined>) {
  const contentKeys = extractTemplateParamKeys(...contents);
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
  editingVariableRows.value = [
    ...paramGridApi.grid.getTableData().fullData,
  ] as VariableRow[];
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
    if (values.contentSchema && values.contentSchema.length > 4000) {
      message.error('字段结构长度不能超过4000个字符');
      return;
    }
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
    if (
      !validateTemplateParamUsage(
        values.title,
        values.content,
        values.redirectUrl,
      )
    )
      return;
    delete values.inAppContent;
    if (formData.value?.id) {
      delete values.templateCode;
    }
    const request: SystemMessageApi.MessageTemplateSaveRequest = {
      channel: values.channel,
      content: values.content,
      contentSchema: values.contentSchema,
      platformTemplateId: values.platformTemplateId,
      redirectUrl: values.redirectUrl,
      remark: values.remark,
      status: values.status,
      templateCode: values.templateCode,
      templateName: values.templateName,
      title: values.title,
    };
    await (formData.value?.id
      ? updateMessageTemplate(formData.value.id, request)
      : createMessageTemplate(request));
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
      formApi.updateSchema([
        {
          componentProps: {
            disabled: true,
          },
          fieldName: 'templateCode',
        },
      ]);
      remark.value = detail.remark || '';
      status.value = detail.status ?? 1;
      setVariableRows(schemaToRows(detail.contentSchema));
      await formApi.setValues({
        ...detail,
        inAppContent: detail.channel === 'IN_APP' ? detail.content : undefined,
      });
    } else {
      formData.value = undefined;
      formApi.updateSchema([
        {
          componentProps: {
            disabled: false,
          },
          fieldName: 'templateCode',
        },
      ]);
      remark.value = '';
      status.value = 1;
      setVariableRows([]);
      await formApi.setValues({ channel: 'IN_APP', status: 1 });
    }
  },
});
</script>

<template>
  <Drawer
    class="w-full max-w-220"
    :title="formData?.id ? '编辑消息模板' : '新增消息模板'"
  >
    <Form />
    <div class="relative flex flex-row items-start pb-4">
      <div
        class="mr-2 flex w-[100px] shrink-0 justify-end pt-1 text-sm font-medium leading-6"
      >
        模板参数
      </div>
      <div class="min-w-0 flex-auto overflow-hidden p-px">
        <div class="w-full rounded border p-3">
          <div class="mb-2 flex justify-end">
            <Button size="small" type="primary" @click="openParamModal">
              参数配置
            </Button>
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
      <div
        class="mr-2 flex w-[100px] shrink-0 justify-end pt-1 text-sm font-medium leading-6"
      >
        备注
      </div>
      <div class="min-w-0 flex-auto overflow-hidden p-px">
        <textarea
          v-model="remark"
          class="min-h-20 w-full rounded border px-3 py-2 text-sm"
          maxlength="255"
        ></textarea>
      </div>
    </div>
    <div class="relative flex flex-row items-center pb-4">
      <div
        class="mr-2 flex w-[100px] shrink-0 justify-end text-sm font-medium leading-6"
      >
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
    <ParamModal
      class="h-[620px] w-[1000px]"
      content-class="min-h-0"
      header-class="py-3!"
    >
      <template #title>
        <div class="flex w-full items-center justify-between pr-10">
          <span>参数配置</span>
          <Button size="small" type="primary" @click="addVariableRow">
            <Plus class="size-4" />
            新增参数
          </Button>
        </div>
      </template>
      <ParamGrid>
        <template #key="{ row }">
          <Input v-model:value="row.key" placeholder="请输入参数名" size="small" />
        </template>
        <template #label="{ row }">
          <Input
            v-model:value="row.label"
            :maxlength="100"
            placeholder="请输入显示名称"
            size="small"
          />
        </template>
        <template #type="{ row }">
          <Select
            v-model:value="row.type"
            class="param-type-select w-full"
            :options="messageTemplateParamTypeOptions"
            size="small"
          />
        </template>
        <template #required="{ row }">
          <Checkbox v-model:checked="row.required" />
        </template>
        <template #description="{ row }">
          <Input
            v-model:value="row.description"
            :maxlength="255"
            placeholder="请输入说明"
            size="small"
          />
        </template>
        <template #action="{ row }">
          <Button danger size="small" type="link" @click="removeVariableRow(row)">
            删除
          </Button>
        </template>
      </ParamGrid>
    </ParamModal>
  </Drawer>
</template>

<style scoped>
:deep(.param-type-select),
:deep(.param-type-select .ant-select-selector),
:deep(.param-type-select .ant-select-selection-item),
:deep(.param-type-select .ant-select-selection-placeholder),
:deep(.param-type-select .ant-select-selection-wrap) {
  justify-content: flex-start !important;
  text-align: left !important;
}
</style>
