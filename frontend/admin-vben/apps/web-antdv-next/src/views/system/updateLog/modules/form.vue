<script lang="ts" setup>
import type { SystemUpdateLogApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { Button } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import { createUpdateLog, getUpdateLogDetail, updateUpdateLog } from '#/api';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemUpdateLogApi.UpdateLog>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.updateLog.name')])
    : $t('ui.actionTitle.create', [$t('system.updateLog.name')]);
});

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

function resetForm() {
  formApi.resetForm();
  formApi.setValues(formData.value || {});
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();
    try {
      await (formData.value?.id
        ? updateUpdateLog(formData.value.id, values)
        : createUpdateLog(values));
      emit('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemUpdateLogApi.UpdateLog>();
      formApi.resetForm();
      if (data?.id) {
        const detail = await getUpdateLogDetail(data.id);
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
    <template #prepend-footer>
      <div class="flex-auto">
        <Button type="primary" danger @click="resetForm">
          {{ $t('common.reset') }}
        </Button>
      </div>
    </template>
  </Drawer>
</template>
