<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '../../../adapter/vxe-table';
import type { SystemNoticeApi } from '../../../api';

import { Page } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '../../../adapter/vxe-table';
import {
  clearMessages,
  deleteMessage,
  getMessagePage,
  markAllMessagesRead,
  markMessageRead,
} from '../../../api';

import { useColumns, useGridFormSchema } from './data';

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getMessagePage({ pageNum: page.currentPage, pageSize: page.pageSize, ...values }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemNoticeApi.UserMessage>,
});

async function onActionClick({ code, row }: OnActionClickParams<SystemNoticeApi.UserMessage>) {
  if (code === 'read') await markMessageRead(row.id);
  if (code === 'delete') await deleteMessage(row.id);
  await gridApi.query();
}

async function markAllRead() {
  await markAllMessagesRead();
  await gridApi.query();
}

async function clearAll() {
  await clearMessages();
  await gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="我的消息">
      <template #toolbar-tools>
        <Button @click="markAllRead">全部已读</Button>
        <Button danger @click="clearAll">清空消息</Button>
      </template>
    </Grid>
  </Page>
</template>
