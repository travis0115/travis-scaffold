<script lang="ts" setup>
import type { DataNode } from 'antdv-next/dist/tree';

import type { Recordable } from '@vben/types';

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
    const { menuIds = [], ...roleValues } = values;
    drawerApi.lock();

    try {
      if (id.value) {
        await updateRole(id.value, roleValues);
        await assignRoleMenus({ roleId: id.value, menuIds });
      } else {
        await createRole(roleValues);
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
      }
    }
  },
});

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

function getNodeClass(node: Recordable<any>) {
  return node.value?.menuType === 2 ? 'inline-flex' : '';
}
</script>
<template>
  <Drawer class="w-full max-w-200" :title="getDrawerTitle">
    <Form>
      <template #menuIds="slotProps">
        <Spin :spinning="loadingMenuTree" :classes="{ root: 'w-full' }">
          <Tree
            :tree-data="menuTree"
            multiple
            bordered
            :default-expanded-level="2"
            :get-node-class="getNodeClass"
            v-bind="slotProps"
            value-field="id"
            label-field="menuName"
            icon-field="icon"
          >
            <template #node="{ value }">
              <IconifyIcon v-if="value.icon" :icon="value.icon" />
              {{ value.menuName }}
            </template>
          </Tree>
        </Spin>
      </template>
    </Form>
  </Drawer>
</template>
