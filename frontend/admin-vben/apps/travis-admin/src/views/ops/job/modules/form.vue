<script lang="ts" setup>
import type { OpsJobApi } from '#/api';

import { computed, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useDebounceFn } from '@vueuse/core';
import {
  Button,
  Form,
  FormItem,
  Input,
  message,
  Modal,
  Select,
  Space,
} from 'antdv-next';

import { InputNumber } from '#/adapter/component';
import { useVbenForm } from '#/adapter/form';
import {
  createJob,
  getJobDetail,
  getJobHandlers,
  getJobUserOptions,
  previewJob,
  updateJob,
} from '#/api';

import { useJobFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<OpsJobApi.Job>();
const previewTimes = ref<string[]>([]);
const userOptions = ref<Array<{ label: string; value: number }>>([]);
const userOptionsLoading = ref(false);
let userSearchSequence = 0;
const cronVisible = ref(false);
const cronModel = reactive({
  frequency: 'DAY',
  hour: 0,
  interval: 5,
  minute: 0,
  second: 0,
  weekday: 2,
});

const [JobForm, formApi] = useVbenForm({
  schema: useJobFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const payload = normalize(await formApi.getValues());
    try {
      validateJson(payload.params, 'JSON 参数');
      validateJson(payload.paramSchema, 'JSON Schema', true);
    } catch (error: any) {
      message.error(error?.message || 'JSON 格式不正确');
      return;
    }
    if (formData.value?.id && !(await confirmUpdate())) return;
    drawerApi.lock();
    try {
      await (formData.value?.id
        ? updateJob(formData.value.id, payload)
        : createJob(payload));
      emit('success');
      drawerApi.close();
    } finally {
      drawerApi.unlock();
    }
  },
  async onOpenChange(open) {
    if (!open) return;
    userSearchSequence++;
    userOptionsLoading.value = false;
    previewTimes.value = [];
    formApi.resetForm();
    const data = drawerApi.getData<OpsJobApi.Job>();
    formData.value = data?.id ? data : undefined;
    const [handlers, detail] = await Promise.all([
      getJobHandlers(),
      data?.id ? getJobDetail(data.id) : Promise.resolve(undefined),
    ]);
    const selectedUserIds = detail
      ? [detail.ownerUserId, ...(detail.alertUserIds || [])].filter(
          (id): id is number => id !== undefined,
        )
      : [];
    const [initialUsers, selectedUsers] = await Promise.all([
      getJobUserOptions(),
      selectedUserIds.length
        ? getJobUserOptions({ userIds: selectedUserIds.join(',') })
        : Promise.resolve([]),
    ]);
    userOptions.value = mergeUserOptions(initialUsers, selectedUsers);
    formApi.updateSchema([
      {
        componentProps: {
          options: handlers.map((name) => ({ label: name, value: name })),
        },
        fieldName: 'handlerName',
      },
    ]);
    updateUserOptionSchema();
    if (detail) {
      await formApi.setValues({
        ...detail,
        excludedDatesText: detail.excludedDates?.join(','),
      });
    } else {
      await formApi.setValues({
        concurrent: 0,
        logRetentionDays: 30,
        misfirePolicy: 0,
        params: '{}',
        priority: 5,
        scheduleType: 'CRON',
      });
    }
  },
});

function formatUserOption(user: OpsJobApi.UserOption) {
  const dept = user.deptName ? ` - ${user.deptName}` : '';
  return {
    label: `${user.nickname}（${user.username}）${dept}`,
    value: user.id,
  };
}

function mergeUserOptions(...groups: OpsJobApi.UserOption[][]) {
  const options = new Map<number, { label: string; value: number }>();
  groups.flat().forEach((user) => options.set(user.id, formatUserOption(user)));
  return [...options.values()];
}

function updateUserOptionSchema() {
  const commonProps = {
    filterOption: false,
    loading: userOptionsLoading.value,
    onSearch: searchUserOptions,
    options: userOptions.value,
    showSearch: true,
  };
  formApi.updateSchema([
    {
      componentProps: { ...commonProps, allowClear: true },
      fieldName: 'ownerUserId',
    },
    {
      componentProps: { ...commonProps, mode: 'multiple' },
      fieldName: 'alertUserIds',
    },
  ]);
}

