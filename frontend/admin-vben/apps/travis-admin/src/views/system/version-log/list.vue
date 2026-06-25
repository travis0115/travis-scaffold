<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemVersionLogApi } from '#/api';

import { ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';
import { formatDate } from '@vben/utils';

import { Button, message, Modal, Tag } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteVersionLog,
  getVersionLogPage,
  updateVersionLogStatus,
} from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { $t } from '#/locales';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});
const previewOpen = ref(false);
const previewRow = ref<SystemVersionLogApi.VersionLog>();
const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.versionUpdate) ? onStatusChange : undefined,
    ),
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
          return await getVersionLogPage({
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
  } as VxeTableGridOptions<SystemVersionLogApi.VersionLog>,
});

function onActionClick(e: OnActionClickParams<SystemVersionLogApi.VersionLog>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
    case 'preview': {
      onPreview(e.row);
      break;
    }
  }
}

function onEdit(row: SystemVersionLogApi.VersionLog) {
  formDrawerApi.setData(row).open();
}

function onDelete(row: SystemVersionLogApi.VersionLog) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.title]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteVersionLog(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.title]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onPreview(row: SystemVersionLogApi.VersionLog) {
  previewRow.value = row;
  previewOpen.value = true;
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

async function onStatusChange(
  newStatus: number,
  row: SystemVersionLogApi.VersionLog,
) {
  await updateVersionLogStatus(row.id, newStatus as 0 | 1);
  onRefresh();
  return true;
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.version.list')">
      <template #toolbar-tools>
        <Button
          v-access:code="SYSTEM_PERMS.versionCreate"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.version.name')]) }}
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
    <Modal
      v-model:open="previewOpen"
      :footer="null"
      title="版本预览"
      width="860px"
    >
      <div class="space-y-4">
        <div class="border-t pt-4">
          <div class="flex items-center gap-3 text-gray-400 text-xs">
            <Tag :style="versionTagStyle">
              {{ formatVersion(previewRow?.version) }}
            </Tag>
            <span>{{
              formatDate(previewRow?.publishTime || previewRow?.createTime)
            }}</span>
          </div>
        </div>
        <h3 class="text-foreground text-lg font-semibold">
          {{ previewRow?.title || $t('system.version.content') }}
        </h3>
        <RichTextPreview :content="previewRow?.content" :min-height="320" />
      </div>
    </Modal>
  </Page>
</template>
