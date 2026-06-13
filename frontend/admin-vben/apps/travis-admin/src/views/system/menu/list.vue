<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button, message, Modal } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteMenu, getMenuTree, moveDownMenu, moveUpMenu } from '#/api';
import { $t } from '#/locales';
import { generateAccess } from '#/router/access';
import { accessRoutes } from '#/router/routes';

import { useColumns } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const gridData = ref<SystemMenuApi.SysMenu[]>([]);

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(onActionClick, gridData),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async () => {
          const result = await getMenuTree();
          // 保存菜单数据，用于判断上移/下移按钮状态
          gridData.value = result;
          return result;
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      zoom: true,
    },
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions,
});

const router = useRouter();

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemMenuApi.SysMenu>) {
  switch (code) {
    case 'delete': {
      onDelete(row);
      break;
    }
    case 'edit': {
      onEdit(row);
      break;
    }
    case 'moveDown': {
      onMoveDown(row);
      break;
    }
    case 'moveUp': {
      onMoveUp(row);
      break;
    }
    default: {
      break;
    }
  }
}

async function onRefresh() {
  const accessStore = useAccessStore();
  const userStore = useUserStore();

  // 重新生成菜单和路由
  const { accessibleMenus, accessibleRoutes } = await generateAccess({
    roles: userStore.userInfo?.roles ?? [],
    router,
    routes: accessRoutes,
  });

  accessStore.setAccessMenus(accessibleMenus);
  accessStore.setAccessRoutes(accessibleRoutes);
  accessStore.setIsAccessChecked(true);
  gridApi.query();
}

function onEdit(row: SystemMenuApi.SysMenu) {
  formDrawerApi.setData(row).open();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

function onToggleMenu(row: SystemMenuApi.SysMenu) {
  if (row.children?.length) {
    gridApi.grid.toggleTreeExpand(row);
  }
}

function onDelete(row: SystemMenuApi.SysMenu) {
  const hasChildren = Boolean(row.children?.length);
  Modal.confirm({
    content: hasChildren
      ? '该菜单下存在子菜单，删除后将同时删除所有子菜单，是否继续？'
      : `确定删除菜单「${row.menuName}」吗？`,
    onOk: async () => {
      const hideLoading = message.loading({
        content: $t('ui.actionMessage.deleting', [row.menuName]),
        duration: 0,
        key: 'action_process_msg',
      });
      try {
        await deleteMenu(row.id);
        message.success({
          content: $t('ui.actionMessage.deleteSuccess', [row.menuName]),
          key: 'action_process_msg',
        });
        await onRefresh();
      } catch (error) {
        hideLoading();
        throw error;
      }
    },
    title: `删除菜单「${row.menuName}」`,
  });
}

function onMoveUp(row: SystemMenuApi.SysMenu) {
  moveUpMenu(row.id).then(() => {
    message.success(`「${row.menuName}」已上移`);
    onRefresh();
  });
}

function onMoveDown(row: SystemMenuApi.SysMenu) {
  moveDownMenu(row.id).then(() => {
    message.success(`「${row.menuName}」已下移`);
    onRefresh();
  });
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <Grid>
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.menu.name')]) }}
        </Button>
      </template>
      <template #title="{ row }">
        <component
          :is="row.children?.length ? 'button' : 'div'"
          class="flex w-full items-center gap-1 text-left"
          :class="{ 'cursor-pointer': row.children?.length }"
          type="button"
          @click="onToggleMenu(row)"
        >
          <div class="size-5 shrink-0">
            <IconifyIcon
              v-if="row.menuType === 2"
              icon="carbon:security"
              class="size-full"
            />
            <IconifyIcon
              v-else-if="row.icon"
              :icon="row.icon || 'carbon:circle-dash'"
              class="size-full"
            />
          </div>
          <span class="flex-auto">{{ row.menuName }}</span>
        </component>
      </template>
    </Grid>
  </Page>
</template>
