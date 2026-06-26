<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemFileApi } from '#/api';

import { computed, nextTick, onMounted, ref, watch } from 'vue';

import { Page, Tree, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, Image, Input, Progress, Upload } from 'antdv-next';

import { useVbenForm, z } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createFileFolder,
  createStorageConfig,
  deleteFile,
  deleteFileFolder,
  FILE_FOLDER_IDS,
  getFileFolders,
  getFilePage,
  getStorageConfigs,
  updateFileFolder,
  UPLOAD_FILE_MAX_SIZE_BYTES,
  UPLOAD_FILE_MAX_SIZE_TEXT,
  uploadFileApi,
} from '#/api';
import { SYSTEM_PERMS } from '#/utils/permissions';

import { useColumns, useGridFormSchema } from './data';

type FolderId = number | string;
type FolderSelection = 'all' | 'unclassified' | FolderId;
type TreeExpose = {
  expandNodes: (value: FolderId | FolderId[]) => void;
};
type UploadTask = {
  errorMessage?: string;
  name: string;
  percent: number;
  status: 'error' | 'success' | 'uploading';
  uid: string;
};

const selectedFolderKey = ref<FolderSelection>('all');
const folderTreeValue = ref<FolderId>();
const folderTreeRef = ref<TreeExpose>();
const folderManualExpandedKeys = ref<FolderId[]>([]);
const folderSearchExpandedKeys = ref<FolderId[]>([]);
const folderRestoreTransitionDisabled = ref(false);
const editingFolder = ref<SystemFileApi.Folder>();
const deletingFolder = ref<SystemFileApi.Folder>();
const folderParentId = ref<FolderId>(0);
const uploadTasks = ref<UploadTask[]>([]);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        query: ({ page, sort }, values) =>
          getFilePage({
            pageNum: page.currentPage,
            pageSize: page.pageSize,
            ...(sort?.order
              ? {
                  asc: sort.order === 'asc',
                  orderBy: sort.field || sort.property,
                }
              : {}),
            ...values,
            ...getFolderQueryParams(),
          }),
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemFileApi.FileInfo>,
});

const folders = ref<SystemFileApi.Folder[]>([]);
const storageConfigs = ref<SystemFileApi.StorageConfig[]>([]);
const folderSearch = ref('');
const folderModalMode = ref<'create' | 'edit'>('create');

const folderTree = computed(() => buildFolderTree(folders.value));
const filteredFolderTree = computed(() =>
  filterFolderTree(folderTree.value, folderSearch.value.trim()),
);
const isFolderSearchActive = computed(() => Boolean(folderSearch.value.trim()));
const folderTreeRenderKey = computed(() =>
  isFolderSearchActive.value ? 'folder-search' : 'folder-default',
);
const folderTreeExpandedKeys = computed({
  get: () =>
    isFolderSearchActive.value
      ? folderSearchExpandedKeys.value
      : folderManualExpandedKeys.value,
  set: (keys: FolderId[]) => {
    if (isFolderSearchActive.value) {
      folderSearchExpandedKeys.value = [...keys];
      return;
    }
    folderManualExpandedKeys.value = [...keys];
  },
});
const showAllEntry = computed(() => matchFolderEntry('全部'));
const showUnclassifiedEntry = computed(() => matchFolderEntry('未分类'));
const fileTableTitle = computed(() => `文件管理 - ${selectedFolderName.value}`);
const selectedFolderName = computed(() => {
  if (selectedFolderKey.value === 'all') return '全部';
  if (selectedFolderKey.value === 'unclassified') return '未分类';
  return getFolderName(selectedFolderKey.value) ?? '全部';
});
const folderModalTitle = computed(() => {
  if (folderModalMode.value === 'edit') return '修改文件夹';
  return isRootFolder(folderParentId.value) ? '新增文件夹' : '新增子文件夹';
});
const folderModalDescription = computed(() => {
  const parentName = getFolderName(folderParentId.value) ?? '根目录';
  if (folderModalMode.value === 'edit') {
    return `当前文件夹：${editingFolder.value?.folderName ?? '-'}；父文件夹：${parentName}`;
  }
  return `父文件夹：${parentName}`;
});
const deletingFolderHasChildren = computed(
  () => getChildFolders(deletingFolder.value?.id).length > 0,
);
const deleteFolderContent = computed(() => {
  const folderName = deletingFolder.value?.folderName ?? '-';
  return deletingFolderHasChildren.value
    ? `删除「${folderName}」后，子文件夹会一并删除，相关文件将清除归属，确认删除？`
    : `删除「${folderName}」后，相关文件将清除归属，确认删除？`;
});

