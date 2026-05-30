<script lang="ts" setup>
import type { DataNode } from 'antdv-next/dist/tree';

import type { SystemRoleApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { Tree, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Spin } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import { assignRoleMenus, createRole, getMenuTree, getRoleDetail, updateRole } from '#/api';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemRoleApi.SysRole>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const menuTree = ref<DataNode[]>([]);
const loadingMenuTree = ref(false);

const id = ref<number>();
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    drawerApi.lock();

    try {
      if (id.value) {
        await updateRole(id.value, values);
        // 更新角色菜单权限
        const checkedKeys = menuCheckedKeys.value;
        await assignRoleMenus({ roleId: id.value, menuIds: checkedKeys });
      } else {
        await createRole(values);
      }
      emits('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },

  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemRoleApi.SysRole>();
      formApi.resetForm();

      if (data?.id) {
        formData.value = data;
        id.value = data.id;
      } else {
        id.value = undefined;
      }

      if (menuTree.value.length === 0) {
        await loadMenuTree();
      }

      await nextTick();
      if (data?.id) {
        // 加载角色详情获取已分配菜单
        const detail = await getRoleDetail(data.id);
        formApi.setValues(detail);
        menuCheckedKeys.value = detail.menuIds || [];
      } else {
        menuCheckedKeys.value = [];
      }
    }
  },
});

const menuCheckedKeys = ref<number[]>([]);

async function loadMenuTree() {
  loadingMenuTree.value = true;
  try {
    const res = await getMenuTree();
    menuTree.value = res as unknown as DataNode[];
  } finally {
    loadingMenuTree.value = false;
  }
}

const getDrawerTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.role.name')])
    : $t('ui.actionTitle.create', [$t('system.role.name')]);
});
</script>
<template>
  <Drawer :title="getDrawerTitle">
    <Form />
    <div class="mt-4">
      <div class="mb-2 text-sm font-medium">
        {{ $t('system.role.assignMenus') }}
      </div>
      <Spin :spinning="loadingMenuTree">
        <Tree
          v-model:checked-keys="menuCheckedKeys"
          :tree-data="menuTree"
          multiple
          bordered
          :default-expanded-level="2"
          value-field="id"
          label-field="menuName"
          icon-field="icon"
          checkable
        >
          <template #node="{ value }">
            <IconifyIcon v-if="value.icon" :icon="value.icon" />
            {{ value.menuName }}
          </template>
        </Tree>
      </Spin>
    </div>
  </Drawer>
</template>
