<script lang="ts" setup>
import type {
  OnActionClickParams,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { SystemFileApi } from '#/api';

import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { ColPage, Tree, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Card,
  Image,
  Input,
  message,
  Popconfirm,
  Progress,
  Tag,
  Upload,
} from 'antdv-next';

import { useVbenForm, z } from '#/adapter/form';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createFileFolder,
  createStorageConfig,
  deleteFile,
  deleteFileFolder,
  deleteStorageConfig,
  getFileFolders,
  getFilePage,
  getStorageConfigDetail,
  getStorageConfigPage,
  getStorageConfigs,
  getStorageTypes,
  getUploadPolicy,
  setDefaultStorageConfig,
  updateFileFolder,
  updateStorageConfig,
  UPLOAD_FILE_MAX_SIZE_BYTES,
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

const defaultStorageTypeOptions = [
  { label: '本地存储', value: 'LOCAL' },
];
const storageTypeOptions = ref<SystemFileApi.StorageTypeOption[]>([
  ...defaultStorageTypeOptions,
]);
const storageTypeFormFields = [
  'storagePath',
  'domain',
  'endpoint',
  'region',
  'bucketId',
  'bucketName',
  'accessKey',
  'secretKey',
];
const storageTypeFormFieldsByType: Record<string, string[]> = {
  LOCAL: ['storagePath', 'domain'],
};
const defaultAllowedUploadExtensions = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'webp',
  'bmp',
  'svg',
  'ico',
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'txt',
  'csv',
  'md',
  'json',
  'xml',
  'html',
  'css',
  'js',
  'ts',
  'vue',
  'zip',
  'rar',
  '7z',
  'tar',
  'gz',
  'mp3',
  'wav',
  'ogg',
  'm4a',
  'mp4',
  'webm',
  'mov',
  'avi',
  'mkv',
];
const httpUrlPrefixRule = /^https?:\/\/.+/;

