<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { nextTick, ref } from 'vue';

import { ColPage, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDict, getDictPage } from '#/api';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import FormDrawerComponent from './modules/form.vue';
import ItemsPanel from './modules/items.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: FormDrawerComponent,
  destroyOnClose: true,
});

const selectedDict = ref<SystemDictApi.SysDict>();

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    commonConfig: {
      componentProps: {
        class: 'w-full',
      },
    },
    schema: useGridFormSchema(),
    submitOnChange: false,
    wrapperClass: 'grid-cols-2',
  },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const result = await getDictPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
          const current = result.records.find(
            (item) => item.id === selectedDict.value?.id,
          );
          selectedDict.value = current ?? result.records[0];
          nextTick(() => {
            if (selectedDict.value) gridApi.grid.setCurrentRow(selectedDict.value);
          });
          return result;
        },
      },
    },
    rowConfig: {
      isCurrent: true,
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
  gridEvents: {
    cellClick: ({ row }: { row: SystemDictApi.SysDict }) => {
      selectedDict.value = row;
      gridApi.grid.setCurrentRow(row);
    },
  },
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemDictApi.SysDict>) {
  switch (code) {
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
      if (selectedDict.value?.id === row.id) selectedDict.value = undefined;
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onRefresh() {
  gridApi.query();
}
</script>
<template>
  <ColPage
    auto-content-height
    :left-min-width="25"
    :left-width="50"
    :right-min-width="40"
    :right-width="50"
    split-handle
    split-line
  >
    <FormDrawer @success="onRefresh" />
    <template #left>
      <Grid :table-title="$t('system.dict.list')">
        <template #toolbar-tools>
          <Button type="primary" @click="onCreate">
            <Plus class="size-5" />
            {{ $t('ui.actionTitle.create', [$t('system.dict.name')]) }}
          </Button>
        </template>
      </Grid>
    </template>
    <ItemsPanel :dict="selectedDict" @success="onRefresh" />
  </ColPage>
</template>
