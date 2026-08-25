<script lang="ts" setup>
// cspell:ignore nums
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import { Button, Input, message, Space, Tag } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  changeJobStatus,
  copyJob,
  deleteJob,
  getJobDetail,
  getJobHandlers,
  getJobPage,
  getJobStats,
  runJob,
} from '#/api';
import { getDictLabel, getDictOptions } from '#/utils/dict';
import { hasAccessCode, OPS_PERMS } from '#/utils/permissions';

import {
  JOB_EXECUTION_STATUS_DICT,
  useJobColumns,
  useJobGridFormSchema,
} from './data';
import JobForm from './modules/form.vue';

const runTarget = ref<OpsJobApi.Job>();
const runParams = ref('{}');
const statsTarget = ref<OpsJobApi.Job>();
const stats = ref<OpsJobApi.Stats>();
const router = useRouter();
const canQueryJobLog = hasAccessCode(OPS_PERMS.jobLogQuery);

const executionStatusOptions = getDictOptions(JOB_EXECUTION_STATUS_DICT);
const statsTrend = computed(() =>
  (stats.value?.trend ?? []).toSorted((a, b) => b.date.localeCompare(a.date)),
);

function executionStatusColor(status?: number) {
  return executionStatusOptions.find((item) => item.value === status)?.color;
}

function successPercent(point: {
  failed: number | string;
  success: number | string;
}) {
  const success = Number(point.success) || 0;
  const failed = Number(point.failed) || 0;
  const total = success + failed;
  return total ? (success * 100) / total : 0;
}

function failurePercent(point: {
  failed: number | string;
  success: number | string;
}) {
  const success = Number(point.success) || 0;
  const failed = Number(point.failed) || 0;
  const total = success + failed;
  return total ? (failed * 100) / total : 0;
}

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: JobForm,
  destroyOnClose: true,
});

const [RunModal, runModalApi] = useVbenModal({
  async onConfirm() {
    await confirmRun();
  },
});

const [StatsModal, statsModalApi] = useVbenModal({
  footer: false,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useJobGridFormSchema() },
  gridOptions: {
    columns: useJobColumns(
      onJobAction,
      hasAccessCode(OPS_PERMS.jobOperation) ? onStatusChange : undefined,
    ),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getJobPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<OpsJobApi.Job>,
});

onMounted(async () => {
  const handlers = await getJobHandlers(true);
  gridApi.formApi.updateSchema([
    {
      componentProps: {
        allowClear: true,
        optionFilterProp: 'label',
        options: handlers.map((handler) => ({
          label: handler.description
            ? `${handler.name} - ${handler.description}`
            : handler.name,
          value: handler.name,
        })),
        showSearch: true,
      },
      fieldName: 'handlerName',
    },
  ]);
});

function onJobAction({ code, row }: OnActionClickParams<OpsJobApi.Job>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') onDelete(row);
  if (code === 'copy') onCopy(row);
  if (code === 'run') void openRun(row);
  if (code === 'stats') openStats(row);
}

function openJobLog(row: OpsJobApi.Job) {
  void router.push({
    path: '/ops/job/log',
    query: { jobName: row.jobName, pageKey: '/ops/job/log' },
  });
}

async function onStatusChange(value: number, row: OpsJobApi.Job) {
  await changeJobStatus(row.id, value);
  message.success(`任务已${getDictLabel('enable_status', value)}`);
  await gridApi.query();
  return true;
}

function onDelete(row: OpsJobApi.Job) {
  deleteJob(row.id).then(async () => {
    message.success('任务已删除');
    await gridApi.query();
  });
}

async function onCopy(row: OpsJobApi.Job) {
  await copyJob(row.id);
  message.success('任务副本已创建并保持暂停');
  await gridApi.query();
}

async function openRun(row: OpsJobApi.Job) {
  const detail = await getJobDetail(row.id);
  runTarget.value = row;
  runParams.value = detail.params || '{}';
  runModalApi.open();
}

async function confirmRun() {
  const target = runTarget.value;
  if (!target) return;
  try {
    JSON.parse(runParams.value || '{}');
  } catch {
    message.error('本次执行参数不是有效的 JSON');
    return;
  }
  runModalApi.lock();
  try {
    await runJob(target.id, runParams.value);
    message.success('任务已提交执行');
    runModalApi.close();
  } catch {
    runModalApi.unlock();
  }
}

