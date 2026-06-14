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
      void deleteMenuRow(row);
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
    case 'remove': {
      onDelete(row);
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

async function deleteMenuRow(row: SystemMenuApi.SysMenu) {
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
  } catch {
    hideLoading();
  }
}

function onDelete(row: SystemMenuApi.SysMenu) {
  Modal.confirm({
    cancelText: $t('common.cancel'),
    content: `该菜单下存在下级菜单，删除「${row.menuName}」后将同时删除所有下级菜单，请确认是否继续？`,
    okText: '确认删除',
    okType: 'danger',
    onOk: () => deleteMenuRow(row),
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
    <Grid :table-title="$t('system.menu.list')">
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
        >
          <div v-if="row.menuType !== 2 && row.icon" class="size-5 shrink-0">
            <IconifyIcon
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
