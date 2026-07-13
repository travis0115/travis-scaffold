<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '../../../adapter/vxe-table';
import type { SystemMessageApi } from '../../../api';

import { onMounted, onUnmounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import { Button, Tag } from 'antdv-next';

import RichTextPreview from '#/components/rich-text-preview/index.vue';

import { useVbenVxeGrid } from '../../../adapter/vxe-table';
import {
  deleteInboxMessage,
  getInboxMessageDetail,
  getInboxMessagePage,
  markAllMessagesRead,
  markMessageRead,
} from '../../../api';
import { useColumns, useGridFormSchema } from './data';

const previewRow = ref<SystemMessageApi.UserMessageDetail>();
const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};
const [PreviewModal, previewModalApi] = useVbenModal({
  closeOnClickModal: true,
  footer: false,
});

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getInboxMessagePage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemMessageApi.UserMessage>,
});

async function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemMessageApi.UserMessage>) {
  if (code === 'preview') await onPreview(row);
  if (code === 'read') await markMessageRead(row.id);
  if (code === 'delete') await deleteInboxMessage(row.id);
  if (code === 'read' || code === 'delete') {
    window.dispatchEvent(new CustomEvent('travis:message-inbox-changed'));
  }
  await gridApi.query();
}

async function onPreview(row: SystemMessageApi.UserMessage) {
  if (row.readStatus === 0) {
    await markMessageRead(row.id);
    window.dispatchEvent(new CustomEvent('travis:message-inbox-changed'));
    row.readStatus = 1;
  }
  previewRow.value = await getInboxMessageDetail(row.messageId);
  previewModalApi.open();
}

async function markAllRead() {
  await markAllMessagesRead();
  window.dispatchEvent(new CustomEvent('travis:message-inbox-changed'));
  await gridApi.query();
}

function refreshInbox() {
  void gridApi.query();
}

onMounted(() => {
  window.addEventListener('travis:message-inbox-changed', refreshInbox);
});

onUnmounted(() => {
  window.removeEventListener('travis:message-inbox-changed', refreshInbox);
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="我的消息">
      <template #toolbar-tools>
        <Button @click="markAllRead">全部已读</Button>
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
      :title="previewRow?.title || '消息预览'"
    >
      <template #title>
        <div class="flex min-w-0 items-start gap-3">
          <span class="h-5 w-1.5 shrink-0 rounded-full bg-primary"></span>
          <div class="min-w-0 space-y-2">
            <div class="flex min-w-0 items-center gap-3">
              <span class="min-w-0 truncate">{{
                previewRow?.title || '消息预览'
              }}</span>
              <Tag
                v-if="previewRow?.metadata?.version"
                :style="versionTagStyle"
              >
                {{ formatVersion(previewRow.metadata.version) }}
              </Tag>
            </div>
            <div class="text-muted-foreground text-xs font-normal">
              {{
                formatDateTime(
                  previewRow?.publishTime || previewRow?.createTime,
                )
              }}
            </div>
          </div>
        </div>
      </template>
      <RichTextPreview :content="previewRow?.content" :min-height="320" />
    </PreviewModal>
  </Page>
</template>
