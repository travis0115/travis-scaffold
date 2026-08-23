<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { OpsErrorLogApi } from '#/api';

import { reactive, ref } from 'vue';

import { Page, useVbenModal, confirm as vbenConfirm } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Alert,
  Button,
  Descriptions,
  DescriptionsItem,
  Divider,
  Empty,
  Form,
  FormItem,
  Input,
  message,
  Select,
  Tag,
  Timeline,
  TimelineItem,
  TypographyParagraph,
} from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteErrorLog,
  getErrorLogDetail,
  getErrorLogPage,
  handleAllPendingErrorLogs,
  handleErrorLog,
} from '#/api';
import { getDictLabel } from '#/utils/dict';
import { OPS_PERMS } from '#/utils/permissions';

import {
  ERROR_LOG_HANDLE_STATUS_DICT,
  handleStatusOptions,
  platformTypeOptions,
  useColumns,
  useGridFormSchema,
} from './data';

const detail = ref<OpsErrorLogApi.ErrorLog>();
const handleTarget = ref<OpsErrorLogApi.ErrorLog>();
const isBatchHandle = ref(false);
const handleForm = reactive<{ remark: string; status: 1 | 2 }>({
  remark: '',
  status: 1,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['exceptionTimeRange', ['startTime', 'endTime']]],
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getErrorLogPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<OpsErrorLogApi.ErrorLog>,
});

const [DetailModal, detailModalApi] = useVbenModal({ footer: false });

