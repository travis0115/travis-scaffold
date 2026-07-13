<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

import { ref } from 'vue';

import {
  Page,
  useVbenDrawer,
  useVbenModal,
  confirm as vbenConfirm,
} from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { formatDate } from '@vben/utils';

import { Button, Tag } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteNotice,
  getNoticePage,
  updateNoticePinned,
  updateNoticeStatus,
} from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});
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
      hasAccessCode(SYSTEM_PERMS.noticeUpdate) ? onPinnedChange : undefined,
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

const [PreviewModal, previewModalApi] = useVbenModal({
  closeOnClickModal: true,
  footer: false,
});

async function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemNoticeApi.Notice>) {
  if (code === 'preview') onPreview(row);
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') deleteNotice(row.id).then(() => gridApi.query());
  if (code === 'publish') {
    try {
      const isRepublish = row.status === 2;
      await vbenConfirm(
        isRepublish
          ? `确认重新发布公告“${row.title}”吗？`
          : `确认发布公告“${row.title}”吗？`,
        isRepublish ? '重新发布' : '发布公告',
      );
    } catch {
      return;
    }
    await updateNoticeStatus(row.id, 1);
    await gridApi.query();
  }
  if (code === 'revoke') {
    try {
      await vbenConfirm(`确认撤回公告“${row.title}”吗？`, '撤回公告');
    } catch {
      return;
    }
    await updateNoticeStatus(row.id, 2);
    await gridApi.query();
  }
}

function onPreview(row: SystemNoticeApi.Notice) {
  previewRow.value = row;
  previewModalApi.open();
}

async function onPinnedChange(isPinned: number, row: SystemNoticeApi.Notice) {
  await updateNoticePinned(row.id, isPinned);
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
          <Plus class="size-5" />
          新增公告
        </Button>
      </template>
      <template #title="{ row }">
        <button
          class="text-foreground hover:text-primary block w-full cursor-pointer border-0 bg-transparent p-0 text-center"
          type="button"
          @click="onPreview(row)"
        >
          {{ row.title }}
        </button>
      </template>
    </Grid>
    <PreviewModal
      class="w-[860px]"
      :fullscreen-button="false"
      :title="previewRow?.title || '公告详情'"
    >
      <template #title>
        <div class="flex min-w-0 items-start gap-3">
          <span class="h-5 w-1.5 shrink-0 rounded-full bg-primary"></span>
          <div class="min-w-0 space-y-2">
            <div class="flex min-w-0 items-center gap-3">
              <span class="min-w-0 truncate">
                {{ previewRow?.title || '公告详情' }}
              </span>
              <Tag
                v-if="previewRow?.isPinned === 1"
                class="shrink-0 text-[10px] leading-4"
                :style="pinnedTagStyle"
              >
                置顶
              </Tag>
            </div>
            <div class="text-muted-foreground text-xs font-normal">
              {{
                formatDate(previewRow?.publishTime || previewRow?.createTime)
              }}
            </div>
          </div>
        </div>
      </template>
      <RichTextPreview :content="previewRow?.content" :min-height="320" />
    </PreviewModal>
  </Page>
</template>
