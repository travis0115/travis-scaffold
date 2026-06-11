<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridColumns,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { computed, nextTick, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDictItem, getDictItems } from '#/api';
import { $t } from '#/locales';

import ItemModalComponent from './item-modal.vue';

const props = defineProps<{
  dict?: SystemDictApi.SysDict;
}>();
const emit = defineEmits(['success']);

const columns: VxeTableGridColumns<SystemDictApi.SysDictItem> = [
  { field: 'label', minWidth: 120, title: $t('system.dict.item.label') },
  { field: 'value', minWidth: 100, title: $t('system.dict.item.value') },
  {
    cellRender: {
      name: 'CellTag',
      options: [
        { color: 'default', label: '默认', value: 'default' },
        { color: 'processing', label: '主要', value: 'primary' },
        { color: 'success', label: '成功', value: 'success' },
        { color: 'warning', label: '警告', value: 'warning' },
        { color: 'error', label: '危险', value: 'danger' },
        { color: 'blue', label: '信息', value: 'info' },
      ],
    },
    field: 'tagStyle',
    title: $t('system.dict.item.tagStyle'),
    width: 90,
  },
  { field: 'sort', title: $t('system.dict.item.sort'), width: 70 },
  {
    field: 'remark',
    formatter: 'emptyPlaceholder',
    minWidth: 120,
    title: $t('system.dict.item.remark'),
  },
  {
    cellRender: { name: 'CellTag' },
    field: 'status',
    fixed: 'right',
    title: $t('system.dict.item.status'),
    width: 90,
  },
  {
    align: 'center',
    cellRender: {
      attrs: {
        nameField: 'label',
        nameTitle: $t('system.dict.item.label'),
        onClick: onActionClick,
      },
      name: 'CellOperation',
      options: ['edit', 'delete'],
    },
    field: 'operation',
    fixed: 'right',
    title: $t('system.dict.operation'),
    width: 140,
  },
];

const tableTitle = computed(() =>
  props.dict ? `字典数据 - ${props.dict.dictName}` : '字典数据',
);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns,
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          return props.dict?.id ? await getDictItems(props.dict.id) : [];
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
      zoom: true,
    },
  } as VxeTableGridOptions<SystemDictApi.SysDictItem>,
});

const [ItemModal, itemModalApi] = useVbenModal({
  connectedComponent: ItemModalComponent,
  destroyOnClose: true,
});

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemDictApi.SysDictItem>) {
  if (code === 'edit') onEditItem(row);
  if (code === 'delete') onDeleteItem(row);
}

function onAddItem() {
  if (!props.dict) return;
  itemModalApi
    .setData({ dictId: props.dict.id, dictName: props.dict.dictName })
    .open();
}

function onEditItem(record: SystemDictApi.SysDictItem) {
  itemModalApi.setData({
    itemId: record.id,
    dictId: props.dict?.id,
    dictName: props.dict?.dictName,
    label: record.label,
    value: record.value,
    sort: record.sort ?? 0,
    status: record.status,
    remark: record.remark,
    tagStyle: record.tagStyle,
  }).open();
}

async function onDeleteItem(record: SystemDictApi.SysDictItem) {
  await deleteDictItem(record.id);
  message.success($t('ui.actionMessage.deleteSuccess', [record.label]));
  await gridApi.query();
  emit('success');
}

async function refreshItems() {
  await nextTick();
  await gridApi.query();
}

function handleItemSuccess() {
  itemModalApi.close();
  refreshItems();
  emit('success');
}

watch(
  () => props.dict?.id,
  () => {
    refreshItems();
  },
);
</script>

<template>
  <div class="h-full">
    <ItemModal @success="handleItemSuccess" />
    <Grid :table-title="tableTitle">
      <template #toolbar-tools>
        <Button type="primary" :disabled="!props.dict" @click="onAddItem">
          <Plus class="size-5" />
          {{ $t('system.dict.item.addItem') }}
        </Button>
      </template>
    </Grid>
  </div>
</template>
