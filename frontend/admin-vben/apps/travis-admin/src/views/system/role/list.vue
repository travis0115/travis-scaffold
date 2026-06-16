<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { App, Button, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteRole, getRolePage, updateRole } from '#/api';
import { $t } from '#/locales';
import { useAuthStore } from '#/store';
import { getDictLabel } from '#/utils/dict';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const { modal } = App.useApp();
const authStore = useAuthStore();

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.roleUpdate) ? onStatusChange : undefined,
    ),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getRolePage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
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
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<SystemRoleApi.SysRole>,
});

function onActionClick(e: OnActionClickParams<SystemRoleApi.SysRole>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
  }
}

function confirm(content: string, title: string) {
  return new Promise((resolve, reject) => {
    modal.confirm({
      content,
      onCancel() {
        reject(new Error('已取消'));
      },
      onOk() {
        resolve(true);
      },
      title,
    });
  });
}

async function onStatusChange(
  newStatus: number,
  row: SystemRoleApi.SysRole,
) {
  try {
    await confirm(
      $t('system.role.confirmStatusChange', {
        roleName: row.roleName,
        status: getDictLabel('sys_status', newStatus),
      }),
      $t('system.role.switchStatus'),
    );
    await updateRole(row.id, { status: newStatus as 0 | 1 });
    await authStore.fetchAccessCodes();
    return true;
  } catch {
    return false;
  }
}

function onEdit(row: SystemRoleApi.SysRole) {
  formDrawerApi.setData(row).open();
}

function onDelete(row: SystemRoleApi.SysRole) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.roleName]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteRole(row.id)
    .then(async () => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.roleName]),
        key: 'action_process_msg',
      });
      await authStore.fetchAccessCodes();
      onRefresh();
    })
    .catch(() => {
      hideLoading();
    });
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <Grid :table-title="$t('system.role.list')">
      <template #toolbar-tools>
        <Button
          v-access:code="SYSTEM_PERMS.roleCreate"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.role.name')]) }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>
