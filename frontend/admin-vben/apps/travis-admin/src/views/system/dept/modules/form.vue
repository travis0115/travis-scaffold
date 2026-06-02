<script lang="ts" setup>
import type { SystemDeptApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createDept, getDeptDetail, updateDept } from '#/api';
import { $t } from '#/locales';

import { useSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemDeptApi.SysDept>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.dept.name')])
    : $t('ui.actionTitle.create', [$t('system.dept.name')]);
});

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  schema: useSchema(),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      drawerApi.lock();
      const data = await formApi.getValues();
      try {
        await (formData.value?.id
          ? updateDept(formData.value.id, data)
          : createDept(data));
        drawerApi.close();
        emit('success');
      } finally {
        drawerApi.lock(false);
      }
    }
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemDeptApi.SysDept>();
      formApi.resetForm();
      if (data?.id) {
        // 编辑时加载完整详情
        const detail = await getDeptDetail(data.id);
        if (detail.parentId === 0) {
          (detail as any).parentId = undefined;
        }
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
    <Form class="mx-4" />
  </Drawer>
</template>
