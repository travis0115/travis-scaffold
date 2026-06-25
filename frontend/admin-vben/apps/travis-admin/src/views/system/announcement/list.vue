<script lang="ts" setup>
import type { OnActionClickParams, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemAnnouncementApi } from '#/api';

import { Page, useVbenDrawer } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteAnnouncement, getAnnouncementPage, updateAnnouncementStatus } from '#/api';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({ connectedComponent: Form, destroyOnClose: true });
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.announcementUpdate) ? onStatusChange : undefined,
    ),
    height: 'auto',
    proxyConfig: { ajax: { query: ({ page }, values) => getAnnouncementPage({ pageNum: page.currentPage, pageSize: page.pageSize, ...values }) } },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemAnnouncementApi.Announcement>,
});

function onActionClick({ code, row }: OnActionClickParams<SystemAnnouncementApi.Announcement>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') deleteAnnouncement(row.id).then(() => gridApi.query());
}

async function onStatusChange(newStatus: number, row: SystemAnnouncementApi.Announcement) {
  await updateAnnouncementStatus(row.id, newStatus);
  await gridApi.query();
  return true;
}
</script>

<template>
  <Page auto-content-height>
    <FormDrawer @success="gridApi.query()" />
    <Grid table-title="系统公告">
      <template #toolbar-tools>
        <Button
          v-access:code="SYSTEM_PERMS.announcementCreate"
          type="primary"
          @click="formDrawerApi.setData({}).open()"
        >
          新增公告
        </Button>
      </template>
    </Grid>
  </Page>
</template>
