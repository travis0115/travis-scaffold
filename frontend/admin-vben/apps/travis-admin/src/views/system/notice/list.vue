<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

import { ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import { Button, Modal, Tag } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteNotice, getNoticePage, updateNoticeStatus } from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});
const previewOpen = ref(false);
const previewRow = ref<SystemNoticeApi.Notice>();
const pinnedTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.noticeUpdate) ? onStatusChange : undefined,
    ),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getNoticePage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemNoticeApi.Notice>,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemNoticeApi.Notice>) {
  if (code === 'preview') {
    previewRow.value = row;
    previewOpen.value = true;
  }
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') deleteNotice(row.id).then(() => gridApi.query());
}

async function onStatusChange(newStatus: number, row: SystemNoticeApi.Notice) {
  await updateNoticeStatus(row.id, newStatus);
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
          v-access:code="SYSTEM_PERMS.noticeCreate"
          type="primary"
          @click="formDrawerApi.setData({}).open()"
        >
          新增公告
        </Button>
      </template>
    </Grid>
    <Modal
      v-model:open="previewOpen"
      :footer="null"
      title="公告预览"
      width="860px"
    >
      <div class="space-y-4">
        <div class="border-t pt-4">
          <div class="flex items-center gap-3 text-gray-400 text-xs">
            <Tag v-if="previewRow?.pinned === 1" :style="pinnedTagStyle">
              置顶
            </Tag>
            <span>{{
              formatDate(previewRow?.publishTime || previewRow?.createTime)
            }}</span>
          </div>
        </div>
        <h3 class="text-foreground text-lg font-semibold">
          {{ previewRow?.title || '公告内容' }}
        </h3>
        <RichTextPreview :content="previewRow?.content" :min-height="320" />
      </div>
    </Modal>
  </Page>
</template>