function getPreviewIcon(row: SystemFileApi.FileInfo) {
  const extension = row.extension?.toLowerCase();
  const mimeType = row.mimeType?.toLowerCase() ?? '';
  if (mimeType.includes('pdf') || extension === 'pdf') {
    return 'lucide:file-text';
  }
  if (
    mimeType.includes('spreadsheet') ||
    ['csv', 'xls', 'xlsx'].includes(extension ?? '')
  ) {
    return 'lucide:file-spreadsheet';
  }
  if (
    mimeType.includes('presentation') ||
    ['ppt', 'pptx'].includes(extension ?? '')
  ) {
    return 'lucide:presentation';
  }
  if (
    mimeType.includes('word') ||
    ['doc', 'docx'].includes(extension ?? '')
  ) {
    return 'lucide:file-text';
  }
  if (
    mimeType.includes('zip') ||
    ['7z', 'gz', 'rar', 'tar', 'zip'].includes(extension ?? '')
  ) {
    return 'lucide:file-archive';
  }
  if (mimeType.includes('audio')) {
    return 'lucide:file-audio';
  }
  if (mimeType.includes('video')) {
    return 'lucide:file-video';
  }
  if (
    mimeType.includes('json') ||
    mimeType.includes('javascript') ||
    mimeType.includes('xml') ||
    ['css', 'html', 'java', 'js', 'json', 'ts', 'vue', 'xml'].includes(
      extension ?? '',
    )
  ) {
    return 'lucide:file-code';
  }
  return 'lucide:file';
}

function getFolderQueryParams() {
  if (selectedFolderKey.value === 'unclassified') {
    return { unclassified: true };
  }
  if (isFolderKey(selectedFolderKey.value)) {
    return { folderId: selectedFolderKey.value };
  }
  return {};
}

const [FolderForm, folderFormApi] = useVbenForm({
  commonConfig: { labelWidth: 88 },
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'folderName',
      label: '文件夹名称',
      rules: z.string().min(1, '请输入文件夹名称'),
    },
  ],
  showDefaultActions: false,
});

const [FolderModal, folderModalApi] = useVbenModal({
  async onConfirm() {
    await saveFolder();
  },
});

const [StorageForm, storageFormApi] = useVbenForm({
  commonConfig: { labelWidth: 88 },
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'configName',
      label: '配置名称',
      rules: z.string().min(1, '请输入配置名称'),
    },
    {
      component: 'Input',
      componentProps: { disabled: true },
      fieldName: 'storageType',
      label: '存储类型',
    },
    {
      component: 'Input',
      fieldName: 'basePath',
      label: '存储目录',
    },
    {
      component: 'Input',
      fieldName: 'accessPrefix',
      label: '访问前缀',
    },
    {
      component: 'Input',
      fieldName: 'domain',
      label: '访问域名',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '否', value: 0 },
          { label: '是', value: 1 },
        ],
      },
      fieldName: 'isDefault',
      label: '设为默认',
    },
  ],
  showDefaultActions: false,
});

const [StorageModal, storageModalApi] = useVbenModal({
  async onConfirm() {
    await saveStorage();
  },
});

const [DeleteFolderModal, deleteFolderModalApi] = useVbenModal({
  async onConfirm() {
    await confirmDeleteFolder();
  },
});

