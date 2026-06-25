<script lang="ts" setup>
import type { SystemAnnouncementApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import {
  createAnnouncement,
  getAnnouncementDetail,
  updateAnnouncement,
} from '#/api';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemAnnouncementApi.Announcement>();
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const data: Record<string, any> = { ...values };
    if (formData.value?.id) delete data.status;
    await (formData.value?.id ? updateAnnouncement(formData.value.id, data) : createAnnouncement(data));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemAnnouncementApi.Announcement>();
    formApi.resetForm();
    formData.value = data;
    formApi.updateSchema([
      {
        fieldName: 'status',
        hide: Boolean(data?.id),
      },
    ]);
    if (data?.id) {
      const detail = await getAnnouncementDetail(data.id);
      await formApi.setValues(detail);
    }
  },
});
</script>

<template><Drawer :title="formData?.id ? '编辑系统公告' : '新增系统公告'"><Form /></Drawer></template>
