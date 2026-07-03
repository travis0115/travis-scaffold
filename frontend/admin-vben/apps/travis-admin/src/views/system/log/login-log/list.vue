<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemLoginLogApi } from '#/api';

import { Page } from '@vben/common-ui';
import { formatLocalDateToUtc } from '@vben/utils';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLoginLogList } from '#/api';

import { useColumns, useGridFormSchema } from './data';

function buildQueryParams(formValues: Record<string, any>) {
  const { loginTimeRange, ...values } = formValues;
  const [startTime, endTime] = Array.isArray(loginTimeRange)
    ? loginTimeRange
    : [];
  return {
    ...values,
    ...(startTime ? { startTime: formatLocalDateToUtc(startTime) } : {}),
    ...(endTime ? { endTime: formatLocalDateToUtc(endTime) } : {}),
  };
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
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
            ...buildQueryParams(formValues),
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    sortConfig: {
      remote: true,
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
