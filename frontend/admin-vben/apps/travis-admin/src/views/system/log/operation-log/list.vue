<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemOperationLogApi } from '#/api';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getOperationLogPage } from '#/api';

import { useColumns, useGridFormSchema } from './data';

const [Grid] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getOperationLogPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
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
    <Grid table-title="操作日志" />
  </Page>
</template>
