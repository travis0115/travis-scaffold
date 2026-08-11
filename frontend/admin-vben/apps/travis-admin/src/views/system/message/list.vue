<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemMessageApi } from '#/api';

import { onMounted, onUnmounted, ref } from 'vue';

import {
  Page,
  useVbenDrawer,
  useVbenModal,
  confirm as vbenConfirm,
} from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import { Button } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMessage,
  getInboxMessageDetail,
  getMessageDetail,
  getMessagePage,
  pushMessage,
  revokeMessage,
} from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});
const previewRow = ref<
  SystemMessageApi.Message | SystemMessageApi.UserMessageDetail
>();
const [PreviewModal, previewModalApi] = useVbenModal({
  closeOnClickModal: true,
  footer: false,
});
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    fieldMappingTime: [
      [
        'publishDateRange',
        ['publishStartDate', 'publishEndDate'],
        null,
      ],
    ],
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page }, values) =>
          getMessagePage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...values,
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemMessageApi.Message>,
});

function refreshMessagePage() {
  void gridApi.query();
}

onMounted(() => {
  window.addEventListener('travis:message-inbox-changed', refreshMessagePage);
});

onUnmounted(() => {
  window.removeEventListener(
    'travis:message-inbox-changed',
    refreshMessagePage,
  );
});

async function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemMessageApi.Message>) {
  if (code === 'edit') formDrawerApi.setData(row).open();
  if (code === 'delete') {
    try {
      await vbenConfirm(`确认删除消息“${row.title}”吗？`, '删除消息');
    } catch {
      return;
    }
    await deleteMessage(row.id);
    await gridApi.query();
  }
  if (code === 'push') {
    await pushMessage(row.id);
    await gridApi.query();
  }
  if (code === 'revoke') {
    try {
      await vbenConfirm(`确认撤回消息“${row.title}”吗？`, '撤回消息');
    } catch {
      return;
    }
    await revokeMessage(row.id);
    await gridApi.query();
  }
  if (code === 'preview') void onPreview(row);
}

async function onPreview(row: SystemMessageApi.Message) {
  previewRow.value =
    row.sourceType && row.sourceType !== 'MANUAL'
      ? await getInboxMessageDetail(row.id)
      : await getMessageDetail(row.id);
  previewModalApi.open();
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
          <Plus class="size-5" />
          新增消息
        </Button>
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
            <div class="min-w-0 truncate">
              {{ previewRow?.title || '消息预览' }}
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