const selectedFolderKey = ref<FolderSelection>('all');
const folderTreeValue = ref<FolderId>();
const folderTreeRef = ref<TreeExpose>();
const folderManualExpandedKeys = ref<FolderId[]>([]);
const folderSearchExpandedKeys = ref<FolderId[]>([]);
const folderRestoreTransitionDisabled = ref(false);
const editingFolder = ref<SystemFileApi.Folder>();
const deletingFolder = ref<SystemFileApi.Folder>();
const folderParentId = ref<FolderId>(0);
const isWindowResizing = ref(false);
const mediaThumbnailsReady = ref(false);
const uploadTasks = ref<UploadTask[]>([]);
const uploadPolicy = ref<SystemFileApi.UploadPolicy>({
  allowedExtensions: defaultAllowedUploadExtensions,
  maxFileSizeBytes: UPLOAD_FILE_MAX_SIZE_BYTES,
});
const folders = ref<SystemFileApi.Folder[]>([]);
const storageConfigs = ref<SystemFileApi.StorageConfig[]>([]);
const storageConfigPageItems = ref<SystemFileApi.StorageConfig[]>([]);
const storageConfigPageLoading = ref(false);
const storageConfigPageNum = ref(1);
const storageConfigPageSize = 5;
const storageConfigTotal = ref(0);
const storageModalMode = ref<'create' | 'edit'>('create');
const editingStorageConfig = ref<SystemFileApi.StorageConfig>();
const storageConfigSearchOptions = computed(() =>
  storageConfigs.value.map((item) => ({
    label: item.configName,
    value: item.id,
  })),
);
const allowedUploadExtensionSet = computed(
  () =>
    new Set(
      uploadPolicy.value.allowedExtensions.map((extension) =>
        extension.replace(/^\./, '').toLowerCase(),
      ),
    ),
);
const uploadAccept = computed(() =>
  [...allowedUploadExtensionSet.value]
    .map((extension) => `.${extension}`)
    .join(','),
);
const uploadMaxSizeText = computed(() =>
  formatFileSizeText(uploadPolicy.value.maxFileSizeBytes),
);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useColumns(onActionClick),
    height: 'auto',
    proxyConfig: {
      ajax: {
        async query({ page, sort }, values) {
          const thumbnailRequestId = beginMediaThumbnailDelay();
          const result = await getFilePage({
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
          });
          scheduleMediaThumbnailsAfterTablePaint(thumbnailRequestId);
          return result;
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<SystemFileApi.FileInfo>,
});

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
const uploadFinished = computed(
  () =>
    uploadTasks.value.length > 0 &&
    uploadTasks.value.every((task) => task.status !== 'uploading'),
);
const storageModalTitle = computed(() =>
  storageModalMode.value === 'edit' ? '编辑存储配置' : '新增存储配置',
);
const storageConfigTotalPages = computed(() =>
  Math.max(1, Math.ceil(storageConfigTotal.value / storageConfigPageSize)),
);

let mediaThumbnailFrame: number | undefined;
let mediaThumbnailRequestId = 0;
let mediaThumbnailTaskTimer: ReturnType<typeof setTimeout> | undefined;
let windowResizeTimer: ReturnType<typeof setTimeout> | undefined;

function isImageFile(row: SystemFileApi.FileInfo) {
  return row.mimeType?.startsWith('image/');
}

function isVideoFile(row: SystemFileApi.FileInfo) {
  return row.mimeType?.startsWith('video/');
}

function shouldShowMediaThumbnail(row: SystemFileApi.FileInfo) {
  return (
    mediaThumbnailsReady.value &&
    !isWindowResizing.value &&
    (isImageFile(row) || isVideoFile(row))
  );
}

function beginMediaThumbnailDelay() {
  mediaThumbnailRequestId += 1;
  mediaThumbnailsReady.value = false;
  if (mediaThumbnailFrame) {
    cancelAnimationFrame(mediaThumbnailFrame);
    mediaThumbnailFrame = undefined;
  }
  if (mediaThumbnailTaskTimer) {
    clearTimeout(mediaThumbnailTaskTimer);
    mediaThumbnailTaskTimer = undefined;
  }
  return mediaThumbnailRequestId;
}

function scheduleMediaThumbnailsAfterTablePaint(requestId: number) {
  mediaThumbnailTaskTimer = setTimeout(() => {
    showMediaThumbnailsAfterTablePaint(requestId);
    mediaThumbnailTaskTimer = undefined;
  }, 0);
}

async function showMediaThumbnailsAfterTablePaint(requestId: number) {
  await nextTick();
  await nextTick();
  mediaThumbnailFrame = requestAnimationFrame(() => {
    mediaThumbnailFrame = requestAnimationFrame(() => {
      if (requestId === mediaThumbnailRequestId) {
        mediaThumbnailsReady.value = true;
      }
      mediaThumbnailFrame = undefined;
    });
  });
}

function handleWindowResize() {
  isWindowResizing.value = true;
  if (windowResizeTimer) {
    clearTimeout(windowResizeTimer);
  }
  windowResizeTimer = setTimeout(() => {
    isWindowResizing.value = false;
  }, 180);
}

function getPreviewIcon(row: SystemFileApi.FileInfo) {
  const extension = row.extension?.toLowerCase();
  const mimeType = row.mimeType?.toLowerCase() ?? '';
  if (mimeType.startsWith('image/')) {
    return 'lucide:image';
  }
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

function getUploadFileExtension(fileName?: string) {
  if (!fileName) return '';
  const dotIndex = fileName.lastIndexOf('.');
  if (dotIndex <= 0 || dotIndex === fileName.length - 1) return '';
  return fileName.slice(dotIndex + 1).toLowerCase();
}

function openMediaPreview(row: SystemFileApi.FileInfo) {
  window.open(row.url, '_blank', 'noopener,noreferrer');
}

function formatFileSizeText(bytes: number) {
  const size = bytes / 1024 / 1024;
  if (size >= 1) {
    return String(Math.floor(size));
  }
  return size >= 0.01 ? size.toFixed(2) : '0.01';
}

function getFolderQueryParams() {
  if (selectedFolderKey.value === 'unclassified') {
    return { folderId: 0 };
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
      rules: z
        .string()
        .min(1, '文件夹名称不能为空')
        .max(20, '文件夹名称长度不能超过20个字符'),
    },
  ],
  showDefaultActions: false,
});