const [UploadProgressModal, uploadProgressModalApi] = useVbenModal({
  footer: false,
  onClosed() {
    uploadTasks.value = [];
  },
});

watch(
  [filteredFolderTree, isFolderSearchActive],
  ([tree, active]) => {
    folderSearchExpandedKeys.value = active
      ? collectFolderExpandableKeys(tree)
      : [];
  },
  { immediate: true },
);

watch(
  folderSearch,
  async (value, oldValue) => {
    if (value.trim() || !oldValue.trim()) return;
    folderRestoreTransitionDisabled.value = true;
    folderSearchExpandedKeys.value = [];
    await nextTick();
    await nextTick();
    folderRestoreTransitionDisabled.value = false;
  },
  { flush: 'sync' },
);

async function loadOptions() {
  const [folderItems, storageConfigItems] = await Promise.all([
    getFileFolders(),
    getStorageConfigs(),
  ]);
  folders.value = folderItems;
  storageConfigs.value = storageConfigItems;
  folderManualExpandedKeys.value = collectFolderExpandableKeys(
    buildFolderTree(folderItems),
  );
}

function buildFolderTree(items: SystemFileApi.Folder[]) {
  const nodeMap = new Map<string, SystemFileApi.Folder>();
  const roots: SystemFileApi.Folder[] = [];
  items.forEach((item) =>
    nodeMap.set(String(item.id), { ...item, children: [] }),
  );
  nodeMap.forEach((node) => {
    const parentKey = String(node.parentId ?? 0);
    if (!isRootFolder(node.parentId) && nodeMap.has(parentKey)) {
      nodeMap.get(parentKey)?.children?.push(node);
    } else {
      roots.push(node);
    }
  });
  const sortNodes = (nodes: SystemFileApi.Folder[]) => {
    nodes.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
    nodes.forEach((node) => sortNodes(node.children ?? []));
  };
  sortNodes(roots);
  return roots;
}

function isFolderKey(value: FolderSelection) {
  return value !== 'all' && value !== 'unclassified';
}

function getSelectedFolderId() {
  return isFolderKey(selectedFolderKey.value)
    ? (selectedFolderKey.value as FolderId)
    : FILE_FOLDER_IDS.MANUAL_UPLOAD;
}

function isRootFolder(id?: FolderId) {
  return id === undefined || id === null || String(id) === '0';
}

function getFolderName(id?: FolderId) {
  if (isRootFolder(id)) return undefined;
  return folders.value.find((item) => String(item.id) === String(id))
    ?.folderName;
}

function getChildFolders(id?: FolderId) {
  if (id === undefined || id === null) return [];
  return folders.value.filter((item) => String(item.parentId) === String(id));
}

function matchFolderEntry(label: string) {
  const keyword = folderSearch.value.trim().toLowerCase();
  return !keyword || label.toLowerCase().includes(keyword);
}

function filterFolderTree(
  nodes: SystemFileApi.Folder[],
  keyword: string,
): SystemFileApi.Folder[] {
  if (!keyword) return nodes;
  const lowerKeyword = keyword.toLowerCase();
  return nodes
    .map((node) => {
      const children = filterFolderTree(node.children ?? [], keyword);
      const matched = node.folderName.toLowerCase().includes(lowerKeyword);
      if (matched || children.length > 0) {
        return { ...node, children };
      }
      return null;
    })
    .filter(Boolean) as SystemFileApi.Folder[];
}

function collectFolderExpandableKeys(nodes: SystemFileApi.Folder[]) {
  const keys: FolderId[] = [];
  nodes.forEach((node) => {
    if (node.children?.length) {
      keys.push(node.id, ...collectFolderExpandableKeys(node.children));
    }
  });
  return keys;
}

function mergeFolderExpandedKeys(keys: FolderId[], nextKeys: FolderId[]) {
  return [...new Set([...keys, ...nextKeys])];
}

