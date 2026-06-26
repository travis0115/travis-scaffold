<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { ref } from 'vue';

import { confirm as vbenConfirm, Page, useVbenModal } from '@vben/common-ui';
import { Download } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  message,
  Space,
} from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cleanJobLogs,
  exportJobLogs,
  getJobLogDetail,
  getJobLogPage,
} from '#/api';
import { OPS_PERMS } from '#/utils/permissions';

import { useLogColumns, useLogGridFormSchema } from './data';

const detail = ref<OpsJobApi.JobLog>();

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useLogGridFormSchema() },
  gridOptions: {
    columns: useLogColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getJobLogPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<OpsJobApi.JobLog>,
});

const [DetailModal, detailModalApi] = useVbenModal({
  footer: false,
});

async function onActionClick({ row }: OnActionClickParams<OpsJobApi.JobLog>) {
  detail.value = await getJobLogDetail(row.id);
  detailModalApi.open();
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
  downloadJson('ops-job-logs.json', await exportJobLogs({}));
}

function onClean() {
  vbenConfirm({
    content: '该操作会清理全部任务执行日志，并使统计缓存重新计算。',
    confirmText: '确认清理',
    icon: 'warning',
    title: '确认清理执行日志',
  })
    .then(async () => {
      await cleanJobLogs();
      message.success('执行日志已清理');
      await gridApi.query();
    })
    .catch(() => {});
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="执行日志">
      <template #toolbar-tools>
        <Space>
          <Button v-access:code="OPS_PERMS.jobLogQuery" @click="onExport">
            <Download class="size-4" />
            导出
          </Button>
          <Button
            danger
            v-access:code="OPS_PERMS.jobOperation"
            @click="onClean"
          >
            清理日志
          </Button>
        </Space>
      </template>
    </Grid>

    <DetailModal class="w-full max-w-225" title="执行日志详情">
      <Descriptions v-if="detail" bordered :column="2">
        <DescriptionsItem label="任务名称">
          {{ detail.jobName }}
        </DescriptionsItem>
        <DescriptionsItem label="执行实例">
          {{ detail.schedulerInstanceId }}
        </DescriptionsItem>
        <DescriptionsItem label="开始时间">
          {{ formatDateTime(detail.startTime) }}
        </DescriptionsItem>
        <DescriptionsItem label="耗时">
          {{ detail.durationMillis }} ms
        </DescriptionsItem>
        <DescriptionsItem label="参数快照" :span="2">
          <pre class="whitespace-pre-wrap">{{ detail.paramsSnapshot }}</pre>
        </DescriptionsItem>
        <DescriptionsItem label="异常类型" :span="2">
          {{ detail.exceptionClass || '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="异常信息" :span="2">
          {{ detail.exceptionMessage || '-' }}
        </DescriptionsItem>
        <DescriptionsItem label="堆栈" :span="2">
          <pre class="max-h-96 overflow-auto whitespace-pre-wrap">{{
            detail.stackTrace || '-'
          }}</pre>
        </DescriptionsItem>
      </Descriptions>
    </DetailModal>
  </Page>
</template>