const [FolderModal, folderModalApi] = useVbenModal({
  async onConfirm() {
    await saveFolder();
  },
});

const [StorageConfigDrawer, storageConfigDrawerApi] = useVbenDrawer({
  destroyOnClose: true,
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    storageConfigPageNum.value = 1;
    await loadStorageConfigPage();
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
      rules: z
        .string()
        .trim()
        .min(1, '请输入配置名称')
        .max(100, '配置名称长度不能超过100'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        onChange: (value?: string) => applyStorageTypeSchema(value),
        options: storageTypeOptions.value,
        placeholder: '请选择存储类型',
        popupClassName: 'storage-type-select-dropdown',
      },
      fieldName: 'storageType',
      label: '存储类型',
      rules: z.preprocess(
        (value) => value ?? '',
        z.string().trim().min(1, '存储类型不能为空'),
      ),
    },
    {
      component: 'Input',
      fieldName: 'storagePath',
      hide: true,
      label: '存储路径',
      rules: z
        .string()
        .trim()
        .min(1, '请输入存储路径')
        .max(500, '存储路径长度不能超过500'),
    },
    {
      component: 'Input',
      fieldName: 'domain',
      hide: true,
      label: '访问域名',
      rules: z
        .string()
        .trim()
        .min(1, '请输入访问域名')
        .max(500, '访问域名长度不能超过500')
        .regex(httpUrlPrefixRule, '访问域名必须以http://或https://开头'),
    },
    {
      component: 'Input',
      fieldName: 'endpoint',
      hide: true,
      label: '服务端点',
      rules: z.string().max(500, 'endpoint长度不能超过500').optional(),
    },
    {
      component: 'Input',
      fieldName: 'region',
      hide: true,
      label: '地域',
      rules: z.string().max(100, 'region长度不能超过100').optional(),
    },
    {
      component: 'Input',
      fieldName: 'bucketId',
      hide: true,
      label: 'Bucket ID',
      rules: z.string().max(200, 'bucketId长度不能超过200').optional(),
    },
    {
      component: 'Input',
      fieldName: 'bucketName',
      hide: true,
      label: 'Bucket 名称',
      rules: z.string().max(200, 'bucketName长度不能超过200').optional(),
    },
    {
      component: 'Input',
      fieldName: 'accessKey',
      hide: true,
      label: 'Access Key',
      rules: z.string().max(500, 'accessKey长度不能超过500').optional(),
    },
    {
      component: 'InputPassword',
      fieldName: 'secretKey',
      hide: true,
      label: 'Secret Key',
      rules: z.string().max(1000, 'secretKey长度不能超过1000').optional(),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '否', value: 0 },
          { label: '是', value: 1 },
        ],
        optionType: 'button',
      },
      fieldName: 'isDefault',
      formItemClass: 'storage-config-radio-item',
      label: '默认',
      labelClass: 'storage-config-radio-label',
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '禁用', value: 0 },
          { label: '启用', value: 1 },
        ],
        optionType: 'button',
      },
      fieldName: 'status',
      formItemClass: 'storage-config-radio-item',
      label: '状态',
      labelClass: 'storage-config-radio-label',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 2 },
      fieldName: 'remark',
      label: '备注',
      rules: z.string().max(500, '备注长度不能超过500').optional(),
    },
  ],
  showDefaultActions: false,
});