const [HandleModal, handleModalApi] = useVbenModal({
  async onConfirm() {
    handleModalApi.lock();
    try {
      const data = {
        remark: handleForm.remark.trim() || undefined,
        status: handleForm.status,
      };
      if (isBatchHandle.value) {
        const count = await handleAllPendingErrorLogs(data);
        message.success(
          count > 0 ? `已批量处理 ${count} 条错误日志` : '暂无待处理错误日志',
        );
      } else if (handleTarget.value) {
        await handleErrorLog(handleTarget.value.id, data);
        message.success('错误日志已处理');
      }
      handleModalApi.close();
      await gridApi.query();
    } catch {
      handleModalApi.unlock();
    }
  },
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<OpsErrorLogApi.ErrorLog>) {
  if (code === 'preview') openDetail(row);
  if (code === 'edit') openHandle(row);
  if (code === 'delete') onDelete(row);
}

async function openDetail(row: OpsErrorLogApi.ErrorLog) {
  detail.value = await getErrorLogDetail(row.id);
  detailModalApi.open();
}

function openHandle(row: OpsErrorLogApi.ErrorLog) {
  isBatchHandle.value = false;
  handleTarget.value = row;
  handleForm.status = 1;
  handleForm.remark = row.handleRemark || '';
  handleModalApi.open();
}

function openBatchHandle() {
  isBatchHandle.value = true;
  handleTarget.value = undefined;
  handleForm.status = 1;
  handleForm.remark = '';
  handleModalApi.open();
}

function onDelete(row: OpsErrorLogApi.ErrorLog) {
  vbenConfirm({
    content: `删除后将同时移除该异常的 ${row.occurrenceCount} 条发生明细，且无法恢复。`,
    confirmText: '确认删除',
    icon: 'warning',
    title: '确认删除错误日志',
  })
    .then(async () => {
      await deleteErrorLog(row.id);
      message.success('错误日志已删除');
      await gridApi.query();
    })
    .catch(() => {});
}

function statusLabel(status?: number) {
  return getDictLabel(ERROR_LOG_HANDLE_STATUS_DICT, status);
}

function statusColor(status?: number) {
  return handleStatusOptions.find((item) => item.value === status)?.color;
}

function platformLabel(platformType?: string) {
  return (
    platformTypeOptions.find((item) => item.value === platformType)?.label ||
    platformType ||
    '-'
  );
}

function sourceInfoTitle(row: OpsErrorLogApi.ErrorLog) {
  return [
    `平台：${platformLabel(row.platformType)}`,
    `模块：${row.moduleName || '-'}`,
    `来源：${row.sourceType || '-'}`,
  ].join('\n');
}

function requestInfoTitle(row: OpsErrorLogApi.ErrorLog) {
  if (!row.requestId && !row.requestUrl && !row.ip) {
    return `业务键：${row.businessKey || '-'}`;
  }
  return [
    `请求ID：${row.requestId || '-'}`,
    `请求地址：${row.requestUrl || '-'}`,
    `请求IP：${row.ip || '-'}`,
  ].join('\n');
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="错误日志">
      <template #toolbar-tools>
        <Button
          v-access:code="OPS_PERMS.errorLogHandle"
          type="primary"
          @click="openBatchHandle"
        >
          批量处理全部
        </Button>
      </template>
      <template #sourceInfo="{ row }">
        <div class="table-cell-pre-line" :title="sourceInfoTitle(row)">
          <span>平台：{{ platformLabel(row.platformType) }}</span>
          <span>模块：{{ row.moduleName || '-' }}</span>
          <span>来源：{{ row.sourceType || '-' }}</span>
        </div>
      </template>
      <template #requestInfo="{ row }">
        <div class="table-cell-pre-line" :title="requestInfoTitle(row)">
          <template v-if="row.requestId || row.requestUrl || row.ip">
            <span>请求ID：{{ row.requestId || '-' }}</span>
            <span>请求地址：{{ row.requestUrl || '-' }}</span>
            <span>请求IP：{{ row.ip || '-' }}</span>
          </template>
          <span v-else>业务键：{{ row.businessKey || '-' }}</span>
        </div>
      </template>
      <template #errorMessage="{ row }">
        <div class="table-cell-pre-line">
          <span>{{ row.exceptionClass || '-' }}</span>
          <span>{{ row.message || '-' }}</span>
        </div>
      </template>
      <template #occurrenceTime="{ row }">
        <div class="table-cell-pre-line">
          <span>首次：{{ formatDateTime(row.firstOccurrenceTime) }}</span>
          <span>最后：{{ formatDateTime(row.lastOccurrenceTime) }}</span>
        </div>
      </template>
    </Grid>

    <DetailModal class="w-[860px]" title="错误日志详情">
      <template v-if="detail">
        <Divider title-placement="start">状态概览</Divider>
        <Descriptions bordered :column="2" size="small">
          <DescriptionsItem label="处理状态">
            <Tag :color="statusColor(detail.status)">
              {{ statusLabel(detail.status) }}
            </Tag>
          </DescriptionsItem>
          <DescriptionsItem label="发生次数">
            {{ detail.occurrenceCount }}
          </DescriptionsItem>
          <DescriptionsItem label="首次发生时间">
            {{ formatDateTime(detail.firstOccurrenceTime) }}
          </DescriptionsItem>
          <DescriptionsItem label="最近发生时间">
            {{ formatDateTime(detail.lastOccurrenceTime) }}
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">异常信息</Divider>
        <Descriptions
          bordered
          :column="2"
          size="small"
          :styles="{ label: { whiteSpace: 'nowrap', width: '120px' } }"
        >
          <DescriptionsItem label="模块名称">
            {{ detail.moduleName || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="来源类型">
            {{ detail.sourceType || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="来源名称" :span="2">
            {{ detail.sourceName || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="异常类型" :span="2">
            {{ detail.exceptionClass }}
          </DescriptionsItem>
          <DescriptionsItem label="异常消息" :span="2">
            {{ detail.message || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="业务键" :span="2">
            {{ detail.businessKey || '-' }}
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">请求信息</Divider>
        <Descriptions bordered :column="2" size="small">
          <DescriptionsItem label="平台类型">
            {{ platformLabel(detail.platformType) }}
          </DescriptionsItem>
          <DescriptionsItem label="请求方式">
            {{ detail.requestMethod || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="请求 ID">
            {{ detail.requestId || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="Trace ID">
            {{ detail.traceId || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="请求 IP">
            {{ detail.ip || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="用户名">
            {{ detail.username || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="请求地址" :span="2">
            {{ detail.requestUrl || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="控制器方法" :span="2">
            {{ detail.controllerMethod || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="脱敏请求参数" :span="2">
            <pre class="whitespace-pre-wrap break-all">{{
              detail.requestParams || '-'
            }}</pre>
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">环境信息</Divider>
        <Descriptions bordered :column="3" size="small">
          <DescriptionsItem label="应用名称">
            {{ detail.applicationName || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="应用版本">
            {{ detail.applicationVersion || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="实例名称">
            {{ detail.instanceName || '-' }}
          </DescriptionsItem>
        </Descriptions>

        <Divider title-placement="start">异常堆栈</Divider>
        <TypographyParagraph
          :copyable="detail.stackTrace ? { text: detail.stackTrace } : false"
        >
          <pre class="max-h-96 overflow-auto whitespace-pre-wrap break-all">{{
            detail.stackTrace || '-'
          }}</pre>
        </TypographyParagraph>

        <Divider title-placement="start">最近发生明细</Divider>
        <Timeline v-if="detail.occurrences?.length">
          <TimelineItem v-for="item in detail.occurrences ?? []" :key="item.id">
            <Descriptions
              bordered
              class="occurrence-descriptions"
              :column="2"
              size="small"
              :styles="{ label: { whiteSpace: 'nowrap', width: '120px' } }"
            >
              <DescriptionsItem label="发生时间">
                {{ formatDateTime(item.occurredTime) }}
              </DescriptionsItem>
              <DescriptionsItem label="用户">
                {{ item.username || '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="请求 ID">
                {{ item.requestId || '-' }}
              </DescriptionsItem>
              <DescriptionsItem label="请求 IP">
                {{ item.ip || '-' }}
              </DescriptionsItem>
            </Descriptions>
          </TimelineItem>
        </Timeline>
        <Empty v-else description="暂无发生明细" />

        <Divider title-placement="start">处理记录</Divider>
        <Descriptions
          bordered
          class="handle-record-descriptions"
          :column="2"
          size="small"
          :styles="{ label: { whiteSpace: 'nowrap', width: '120px' } }"
        >
          <DescriptionsItem label="处理人">
            {{ detail.handledByUsername || '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="处理时间">
            {{ detail.handledTime ? formatDateTime(detail.handledTime) : '-' }}
          </DescriptionsItem>
          <DescriptionsItem label="处理备注" :span="2">
            {{ detail.handleRemark || '-' }}
          </DescriptionsItem>
        </Descriptions>
      </template>
    </DetailModal>

    <HandleModal
      :title="isBatchHandle ? '批量处理全部错误日志' : '处理错误日志'"
    >
      <Form layout="vertical">
        <Alert
          v-if="isBatchHandle"
          class="mb-4"
          message="将处理系统中全部待处理错误日志，不受当前筛选条件影响。"
          show-icon
          type="warning"
        />
        <FormItem label="处理结果" required>
          <Select
            v-model:value="handleForm.status"
            :options="handleStatusOptions.filter((item) => item.value !== 0)"
          />
        </FormItem>
        <FormItem label="处理备注">
          <Input.TextArea
            v-model:value="handleForm.remark"
            :maxlength="500"
            :rows="6"
            show-count
          />
        </FormItem>
      </Form>
    </HandleModal>
  </Page>
</template>

<style scoped>
.table-cell-pre-line {
  display: flex;
  flex-direction: column;
  line-height: 1.5rem;
  text-align: left;
}

.occurrence-descriptions :deep(.ant-descriptions-view table) {
  table-layout: fixed;
}

.handle-record-descriptions :deep(.ant-descriptions-view table) {
  table-layout: fixed;
}

.occurrence-descriptions :deep(.ant-descriptions-row > :nth-child(2)) {
  width: calc(65% - 120px);
}

.occurrence-descriptions :deep(.ant-descriptions-row > :nth-child(4)) {
  width: calc(35% - 120px);
}
</style>
