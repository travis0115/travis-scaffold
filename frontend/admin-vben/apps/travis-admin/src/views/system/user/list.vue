<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { Page, Tree, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import { Button, Card, Input, message } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteUser,
  getDeptTree,
  getOnlineUserCount,
  getUserPage,
  updateUserStatus,
} from '#/api';
import { isDeptEnabled } from '#/features';
import { $t } from '#/locales';
import { hasAccessCode, SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ResetPasswordModal from './modules/reset-password-modal.vue';

type TreeKey = number | string;

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [ResetPwdModal, resetPwdModalApi] = useVbenModal({
  connectedComponent: ResetPasswordModal,
  destroyOnClose: true,
});

// 部门树相关
const showDeptTree = isDeptEnabled();
const deptListSource = ref<any[]>([]);
const deptSearchValue = ref('');
const onlineOnly = ref(false);
const onlineUserCount = ref(0);
const selectedDeptId = ref<number>();
const deptManualExpandedKeys = ref<TreeKey[]>([]);
const deptSearchExpandedKeys = ref<TreeKey[]>([]);
const deptRestoreTransitionDisabled = ref(false);
const showAllDeptEntry = computed(() =>
  matchDeptEntry($t('system.user.allDepts')),
);
const showUnassignedDeptEntry = computed(() => matchDeptEntry('未归属'));
const deptList = computed(() =>
  filterDeptTree(deptListSource.value, deptSearchValue.value.trim()),
);
const isDeptSearchActive = computed(() => Boolean(deptSearchValue.value.trim()));
const deptTreeRenderKey = computed(() =>
  isDeptSearchActive.value ? 'dept-search' : 'dept-default',
);
const deptTreeExpandedKeys = computed({
  get: () =>
    isDeptSearchActive.value
      ? deptSearchExpandedKeys.value
      : deptManualExpandedKeys.value,
  set: (keys: TreeKey[]) => {
    if (isDeptSearchActive.value) {
      deptSearchExpandedKeys.value = [...keys];
      return;
    }
    deptManualExpandedKeys.value = [...keys];
  },
});
const deptTreeExpanded = computed(() => deptTreeExpandedKeys.value.length > 0);
const userTableTitle = computed(() => {
  const title = onlineOnly.value
    ? $t('system.user.onlineList')
    : $t('system.user.list');
  if (!showDeptTree && !onlineOnly.value) return title;
  return `${title} - ${selectedDeptName.value}`;
});
const selectedDeptName = computed(() => {
  if (selectedDeptId.value === undefined) {
    return $t('system.user.allDepts');
  }
  if (selectedDeptId.value === 0) {
    return '未归属';
  }
  return (
    findDeptById(deptListSource.value, selectedDeptId.value)?.deptName ??
    $t('system.user.allDepts')
  );
});

async function loadDeptList() {
  try {
    const data = await getDeptTree();
    deptListSource.value = data;
    deptManualExpandedKeys.value = collectDeptExpandableKeys(data);
  } catch {
    deptListSource.value = [];
    deptManualExpandedKeys.value = [];
  }
}

async function loadOnlineUserCount() {
  try {
    onlineUserCount.value = await getOnlineUserCount();
  } catch {
    onlineUserCount.value = 0;
  }
}

function filterDeptTree(nodes: any[], keyword: string): any[] {
  if (!keyword) return nodes;
  const lowerKeyword = keyword.toLowerCase();
  return nodes
    .map((node: any) => {
      const children = filterDeptTree(node.children ?? [], keyword);
      const matched = node.deptName.toLowerCase().includes(lowerKeyword);
      if (matched || children.length > 0) {
        return { ...node, children };
      }
      return null;
    })
    .filter(Boolean) as any[];
}

function collectDeptExpandableKeys(nodes: any[]) {
  const keys: TreeKey[] = [];
  nodes.forEach((node) => {
    if (node.children?.length) {
      keys.push(node.id, ...collectDeptExpandableKeys(node.children));
    }
  });
  return keys;
}

function matchDeptEntry(label: string) {
  const keyword = deptSearchValue.value.trim().toLowerCase();
  return !keyword || label.toLowerCase().includes(keyword);
}

watch(
  [deptList, isDeptSearchActive],
  ([tree, active]) => {
    deptSearchExpandedKeys.value = active
      ? collectDeptExpandableKeys(tree)
      : [];
  },
  { immediate: true },
);

watch(
  deptSearchValue,
  async (value, oldValue) => {
    if (value.trim() || !oldValue.trim()) return;
    deptRestoreTransitionDisabled.value = true;
    deptSearchExpandedKeys.value = [];
    await nextTick();
    await nextTick();
    deptRestoreTransitionDisabled.value = false;
  },
  { flush: 'sync' },
);

function mergeDeptExpandedKeys(keys: TreeKey[], nextKeys: TreeKey[]) {
  return [...new Set([...keys, ...nextKeys])];
}

function findDeptPath(
  nodes: any[],
  id: TreeKey,
  parents: TreeKey[] = [],
): undefined | { hasChildren: boolean; parents: TreeKey[] } {
  for (const node of nodes) {
    if (String(node.id) === String(id)) {
      return {
        hasChildren: Boolean(node.children?.length),
        parents,
      };
    }
    const matched = findDeptPath(node.children ?? [], id, [
      ...parents,
      node.id,
    ]);
    if (matched) return matched;
  }
  return undefined;
}

function findDeptById(nodes: any[], id: TreeKey): any | undefined {
  for (const node of nodes) {
    if (String(node.id) === String(id)) {
      return node;
    }
    const matched = findDeptById(node.children ?? [], id);
    if (matched) return matched;
  }
  return undefined;
}

function rememberDeptSearchSelection(item: any) {
  if (!isDeptSearchActive.value) return;
  const dept = item.value;
  const path = findDeptPath(deptListSource.value, dept.id);
  if (!path) return;
  const keys = [...path.parents];
  if (path.hasChildren) {
    keys.push(dept.id);
  }
  deptManualExpandedKeys.value = mergeDeptExpandedKeys(
    deptManualExpandedKeys.value,
    keys,
  );
}

function onSelectDept(item: any) {
  rememberDeptSearchSelection(item);
  selectedDeptId.value = item?.value?.id;
  gridApi.query();
}

function onSelectAllDept() {
  selectedDeptId.value = undefined;
  gridApi.query();
}

function onSelectUnassignedDept() {
  selectedDeptId.value = 0;
  gridApi.query();
}

function onToggleDeptExpanded() {
  if (deptTreeExpanded.value) {
    if (isDeptSearchActive.value) {
      deptSearchExpandedKeys.value = [];
      return;
    }
    deptManualExpandedKeys.value = [];
    return;
  }
  const expandedKeys = collectDeptExpandableKeys(deptList.value);
  if (isDeptSearchActive.value) {
    deptSearchExpandedKeys.value = expandedKeys;
    return;
  }
  deptManualExpandedKeys.value = expandedKeys;
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: false,
  },
  gridOptions: {
    columns: useColumns(
      onActionClick,
      hasAccessCode(SYSTEM_PERMS.userUpdate) ? onStatusChange : undefined,
    ),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page, sort }, formValues) => {
          const orderParams = sort?.order
            ? { asc: sort.order === 'asc', orderBy: sort.field || sort.property }
            : {};
          const result = await getUserPage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            onlineOnly: onlineOnly.value,
            ...orderParams,
            ...formValues,
            ...(selectedDeptId.value === undefined
              ? {}
              : { deptId: selectedDeptId.value }),
          });
          result.records?.forEach((item) => {
            item.onlineStatus = item.online ? 1 : 0;
          });
          await loadOnlineUserCount();
          return result;
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    sortConfig: {
      remote: true,
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

async function onStatusChange(
  newStatus: number,
  row: SystemUserApi.SysUser,
) {
  try {
    await updateUserStatus(row.id, newStatus as 0 | 1);
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

function onToggleOnlineOnly() {
  onlineOnly.value = !onlineOnly.value;
  gridApi.query();
}

async function onResetPassword(row: SystemUserApi.SysUser) {
  resetPwdModalApi.setData({ id: row.id, nickname: row.nickname }).open();
}

onMounted(() => {
  if (showDeptTree) {
    loadDeptList();
  }
  loadOnlineUserCount();
});
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <ResetPwdModal @success="onRefresh" />
    <div v-if="showDeptTree" class="flex size-full">
      <!-- 左侧部门树 -->
      <Card class="folder-panel w-72 flex-none">
        <div class="mb-4 flex items-center justify-between">
          <span class="folder-panel-title">部门</span>
          <Button
            class="folder-create-btn"
            type="link"
            @click="onToggleDeptExpanded"
          >
            {{ deptTreeExpanded ? '全部收起' : '全部展开' }}
          </Button>
        </div>

        <Input
          v-model:value="deptSearchValue"
          allow-clear
          placeholder="请输入部门名称"
          class="folder-search mb-5"
        >
          <template #prefix>
            <IconifyIcon
              icon="lucide:search"
              class="size-4 text-muted-foreground"
            />
          </template>
        </Input>
        <div
          v-if="showAllDeptEntry"
          class="folder-row"
          :class="
            selectedDeptId === undefined
              ? 'folder-row-selected'
              : 'folder-row-normal'
          "
          @click="onSelectAllDept"
        >
          <span class="folder-row-spacer"></span>
          <IconifyIcon icon="lucide:folder" class="folder-icon" />
          <span class="folder-name">{{ $t('system.user.allDepts') }}</span>
        </div>
        <div
          v-if="showUnassignedDeptEntry"
          class="folder-row"
          :class="
            selectedDeptId === 0 ? 'folder-row-selected' : 'folder-row-normal'
          "
          @click="onSelectUnassignedDept"
        >
          <span class="folder-row-spacer"></span>
          <IconifyIcon icon="lucide:folder" class="folder-icon" />
          <span class="folder-name">未归属</span>
        </div>
        <Tree
          v-if="deptList.length > 0"
          :key="deptTreeRenderKey"
          v-model="selectedDeptId"
          v-model:expanded-keys="deptTreeExpandedKeys"
          class="side-tree folder-tree"
          :tree-data="deptList"
          :default-expanded-level="0"
          :show-icon="false"
          :show-toolbar="false"
          label-field="deptName"
          :transition="!deptRestoreTransitionDisabled"
          value-field="id"
          children-field="children"
          @select="onSelectDept"
        >
          <template #node="{ value: dept }">
            <div class="folder-node">
              <IconifyIcon icon="lucide:folder" class="folder-icon" />
              <span class="folder-name">{{ dept.deptName }}</span>
            </div>
          </template>
        </Tree>
      </Card>
      <!-- 右侧表格 -->
      <div class="ml-4 min-w-0 flex-1">
        <Grid :table-title="userTableTitle">
          <template #toolbar-tools>
            <Button @click="onToggleOnlineOnly">
              <IconifyIcon
                :icon="onlineOnly ? 'lucide:users' : 'lucide:activity'"
                class="size-4"
              />
              {{
                onlineOnly
                  ? $t('system.user.allUsers')
                  : $t('system.user.onlineUsers', [onlineUserCount])
              }}
            </Button>
            <Button
              v-access:code="SYSTEM_PERMS.userCreate"
              type="primary"
              @click="onCreate"
            >
              <Plus class="size-5" />
              {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
            </Button>
          </template>
        </Grid>
      </div>
    </div>
    <!-- 未启用部门时只显示表格 -->
    <Grid v-else :table-title="userTableTitle">
      <template #toolbar-tools>
        <Button @click="onToggleOnlineOnly">
          <IconifyIcon
            :icon="onlineOnly ? 'lucide:users' : 'lucide:activity'"
            class="size-4"
          />
          {{
            onlineOnly
              ? $t('system.user.allUsers')
              : $t('system.user.onlineUsers', [onlineUserCount])
          }}
        </Button>
        <Button
          v-access:code="SYSTEM_PERMS.userCreate"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
        </Button>
      </template>
    </Grid>
  </Page>
</template>

<style scoped>
.folder-panel {
  min-height: 100%;
  border-color: hsl(var(--border));
  border-radius: 12px;
}

.folder-panel :deep(.ant-card-body) {
  padding: 18px 16px;
}

.folder-panel-title {
  color: hsl(var(--foreground));
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.folder-create-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  height: 28px;
  padding: 0;
  color: hsl(var(--primary));
  font-size: 14px;
}

.folder-search {
  height: 32px;
  border-color: hsl(var(--border));
  border-radius: 6px;
  font-size: 14px;
}

.folder-search :deep(.ant-input) {
  font-size: 14px;
}

.folder-row {
  display: flex;
  align-items: center;
  min-width: 0;
  height: 32px;
  padding-right: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  transition:
    color 0.16s ease,
    background-color 0.16s ease;
}

.folder-row-normal:hover {
  background-color: hsl(var(--accent) / 50%);
  color: hsl(var(--foreground));
}

.folder-row-selected {
  background-color: hsl(var(--primary) / 12%);
  color: hsl(var(--foreground));
}

.folder-row-spacer {
  width: 18px;
  height: 22px;
  flex: none;
}

.folder-icon {
  width: 18px;
  height: 18px;
  flex: none;
  margin-right: 8px;
  color: hsl(var(--muted-foreground));
}

.folder-node {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
}

.folder-name {
  min-width: 0;
  overflow: hidden;
  flex: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-tree {
  margin-top: 2px;
}

.side-tree :deep(.tree-node) {
  height: 32px;
  padding-right: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  font-weight: 400;
  line-height: 1;
  transition:
    color 0.16s ease,
    background-color 0.16s ease;
}

.side-tree :deep(.tree-node:hover) {
  background-color: hsl(var(--accent) / 50%);
  color: hsl(var(--foreground));
}

.side-tree :deep(.tree-node[data-selected]) {
  background-color: hsl(var(--primary) / 12%) !important;
  color: hsl(var(--foreground));
}

.side-tree :deep(.tree-node > .item-checkbox) {
  min-width: 0;
  flex: 1;
}

.side-tree :deep(.tree-node > .item-checkbox > .item-checkbox) {
  min-width: 0;
}
</style>
