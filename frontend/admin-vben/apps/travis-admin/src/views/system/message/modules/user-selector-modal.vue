<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, Checkbox } from 'antdv-next';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAppUserPage, getUserPage } from '#/api';

type UserId = number | string;
type UserRow = SystemUserApi.UserOption & {
  _checked?: boolean;
  email?: string;
};

const emit = defineEmits<{
  success: [options: SystemUserApi.UserOption[], ids: string[]];
}>();

const receiverType = ref('admin');
const selectedIds = ref<Set<string>>(new Set());
const selectedRows = ref<Map<string, UserRow>>(new Map());
const currentPageRows = ref<UserRow[]>([]);

const currentPageAllChecked = computed(
  () =>
    currentPageRows.value.length > 0 &&
    currentPageRows.value.every((row) => selectedIds.value.has(String(row.id))),
);
const currentPageSomeChecked = computed(
  () =>
    !currentPageAllChecked.value &&
    currentPageRows.value.some((row) => selectedIds.value.has(String(row.id))),
);

const emptyValueFormatter = ({ cellValue }: { cellValue: unknown }) =>
  cellValue === undefined || cellValue === null || cellValue === ''
    ? '-'
    : String(cellValue);

const adminColumns = [
  {
    align: 'center' as const,
    headerAlign: 'center' as const,
    showOverflow: false,
    slots: { default: 'selection', header: 'selection_header' },
    width: 48,
  },
  {
    field: 'username',
    formatter: emptyValueFormatter,
    minWidth: 120,
    title: '用户名',
  },
  {
    field: 'nickname',
    formatter: emptyValueFormatter,
    minWidth: 120,
    title: '昵称',
  },
  {
    field: 'mobile',
    formatter: emptyValueFormatter,
    minWidth: 130,
    title: '手机号',
  },
  {
    field: 'email',
    formatter: emptyValueFormatter,
    minWidth: 180,
    title: '邮箱',
  },
  {
    field: 'deptName',
    formatter: emptyValueFormatter,
    minWidth: 130,
    title: '部门',
  },
];
const appColumns = [
  {
    align: 'center' as const,
    headerAlign: 'center' as const,
    showOverflow: false,
    slots: { default: 'selection', header: 'selection_header' },
    width: 48,
  },
  {
    field: 'nickname',
    formatter: emptyValueFormatter,
    minWidth: 160,
    title: '昵称',
  },
  {
    field: 'mobile',
    formatter: emptyValueFormatter,
    minWidth: 160,
    title: '手机号',
  },
];
const adminSearchSchema = [
  { component: 'Input' as const, fieldName: 'username', label: '用户名' },
  { component: 'Input' as const, fieldName: 'nickname', label: '昵称' },
  { component: 'Input' as const, fieldName: 'mobile', label: '手机号' },
  { component: 'Input' as const, fieldName: 'email', label: '邮箱' },
];
const appSearchSchema = [
  { component: 'Input' as const, fieldName: 'nickname', label: '昵称' },
  { component: 'Input' as const, fieldName: 'mobile', label: '手机号' },
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: adminSearchSchema,
    wrapperClass: 'grid-cols-2',
  },
  gridOptions: {
    columns: adminColumns,
    height: '100%',
    pagerConfig: { pageSize: 20 },
    proxyConfig: {
      autoLoad: false,
      ajax: {
        query: async ({ page }, values) => {
          const params = {
            pageNum: page.currentPage,
            pageSize: page.pageSize,
          };
          try {
            const result =
              receiverType.value === 'app'
                ? await getAppUserPage({ ...params, ...values })
                : await getUserPage({ ...params, ...values, status: 1 });
            const records = result.records.map((row) => {
              const checked = selectedIds.value.has(String(row.id));
              if (checked) selectedRows.value.set(String(row.id), row);
              return { ...row, _checked: checked };
            });
            currentPageRows.value = records;
            return {
              ...result,
              records,
            };
          } catch {
            currentPageRows.value = [];
            return {
              pageNum: page.currentPage,
              pageSize: page.pageSize,
              records: [],
              total: 0,
              totalPages: 0,
            };
          }
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<UserRow>,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    emit('success', [...selectedRows.value.values()], [...selectedIds.value]);
    modalApi.close();
  },
  async onOpened() {
    const data = modalApi.getData<{
      receiverType: string;
      selectedIds?: UserId[];
      selectedOptions?: UserRow[];
    }>();
    receiverType.value = data.receiverType;
    selectedIds.value = new Set((data.selectedIds || []).map(String));
    selectedRows.value = new Map(
      (data.selectedOptions || []).map((row) => [String(row.id), row]),
    );
    gridApi.setGridOptions({
      columns: receiverType.value === 'app' ? appColumns : adminColumns,
    });
    await nextTick();
    gridApi.formApi.setState({
      schema:
        receiverType.value === 'app' ? appSearchSchema : adminSearchSchema,
      wrapperClass: 'grid-cols-2',
    });
    await gridApi.formApi.resetForm();
    await gridApi.query();
  },
  zIndex: 2200,
});

function updateSelectedIds(rows: UserRow[], checked: boolean) {
  const next = new Set(selectedIds.value);
  rows.forEach((row) => {
    const id = String(row.id);
    if (checked) {
      next.add(id);
      selectedRows.value.set(id, row);
    } else {
      next.delete(id);
      selectedRows.value.delete(id);
    }
  });
  selectedIds.value = next;
}

async function clearSelectedUsers() {
  selectedIds.value = new Set();
  selectedRows.value = new Map();
}

function handleCurrentPageSelection(checked: boolean) {
  updateSelectedIds(currentPageRows.value, checked);
}
</script>

<template>
  <Modal
    class="h-[760px] w-[1200px]"
    content-class="min-h-0 overflow-hidden"
    title="用户"
  >
    <Grid>
      <template #selection="{ row }">
        <div class="flex w-full items-center justify-center">
          <Checkbox
            :checked="selectedIds.has(String(row.id))"
            @change="updateSelectedIds([row], $event.target.checked)"
          />
        </div>
      </template>
      <template #selection_header>
        <div class="flex w-full items-center justify-center">
          <Checkbox
            :checked="currentPageAllChecked"
            :indeterminate="currentPageSomeChecked"
            @change="handleCurrentPageSelection($event.target.checked)"
          />
        </div>
      </template>
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <span class="text-sm text-muted-foreground">
            已选择 {{ selectedIds.size }} 个用户
          </span>
          <Button size="small" @click="clearSelectedUsers">清空已选</Button>
        </div>
      </template>
    </Grid>
  </Modal>
</template>