function findFolderPath(
  nodes: SystemFileApi.Folder[],
  id: FolderId,
  parents: FolderId[] = [],
): undefined | { hasChildren: boolean; parents: FolderId[] } {
  for (const node of nodes) {
    if (String(node.id) === String(id)) {
      return {
        hasChildren: Boolean(node.children?.length),
        parents,
      };
    }
    const matched = findFolderPath(node.children ?? [], id, [
      ...parents,
      node.id,
    ]);
    if (matched) return matched;
  }
  return undefined;
}

function rememberFolderSearchSelection(item: any) {
  if (!isFolderSearchActive.value) return;
  const folder = item.value as SystemFileApi.Folder;
  const path = findFolderPath(folderTree.value, folder.id);
  if (!path) return;
  const keys = [...path.parents];
  if (path.hasChildren) {
    keys.push(folder.id);
  }
  folderManualExpandedKeys.value = mergeFolderExpandedKeys(
    folderManualExpandedKeys.value,
    keys,
  );
}

function onActionClick({
  code,
  row,
}: OnActionClickParams<SystemFileApi.FileInfo>) {
  if (code === 'delete') {
    deleteFile(row.id).then(() => gridApi.query());
  }
}

function upsertUploadTask(file: any, values: Partial<UploadTask>) {
  const uid = String(file.uid ?? file.name);
  const index = uploadTasks.value.findIndex((item) => item.uid === uid);
  const nextTask: UploadTask = {
    name: file.name || '文件',
    percent: 0,
    status: 'uploading',
    uid,
    ...values,
  };
  if (index === -1) {
    uploadTasks.value = [...uploadTasks.value, nextTask];
    uploadProgressModalApi.open();
    return;
  }
  uploadTasks.value = uploadTasks.value.map((item) =>
    item.uid === uid ? { ...item, ...values } : item,
  );
}

function getUploadErrorMessage(error: unknown) {
  const uploadError = error as {
    message?: string;
    response?: { data?: { error?: string; message?: string; msg?: string } };
  };
  return (
    uploadError?.response?.data?.msg ||
    uploadError?.response?.data?.error ||
    uploadError?.response?.data?.message ||
    uploadError?.message ||
    '上传失败'
  );
}

async function customRequest({ file, onError, onProgress, onSuccess }: any) {
  upsertUploadTask(file, {
    errorMessage: undefined,
    percent: 0,
    status: 'uploading',
  });
  if ((file.size || 0) > UPLOAD_FILE_MAX_SIZE_BYTES) {
    const errorMessage = `文件大小不能超过 ${UPLOAD_FILE_MAX_SIZE_TEXT}MB`;
    const error = new Error(errorMessage);
    upsertUploadTask(file, { errorMessage, status: 'error' });
    onError?.(error);
    throw error;
  }
  try {
    await uploadFileApi(file, getSelectedFolderId(), (event) => {
      const percent = event.total
        ? Math.round((event.loaded / event.total) * 100)
        : 0;
      upsertUploadTask(file, { percent, status: 'uploading' });
      onProgress?.({ percent }, file);
    });
    upsertUploadTask(file, { percent: 100, status: 'success' });
    onSuccess?.();
    gridApi.query();
  } catch (error) {
    upsertUploadTask(file, {
      errorMessage: getUploadErrorMessage(error),
      status: 'error',
    });
    onError?.(error);
    throw error;
  }
}

function selectAllFiles() {
  selectedFolderKey.value = 'all';
  folderTreeValue.value = undefined;
  gridApi.query();
}

function selectUnclassifiedFiles() {
  selectedFolderKey.value = 'unclassified';
  folderTreeValue.value = undefined;
  gridApi.query();
}

function selectFolder(folder: SystemFileApi.Folder) {
  selectedFolderKey.value = folder.id;
  folderTreeValue.value = folder.id;
  gridApi.query();
}

