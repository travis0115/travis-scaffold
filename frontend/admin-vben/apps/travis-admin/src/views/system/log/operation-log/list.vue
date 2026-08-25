<script lang="ts" setup>
import type { CSSProperties } from 'vue';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api';

import { EllipsisText, Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOperationLogPage } from '#/api';

import { useColumns, useGridFormSchema } from './data';

const tableCellTooltipMaxWidth = 640;
const tableCellTooltipOverlayStyle: CSSProperties = {
  textAlign: 'left',
  whiteSpace: 'normal',
  wordBreak: 'break-all',
};

const [Grid] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['operationTimeRange', ['startTime', 'endTime']]],
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) => {
          return getOperationLogPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          });
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<SystemOperationLogApi.OperationLog>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="操作日志">
      <template #operationInfo="{ row }">
        <div class="table-cell-pre-line">
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            模块：{{ row.module || '-' }}
          </EllipsisText>
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            操作：{{ row.description || '-' }}
          </EllipsisText>
        </div>
      </template>
      <template #requestInfo="{ row }">
        <div class="table-cell-pre-line">
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            地址：{{ row.requestUrl || '-' }}
          </EllipsisText>
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            请求ID：{{ row.requestId || '-' }}
          </EllipsisText>
        </div>
      </template>
      <template #ipInfo="{ row }">
        <div class="table-cell-pre-line">
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            {{ row.ip || '-' }}
          </EllipsisText>
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            {{ row.location || '-' }}
          </EllipsisText>
        </div>
      </template>
      <template #deviceInfo="{ row }">
        <div class="table-cell-pre-line">
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            浏览器：{{ row.browser || '-' }}
          </EllipsisText>
          <EllipsisText
            class="table-cell-ellipsis"
            :tooltip-max-width="tableCellTooltipMaxWidth"
            :tooltip-overlay-style="tableCellTooltipOverlayStyle"
            tooltip-when-ellipsis
          >
            系统：{{ row.os || '-' }}
          </EllipsisText>
        </div>
      </template>
    </Grid>
  </Page>
</template>

<style scoped>
.table-cell-pre-line {
  line-height: 1.5rem;
  text-align: center;
  white-space: pre-line;
}

.table-cell-ellipsis {
  width: 100%;
}
</style>