const searchUserOptions = useDebounceFn(async (keyword: string) => {
  const sequence = ++userSearchSequence;
  userOptionsLoading.value = true;
  updateUserOptionSchema();
  try {
    const users = await getJobUserOptions({
      keyword: keyword.trim() || undefined,
    });
    if (sequence !== userSearchSequence) return;
    const values = await formApi.getValues();
    const selectedIds = new Set<number>([
      ...(values.ownerUserId ? [values.ownerUserId] : []),
      ...(values.alertUserIds || []),
    ]);
    const options = new Map(
      userOptions.value
        .filter((option) => selectedIds.has(option.value))
        .map((option) => [option.value, option]),
    );
    users.forEach((user) => options.set(user.id, formatUserOption(user)));
    userOptions.value = [...options.values()];
  } finally {
    if (sequence === userSearchSequence) {
      userOptionsLoading.value = false;
      updateUserOptionSchema();
    }
  }
}, 300);

const title = computed(() =>
  formData.value?.id ? '编辑调度任务' : '新增调度任务',
);

function normalize(values: Record<string, any>): Record<string, any> {
  const excludedDates = String(values.excludedDatesText || '')
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean);
  const payload: Record<string, any> = { ...values, excludedDates };
  delete payload.excludedDatesText;
  return payload;
}

function validateJson(
  value: string | undefined,
  label: string,
  optional = false,
) {
  if (!value?.trim() && optional) return;
  try {
    JSON.parse(value || '{}');
  } catch {
    throw new Error(`${label}格式不正确`);
  }
}

async function onPreview() {
  try {
    const payload = normalize(await formApi.getValues());
    previewTimes.value = await previewJob(payload, 7);
  } catch (error: any) {
    message.error(error?.message || '无法预览执行时间');
  }
}

function generateCron() {
  const { frequency, hour, interval, minute, second, weekday } = cronModel;
  const expression = {
    DAY: `${second} ${minute} ${hour} * * ?`,
    HOUR: `${second} ${minute} */${interval} * * ?`,
    MINUTE: `${second} */${interval} * * * ?`,
    WEEK: `${second} ${minute} ${hour} ? * ${weekday}`,
  }[frequency];
  formApi.setFieldValue('cronExpression', expression);
  cronVisible.value = false;
}

function confirmUpdate() {
  return new Promise<boolean>((resolve) => {
    Modal.confirm({
      content: '修改运行中的任务会立即重建 Quartz 调度配置，确认继续吗？',
      onCancel: () => resolve(false),
      onOk: () => resolve(true),
      title: '确认修改任务',
    });
  });
}
</script>

<template>
  <Drawer class="w-full max-w-220" :title="title">
    <JobForm />
    <Space class="mb-4 ml-30">
      <Button @click="cronVisible = true">Cron 生成器</Button>
      <Button @click="onPreview">预览执行时间</Button>
    </Space>
    <div v-if="previewTimes.length" class="ml-30 rounded border p-3 text-sm">
      <div class="mb-2 font-medium">未来执行时间</div>
      <div v-for="item in previewTimes" :key="item">{{ item }}</div>
    </div>
  </Drawer>

  <Modal v-model:open="cronVisible" title="Cron 可视化生成" @ok="generateCron">
    <Form layout="vertical">
      <FormItem label="执行频率">
        <Select
          v-model:value="cronModel.frequency"
          :options="[
            { label: '每隔 N 分钟', value: 'MINUTE' },
            { label: '每隔 N 小时', value: 'HOUR' },
            { label: '每天', value: 'DAY' },
            { label: '每周', value: 'WEEK' },
          ]"
        />
      </FormItem>
      <FormItem
        v-if="['MINUTE', 'HOUR'].includes(cronModel.frequency)"
        label="间隔"
      >
        <InputNumber v-model:value="cronModel.interval" :min="1" />
      </FormItem>
      <FormItem
        v-if="['DAY', 'WEEK'].includes(cronModel.frequency)"
        label="小时"
      >
        <InputNumber v-model:value="cronModel.hour" :max="23" :min="0" />
      </FormItem>
      <FormItem v-if="cronModel.frequency !== 'MINUTE'" label="分钟">
        <InputNumber v-model:value="cronModel.minute" :max="59" :min="0" />
      </FormItem>
      <FormItem label="秒">
        <InputNumber v-model:value="cronModel.second" :max="59" :min="0" />
      </FormItem>
      <FormItem v-if="cronModel.frequency === 'WEEK'" label="星期">
        <Select
          v-model:value="cronModel.weekday"
          :options="[
            { label: '星期日', value: 1 },
            { label: '星期一', value: 2 },
            { label: '星期二', value: 3 },
            { label: '星期三', value: 4 },
            { label: '星期四', value: 5 },
            { label: '星期五', value: 6 },
            { label: '星期六', value: 7 },
          ]"
        />
      </FormItem>
      <FormItem label="说明">
        <Input value="生成后仍会由后端 Quartz 再次校验" disabled />
      </FormItem>
    </Form>
  </Modal>
</template>
