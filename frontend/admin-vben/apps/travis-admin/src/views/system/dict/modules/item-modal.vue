<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm, z } from '#/adapter/form';
import { createDictItem, updateDictItem } from '#/api';
import { $t } from '#/locales';

const emit = defineEmits(['success']);
const formData = ref<Record<string, any>>({});
const dictId = ref(0);
const dictName = ref('');

const getTitle = computed(() => {
  return formData.value?.itemId
    ? $t('ui.actionTitle.edit', ['数据项'])
    : $t('system.dict.item.addItem');
});

const [Form, formApi] = useVbenForm({
  schema: [
    {
      component: 'Input',
      fieldName: 'label',
      label: $t('system.dict.item.label'),
      rules: z
        .string()
        .min(1, '请输入标签'),
    },
    {
      component: 'Input',
      fieldName: 'value',
      label: $t('system.dict.item.value'),
      rules: z
        .string()
        .min(1, '请输入值'),
    },
    {
      component: 'Select',
      componentProps: {
        options: [
          { label: '默认', value: 'default' },
          { label: '主要', value: 'primary' },
          { label: '成功', value: 'success' },
          { label: '警告', value: 'warning' },
          { label: '危险', value: 'danger' },
          { label: '信息', value: 'info' },
        ],
      },
      defaultValue: 'default',
      fieldName: 'tagStyle',
      label: $t('system.dict.item.tagStyle'),
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: $t('system.dict.item.sort'),
      defaultValue: 0,
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
        optionType: 'button',
      },
      defaultValue: 1,
      fieldName: 'status',
      label: $t('system.dict.item.status'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.dict.item.remark'),
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    modalApi.lock();
    try {
      const payload = { dictId: dictId.value, ...values };
      const savePromise = formData.value?.itemId
        ? updateDictItem(formData.value.itemId, payload)
        : createDictItem(payload);
      await savePromise;
      emit('success');
      modalApi.close();
    } catch {
      modalApi.unlock();
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<{ dictId: number; dictName: string; itemId?: number; label?: string; remark?: string; sort?: number; status?: number; tagStyle?: string; value?: string }>();
      formApi.resetForm();
      if (data) {
        dictId.value = data.dictId;
        dictName.value = data.dictName;
        formData.value = data;
        formApi.setValues(data);
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form />
  </Modal>
</template>