const [StorageModal, storageModalApi] = useVbenModal({
  async onConfirm() {
    await saveStorage();
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    await nextTick();
    await storageFormApi.resetForm();
    updateStorageTypeOptions();
    if (storageModalMode.value === 'edit' && editingStorageConfig.value) {
      await storageFormApi.setValues(editingStorageConfig.value);
      applyStorageTypeSchema(editingStorageConfig.value.storageType);
      return;
    }
    await resetStorageForm();
  },
  zIndex: 2200,
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
  const [folderItems, storageConfigItems, policy, storageTypeItems] = await Promise.all([
    getFileFolders(),
    getStorageConfigs(),
    getUploadPolicy(),
    getStorageTypes().catch(() => defaultStorageTypeOptions),
  ]);
  folders.value = folderItems;
  storageConfigs.value = storageConfigItems;
  storageTypeOptions.value = storageTypeItems;
  uploadPolicy.value = {
    allowedExtensions: policy.allowedExtensions?.length
      ? policy.allowedExtensions
      : defaultAllowedUploadExtensions,
    maxFileSizeBytes: policy.maxFileSizeBytes || UPLOAD_FILE_MAX_SIZE_BYTES,
  };
  gridApi.formApi.updateSchema([
    {
      componentProps: {
        allowClear: true,
        options: storageConfigSearchOptions.value,
      },
      fieldName: 'storageConfigId',
    },
  ]);
  folderManualExpandedKeys.value = collectFolderExpandableKeys(
    buildFolderTree(folderItems),
  );
}

async function loadStorageConfigPage() {
  storageConfigPageLoading.value = true;
  storageConfigPageItems.value = [];
  try {
    const result = await getStorageConfigPage({
      pageNum: storageConfigPageNum.value,
      pageSize: storageConfigPageSize,
    });
    storageConfigPageItems.value = result.records ?? [];
    storageConfigTotal.value = result.total ?? 0;
    const totalPages = Math.max(
      1,
      Math.ceil(storageConfigTotal.value / storageConfigPageSize),
    );
    if (
      storageConfigPageItems.value.length === 0 &&
      storageConfigPageNum.value > totalPages
    ) {
      storageConfigPageNum.value = totalPages;
      await loadStorageConfigPage();
    }
  } finally {
    storageConfigPageLoading.value = false;
  }
}

function changeStorageConfigPage(offset: number) {
  const nextPage = storageConfigPageNum.value + offset;
  if (nextPage < 1 || nextPage > storageConfigTotalPages.value) {
    return;
  }
  storageConfigPageNum.value = nextPage;
  loadStorageConfigPage();
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
    : undefined;
}

function isRootFolder(id?: FolderId) {
  return id === undefined || id === null || String(id) === '0';
}

function getFolderName(id?: FolderId) {
  if (isRootFolder(id)) return undefined;
  return folders.value.find((item) => String(item.id) === String(id))
    ?.folderName;
}

function getStorageTypeLabel(type?: string) {
  return (
    storageTypeOptions.value.find((item) => item.value === type)?.label ?? type
  );
}

function updateStorageTypeOptions() {
  storageFormApi.updateSchema([
    {
      componentProps: {
        allowClear: true,
        onChange: (value?: string) => applyStorageTypeSchema(value),
        options: storageTypeOptions.value,
        placeholder: '请选择存储类型',
        popupClassName: 'storage-type-select-dropdown',
      },
      fieldName: 'storageType',
    },
  ]);
}

function applyStorageTypeSchema(storageType?: string) {
  const visibleFields = new Set(
    storageType ? (storageTypeFormFieldsByType[storageType] ?? []) : [],
  );
  storageFormApi.updateSchema(
    storageTypeFormFields.map((fieldName) => ({
      fieldName,
      hide: !visibleFields.has(fieldName),
    })),
  );
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
  const extension = getUploadFileExtension(file.name);
  if (!allowedUploadExtensionSet.value.has(extension)) {
    const errorMessage = '不支持上传该文件类型';
    const error = new Error(errorMessage);
    upsertUploadTask(file, { errorMessage, status: 'error' });
    onError?.(error);
    throw error;
  }
  if ((file.size || 0) > uploadPolicy.value.maxFileSizeBytes) {
    const errorMessage = `文件大小不能超过 ${uploadMaxSizeText.value}MB`;
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

function closeUploadProgress() {
  uploadProgressModalApi.close();
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
  Object.keys(values).forEach((key) => {
    if (typeof values[key] === 'string') {
      values[key] = values[key].trim();
    }
  });
  storageModalApi.lock();
  try {
    if (storageModalMode.value === 'edit' && editingStorageConfig.value) {
      await updateStorageConfig(editingStorageConfig.value.id, values);
      message.success('存储配置已更新');
    } else {
      await createStorageConfig(values);
      message.success('存储配置已创建');
    }
    await loadOptions();
    await loadStorageConfigPage();
    storageModalMode.value = 'create';
    editingStorageConfig.value = undefined;
    storageModalApi.close();
  } catch {
    storageModalApi.unlock();
  }
}

async function resetStorageForm() {
  await storageFormApi.resetForm();
  await storageFormApi.setValues({
    storagePath: '',
    configName: '',
    domain: '',
    endpoint: '',
    region: '',
    bucketId: '',
    bucketName: '',
    accessKey: '',
    secretKey: '',
    isDefault: 0,
    remark: '',
    status: 1,
    storageType: undefined,
  });
  applyStorageTypeSchema();
}

function openStorageModal() {
  storageConfigDrawerApi.open();
}

function openStorageFormModal() {
  storageModalApi.open();
}

function createStorageConfigItem() {
  storageModalMode.value = 'create';
  editingStorageConfig.value = undefined;
  openStorageFormModal();
}

async function editStorageConfig(config: SystemFileApi.StorageConfig) {
  storageModalMode.value = 'edit';
  const detail = await getStorageConfigDetail(config.id);
  editingStorageConfig.value = detail;
  openStorageFormModal();
}

async function markDefaultStorageConfig(config: SystemFileApi.StorageConfig) {
  if (config.isDefault === 1) return;
  await setDefaultStorageConfig(config.id);
  message.success('默认存储配置已更新');
  await loadOptions();
  await loadStorageConfigPage();
}

async function removeStorageConfig(config: SystemFileApi.StorageConfig) {
  await deleteStorageConfig(config.id);
  message.success('存储配置已删除');
  await loadOptions();
  await loadStorageConfigPage();
}

onMounted(() => {
  loadOptions();
  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
  if (mediaThumbnailTaskTimer) {
    clearTimeout(mediaThumbnailTaskTimer);
  }
  if (mediaThumbnailFrame) {
    cancelAnimationFrame(mediaThumbnailFrame);
  }
  if (windowResizeTimer) {
    clearTimeout(windowResizeTimer);
  }
});
</script>

<template>
  <div class="h-full min-h-0">
    <ColPage
      auto-content-height
      :left-max-width="34"
      :left-min-width="22"
      :left-width="22"
      :right-min-width="50"
      :right-width="78"
      split-handle
      split-line
    >
      <template #left>
        <Card class="folder-panel h-full">
          <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
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
    </template>
    <Grid :table-title="fileTableTitle">
      <template #toolbar-tools>
        <div class="flex items-center gap-2">
          <Button
            v-access:code="SYSTEM_PERMS.fileUpload"
            @click="openStorageModal"
          >
            <IconifyIcon icon="lucide:settings" class="size-4" />
            存储配置（{{ storageConfigs.length }}）
          </Button>
          <Upload
            v-access:code="SYSTEM_PERMS.fileUpload"
            :accept="uploadAccept"
            :custom-request="customRequest"
            multiple
            :show-upload-list="false"
          >
            <Button v-access:code="SYSTEM_PERMS.fileUpload" type="primary">
              <IconifyIcon icon="lucide:upload" class="size-4" />
              上传文件
            </Button>
          </Upload>
        </div>
      </template>
      <template #preview="{ row }">
        <div class="file-preview-cell">
          <div
            v-if="shouldShowMediaThumbnail(row) && isImageFile(row)"
            class="file-preview-image-frame"
          >
            <Image
              class="file-preview-image"
              :height="32"
              :preview="{ src: row.url }"
              :src="row.url"
              :width="32"
            />
          </div>
          <button
            v-else-if="shouldShowMediaThumbnail(row) && isVideoFile(row)"
            class="file-preview-media-button"
            title="预览"
            type="button"
            @click="openMediaPreview(row)"
          >
            <video
              class="file-preview-media"
              muted
              playsinline
              preload="metadata"
              :src="row.url"
            ></video>
            <span class="file-preview-video-mask">
              <IconifyIcon icon="lucide:play" class="size-5" />
            </span>
          </button>
          <Button
            v-else-if="isImageFile(row) || isVideoFile(row)"
            type="link"
            html-type="button"
            title="预览"
            class="file-preview-button"
            @click="openMediaPreview(row)"
          >
            <IconifyIcon :icon="getPreviewIcon(row)" class="size-7" />
          </Button>
          <Button
            v-else
            type="link"
            html-type="button"
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
  </ColPage>
  <FolderModal :description="folderModalDescription" :title="folderModalTitle">
    <FolderForm />
  </FolderModal>
  <StorageConfigDrawer class="w-full max-w-200" title="存储配置">
    <div class="storage-config-toolbar">
      <Button
        v-access:code="SYSTEM_PERMS.fileUpload"
        type="primary"
        @click.stop="createStorageConfigItem"
      >
        <IconifyIcon icon="lucide:plus" class="size-4" />
        新增配置
      </Button>
    </div>

    <div class="storage-config-list-header">
      <span>配置名称</span>
      <span>存储信息</span>
      <span>操作</span>
    </div>
    <div class="storage-config-list">
      <div
        v-for="config in storageConfigPageItems"
        :key="config.id"
        class="storage-config-item"
        :class="{
          'storage-config-item-current': config.isDefault === 1,
          'storage-config-item-disabled': config.status !== 1,
        }"
      >
        <div class="storage-config-main">
          <div class="storage-config-name-row">
            <span class="storage-config-name">{{ config.configName }}</span>
            <Tag v-if="config.isDefault === 1" color="blue">默认</Tag>
            <Tag :color="config.status === 1 ? 'green' : 'default'">
              {{ config.status === 1 ? '启用' : '禁用' }}
            </Tag>
          </div>
          <div class="storage-config-meta">
            {{ getStorageTypeLabel(config.storageType) }}
          </div>
        </div>
        <div class="storage-config-detail">
          <span>访问域名：{{ config.domain || '未配置' }}</span>
          <span>存储路径：{{ config.storagePath || '-' }}</span>
        </div>
        <div class="storage-config-actions">
          <Button
            v-access:code="SYSTEM_PERMS.fileUpload"
            size="small"
            type="link"
            @click.stop="editStorageConfig(config)"
          >
            编辑
          </Button>
          <Button
            v-if="config.isDefault !== 1"
            v-access:code="SYSTEM_PERMS.fileUpload"
            class="storage-config-default-action"
            size="small"
            type="link"
            @click="markDefaultStorageConfig(config)"
          >
            设为默认
          </Button>
          <Popconfirm
            v-if="config.isDefault !== 1"
            title="删除后不可恢复，确认删除该存储配置？"
            @confirm="removeStorageConfig(config)"
          >
            <Button
              v-access:code="SYSTEM_PERMS.fileDelete"
              danger
              size="small"
              type="link"
            >
              删除
            </Button>
          </Popconfirm>
        </div>
      </div>
      <div v-if="storageConfigPageLoading" class="storage-config-empty">
        加载中...
      </div>
      <div
        v-else-if="storageConfigPageItems.length === 0"
        class="storage-config-empty"
      >
        暂无存储配置
      </div>
    </div>
    <template #footer>
      <div class="storage-config-pagination">
        <span class="storage-config-total"
          >共 {{ storageConfigTotal }} 个配置</span
        >
        <div class="storage-config-page-actions">
          <span>
            第 {{ storageConfigPageNum }} / {{ storageConfigTotalPages }} 页
          </span>
          <Button
            size="small"
            :disabled="storageConfigPageNum <= 1"
            @click="changeStorageConfigPage(-1)"
          >
            上一页
          </Button>
          <Button
            size="small"
            :disabled="storageConfigPageNum >= storageConfigTotalPages"
            @click="changeStorageConfigPage(1)"
          >
            下一页
          </Button>
        </div>
      </div>
    </template>
  </StorageConfigDrawer>
  <StorageModal
    class="w-[760px]"
    :fullscreen-button="false"
    :title="storageModalTitle"
  >
    <div class="storage-config-form">
      <StorageForm />
    </div>
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
      <div class="upload-progress-actions">
        <Button @click="closeUploadProgress">取消</Button>
        <Button
          type="primary"
          :disabled="!uploadFinished"
          @click="closeUploadProgress"
        >
          完成
        </Button>
      </div>
    </div>
    </UploadProgressModal>
  </div>
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
  white-space: nowrap;
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
  display: flex;
  flex-direction: column;
  width: 592px;
  height: 336px;
}

.upload-progress-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 0;
  flex: 1;
  overflow-y: auto;
}

