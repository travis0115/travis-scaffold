<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { Page, useVbenDrawer } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMessagePush, getMessagePushPage, updateMessagePushStatus } from '#/api';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({ connectedComponent: Form, destroyOnClose: true });
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.messageUpdate) ? onStatusChange : undefined,
    ),
    height: 'auto',
    proxyConfig: { ajax: { query: ({ page }, values) => getMessagePushPage({ pageNum: page.currentPage, pageSize: page.pageSize, ...values }) } },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemMessageApi.Message>,
});

function onActionClick({ code, row }: OnActionClickParams<SystemMessageApi.Message>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') deleteMessagePush(row.id).then(() => gridApi.query());
}

async function onStatusChange(newStatus: number, row: SystemMessageApi.Message) {
  await updateMessagePushStatus(row.id, newStatus);
  await gridApi.query();
  return true;
}
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="gridApi.query()" />
    <Grid table-title="消息推送">
      <template #toolbar-tools>
        <Button
          v-access:code="SYSTEM_PERMS.messageCreate"
          type="primary"
          @click="formDrawerApi.setData({}).open()"
        >
          新增消息
        </Button>
      </template>
    </Grid>
  </Page>
</template>