function onSelectFolderNode(item: any) {
  rememberFolderSearchSelection(item);
  selectFolder(item.value as SystemFileApi.Folder);
}

function getNextFolderSort(parentId: FolderId) {
  const siblingSorts = folders.value
    .filter((item) => String(item.parentId ?? 0) === String(parentId))
    .map((item) => item.sort ?? 0);
  return siblingSorts.length > 0 ? Math.max(...siblingSorts) + 1 : 0;
}

async function openCreateFolder(parentId: FolderId = 0) {
  folderModalMode.value = 'create';
  editingFolder.value = undefined;
  folderParentId.value = parentId;
  folderModalApi.open();
  await nextTick();
  await folderFormApi.resetForm();
  await folderFormApi.setValues({ folderName: '' });
}

async function openEditFolder(folder: SystemFileApi.Folder) {
  folderModalMode.value = 'edit';
  editingFolder.value = folder;
  folderParentId.value = folder.parentId ?? 0;
  selectedFolderKey.value = folder.id;
  folderTreeValue.value = folder.id;
  folderModalApi.open();
  await nextTick();
  await folderFormApi.resetForm();
  await folderFormApi.setValues({ folderName: folder.folderName });
}

async function saveFolder() {
  const { valid } = await folderFormApi.validate();
  if (!valid) return;
  const values = await folderFormApi.getValues();
  const payload = {
    folderName: values.folderName,
    parentId: folderParentId.value,
    sort:
      folderModalMode.value === 'edit'
        ? (editingFolder.value?.sort ?? 0)
        : getNextFolderSort(folderParentId.value),
  };
  folderModalApi.lock();
  try {
    if (folderModalMode.value === 'edit' && editingFolder.value) {
      await updateFileFolder(editingFolder.value.id, payload);
      selectedFolderKey.value = editingFolder.value.id;
      folderTreeValue.value = editingFolder.value.id;
    } else {
      await createFileFolder(payload);
    }
    folderModalApi.close();
    await loadOptions();
    if (!isRootFolder(folderParentId.value)) {
      folderTreeRef.value?.expandNodes(folderParentId.value);
    }
    gridApi.query();
  } catch {
    folderModalApi.unlock();
  }
}

function removeFolder(folder: SystemFileApi.Folder) {
  if (folder.isBuiltin === 1) return;
  selectedFolderKey.value = folder.id;
  folderTreeValue.value = folder.id;
  deletingFolder.value = folder;
  deleteFolderModalApi.open();
}

async function confirmDeleteFolder() {
  if (!deletingFolder.value) return;
  deleteFolderModalApi.lock();
  try {
    await deleteFileFolder(deletingFolder.value.id);
    selectedFolderKey.value = 'all';
    folderTreeValue.value = undefined;
    deleteFolderModalApi.close();
    await loadOptions();
    gridApi.query();
  } catch {
    deleteFolderModalApi.unlock();
  }
}

async function saveStorage() {
  const { valid } = await storageFormApi.validate();
  if (!valid) return;
  const values = await storageFormApi.getValues();
  storageModalApi.lock();
  try {
    await createStorageConfig(values);
    storageModalApi.close();
    await loadOptions();
  } catch {
    storageModalApi.unlock();
  }
}

async function openStorageModal() {
  await storageFormApi.resetForm();
  await storageFormApi.setValues({
    accessPrefix: '/files',
    basePath: '${user.home}/data/uploads',
    configName: '',
    domain: '',
    isDefault: 0,
    status: 1,
    storageType: 'LOCAL',
  });
  storageModalApi.open();
}

onMounted(loadOptions);
</script>