async function openStats(row: OpsJobApi.Job) {
  statsTarget.value = row;
  stats.value = await getJobStats(row.id);
  statsModalApi.open();
}
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="gridApi.query" />
    <Grid table-title="任务管理">
      <template #jobName="{ row }">
        <Button v-if="canQueryJobLog" type="link" @click="openJobLog(row)">
          {{ row.jobName }}
        </Button>
        <template v-else>{{ row.jobName }}</template>
      </template>
      <template #toolbar-tools>
        <Space>
          <Button
            v-access:code="OPS_PERMS.jobUpdate"
            type="primary"
            @click="formDrawerApi.setData({}).open()"
          >
            <Plus class="size-4" />
            新增任务
          </Button>
        </Space>
      </template>
      <template #executionInfo="{ row }">
        <div class="table-cell-pre-line">
          <span>
            上次执行结果：<Tag
              v-if="
                row.lastExecutionStatus !== null &&
                row.lastExecutionStatus !== undefined
              "
              :color="executionStatusColor(row.lastExecutionStatus)"
            >
              {{
                getDictLabel(JOB_EXECUTION_STATUS_DICT, row.lastExecutionStatus)
              }}
            </Tag>
            <template v-else>-</template>
          </span>
          <span>
            上次执行时间：{{
              row.lastExecutionTime
                ? formatDateTime(row.lastExecutionTime)
                : '-'
            }}
          </span>
          <span>
            下次执行时间：{{
              row.nextFireTime ? formatDateTime(row.nextFireTime) : '-'
            }}
          </span>
        </div>
      </template>
    </Grid>

    <RunModal class="w-[640px]" title="立即执行任务">
      <div class="space-y-4">
        <section
          class="rounded-lg border border-primary/20 bg-primary/5 px-4 py-3.5"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <span class="h-5 w-1 rounded-full bg-primary"></span>
                <span class="truncate font-medium">
                  {{ runTarget?.jobName || '-' }}
                </span>
              </div>
              <div class="mt-2 flex items-center gap-2 pl-3 text-xs">
                <span class="text-muted-foreground">任务处理器</span>
                <code
                  class="truncate rounded bg-background/80 px-2 py-1 text-foreground"
                >
                  {{ runTarget?.handlerName || '-' }}
                </code>
              </div>
            </div>
            <Tag color="blue">手动执行</Tag>
          </div>
        </section>

        <div
          class="flex items-start gap-2.5 rounded-lg border border-warning/30 bg-warning/10 px-4 py-3 text-sm"
        >
          <span class="mt-1 size-2 shrink-0 rounded-full bg-warning"></span>
          <p class="leading-6 text-muted-foreground">
            立即执行不会修改任务配置和原调度计划，提交前请确认本次执行参数。
          </p>
        </div>

        <section class="overflow-hidden rounded-lg border border-border/70">
          <div
            class="flex items-center justify-between border-b border-border/60 bg-muted/20 px-4 py-2.5"
          >
            <div>
              <div class="text-sm font-medium">本次执行参数</div>
              <div class="mt-0.5 text-xs text-muted-foreground">
                可在默认参数基础上进行临时调整
              </div>
            </div>
            <span
              class="rounded bg-background px-2 py-1 font-mono text-xs text-muted-foreground"
            >
              JSON
            </span>
          </div>
          <Input.TextArea
            v-model:value="runParams"
            class="run-params-editor"
            :rows="8"
            :spellcheck="false"
          />
          <div
            class="border-t border-border/60 bg-muted/10 px-4 py-2 text-xs text-muted-foreground"
          >
            参数必须是合法的 JSON 格式；无额外参数时请使用 {}。
          </div>
        </section>
      </div>
    </RunModal>

    <StatsModal
      :title="`执行统计 - ${statsTarget?.jobName ?? ''}`"
      class="w-[800px]"
    >
      <div v-if="stats" class="space-y-4">
        <div class="grid grid-cols-4 gap-3">
          <div class="rounded-lg border border-border/60 bg-muted/20 p-4">
            <div class="text-xs text-muted-foreground">执行总次数</div>
            <div class="mt-2 text-2xl font-semibold tabular-nums">
              {{ stats.total }}
            </div>
          </div>
          <div class="rounded-lg border border-primary/20 bg-primary/5 p-4">
            <div class="text-xs text-muted-foreground">成功率</div>
            <div class="mt-2 text-2xl font-semibold text-primary tabular-nums">
              {{ stats.successRate.toFixed(2)
              }}<span class="ml-0.5 text-sm">%</span>
            </div>
          </div>
          <div class="rounded-lg border border-border/60 bg-muted/20 p-4">
            <div class="text-xs text-muted-foreground">平均耗时</div>
            <div class="mt-2 text-2xl font-semibold tabular-nums">
              {{ stats.averageDurationMillis
              }}<span class="ml-1 text-xs font-normal text-muted-foreground">
                ms
              </span>
            </div>
          </div>
          <div class="rounded-lg border border-border/60 bg-muted/20 p-4">
            <div class="text-xs text-muted-foreground">P95 耗时</div>
            <div class="mt-2 text-2xl font-semibold tabular-nums">
              {{ stats.p95DurationMillis
              }}<span class="ml-1 text-xs font-normal text-muted-foreground">
                ms
              </span>
            </div>
          </div>
        </div>

        <section class="overflow-hidden rounded-lg border border-border/60">
          <div
            class="flex items-center justify-between border-b border-border/60 bg-muted/20 px-4 py-3"
          >
            <div class="flex items-center gap-2 font-medium">
              <span class="h-5 w-1 rounded-full bg-primary"></span>
              最近 7 天趋势
            </div>
            <div class="flex items-center gap-4 text-xs text-muted-foreground">
              <span class="flex items-center gap-1.5">
                <i class="size-2 rounded-full bg-success"></i>成功
              </span>
              <span class="flex items-center gap-1.5">
                <i class="size-2 rounded-full bg-destructive"></i>失败
              </span>
            </div>
          </div>

          <div class="space-y-1 p-3">
            <div
              v-for="point in statsTrend"
              :key="point.date"
              class="grid grid-cols-[96px_minmax(0,1fr)_220px] items-center gap-3 rounded-md px-2 py-2 transition-colors hover:bg-muted/30"
            >
              <span
                class="whitespace-nowrap text-sm tabular-nums text-muted-foreground"
              >
                {{ point.date }}
              </span>
              <div class="flex h-2.5 overflow-hidden rounded-full bg-muted">
                <div
                  class="h-full bg-success transition-[width] duration-300"
                  :style="{ width: `${successPercent(point)}%` }"
                ></div>
                <div
                  class="h-full bg-destructive transition-[width] duration-300"
                  :style="{ width: `${failurePercent(point)}%` }"
                ></div>
              </div>
              <span class="whitespace-nowrap text-right text-xs tabular-nums">
                <span class="font-medium text-success">
                  成功 {{ point.success }}
                </span>
                <span class="mx-1.5 text-border">/</span>
                <span class="text-muted-foreground">
                  失败 {{ point.failed }}
                </span>
                <span class="mx-1.5 text-border">/</span>
                <span class="font-medium text-primary">
                  成功率 {{ successPercent(point).toFixed(2) }}%
                </span>
              </span>
            </div>
          </div>

          <div
            class="flex items-center gap-6 border-t border-border/60 bg-muted/10 px-4 py-3 text-sm"
          >
            <span>
              <span class="text-muted-foreground">连续失败</span>
              <strong class="ml-2 font-semibold tabular-nums">
                {{ stats.consecutiveFailures }} 次
              </strong>
            </span>
            <span class="h-4 w-px bg-border"></span>
            <span>
              <span class="text-muted-foreground">最大耗时</span>
              <strong class="ml-2 font-semibold tabular-nums">
                {{ stats.maxDurationMillis }} ms
              </strong>
            </span>
          </div>
        </section>
      </div>
    </StatsModal>
  </Page>
</template>

<style scoped>
.table-cell-pre-line {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.5rem;
  text-align: left;
}

:deep(.run-params-editor) {
  padding: 0.875rem 1rem;
  font-family:
    ui-monospace, SFMono-Regular, menlo, monaco, consolas, 'Liberation Mono',
    monospace;
  line-height: 1.65;
  resize: vertical;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}

:deep(.run-params-editor:focus) {
  box-shadow: inset 0 0 0 1px hsl(var(--primary) / 35%);
}
</style>
