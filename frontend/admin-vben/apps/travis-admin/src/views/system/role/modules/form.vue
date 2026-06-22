<script lang="ts" setup>
import type { DataNode } from 'antdv-next/dist/tree';

import type { Recordable } from '@vben/types';

import type { SystemRoleApi } from '#/api';

import { computed, nextTick, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Tree, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Checkbox, Spin } from 'antdv-next';

import { useVbenForm } from '#/adapter/form';
import { assignRoleMenus, createRole, getMenuTree, getRoleDetail, updateRole } from '#/api';
import { $t } from '#/locales';
import { generateAccess } from '#/router/access';
import { accessRoutes } from '#/router/routes';
import { useAuthStore } from '#/store';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemRoleApi.SysRole>();

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
});

const menuTree = ref<DataNode[]>([]);
const loadingMenuTree = ref(false);
const permissionAllChecked = ref(false);
const permissionExpanded = ref(false);
const permissionExpandedKeys = ref<Array<number | string>>([]);
const permissionParentLinked = ref(true);
const treeRenderKey = ref(0);
const router = useRouter();
const accessStore = useAccessStore();
const authStore = useAuthStore();
const userStore = useUserStore();

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
        const shouldRefreshAccess = isCurrentUserRole(roleValues.roleCode);
        delete (roleValues as any).status;
        await updateRole(id.value, roleValues);
        await assignRoleMenus({ roleId: id.value, menuIds });
        if (shouldRefreshAccess) {
          await refreshCurrentUserAccess();
        }
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
      formApi.updateSchema([
        {
          fieldName: 'status',
          hide: Boolean(data?.id),
        },
      ]);
      permissionAllChecked.value = false;
      permissionExpanded.value = false;
      permissionExpandedKeys.value = [];
      permissionParentLinked.value = true;
      treeRenderKey.value += 1;

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

function collectMenuIds(nodes: DataNode[], onlyParents = false) {
  const ids: Array<number | string> = [];
  const walk = (items: DataNode[]) => {
    items.forEach((item) => {
      const children = item.children as DataNode[] | undefined;
      if (!onlyParents || children?.length) {
        ids.push(item.id as number | string);
      }
      if (children?.length) {
        walk(children);
      }
    });
  };
  walk(nodes);
  return ids;
}

function isCurrentUserRole(roleCode?: string) {
  const oldRoleCode = formData.value?.roleCode;
  const currentRoleCodes = userStore.userInfo?.roles ?? [];
  return [oldRoleCode, roleCode].some(
    (code) => code && currentRoleCodes.includes(code),
  );
}

async function refreshCurrentUserAccess() {
  const [userInfo] = await Promise.all([
    authStore.fetchUserInfo(),
    authStore.fetchAccessCodes(),
  ]);
  const { accessibleMenus, accessibleRoutes } = await generateAccess({
    roles: userInfo.roles ?? [],
    router,
    routes: accessRoutes,
  });

  accessStore.setAccessMenus(accessibleMenus);
  accessStore.setAccessRoutes(accessibleRoutes);
  accessStore.setIsAccessChecked(true);
}

function onPermissionExpandChange(checked: boolean) {
  permissionExpanded.value = checked;
  permissionExpandedKeys.value = checked ? collectMenuIds(menuTree.value, true) : [];
  treeRenderKey.value += 1;
}

function onPermissionAllChange(checked: boolean) {
  permissionAllChecked.value = checked;
  formApi.setValues({
    menuIds: checked ? collectMenuIds(menuTree.value) : [],
  });
}
</script>
<template>
  <Drawer class="w-full max-w-200" :title="getDrawerTitle">
    <Form>
      <template #menuIds="slotProps">
        <Spin :spinning="loadingMenuTree" :classes="{ root: 'w-full' }">
          <Tree
            :key="treeRenderKey"
            :tree-data="menuTree"
            multiple
            bordered
            :default-expanded-keys="permissionExpandedKeys"
            :get-node-class="getNodeClass"
            v-bind="slotProps"
            :check-strictly="!permissionParentLinked"
            :show-toolbar="false"
            value-field="id"
            label-field="menuName"
            icon-field="icon"
          >
            <template #header>
              <div class="flex flex-wrap items-center gap-4 px-1 py-0.5 text-sm">
                <span class="text-muted-foreground">菜单权限</span>
                <Checkbox
                  :checked="permissionExpanded"
                  @update:checked="onPermissionExpandChange"
                >
                  展开/折叠
                </Checkbox>
                <Checkbox
                  :checked="permissionAllChecked"
                  @update:checked="onPermissionAllChange"
                >
                  全选/全不选
                </Checkbox>
                <Checkbox v-model:checked="permissionParentLinked">
                  父子联动
                </Checkbox>
              </div>
            </template>
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