<template>
  <Page auto-content-height>
    <div class="flex size-full">
      <Card class="folder-panel w-72 flex-none">
        <div class="mb-4 flex items-center justify-between">
          <span class="folder-panel-title">文件夹</span>
          <Button
            v-access:code="SYSTEM_PERMS.fileUpload"
            class="folder-create-btn"
            type="link"
            @click="openCreateFolder()"
          >
            <IconifyIcon icon="lucide:plus" class="size-4" />
            新建
          </Button>
        </div>

        <Input
          v-model:value="folderSearch"
          allow-clear
          class="folder-search mb-5"
          placeholder="请输入文件夹名称"
        >
          <template #prefix>
            <IconifyIcon
              icon="lucide:search"
              class="size-4 text-muted-foreground"
            />
          </template>
        </Input>

        <div
          v-if="showAllEntry"
          class="folder-row"
          :class="
            selectedFolderKey === 'all'
              ? 'folder-row-selected'
              : 'folder-row-normal'
          "
          @click="selectAllFiles"
        >
          <span class="folder-row-spacer"></span>
          <IconifyIcon icon="lucide:folder" class="folder-icon" />
          <span class="folder-name">全部</span>
        </div>

        <div
          v-if="showUnclassifiedEntry"
          class="folder-row"
          :class="
            selectedFolderKey === 'unclassified'
              ? 'folder-row-selected'
              : 'folder-row-normal'
          "
          @click="selectUnclassifiedFiles"
        >
          <span class="folder-row-spacer"></span>
          <IconifyIcon icon="lucide:folder" class="folder-icon" />
          <span class="folder-name">未分类</span>
        </div>

        <Tree
          v-if="filteredFolderTree.length > 0"
          ref="folderTreeRef"
          :key="folderTreeRenderKey"
          v-model="folderTreeValue"
          v-model:expanded-keys="folderTreeExpandedKeys"
          class="side-tree folder-tree"
          :default-expanded-level="0"
          :show-icon="false"
          :show-toolbar="false"
          :tree-data="filteredFolderTree"
          :transition="!folderRestoreTransitionDisabled"
          children-field="children"
          label-field="folderName"
          value-field="id"
          @select="onSelectFolderNode"
        >
          <template #node="{ value: folder }">
            <div class="folder-node group">
              <IconifyIcon icon="lucide:folder" class="folder-icon" />
              <span class="folder-name">{{ folder.folderName }}</span>
              <div class="folder-actions" @click.stop>
                <button
                  v-access:code="SYSTEM_PERMS.fileUpload"
                  class="folder-action"
                  title="新建子文件夹"
                  type="button"
                  @click="openCreateFolder(folder.id)"
                >
                  <IconifyIcon icon="lucide:plus" class="size-4" />
                </button>
                <button
                  v-access:code="SYSTEM_PERMS.fileUpload"
                  class="folder-action"
                  title="修改文件夹"
                  type="button"
                  @click="openEditFolder(folder)"
                >
                  <IconifyIcon icon="lucide:square-pen" class="size-4" />
                </button>
                <button
                  v-if="folder.isBuiltin !== 1"
                  v-access:code="SYSTEM_PERMS.fileDelete"
                  class="folder-action"
                  title="删除文件夹"
                  type="button"
                  @click="removeFolder(folder)"
                >
                  <IconifyIcon icon="lucide:trash-2" class="size-4" />
                </button>
              </div>
            </div>
          </template>
        </Tree>
      </Card>
      <div class="ml-4 min-w-0 flex-1">
        <Grid :table-title="fileTableTitle">
          <template #toolbar-tools>
            <div class="flex items-center gap-2">
              <Button
                v-access:code="SYSTEM_PERMS.fileUpload"
                @click="openStorageModal"
              >
                存储配置（{{ storageConfigs.length }}）
              </Button>
              <Upload
                v-access:code="SYSTEM_PERMS.fileUpload"
                :custom-request="customRequest"
                multiple
                :show-upload-list="false"
              >
                <Button v-access:code="SYSTEM_PERMS.fileUpload" type="primary">
                  上传文件
                </Button>
              </Upload>
            </div>
          </template>
          <template #preview="{ row }">
            <div class="file-preview-cell">
              <Image
                v-if="row.mimeType?.startsWith('image/')"
                :src="row.url"
                :width="32"
                :height="32"
                class="file-preview-image object-cover"
              />
              <Button
                v-else
                type="link"
                :href="row.url"
                target="_blank"
                title="预览"
                class="file-preview-button"
              >
                <IconifyIcon :icon="getPreviewIcon(row)" class="size-7" />
              </Button>
            </div>
          </template>
        </Grid>
      </div>
    </div>
    <FolderModal
      :description="folderModalDescription"
      :title="folderModalTitle"
    >
      <FolderForm />
    </FolderModal>
    <StorageModal title="新增存储配置">
      <StorageForm />
    </StorageModal>
    <DeleteFolderModal title="删除文件夹">
      <p class="text-sm text-muted-foreground">{{ deleteFolderContent }}</p>
    </DeleteFolderModal>
    <UploadProgressModal class="w-[640px]" title="上传进度">
      <div class="upload-progress-panel">
        <div class="upload-progress-list">
          <div
            v-for="task in uploadTasks"
            :key="task.uid"
            class="upload-progress-item"
            :class="`upload-progress-item-${task.status}`"
          >
            <div class="upload-progress-header">
              <span class="upload-progress-name">{{ task.name }}</span>
              <span
                class="upload-progress-status"
                :class="`upload-progress-status-${task.status}`"
              >
                {{
                  task.status === 'success'
                    ? '已完成'
                    : task.status === 'error'
                      ? '上传失败'
                      : '上传中'
                }}
              </span>
            </div>
            <Progress
              :percent="task.percent"
              :status="task.status === 'error' ? 'exception' : 'normal'"
              size="small"
            />
            <div v-if="task.errorMessage" class="upload-progress-error">
              {{ task.errorMessage }}
            </div>
          </div>
          <div
            v-if="uploadTasks.length === 0"
            class="flex h-full items-center justify-center text-sm text-muted-foreground"
          >
            暂无上传任务
          </div>
        </div>
      </div>
    </UploadProgressModal>
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

