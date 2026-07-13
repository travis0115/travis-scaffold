<script lang="ts" setup>
import type { SystemNoticeApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createNotice, getNoticeDetail, updateNotice } from '#/api';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemNoticeApi.Notice>();

const getTitle = computed(() => {
  return formData.value?.id ? '编辑系统公告' : '新增系统公告';
});

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const data: Record<string, any> = { ...values };
    if (formData.value?.id) delete data.status;
    drawerApi.lock();
    try {
      await (formData.value?.id
        ? updateNotice(formData.value.id, data)
        : createNotice(data));
      emit('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemNoticeApi.Notice>();
    drawerApi.setState({ confirmText: undefined });
    formApi.resetForm();
    formApi.updateSchema([
      {
        fieldName: 'status',
        hide: Boolean(data?.id),
      },
    ]);
    if (data?.id) {
      const detail = await getNoticeDetail(data.id);
      formData.value = detail;
      await formApi.setValues(detail);
    } else {
      formData.value = undefined;
    }
  },
});
</script>

<template>
  <Drawer class="w-full max-w-220" :title="getTitle">
    <Form />
  </Drawer>
</template>
