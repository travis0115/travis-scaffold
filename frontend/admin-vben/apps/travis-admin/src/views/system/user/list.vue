<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { onMounted, ref, watch } from 'vue';

import { Page, Tree, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { App, Button, Card, InputSearch, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteUser, getDeptTree, getUserPage, updateUser } from '#/api';
import { isDeptEnabled } from '#/features';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ResetPasswordModal from './modules/reset-password-modal.vue';

const { modal } = App.useApp();

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [ResetPwdModal, resetPwdModalApi] = useVbenModal({
  connectedComponent: ResetPasswordModal,
  destroyOnClose: true,
});

// 部门树相关
const deptList = ref<any[]>([]);
const deptListSource = ref<any[]>([]);
const deptSearchValue = ref('');
const selectedDeptId = ref<number>();

async function loadDeptList() {
  try {
    const data = await getDeptTree();
    deptListSource.value = data;
    deptList.value = data;
  } catch {
    deptListSource.value = [];
    deptList.value = [];
  }
}

function searchDept(value: string) {
  if (!value) {
    deptList.value = deptListSource.value;
    return;
  }
  function filterNodes(nodes: any[]): any[] {
    return nodes
      .map((node: any) => {
        const children = node.children ? filterNodes(node.children) : [];
        const match = node.deptName
          .toLowerCase()
          .includes(value.toLowerCase());
        if (match || children.length > 0) {
          return { ...node, children: children.length > 0 ? children : node.children };
        }
        return null;
      })
      .filter(Boolean) as any[];
  }
  deptList.value = filterNodes(deptListSource.value);
}

watch(deptSearchValue, (value) => {
  searchDept(value);
});

function onSelectDept(item: any) {
  selectedDeptId.value = item?.value?.id;
  gridApi.query();
}

function onSelectAllDept() {
  selectedDeptId.value = undefined;
  gridApi.query();
}

const showDeptTree = isDeptEnabled();

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(onActionClick, onStatusChange),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            ...(selectedDeptId.value ? { deptId: selectedDeptId.value } : {}),
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
  } as VxeTableGridOptions<SystemUserApi.SysUser>,
});

function onActionClick(e: OnActionClickParams<SystemUserApi.SysUser>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
    case 'resetPassword': {
      onResetPassword(e.row);
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
  row: SystemUserApi.SysUser,
) {
  const status: Recordable<string> = {
    0: '禁用',
    1: '启用',
  };
  try {
    await confirm(
      `你要将${row.username}的状态切换为 【${status[newStatus.toString()]}】 吗？`,
      `切换状态`,
    );
    await updateUser(row.id, { status: newStatus as 0 | 1 });
    return true;
  } catch {
    return false;
  }
}

function onEdit(row: SystemUserApi.SysUser) {
  formDrawerApi.setData(row).open();
}

function onDelete(row: SystemUserApi.SysUser) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.username]),
    duration: 0,
    key: 'action_process_msg',
  });
  deleteUser(row.id)
    .then(() => {
      message.success({
        content: $t('ui.actionMessage.deleteSuccess', [row.username]),
        key: 'action_process_msg',
      });
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

async function onResetPassword(row: SystemUserApi.SysUser) {
  resetPwdModalApi.setData({ id: row.id, nickname: row.nickname }).open();
}

onMounted(() => {
  if (showDeptTree) {
    loadDeptList();
  }
});
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <ResetPwdModal @success="onRefresh" />
    <div v-if="showDeptTree" class="flex size-full">
      <!-- 左侧部门树 -->
      <Card class="w-1/6 flex-none">
        <InputSearch
          v-model:value="deptSearchValue"
          :placeholder="$t('system.dept.name')"
          class="mb-2"
        />
        <div
          class="mb-1 cursor-pointer rounded p-1 text-sm"
          :class="
            selectedDeptId === undefined
              ? 'bg-accent text-accent-foreground font-medium'
              : 'text-foreground/80 hover:bg-accent/50'
          "
          @click="onSelectAllDept"
        >
          {{ $t('system.user.allDepts') }}
        </div>
        <Tree
          :tree-data="deptList"
          :default-expanded-level="2"
          label-field="deptName"
          value-field="id"
          children-field="children"
          @select="onSelectDept"
        />
      </Card>
      <!-- 右侧表格 -->
      <div class="ml-4 w-5/6">
        <Grid :table-title="$t('system.user.list')">
          <template #toolbar-tools>
            <Button type="primary" @click="onCreate">
              <Plus class="size-5" />
              {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
            </Button>
          </template>
        </Grid>
      </div>
    </div>
    <!-- 未启用部门时只显示表格 -->
    <Grid v-else :table-title="$t('system.user.list')">
      <template #toolbar-tools>
        <Button type="primary" @click="onCreate">
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>