.folder-actions {
  display: inline-flex;
  align-items: center;
  flex: none;
  gap: 8px;
  margin-left: 8px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.16s ease;
}

.folder-row:hover .folder-actions,
.folder-node:hover .folder-actions {
  opacity: 1;
  pointer-events: auto;
}

.folder-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: 0;
  color: hsl(var(--primary));
  background: transparent;
}

.folder-action:hover {
  color: hsl(var(--primary) / 80%);
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

.upload-progress-panel {
  width: 592px;
  height: 360px;
}

.upload-progress-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  overflow-y: auto;
}

.upload-progress-item {
  padding: 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
}

.upload-progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 13px;
}

.upload-progress-name {
  min-width: 0;
  overflow: hidden;
  color: hsl(var(--foreground));
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-progress-status {
  flex: none;
  color: hsl(var(--primary));
  font-size: 12px;
}

.upload-progress-status-success {
  color: hsl(var(--primary));
}

.upload-progress-status-error {
  color: hsl(var(--destructive));
}

.upload-progress-error {
  margin-top: 6px;
  color: hsl(var(--destructive));
  font-size: 12px;
  line-height: 18px;
}

.upload-progress-item :deep(.ant-progress-bg) {
  background-color: hsl(var(--primary)) !important;
}

.upload-progress-item-error :deep(.ant-progress-bg) {
  background-color: hsl(var(--destructive)) !important;
}

.file-preview-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 48px;
  height: 48px;
  margin: 4px auto;
  padding: 8px;
}

.file-preview-cell :deep(.ant-image) {
  width: 32px !important;
  height: 32px !important;
  overflow: hidden;
}

.file-preview-cell :deep(.ant-image-img) {
  width: 32px !important;
  height: 32px !important;
}

.file-preview-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  color: hsl(var(--muted-foreground));
}

.file-preview-button:hover {
  color: hsl(var(--primary));
}
</style>
