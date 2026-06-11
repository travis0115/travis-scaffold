<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

import { Page, useVbenDrawer } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteNotice, getNoticePage } from '#/api';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({ connectedComponent: Form, destroyOnClose: true });
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: { ajax: { query: ({ page }, values) => getNoticePage({ pageNum: page.currentPage, pageSize: page.pageSize, ...values }) } },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemNoticeApi.Notice>,
});

function onActionClick({ code, row }: OnActionClickParams<SystemNoticeApi.Notice>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') deleteNotice(row.id).then(() => gridApi.query());
}
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="gridApi.query()" />
    <Grid table-title="通知公告">
      <template #toolbar-tools><Button type="primary" @click="formDrawerApi.setData({}).open()">新增公告</Button></template>
    </Grid>
  </Page>
</template>
