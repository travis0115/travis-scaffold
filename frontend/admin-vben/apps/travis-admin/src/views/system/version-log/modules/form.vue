<script lang="ts" setup>
import type { SystemVersionLogApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createVersionLog, getVersionLogDetail, updateVersionLog } from '#/api';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemVersionLogApi.VersionLog>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.versionLog.name')])
    : $t('ui.actionTitle.create', [$t('system.versionLog.name')]);
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
    drawerApi.lock();
    try {
      await (formData.value?.id
        ? updateVersionLog(formData.value.id, values)
        : createVersionLog(values));
      emit('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemVersionLogApi.VersionLog>();
      formApi.resetForm();
      if (data?.id) {
        const detail = await getVersionLogDetail(data.id);
        formData.value = detail;
        formApi.setValues(detail);
      } else {
        formData.value = undefined;
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
