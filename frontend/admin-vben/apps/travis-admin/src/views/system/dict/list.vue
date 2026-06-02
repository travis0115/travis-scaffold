<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDict, getDictPage } from '#/api';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import FormDrawerComponent from './modules/form.vue';
import ItemsDrawerComponent from './modules/items.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: FormDrawerComponent,
  destroyOnClose: true,
});

const [ItemsDrawer, itemsDrawerApi] = useVbenDrawer({
  connectedComponent: ItemsDrawerComponent,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getDictPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<SystemDictApi.SysDict>,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemDictApi.SysDict>) {
  switch (code) {
    case 'addItem': {
      onAddItem(row);
      break;
    }
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
  }
}

function onEdit(row: SystemDictApi.SysDict) {
  formDrawerApi.setData(row).open();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

function onDelete(row: SystemDictApi.SysDict) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.dictName]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteDict(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.dictName]),
        key: 'action_process_msg',
      });
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onAddItem(row: SystemDictApi.SysDict) {
  itemsDrawerApi.setData({ id: row.id, dictName: row.dictName }).open();
}

function onRefresh() {
  gridApi.query();
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <ItemsDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.dict.list')">
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.dict.name')]) }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>
