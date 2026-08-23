<script lang="ts" setup>
import type { OpsJobApi } from '#/api';

import { computed, reactive, ref } from 'vue';

import { confirm, useVbenDrawer, useVbenModal } from '@vben/common-ui';

import { useDebounceFn } from '@vueuse/core';
import {
  Button,
  Form,
  FormItem,
  message,
  Select,
  TimePicker,
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
import { OPS_PERMS } from '#/utils/permissions';

import { useJobFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<OpsJobApi.Job>();
const previewTimes = ref<string[]>([]);
const previewLoading = ref(false);
const userOptions = ref<Array<{ label: string; value: number }>>([]);
const userOptionsLoading = ref(false);
let userSearchSequence = 0;
const cronModel = reactive({
  frequency: 'INTERVAL',
  interval: 5,
  intervalUnit: 'MINUTE',
  monthday: 1,
  time: '00:00:00',
  weekday: 2,
});
const cronFrequencyOptions = [
  {
    description: '按秒、分钟或小时周期运行',
    label: '固定间隔',
    value: 'INTERVAL',
  },
  { description: '每天固定时间运行', label: '每天', value: 'DAY' },
  { description: '每周固定时间运行', label: '每周', value: 'WEEK' },
  { description: '每月固定日期和时间运行', label: '每月', value: 'MONTH' },
];
const intervalUnitOptions = [
  { label: '秒', value: 'SECOND' },
  { label: '分钟', value: 'MINUTE' },
  { label: '小时', value: 'HOUR' },
];
const monthdayOptions = Array.from({ length: 31 }, (_, index) => ({
  label: `${index + 1} 日`,
  value: index + 1,
}));
const weekdayOptions = [
  { label: '星期日', value: 1 },
  { label: '星期一', value: 2 },
  { label: '星期二', value: 3 },
  { label: '星期三', value: 4 },
  { label: '星期四', value: 5 },
  { label: '星期五', value: 6 },
  { label: '星期六', value: 7 },
];

const [CronModal, cronModalApi] = useVbenModal({
  onConfirm() {
    generateCron();
  },
});

const [JobForm, formApi] = useVbenForm({
  commonConfig: {
    labelClass: 'whitespace-nowrap',
    labelWidth: 140,
  },
  schema: useJobFormSchema(() => cronModalApi.open()),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const payload = buildJobPayload(await formApi.getValues());
    if (formData.value?.id) {
      payload.lockVersion = formData.value.lockVersion;
    }
    try {
      validateJson(payload.params, 'JSON 参数');
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
    if (detail) formData.value = detail;
    const selectedUserIds = detail?.alertUserIds || [];
    const [initialUsers, selectedUsers] = await Promise.all([
      getJobUserOptions(),
      selectedUserIds.length > 0
        ? getJobUserOptions({ userIds: selectedUserIds.join(',') })
        : Promise.resolve([]),
    ]);
    userOptions.value = mergeUserOptions(initialUsers, selectedUsers);
    formApi.updateSchema([
      {
        componentProps: {
          options: handlers.map((handler) => ({
            label: handler.description
              ? `${handler.name} - ${handler.description}`
              : handler.name,
            value: handler.name,
          })),
        },
        fieldName: 'handlerName',
      },
    ]);
    updateUserOptionSchema();
    if (detail) {
      await formApi.setValues({
        ...detail,
        intervalSeconds:
          detail.intervalMillis === undefined
            ? undefined
            : detail.intervalMillis / 1000,
      });
    } else {
      cronModel.frequency = 'INTERVAL';
      await formApi.setValues({
        concurrent: 0,
        misfirePolicy: 0,
        params: '{}',
        scheduleType: 'INTERVAL',
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
    const selectedUserIds: number[] = values.alertUserIds || [];
    const selectedIds = new Set(selectedUserIds);
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
const generatedCronExpression = computed(buildCronExpression);
const generatedCronDescription = computed(() => {
  if (cronModel.frequency === 'INTERVAL') {
    const unit = intervalUnitOptions.find(
      (option) => option.value === cronModel.intervalUnit,
    )?.label;
    return `每隔 ${cronModel.interval} ${unit ?? ''}执行`;
  }
  if (cronModel.frequency === 'WEEK') {
    const weekday = weekdayOptions.find(
      (option) => option.value === cronModel.weekday,
    )?.label;
    return `每周${weekday ?? ''} ${cronModel.time} 执行`;
  }
  if (cronModel.frequency === 'MONTH') {
    return `每月 ${cronModel.monthday} 日 ${cronModel.time} 执行`;
  }
  return `每天 ${cronModel.time} 执行`;
});

function validateJson(value: string | undefined, label: string) {
  try {
    JSON.parse(value || '{}');
  } catch {
    throw new Error(`${label}格式不正确`);
  }
}

function buildJobPayload(values: Record<string, any>): Record<string, any> {
  const payload = { ...values };
  payload.intervalMillis =
    payload.intervalSeconds === undefined
      ? undefined
      : payload.intervalSeconds * 1000;
  delete payload.intervalSeconds;
  return payload;
}

async function onPreview() {
  previewTimes.value = [];
  const values = await formApi.getValues();
  const previewField = {
    CRON: 'cronExpression',
    INTERVAL: 'intervalSeconds',
    ONCE: 'executeAt',
  }[values.scheduleType as OpsJobApi.ScheduleType];
  const { valid } = await formApi.validateField(previewField ?? 'scheduleType');
  if (!valid) return;

  previewLoading.value = true;
  try {
    previewTimes.value = await previewJob(
      {
        cronExpression: values.cronExpression,
        executeAt: values.executeAt,
        intervalMillis:
          values.intervalSeconds === undefined
            ? undefined
            : values.intervalSeconds * 1000,
        scheduleType: values.scheduleType,
      },
      7,
    );
  } catch (error: any) {
    const responseData = error?.response?.data ?? error?.data ?? {};
    message.error(
      responseData?.msg ||
        responseData?.error ||
        responseData?.message ||
        error?.message ||
        '无法预览执行时间',
    );
  } finally {
    previewLoading.value = false;
  }
}

function buildCronExpression() {
  const { frequency, interval, intervalUnit, monthday, time, weekday } =
    cronModel;
  if (frequency === 'INTERVAL') {
    return {
      HOUR: `0 0 */${interval} * * ?`,
      MINUTE: `0 */${interval} * * * ?`,
      SECOND: `*/${interval} * * * * ?`,
    }[intervalUnit];
  }
  const [hour, minute, second] = time.split(':').map(Number);
  return {
    DAY: `${second} ${minute} ${hour} * * ?`,
    MONTH: `${second} ${minute} ${hour} ${monthday} * ?`,
    WEEK: `${second} ${minute} ${hour} ? * ${weekday}`,
  }[frequency];
}

function generateCron() {
  formApi.setFieldValue('cronExpression', generatedCronExpression.value);
  cronModalApi.close();
}

function normalizeInterval() {
  const max = cronModel.intervalUnit === 'HOUR' ? 23 : 59;
  cronModel.interval = Math.min(cronModel.interval, max);
}

async function confirmUpdate() {
  try {
    await confirm({
      content: '修改运行中的任务会立即重建 Quartz 调度配置，确认继续吗？',
      icon: 'warning',
      title: '确认修改任务',
    });
    return true;
  } catch {
    return false;
  }
}
</script>

<template>
  <Drawer class="w-full max-w-220" :title="title">
    <JobForm />
    <Button
      v-access:code="OPS_PERMS.jobQuery"
      :loading="previewLoading"
      class="mb-4 ml-30"
      @click="onPreview"
    >
      预览执行时间
    </Button>
    <div
      v-if="previewTimes.length > 0"
      class="ml-30 rounded border p-3 text-sm"
    >
      <div class="mb-2 font-medium">未来执行时间</div>
      <div v-for="item in previewTimes" :key="item">{{ item }}</div>
    </div>
  </Drawer>

  <CronModal class="w-[640px]" title="Cron 表达式生成器">
    <div class="space-y-4">
      <section>
        <div class="mb-2 text-sm font-medium">选择执行频率</div>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="option in cronFrequencyOptions"
            :key="option.value"
            class="rounded-lg border px-4 py-3 text-left transition-colors"
            :class="
              cronModel.frequency === option.value
                ? 'border-primary bg-primary/5 text-primary'
                : 'border-border bg-card hover:border-primary/40 hover:bg-muted/20'
            "
            type="button"
            @click="cronModel.frequency = option.value"
          >
            <span class="block text-sm font-medium">{{ option.label }}</span>
            <span class="mt-1 block text-xs text-muted-foreground">
              {{ option.description }}
            </span>
          </button>
        </div>
      </section>

      <section class="rounded-lg border border-border/60 bg-muted/15 p-4">
        <div class="mb-3 flex items-center gap-2 text-sm font-medium">
          <span class="h-4 w-1 rounded-full bg-primary"></span>
          设置执行参数
        </div>
        <Form class="grid grid-cols-2 gap-x-4" layout="vertical">
          <FormItem
            v-if="cronModel.frequency === 'INTERVAL'"
            class="mb-0"
            label="间隔数值"
          >
            <InputNumber
              v-model:value="cronModel.interval"
              class="w-full"
              :max="cronModel.intervalUnit === 'HOUR' ? 23 : 59"
              :min="1"
            />
          </FormItem>
          <FormItem
            v-if="cronModel.frequency === 'INTERVAL'"
            class="mb-0"
            label="间隔单位"
          >
            <Select
              v-model:value="cronModel.intervalUnit"
              class="w-full"
              :options="intervalUnitOptions"
              @change="normalizeInterval"
            />
          </FormItem>
          <FormItem
            v-if="cronModel.frequency === 'MONTH'"
            class="mb-0"
            label="日期"
          >
            <Select
              v-model:value="cronModel.monthday"
              class="w-full"
              :options="monthdayOptions"
            />
          </FormItem>
          <FormItem
            v-if="cronModel.frequency === 'WEEK'"
            class="mb-0"
            label="星期"
          >
            <Select
              v-model:value="cronModel.weekday"
              class="w-full"
              :options="weekdayOptions"
            />
          </FormItem>
          <FormItem
            v-if="cronModel.frequency !== 'INTERVAL'"
            class="mb-0"
            :class="{ 'col-span-2': cronModel.frequency === 'DAY' }"
            label="执行时间"
          >
            <TimePicker
              v-model:value="cronModel.time"
              :allow-clear="false"
              class="w-full"
              format="HH:mm:ss"
              value-format="HH:mm:ss"
            />
          </FormItem>
        </Form>
      </section>

      <section
        class="overflow-hidden rounded-lg border border-primary/20 bg-primary/5"
      >
        <div
          class="border-b border-primary/10 px-4 py-2 text-xs text-muted-foreground"
        >
          实时预览
        </div>
        <div class="flex items-center justify-between gap-4 px-4 py-3">
          <div class="min-w-0">
            <code class="text-base font-semibold text-primary">
              {{ generatedCronExpression }}
            </code>
            <div class="mt-1 text-xs text-muted-foreground">
              {{ generatedCronDescription }}
            </div>
          </div>
          <span
            class="shrink-0 rounded bg-background px-2 py-1 text-xs text-muted-foreground"
          >
            Quartz Cron
          </span>
        </div>
      </section>

      <p class="text-xs text-muted-foreground">
        生成结果写入表单后，保存和启用任务时仍会由后端 Quartz 校验。
      </p>
    </div>
  </CronModal>
</template>
