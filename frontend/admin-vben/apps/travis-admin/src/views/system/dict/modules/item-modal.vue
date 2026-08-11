<script lang="ts" setup>
import { computed, h, ref } from 'vue';
import type { Id } from '#/api/types';

import { useVbenDrawer } from '@vben/common-ui';

import { Tag } from 'antdv-next';

import { useVbenForm, z } from '#/adapter/form';
import { createDictItem, updateDictItem } from '#/api';
import { $t } from '#/locales';
import { enableStatusOptions } from '#/utils/business-options';
import { reloadDictOptions } from '#/utils/dict';

import { tagStyleOptions } from './tag-style-options';

const emit = defineEmits(['success']);
const formData = ref<Record<string, any>>({});
const dictId = ref<Id>(0);
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
        .min(1, '字典标签不能为空')
        .max(20, '字典标签长度不能超过20个字符'),
    },
    {
      component: 'Input',
      fieldName: 'value',
      label: $t('system.dict.item.value'),
      rules: z
        .string()
        .min(1, '字典值不能为空')
        .max(100, '字典值长度不能超过100个字符'),
    },
    {
      component: 'Select',
      componentProps: {
        options: tagStyleOptions.map((item) => ({
          label: h(Tag, { color: item.color }, () => item.label),
          value: item.value,
        })),
      },
      defaultValue: 'default',
      fieldName: 'tagStyle',
      label: $t('system.dict.item.tagStyle'),
      rules: z
        .string()
        .max(100, '展示样式长度不能超过100个字符')
        .optional()
        .or(z.literal('')),
    },
    {
      component: 'InputNumber',
      componentProps: { max: 9999, min: 0 },
      fieldName: 'sort',
      label: $t('system.dict.item.sort'),
      defaultValue: 1,
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: enableStatusOptions,
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
      rules: z
        .string()
        .max(255, '备注长度不能超过255个字符')
        .optional()
        .or(z.literal('')),
    },
  ],
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    try {
      const payload = { dictId: dictId.value, ...values };
      if (formData.value?.itemId) delete (payload as any).status;
      const savePromise = formData.value?.itemId
        ? updateDictItem(formData.value.itemId, payload)
        : createDictItem(payload);
      await savePromise;
      await reloadDictOptions();
      emit('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<{
        dictId: Id;
        dictName: string;
        itemId?: Id;
        label?: string;
        remark?: string;
        sort?: number;
        status?: number;
        tagStyle?: string;
        value?: string;
      }>();
      formApi.resetForm();
      formApi.updateSchema([
        {
          fieldName: 'status',
          hide: Boolean(data?.itemId),
        },
      ]);
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
  <Drawer :title="getTitle">
    <Form />
  </Drawer>
</template>
