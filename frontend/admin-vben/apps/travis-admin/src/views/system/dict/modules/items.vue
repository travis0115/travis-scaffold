<script lang="ts" setup>
import type { SystemDictApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer, useVbenModal } from '@vben/common-ui';

import { Button, message, Modal, Table, Tag } from 'antdv-next';

import { deleteDictItem, getDictItems } from '#/api';
import { $t } from '#/locales';

import ItemModalComponent from './item-modal.vue';

const emit = defineEmits(['success']);

const items = ref<SystemDictApi.SysDictItem[]>([]);
const loading = ref(false);
const dictId = ref(0);
const dictName = ref('');

const columns = [
  { title: $t('system.dict.item.label'), dataIndex: 'label', width: 150 },
  { title: $t('system.dict.item.value'), dataIndex: 'value', width: 120 },
  { title: $t('system.dict.item.tagStyle'), dataIndex: 'tagStyle', width: 100 },
  { title: $t('system.dict.item.sort'), dataIndex: 'sort', width: 80 },
  { title: $t('system.dict.item.status'), dataIndex: 'status', width: 100 },
  { title: $t('system.dict.item.remark'), dataIndex: 'remark', ellipsis: true },
  {
    title: $t('system.dict.operation'),
    dataIndex: 'operation',
    width: 160,
    align: 'center' as const,
  },
];

const [ItemModal, itemModalApi] = useVbenModal({
  connectedComponent: ItemModalComponent,
  destroyOnClose: true,
});

async function loadItems() {
  loading.value = true;
  try {
    items.value = await getDictItems(dictId.value);
  } finally {
    loading.value = false;
  }
}

function onAddItem() {
  itemModalApi.setData({ dictId: dictId.value, dictName: dictName.value }).open();
}

function onEditItem(record: SystemDictApi.SysDictItem) {
  itemModalApi.setData({
    itemId: record.id,
    dictId: dictId.value,
    dictName: dictName.value,
    label: record.label,
    value: record.value,
    sort: record.sort ?? 0,
    status: record.status,
    remark: record.remark,
  }).open();
}

function onDeleteItem(record: SystemDictApi.SysDictItem) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除数据项「${record.label}」吗？`,
    async onOk() {
      await deleteDictItem(record.id);
      message.success($t('ui.actionMessage.deleteSuccess', [record.label]));
      await loadItems();
      emit('success');
    },
  });
}

function handleItemSuccess() {
  itemModalApi.close();
  loadItems();
  emit('success');
}

function getStatusColor(status: number) {
  return status === 1 ? 'success' : 'error';
}

function getStatusText(status: number) {
  return status === 1 ? $t('common.enabled') : $t('common.disabled');
}

const [ItemsDrawer, itemsDrawerApi] = useVbenDrawer({
  class: 'w-180',
  async onOpenChange(isOpen: boolean) {
    if (isOpen) {
      const data = itemsDrawerApi.getData<{ dictName: string; id: number }>();
      if (data) {
        dictId.value = data.id;
        dictName.value = data.dictName;
        await loadItems();
      }
    }
  },
});
</script>

<template>
  <ItemsDrawer :title="`${$t('system.dict.items')} - ${dictName}`">
    <ItemModal @success="handleItemSuccess" />
    <div class="mb-3 flex items-center justify-end">
      <Button type="primary" @click="onAddItem">
        {{ $t('system.dict.item.addItem') }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :data-source="items"
      :loading="loading"
      row-key="id"
      size="small"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <Tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </Tag>
        </template>
        <template v-else-if="column.dataIndex === 'remark'">
          {{ record.remark || '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'operation'">
          <Button type="link" size="small" @click="onEditItem(record)">
            {{ $t('common.edit') }}
          </Button>
          <Button type="link" danger size="small" @click="onDeleteItem(record)">
            {{ $t('common.delete') }}
          </Button>
        </template>
      </template>
    </Table>
  </ItemsDrawer>
</template>
