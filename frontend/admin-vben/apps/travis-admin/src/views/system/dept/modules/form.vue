<script lang="ts" setup>
import type { SystemDeptApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createDept, getDeptDetail, getDeptTree, updateDept } from '#/api';
import { $t } from '#/locales';

import { useSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemDeptApi.SysDept>();

async function getParentDeptTree() {
  const deptTree = await getDeptTree();
  const currentId = formData.value?.id;
  if (!currentId) {
    return deptTree;
  }
  const filterTree = (
    departments: SystemDeptApi.SysDept[],
  ): SystemDeptApi.SysDept[] =>
    departments
      .filter((department) => `${department.id}` !== `${currentId}`)
      .map((department) => ({
        ...department,
        children: department.children
          ? filterTree(department.children)
          : undefined,
      }));
  return filterTree(deptTree);
}

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.dept.name')])
    : $t('ui.actionTitle.create', [$t('system.dept.name')]);
});

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  schema: useSchema(getParentDeptTree),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      drawerApi.lock();
      const data = await formApi.getValues();
      data.parentId ??= 0;
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
      formData.value = data?.id ? data : undefined;
      await formApi.resetForm();
      if (data?.id) {
        // 编辑时加载完整详情
        const detail = await getDeptDetail(data.id);
        formData.value = detail;
        await formApi.setValues({
          ...detail,
          parentId: `${detail.parentId}` === '0' ? undefined : detail.parentId,
        });
      } else if (data?.parentId !== undefined && data.parentId !== null) {
        await formApi.setValues({ parentId: data.parentId });
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
