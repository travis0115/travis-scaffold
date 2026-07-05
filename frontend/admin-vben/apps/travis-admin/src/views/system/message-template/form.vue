<script lang="ts" setup>
import type { SystemMessageApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import {
  createMessageTemplate,
  getMessageTemplateDetail,
  updateMessageTemplate,
} from '#/api';

import { useFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.MessageTemplate>();
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    await (formData.value?.id
      ? updateMessageTemplate(formData.value.id, values)
      : createMessageTemplate(values));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemMessageApi.MessageTemplate>();
    formApi.resetForm();
    if (data?.id) {
      const detail = await getMessageTemplateDetail(data.id);
      formData.value = detail;
      await formApi.setValues(detail);
    } else {
      formData.value = undefined;
      await formApi.setValues({ channel: 'IN_APP', status: 1 });
    }
  },
});
</script>

<template>
  <Drawer class="w-full max-w-220" :title="formData?.id ? '编辑消息模板' : '新增消息模板'">
    <Form />
  </Drawer>
</template>
