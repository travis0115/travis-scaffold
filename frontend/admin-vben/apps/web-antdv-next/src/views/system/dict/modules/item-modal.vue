<script lang="ts" setup>
import type { SystemDictApi } from '#/api';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { Button, message, Table } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import {
  createDictItem,
  deleteDictItem,
  getDictItems,
  updateDictItem,
} from '#/api';
import { $t } from '#/locales';

import { useItemColumns, useItemFormSchema } from '../data';

const emit = defineEmits(['success']);

const dictId = ref<number>();
const loading = ref(false);
const items = ref<SystemDictApi.SysDictItem[]>([]);
const editingItem = ref<SystemDictApi.SysDictItem>();

const [ItemForm, itemFormApi] = useVbenForm({
  schema: useItemFormSchema(),
  showDefaultActions: false,
  layout: 'inline',
});

const columns = useItemColumns();

async function loadItems() {
  if (!dictId.value) return;
  loading.value = true;
  try {
    items.value = await getDictItems(dictId.value);
  } finally {
    loading.value = false;
  }
}

function onCreateItem() {
  editingItem.value = undefined;
  itemFormApi.resetForm();
}

async function onEditItem(record: SystemDictApi.SysDictItem) {
  editingItem.value = record;
  itemFormApi.setValues(record);
}

async function onDeleteItem(record: SystemDictApi.SysDictItem) {
  await deleteDictItem(record.id);
  message.success(
    $t('ui.actionMessage.deleteSuccess', [record.label]),
  );
  await loadItems();
  emit('success');
}

async function onSaveItem() {
  const { valid } = await itemFormApi.validate();
  if (!valid) return;
  const values = await itemFormApi.getValues();
  await (editingItem.value?.id
    ? updateDictItem(editingItem.value.id, values)
    : createDictItem({ ...values, dictId: dictId.value }));
  message.success($t('ui.actionMessage.saveSuccess'));
  editingItem.value = undefined;
  itemFormApi.resetForm();
  await loadItems();
  emit('success');
}

function onCancelEdit() {
  editingItem.value = undefined;
  itemFormApi.resetForm();
}

const [Modal, modalApi] = useVbenModal({
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<{ id: number }>();
      if (data?.id) {
        dictId.value = data.id;
        editingItem.value = undefined;
        itemFormApi.resetForm();
        loadItems();
      }
    }
  },
});
</script>

<template>
  <Modal
    :title="$t('system.dict.items')"
    class="w-[800px]"
    :footer="false"
  >
    <div class="mb-4">
      <div v-if="editingItem || editingItem === undefined" class="mb-4">
        <div class="mb-2 flex items-center justify-between">
          <span class="text-sm font-medium">
            {{
              editingItem
                ? $t('ui.actionTitle.edit', [$t('system.dict.name')])
                : $t('ui.actionTitle.create', [$t('system.dict.name')])
            }}
          </span>
          <div>
            <Button size="small" type="primary" @click="onSaveItem">
              {{ $t('common.confirm') }}
            </Button>
            <Button size="small" class="ml-2" @click="onCancelEdit">
              {{ $t('common.cancel') }}
            </Button>
          </div>
        </div>
        <ItemForm />
      </div>
    </div>
    <div class="mb-2 flex items-center justify-between">
      <span class="text-sm font-medium">
        {{ $t('system.dict.items') }}
      </span>
      <Button size="small" type="primary" @click="onCreateItem">
        <Plus class="mr-1 size-4" />
        {{ $t('ui.actionTitle.create', [$t('system.dict.name')]) }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :data-source="items"
      :loading="loading"
      row-key="id"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.field === 'operation'">
          <Button type="link" size="small" @click="onEditItem(record)">
            {{ $t('common.edit') }}
          </Button>
          <Button type="link" danger size="small" @click="onDeleteItem(record)">
            {{ $t('common.delete') }}
          </Button>
        </template>
      </template>
    </Table>
  </Modal>
</template>