.upload-progress-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 16px;
  padding-bottom: 4px;
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

.storage-config-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 32px;
}

.storage-config-total {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.storage-config-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
  margin-bottom: 18px;
}

.storage-config-list-header {
  display: grid;
  grid-template-columns: minmax(170px, 0.75fr) minmax(0, 1fr) 168px;
  gap: 14px;
  margin-top: 14px;
  padding: 0 14px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 20px;
}

.storage-config-list-header span:last-child {
  padding-left: 7px;
}

.storage-config-item {
  display: grid;
  grid-template-columns: minmax(170px, 0.75fr) minmax(0, 1fr) 168px;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
}

.storage-config-item-current {
  border-color: hsl(var(--primary) / 45%);
  background-color: hsl(var(--primary) / 8%);
}

.storage-config-item-disabled {
  opacity: 0.62;
}

.storage-config-main {
  min-width: 0;
}

.storage-config-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.storage-config-name {
  min-width: 0;
  overflow: hidden;
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.storage-config-meta,
.storage-config-detail {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  line-height: 20px;
}

.storage-config-meta {
  margin-top: 6px;
}

.storage-config-detail {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.storage-config-detail span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.storage-config-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 2px;
  white-space: nowrap;
}

.storage-config-default-action {
  color: hsl(181 84% 32%);
}

.storage-config-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
  border: 1px dashed hsl(var(--border));
  border-radius: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

.storage-config-pagination {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.storage-config-page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.storage-config-form {
  padding-right: 4px;
}

.storage-config-form :deep(.storage-config-radio-item) {
  align-items: center !important;
}

.storage-config-form :deep(.storage-config-radio-label) {
  align-items: center;
  height: 32px;
  padding-top: 0 !important;
}

.storage-config-form :deep(.storage-config-radio-label > label) {
  align-items: center;
}

.storage-config-form :deep(.storage-config-radio-item > div) {
  min-height: 32px;
  display: flex;
  align-items: center;
}

:global(.storage-type-select-dropdown) {
  z-index: 2301;
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

.file-preview-media-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  overflow: hidden;
  background: transparent;
  cursor: pointer;
}

.file-preview-image-frame {
  width: 32px !important;
  height: 32px !important;
  overflow: hidden;
}

.file-preview-image-frame :deep(.ant-image) {
  width: 32px !important;
  height: 32px !important;
  overflow: hidden;
}

.file-preview-image-frame :deep(.ant-image-img) {
  width: 32px !important;
  height: 32px !important;
  object-fit: cover !important;
}

.file-preview-media {
  display: block;
  width: 32px;
  height: 32px;
  object-fit: cover;
}

.file-preview-video-mask {
  position: absolute;
  inset: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: rgb(0 0 0 / 45%);
  opacity: 0;
  transition: opacity 0.16s ease;
}

.file-preview-media-button:hover .file-preview-video-mask {
  opacity: 1;
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
