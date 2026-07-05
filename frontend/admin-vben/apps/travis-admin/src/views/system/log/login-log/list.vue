<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemLoginLogApi } from '#/api';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLoginLogList } from '#/api';

import { useColumns, useGridFormSchema } from './data';

const [Grid] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [['loginTimeRange', ['startTime', 'endTime']]],
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page, sort }, formValues) => {
          const orderParams = sort?.order
            ? {
                asc: sort.order === 'asc',
                orderBy: sort.field || sort.property,
              }
            : {};
          return await getLoginLogList({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...orderParams,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<SystemLoginLogApi.LoginLog>,
});
</script>
<template>
  <Page auto-content-height>
    <Grid table-title="登录日志" />
  </Page>
</template>
