<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { ArrowUpToLine, Download, Plus } from '@vben/icons';

import {
  Button,
  Col,
  Input,
  message,
  Modal,
  Progress,
  Row,
  Space,
  Statistic,
} from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  changeJobStatus,
  copyJob,
  deleteJob,
  exportJobs,
  getJobPage,
  getJobStats,
  importJobs,
  runJob,
} from '#/api';

import { useJobColumns, useJobGridFormSchema } from './data';
import JobForm from './modules/form.vue';

const runVisible = ref(false);
const runTarget = ref<OpsJobApi.Job>();
const runParams = ref('{}');
const statsVisible = ref(false);
const statsTarget = ref<OpsJobApi.Job>();
const stats = ref<OpsJobApi.Stats>();
const importInput = ref<HTMLInputElement>();

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: JobForm,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useJobGridFormSchema() },
  gridOptions: {
    columns: useJobColumns(onJobAction, onStatusChange),
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

function onJobAction({ code, row }: OnActionClickParams<OpsJobApi.Job>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') onDelete(row);
  if (code === 'copy') onCopy(row);
  if (code === 'run') openRun(row);
  if (code === 'stats') openStats(row);
}

async function onStatusChange(value: number, row: OpsJobApi.Job) {
  await changeJobStatus(row.id, value);
  message.success(value === 1 ? '任务已启用' : '任务已暂停');
  await gridApi.query();
  return true;
}

function onDelete(row: OpsJobApi.Job) {
  Modal.confirm({
    content: '任务删除后不再调度，但历史执行日志会保留。',
    onOk: async () => {
      await deleteJob(row.id);
      message.success('任务已删除');
      await gridApi.query();
    },
    title: `删除任务“${row.jobName}”`,
  });
}

async function onCopy(row: OpsJobApi.Job) {
  await copyJob(row.id);
  message.success('任务副本已创建并保持暂停');
  await gridApi.query();
}

function openRun(row: OpsJobApi.Job) {
  runTarget.value = row;
  runParams.value = row.params || '{}';
  runVisible.value = true;
}

async function confirmRun() {
  try {
    JSON.parse(runParams.value || '{}');
  } catch {
    message.error('本次执行参数不是有效的 JSON');
    return;
  }
  await runJob(runTarget.value!.id, runParams.value);
  message.success('任务已提交执行');
  runVisible.value = false;
}

async function openStats(row: OpsJobApi.Job) {
  statsTarget.value = row;
  stats.value = await getJobStats(row.id);
  statsVisible.value = true;
}

function downloadJson(filename: string, value: unknown) {
  const blob = new Blob([JSON.stringify(value, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

async function onExport() {
  downloadJson('ops-jobs.json', await exportJobs());
}

async function onImportFile(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  let jobs: unknown;
  try {
    jobs = JSON.parse(await file.text());
  } catch {
    message.error('导入文件不是有效的 JSON');
    input.value = '';
    return;
  }
  if (!Array.isArray(jobs)) {
    message.error('导入文件必须是任务数组');
    input.value = '';
    return;
  }
  await importJobs(jobs);
  message.success('任务导入完成，新任务默认暂停');
  await gridApi.query();
  input.value = '';
}
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="gridApi.query" />
    <Grid table-title="任务管理">
      <template #toolbar-tools>
        <Space>
          <Button v-access:code="['ops:job:view']" @click="onExport">
            <Download class="size-4" />
            导出
          </Button>
          <Button
            v-access:code="['ops:job:edit']"
            @click="importInput?.click()"
          >
            <ArrowUpToLine class="size-4" />
            导入
          </Button>
          <Button
            v-access:code="['ops:job:edit']"
            type="primary"
            @click="formDrawerApi.setData({}).open()"
          >
            <Plus class="size-4" />
            新增任务
          </Button>
          <input
            ref="importInput"
            accept="application/json"
            class="hidden"
            type="file"
            @change="onImportFile"
          />
        </Space>
      </template>
    </Grid>

    <Modal v-model:open="runVisible" title="立即执行任务" @ok="confirmRun">
      <p class="mb-3">立即执行不会改变原调度计划。请确认本次执行参数。</p>
      <Input.TextArea v-model:value="runParams" :rows="10" />
    </Modal>

    <Modal
      v-model:open="statsVisible"
      :footer="null"
      :title="`执行统计 - ${statsTarget?.jobName ?? ''}`"
      width="760px"
    >
      <Row v-if="stats" :gutter="12">
        <Col :span="6">
          <Statistic title="总次数" :value="stats.total" />
        </Col>
        <Col :span="6">
          <Statistic
            title="成功率"
            :precision="2"
            suffix="%"
            :value="stats.successRate"
          />
        </Col>
        <Col :span="6">
          <Statistic
            title="平均耗时(ms)"
            :value="stats.averageDurationMillis"
          />
        </Col>
        <Col :span="6">
          <Statistic title="P95耗时(ms)" :value="stats.p95DurationMillis" />
        </Col>
      </Row>
      <div v-if="stats" class="mt-6">
        <div class="mb-3 font-medium">最近 7 天趋势</div>
        <div
          v-for="point in stats.trend"
          :key="point.date"
          class="mb-2 flex items-center gap-3"
        >
          <span class="w-24">{{ point.date }}</span>
          <Progress
            :percent="
              point.success + point.failed
                ? (point.success * 100) / (point.success + point.failed)
                : 0
            "
            :show-info="false"
          />
          <span class="w-32">
            成功 {{ point.success }} / 失败 {{ point.failed }}
          </span>
        </div>
        <div class="mt-4">
          连续失败：{{ stats.consecutiveFailures }} 次，最大耗时：{{
            stats.maxDurationMillis
          }}
          ms
        </div>
      </div>
    </Modal>
  </Page>
</template>
