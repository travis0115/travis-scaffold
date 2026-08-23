<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { OpsJobApi } from '#/api';

import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal, confirm as vbenConfirm } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  Descriptions,
  DescriptionsItem,
  Divider,
  message,
  Tag,
  TypographyParagraph,
} from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cleanJobLogs,
  getJobHandlers,
  getJobLogDetail,
  getJobLogPage,
} from '#/api';
import { getDictLabel, getDictOptions } from '#/utils/dict';
import { OPS_PERMS } from '#/utils/permissions';

import {
  JOB_EXECUTION_STATUS_DICT,
  useLogColumns,
  useLogGridFormSchema,
} from './data';

const detail = ref<OpsJobApi.JobLog>();
const executionStatusOptions = getDictOptions(JOB_EXECUTION_STATUS_DICT);
const route = useRoute();
const routeJobName = computed(() => {
  const value = route.query.jobName;
  return (Array.isArray(value) ? value[0] : value) || '';
});
const routeStatus = computed(() => {
  const value = Array.isArray(route.query.status)
    ? route.query.status[0]
    : route.query.status;
  const status = Number(value);
  return status === 0 || status === 1 || status === 2 ? status : undefined;
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['executionTimeRange', ['startTime', 'endTime']]],
    schema: useLogGridFormSchema(),
  },
  gridOptions: {
    columns: useLogColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      autoLoad: !routeJobName.value && routeStatus.value === undefined,
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

async function applyRouteQuery() {
  if (route.path !== '/ops/job/log') return;
  await Promise.all([
    gridApi.formApi.setFieldValue('jobName', routeJobName.value || undefined),
    gridApi.formApi.setFieldValue('status', routeStatus.value),
  ]);
  gridApi.formApi.setLatestSubmissionValues(await gridApi.formApi.getValues());
  await gridApi.query();
}

onMounted(async () => {
  if (routeJobName.value || routeStatus.value !== undefined) {
    await applyRouteQuery();
  }

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

watch([routeJobName, routeStatus], applyRouteQuery);

const [DetailModal, detailModalApi] = useVbenModal({
  footer: false,
});

async function onActionClick({ row }: OnActionClickParams<OpsJobApi.JobLog>) {
  detail.value = await getJobLogDetail(row.id);
  detailModalApi.open();
}

function statusColor(status?: number) {
  return executionStatusOptions.find((item) => item.value === status)?.color;
}

function statusLabel(status?: number) {
  return getDictLabel(JOB_EXECUTION_STATUS_DICT, status) || '-';
}

function formatDuration(durationMillis?: number) {
  return durationMillis === undefined || durationMillis === null
    ? '-'
    : `${durationMillis} ms`;
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
        <Button
          class="transition-all enabled:hover:!border-destructive-hover enabled:hover:!bg-destructive/10 enabled:hover:!text-destructive-hover enabled:hover:shadow-sm"
          danger
          v-access:code="OPS_PERMS.jobOperation"
          @click="onClean"
        >
          清理日志
        </Button>
      </template>
    </Grid>

    <DetailModal class="w-[860px]" title="执行日志详情">
      <template v-if="detail">
        <Divider title-placement="start">执行概览</Divider>
        <Descriptions bordered :column="2" size="small">
          <DescriptionsItem label="执行状态">
            <Tag :color="statusColor(detail.status)">
              {{ statusLabel(detail.status) }}
            </Tag>
          </DescriptionsItem>
          <DescriptionsItem label="执行耗时">
            {{ formatDuration(detail.durationMillis) }}
          </DescriptionsItem>
          <DescriptionsItem label="开始时间">
            {{ detail.startTime ? formatDateTime(detail.startTime) : '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="结束时间">
            {{ detail.endTime ? formatDateTime(detail.endTime) : '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="计划触发时间">
            {{
              detail.scheduledFireTime
                ? formatDateTime(detail.scheduledFireTime)
                : '-'
            }}
          </DescriptionsItem>
          <DescriptionsItem label="执行结果" :span="2">
            {{ detail.resultMessage || '-' }}
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">任务信息</Divider>
        <Descriptions bordered :column="2" size="small">
          <DescriptionsItem label="任务名称">
            {{ detail.jobName || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="任务处理器">
            {{ detail.handlerName || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="调度器实例">
            {{ detail.schedulerInstanceId || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="触发实例 ID">
            {{ detail.fireInstanceId || '-' }}
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">参数快照</Divider>
        <TypographyParagraph
          :copyable="
            detail.paramsSnapshot ? { text: detail.paramsSnapshot } : false
          "
        >
          <pre class="max-h-64 overflow-auto whitespace-pre-wrap break-all">{{
            detail.paramsSnapshot || '-'
          }}</pre>
        </TypographyParagraph>

        <Divider title-placement="start">异常信息</Divider>
        <Descriptions
          bordered
          :column="1"
          size="small"
          :styles="{ label: { whiteSpace: 'nowrap', width: '120px' } }"
        >
          <DescriptionsItem label="异常类型">
            {{ detail.exceptionClass || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="异常消息">
            {{ detail.exceptionMessage || '-' }}
          </DescriptionsItem>
        </Descriptions>
        <template v-if="detail.stackTrace">
          <Divider title-placement="start">异常堆栈</Divider>
          <TypographyParagraph :copyable="{ text: detail.stackTrace }">
            <pre class="max-h-96 overflow-auto whitespace-pre-wrap break-all">{{
              detail.stackTrace
            }}</pre>
          </TypographyParagraph>
        </template>
      </template>
    </DetailModal>
  </Page>
</template>
