/**
 * 通用组件共同的使用的基础组件，原先放在 adapter/form 内部，限制了使用范围，这里提取出来，方便其他地方使用
 * 可用于 vben-form、vben-modal、vben-drawer 等组件使用,
 */

/* eslint-disable vue/one-component-per-file */

import type {
  AutoCompleteProps,
  ButtonProps,
  CascaderProps,
  CheckboxGroupProps,
  CheckboxProps,
  DatePickerProps,
  DividerProps,
  InputNumberProps,
  InputProps,
  MentionsProps,
  RadioGroupProps,
  RadioProps,
  RangePickerProps,
  RateProps,
  SelectProps,
  SpaceProps,
  SwitchProps,
  TextAreaProps,
  TimePickerProps,
  TreeSelectProps,
  UploadChangeParam,
  UploadFile,
  UploadProps,
} from 'antdv-next';

import type { Component, Ref } from 'vue';

import type {
  ApiComponentSharedProps,
  BaseFormComponentType,
  IconPickerProps,
} from '@vben/common-ui';
import type { TipTapProps } from '@vben/plugins/tiptap';
import type { Recordable } from '@vben/types';

import type { SystemFileApi } from '#/api';

import {
  computed,
  defineAsyncComponent,
  defineComponent,
  h,
  onMounted,
  ref,
  render,
  unref,
  watch,
} from 'vue';

import {
  ApiComponent,
  globalShareState,
  IconPicker,
  Tree,
  VCropper,
} from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';
import { VbenTiptap } from '@vben/plugins/tiptap';
import { isEmpty } from '@vben/utils';

import { message, Modal, notification } from 'antdv-next';

import { UPLOAD_FILE_MAX_SIZE_BYTES, uploadFileApi } from '#/api';
import { getFileFolders, getFilePage } from '#/api/system/file-management';

type AdapterUploadProps = UploadProps & {
  aspectRatio?: string;
  crop?: boolean;
  handleChange?: (event: UploadChangeParam) => void;
  maxSize?: number;
  onHandleChange?: (event: UploadChangeParam) => void;
};

type FolderId = number | string;
type FolderSelection = 'all' | 'unclassified' | FolderId;
type RichEditorProps = TipTapProps;

const AutoComplete = defineAsyncComponent(
  () => import('antdv-next/dist/auto-complete/index'),
);
const Button = defineAsyncComponent(
  () => import('antdv-next/dist/button/index'),
);
const Checkbox = defineAsyncComponent(
  () => import('antdv-next/dist/checkbox/index'),
);
const CheckboxGroup = defineAsyncComponent(
  () => import('antdv-next/dist/checkbox/Group'),
);
const DatePicker = defineAsyncComponent(
  () => import('antdv-next/dist/date-picker/index'),
);
const Divider = defineAsyncComponent(
  () => import('antdv-next/dist/divider/index'),
);
const Input = defineAsyncComponent(() => import('antdv-next/dist/input/index'));
const InputNumber = defineAsyncComponent(
  () => import('antdv-next/dist/input-number/index'),
);
const InputPassword = defineAsyncComponent(() =>
  import('antdv-next/dist/input/index').then((res) => res.InputPassword),
);
const Mentions = defineAsyncComponent(
  () => import('antdv-next/dist/mentions/index'),
);
const Radio = defineAsyncComponent(() => import('antdv-next/dist/radio/index'));
const RadioGroup = defineAsyncComponent(() =>
  import('antdv-next/dist/radio/index').then((res) => res.RadioGroup),
);
const RangePicker = defineAsyncComponent(() =>
  import('antdv-next/dist/date-picker/index').then(
    (res) => res.DateRangePicker,
  ),
);
const Rate = defineAsyncComponent(() => import('antdv-next/dist/rate/index'));
const Select = defineAsyncComponent(
  () => import('antdv-next/dist/select/index'),
);
const Space = defineAsyncComponent(() => import('antdv-next/dist/space/index'));
const Switch = defineAsyncComponent(
  () => import('antdv-next/dist/switch/index'),
);
const Textarea = defineAsyncComponent(
  () => import('antdv-next/dist/input/TextArea'),
);
const TimePicker = defineAsyncComponent(
  () => import('antdv-next/dist/time-picker/index'),
);
const TreeSelect = defineAsyncComponent(
  () => import('antdv-next/dist/tree-select/index'),
);
const Cascader = defineAsyncComponent(
  () => import('antdv-next/dist/cascader/index'),
);
const Upload = defineAsyncComponent(
  () => import('antdv-next/dist/upload/index'),
);
const Image = defineAsyncComponent(() => import('antdv-next/dist/image/index'));
const PreviewGroup = defineAsyncComponent(() =>
  import('antdv-next/dist/image/index').then((res) => res.ImagePreviewGroup),
);

const withDefaultPlaceholder = (
  component: Component,
  type: 'input' | 'select',
  componentProps: Recordable<any> = {},
) => {
  return defineComponent({
    name: component.name,
    inheritAttrs: false,
    setup: (props: any, { attrs, expose, slots }) => {
      const placeholder =
        props?.placeholder ||
        attrs?.placeholder ||
        $t(`ui.placeholder.${type}`);
      // 透传组件暴露的方法
      const innerRef = ref();
      expose(
        new Proxy(
          {},
          {
            get: (_target, key) => innerRef.value?.[key],
            has: (_target, key) => key in (innerRef.value || {}),
          },
        ),
      );
      return () =>
        h(
          component,
          { ...componentProps, placeholder, ...props, ...attrs, ref: innerRef },
          slots,
        );
    },
  });
};

const toNumber = (value: unknown) => {
  const numberValue = Number(value);
  return Number.isFinite(numberValue) ? numberValue : undefined;
};

const isRequestError = (error: unknown) => {
  const requestError = error as {
    code?: string;
    config?: unknown;
    isAxiosError?: boolean;
    message?: string;
    request?: unknown;
    response?: unknown;
  };
  return Boolean(
    requestError?.isAxiosError ||
      requestError?.config ||
      requestError?.request ||
      requestError?.response ||
      requestError?.code === 'ERR_NETWORK' ||
      requestError?.message === 'Network Error',
  );
};

const buildFolderTree = (items: SystemFileApi.Folder[]) => {
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
};

const collectFolderExpandableKeys = (nodes: SystemFileApi.Folder[]) => {
  const keys: FolderId[] = [];
  nodes.forEach((node) => {
    if (node.children?.length) {
      keys.push(node.id, ...collectFolderExpandableKeys(node.children));
    }
  });
  return keys;
};

const findFolderPath = (
  nodes: SystemFileApi.Folder[],
  id: FolderId,
  parents: FolderId[] = [],
): undefined | { hasChildren: boolean; parents: FolderId[] } => {
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
};

const filterFolderTree = (
  nodes: SystemFileApi.Folder[],
  keyword: string,
): SystemFileApi.Folder[] => {
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
};

const isRootFolder = (id?: FolderId) =>
  id === undefined || id === null || String(id) === '0';

const isFolderKey = (value: FolderSelection) =>
  value !== 'all' && value !== 'unclassified';

const mergeFolderExpandedKeys = (keys: FolderId[], nextKeys: FolderId[]) => [
  ...new Set([...keys, ...nextKeys]),
];

const ensureRichEditorFilePickerStyle = () => {
  if (document.querySelector('#rich-editor-file-picker-style')) return;
  const style = document.createElement('style');
  style.id = 'rich-editor-file-picker-style';
  style.textContent = `
.rich-editor-file-picker .folder-row {
  display: flex;
  align-items: center;
  min-width: 0;
  height: 32px;
  padding-right: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  transition: color 0.16s ease, background-color 0.16s ease;
}
.rich-editor-file-picker .folder-row-normal:hover {
  background-color: hsl(var(--accent) / 50%);
  color: hsl(var(--foreground));
}
.rich-editor-file-picker .folder-row-selected,
.rich-editor-file-picker .side-tree .tree-node[data-selected] {
  background-color: hsl(var(--primary) / 12%) !important;
  color: hsl(var(--foreground));
}
.rich-editor-file-picker .folder-search {
  height: 32px;
  border-color: hsl(var(--border));
  border-radius: 6px;
  font-size: 14px;
}
.rich-editor-file-picker .folder-search .ant-input {
  font-size: 14px;
}
.rich-editor-file-picker .folder-row-spacer {
  width: 18px;
  height: 22px;
  flex: none;
}
.rich-editor-file-picker .folder-icon {
  width: 18px;
  height: 18px;
  flex: none;
  margin-right: 8px;
  color: hsl(var(--muted-foreground));
}
.rich-editor-file-picker .folder-node {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
}
.rich-editor-file-picker .folder-name {
  min-width: 0;
  overflow: hidden;
  flex: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rich-editor-file-picker .folder-tree {
  margin-top: 2px;
}
.rich-editor-file-picker .side-tree .tree-node {
  height: 32px;
  padding-right: 8px;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  font-weight: 400;
  line-height: 1;
  transition: color 0.16s ease, background-color 0.16s ease;
}
.rich-editor-file-picker .side-tree .tree-node:hover {
  background-color: hsl(var(--accent) / 50%);
  color: hsl(var(--foreground));
}
`;
  document.head.append(style);
};

const selectRichEditorImage = () => {
  return new Promise<undefined | { id?: number | string; url: string }[]>(
    (resolve) => {
      const FilePicker = defineComponent({
        name: 'RichEditorFilePicker',
        setup() {
          ensureRichEditorFilePickerStyle();
          const pageSize = 20;
          const fileName = ref('');
          const folderSearch = ref('');
          const files = ref<SystemFileApi.FileInfo[]>([]);
          const folders = ref<SystemFileApi.Folder[]>([]);
          const folderTreeValue = ref<FolderId>();
          const folderManualExpandedKeys = ref<FolderId[]>([]);
          const folderSearchExpandedKeys = ref<FolderId[]>([]);
          const loading = ref(false);
          const pageNum = ref(1);
          const selectedFolderKey = ref<FolderSelection>('all');
          const selectedFileIds = ref<(number | string)[]>([]);
          const selectedFileMap = ref<Record<string, SystemFileApi.FileInfo>>(
            {},
          );
          const total = ref(0);
          const folderTree = computed(() => buildFolderTree(folders.value));
          const filteredFolderTree = computed(() =>
            filterFolderTree(folderTree.value, folderSearch.value.trim()),
          );
          const isFolderSearchActive = computed(() =>
            Boolean(folderSearch.value.trim()),
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
          const showAllEntry = computed(() => matchFolderEntry('全部图片'));
          const showUnclassifiedEntry = computed(() =>
            matchFolderEntry('未分类'),
          );
          const totalPages = computed(() =>
            Math.max(1, Math.ceil(total.value / pageSize)),
          );
          const selectedFiles = computed(() =>
            Object.values(selectedFileMap.value),
          );

          const loadFiles = async () => {
            loading.value = true;
            try {
              const result = await getFilePage({
                fileName: fileName.value || undefined,
                ...(selectedFolderKey.value === 'unclassified'
                  ? { unclassified: true }
                  : {}),
                ...(isFolderKey(selectedFolderKey.value)
                  ? { folderId: selectedFolderKey.value }
                  : {}),
                mimeType: 'image/',
                pageNum: pageNum.value,
                pageSize,
              });
              files.value = result.records ?? [];
              total.value = result.total ?? 0;
            } finally {
              loading.value = false;
            }
          };

          watch(
            [filteredFolderTree, isFolderSearchActive],
            ([tree, active]) => {
              folderSearchExpandedKeys.value = active
                ? collectFolderExpandableKeys(tree)
                : [];
            },
            { immediate: true },
          );

          onMounted(async () => {
            const [folderItems] = await Promise.all([
              getFileFolders(),
              loadFiles(),
            ]);
            folders.value = folderItems;
            folderManualExpandedKeys.value = collectFolderExpandableKeys(
              buildFolderTree(folderItems),
            );
          });

          const confirm = () => {
            resolve(
              selectedFiles.value.map((file) => ({ id: file.id, url: file.url })),
            );
            modal.destroy();
          };

          const toggle = (file: SystemFileApi.FileInfo) => {
            const key = String(file.id);
            if (selectedFileMap.value[key]) {
              const { [key]: _removed, ...rest } = selectedFileMap.value;
              selectedFileMap.value = rest;
              selectedFileIds.value = selectedFileIds.value.filter(
                (id) => id !== file.id,
              );
              return;
            }
            selectedFileMap.value = { ...selectedFileMap.value, [key]: file };
            selectedFileIds.value = [...selectedFileIds.value, file.id];
          };

          const matchFolderEntry = (label: string) => {
            const keyword = folderSearch.value.trim().toLowerCase();
            return !keyword || label.toLowerCase().includes(keyword);
          };

          const selectFolder = (folder: FolderSelection) => {
            selectedFolderKey.value = folder;
            folderTreeValue.value = isFolderKey(folder) ? folder : undefined;
            pageNum.value = 1;
            loadFiles();
          };

          const search = () => {
            pageNum.value = 1;
            loadFiles();
          };

          const changePage = (offset: number) => {
            const nextPage = pageNum.value + offset;
            if (nextPage < 1 || nextPage > totalPages.value) {
              return;
            }
            pageNum.value = nextPage;
            loadFiles();
          };

          const renderFolderRow = (label: string, folder: FolderSelection) =>
            h(
              'div',
              {
                class: [
                  'folder-row',
                  selectedFolderKey.value === folder
                    ? 'folder-row-selected'
                    : 'folder-row-normal',
                ],
                onClick: () => selectFolder(folder),
              },
              [
                h('span', { class: 'folder-row-spacer' }),
                h(IconifyIcon, {
                  class: 'folder-icon',
                  icon: 'lucide:folder',
                }),
                h('span', { class: 'folder-name' }, label),
              ],
            );

          const onSelectFolderNode = (item: any) => {
            const folder = item.value as SystemFileApi.Folder;
            if (isFolderSearchActive.value) {
              const path = findFolderPath(folderTree.value, folder.id);
              if (path) {
                const keys = [...path.parents];
                if (path.hasChildren) {
                  keys.push(folder.id);
                }
                folderManualExpandedKeys.value = mergeFolderExpandedKeys(
                  folderManualExpandedKeys.value,
                  keys,
                );
              }
            }
            selectFolder(folder.id);
          };

          const renderFolderTree = () =>
            filteredFolderTree.value.length > 0
              ? h(
                  Tree,
                  {
                    childrenField: 'children',
                    class: 'side-tree folder-tree',
                    defaultExpandedLevel: 0,
                    expandedKeys: folderTreeExpandedKeys.value,
                    labelField: 'folderName',
                    modelValue: folderTreeValue.value,
                    'onUpdate:expandedKeys': (keys: FolderId[]) => {
                      folderTreeExpandedKeys.value = keys;
                    },
                    'onUpdate:modelValue': (value: FolderId) => {
                      folderTreeValue.value = value;
                    },
                    onSelect: onSelectFolderNode,
                    showIcon: false,
                    showToolbar: false,
                    transition: true,
                    treeData: filteredFolderTree.value,
                    valueField: 'id',
                  },
                  {
                    node: ({ value: folder }: { value: SystemFileApi.Folder }) =>
                      h('div', { class: 'folder-node' }, [
                        h(IconifyIcon, {
                          class: 'folder-icon',
                          icon: 'lucide:folder',
                        }),
                        h('span', { class: 'folder-name' }, folder.folderName),
                      ]),
                  },
                )
              : null;

          const renderImages = () => {
            if (loading.value) {
              return h(
                'div',
                {
                  class:
                    'flex h-[456px] items-center justify-center text-muted-foreground',
                },
                '加载中...',
              );
            }
            if (files.value.length === 0) {
              return h(
                'div',
                {
                  class:
                    'flex h-[456px] items-center justify-center text-muted-foreground',
                },
                '暂无图片',
              );
            }
            return h(
              'div',
              {
                class:
                  'grid h-[456px] grid-cols-5 content-start gap-3 overflow-auto pr-1',
              },
              files.value.map((file) => {
                const selected = selectedFileIds.value.includes(file.id);
                return h(
                  'button',
                  {
                    class: [
                      'group relative overflow-hidden rounded-md border bg-background text-left transition hover:border-primary hover:bg-muted/40',
                      selected
                        ? 'border-primary bg-muted/40'
                        : 'border-border',
                    ],
                    onClick: () => toggle(file),
                    type: 'button',
                  },
                  [
                    h('img', {
                      alt: file.originalName,
                      class: 'aspect-square w-full object-cover',
                      src: file.url,
                    }),
                    selected
                      ? h(
                          'span',
                          {
                            class:
                              'absolute right-2 top-2 rounded-full bg-primary p-1 text-primary-foreground',
                          },
                          [
                            h(IconifyIcon, {
                              class: 'size-3.5',
                              icon: 'lucide:check',
                            }),
                          ],
                        )
                      : null,
                    h(
                      'div',
                      {
                        class:
                          'truncate px-2 py-1 text-xs text-muted-foreground group-hover:text-primary',
                        title: file.originalName,
                      },
                      file.originalName,
                    ),
                  ],
                );
              }),
            );
          };

          const renderContent = () =>
            h('div', { class: 'rich-editor-file-picker flex h-[640px] flex-col' }, [
              h('div', { class: 'flex items-center justify-between' }, [
                h('div', { class: 'text-base font-medium' }, '选择图片'),
                h(
                  'button',
                  {
                    class:
                      'rounded p-1 text-muted-foreground transition hover:bg-muted hover:text-foreground',
                    onClick: () => {
                      resolve(undefined);
                      modal.destroy();
                    },
                    type: 'button',
                  },
                  [h(IconifyIcon, { class: 'size-5', icon: 'lucide:x' })],
                ),
              ]),
              h('div', { class: 'mt-4 flex min-h-0 flex-1 gap-4' }, [
                h(
                  'aside',
                  {
                    class:
                      'flex h-full w-60 shrink-0 flex-col border-r border-border pr-3',
                  },
                  [
                    h(
                      Input,
                      {
                        allowClear: true,
                        class: 'folder-search mb-3',
                        'onUpdate:value': (value: string) => {
                          folderSearch.value = value;
                        },
                        placeholder: '请输入文件夹名称',
                        value: folderSearch.value,
                      },
                      {
                        prefix: () =>
                          h(IconifyIcon, {
                            class: 'size-4 text-muted-foreground',
                            icon: 'lucide:search',
                          }),
                      },
                    ),
                    h('div', { class: 'min-h-0 flex-1 overflow-auto' }, [
                      showAllEntry.value
                        ? renderFolderRow('全部图片', 'all')
                        : null,
                      showUnclassifiedEntry.value
                        ? renderFolderRow('未分类', 'unclassified')
                        : null,
                      renderFolderTree(),
                    ]),
                  ],
                ),
                h('section', { class: 'flex min-w-0 flex-1 flex-col' }, [
                  h('div', { class: 'flex gap-2' }, [
                    h('input', {
                      class:
                        'h-9 min-w-0 flex-1 rounded-md border border-input bg-background px-3 text-sm outline-none focus:border-primary',
                      onInput: (event: Event) => {
                        fileName.value = (event.target as HTMLInputElement).value;
                      },
                      onKeydown: (event: KeyboardEvent) => {
                        if (event.key === 'Enter') search();
                      },
                      placeholder: '搜索文件名',
                      value: fileName.value,
                    }),
                    h(
                      'button',
                      {
                        class:
                          'h-9 rounded-md bg-primary px-3 text-sm text-primary-foreground',
                        onClick: search,
                        type: 'button',
                      },
                      '搜索',
                    ),
                  ]),
                  h('div', { class: 'mt-3 min-h-0 flex-1' }, [renderImages()]),
                  h('div', { class: 'flex h-9 shrink-0 items-center justify-between text-xs text-muted-foreground' }, [
                    h('span', `共 ${total.value} 个文件，已选 ${selectedFileIds.value.length} 个`),
                    h('div', { class: 'flex items-center gap-2' }, [
                      h(
                        'button',
                        {
                          class:
                            'rounded border border-border px-3 py-1 transition enabled:hover:border-primary enabled:hover:text-primary disabled:opacity-50',
                          disabled: pageNum.value <= 1,
                          onClick: () => changePage(-1),
                          type: 'button',
                        },
                        '上一页',
                      ),
                      h('span', `第 ${pageNum.value} / ${totalPages.value} 页`),
                      h(
                        'button',
                        {
                          class:
                            'rounded border border-border px-3 py-1 transition enabled:hover:border-primary enabled:hover:text-primary disabled:opacity-50',
                          disabled: pageNum.value >= totalPages.value,
                          onClick: () => changePage(1),
                          type: 'button',
                        },
                        '下一页',
                      ),
                    ]),
                  ]),
                ]),
              ]),
              h('div', { class: 'mt-3 flex shrink-0 justify-end gap-2 border-t border-border pt-3' }, [
                h(
                  'button',
                  {
                    class:
                      'h-9 rounded-md border border-border px-4 text-sm transition hover:border-primary hover:text-primary',
                    onClick: () => {
                      resolve(undefined);
                      modal.destroy();
                    },
                    type: 'button',
                  },
                  '取消',
                ),
                h(
                  'button',
                  {
                    class:
                      'h-9 rounded-md bg-primary px-4 text-sm text-primary-foreground disabled:opacity-50',
                    disabled: selectedFileIds.value.length === 0,
                    onClick: confirm,
                    type: 'button',
                  },
                  '确定',
                ),
              ]),
            ]);

          return () =>
            h('div', { class: 'min-h-120' }, [renderContent()]);
        },
      });

      const modal = Modal.confirm({
        centered: true,
        content: h(FilePicker),
        footer: null,
        icon: null,
        onCancel: () => resolve(undefined),
        title: null,
        width: 1180,
      });
    },
  );
};

const createRichEditorImageUpload = (
  imageUpload?: TipTapProps['imageUpload'],
): NonNullable<TipTapProps['imageUpload']> => ({
  maxSize: UPLOAD_FILE_MAX_SIZE_BYTES,
  select: selectRichEditorImage,
  ...imageUpload,
  onUploadError: (error: unknown) => {
    const hasCustomHandler = Boolean(imageUpload?.onUploadError);
    const handled = imageUpload?.onUploadError?.(error);
    if (handled !== undefined) {
      return handled;
    }
    if (isRequestError(error)) {
      return false;
    }
    return hasCustomHandler ? false : true;
  },
  upload:
    imageUpload?.upload ??
    (async (file: File, onProgress?: (percent: number) => void) => {
      const result = await uploadFileApi(file, undefined, (event) => {
        if (!event.total) return;
        onProgress?.(Math.round((event.loaded / event.total) * 100));
      });
      return { id: result.id, url: result.url };
    }),
});

const RichEditorComponent = defineComponent({
  name: 'RichEditor',
  inheritAttrs: false,
  setup(_props, { attrs, expose, slots }) {
    const innerRef = ref();
    expose(
      new Proxy(
        {},
        {
          get: (_target, key) => innerRef.value?.[key],
          has: (_target, key) => key in (innerRef.value || {}),
        },
      ),
    );

    return () => {
      const {
        imageUpload,
        placeholder: inputPlaceholder,
        ...restAttrs
      } = attrs as RichEditorProps;
      return h(
        VbenTiptap,
        {
          ...restAttrs,
          imageUpload: createRichEditorImageUpload(imageUpload),
          placeholder: inputPlaceholder || $t('ui.placeholder.input'),
          ref: innerRef,
        },
        slots,
      );
    };
  },
});

const getDecimalLength = (value: number) => {
  const text = String(value);
  return text.includes('.') ? text.split('.')[1]?.length || 0 : 0;
};

const normalizeNumber = (value: number, step: number) => {
  const precision = Math.max(getDecimalLength(value), getDecimalLength(step));
  return Number(value.toFixed(precision));
};

const StepperInputNumber = defineComponent({
  name: 'StepperInputNumber',
  inheritAttrs: false,
  emits: ['change', 'update:value'],
  setup(_props, { attrs, emit, expose }) {
    const innerRef = ref();
    expose(
      new Proxy(
        {},
        {
          get: (_target, key) => innerRef.value?.[key],
          has: (_target, key) => key in (innerRef.value || {}),
        },
      ),
    );

    const updateValue = (value: unknown) => {
      const numberValue = toNumber(value);
      emit('update:value', numberValue);
      emit('change', numberValue);
    };

    const getNextValue = (direction: -1 | 1) => {
      const step = toNumber(attrs.step) ?? 1;
      const min = toNumber(attrs.min);
      const max = toNumber(attrs.max);
      const current = toNumber(attrs.value) ?? min ?? 0;
      const next = normalizeNumber(current + step * direction, step);
      return Math.min(max ?? next, Math.max(min ?? next, next));
    };

    return () => {
      const {
        class: className,
        style,
        value,
        wrapperStyle,
        ...inputNumberAttrs
      } = attrs;
      const numberValue = toNumber(value);
      const min = toNumber(attrs.min);
      const max = toNumber(attrs.max);
      const disabled = Boolean(attrs.disabled);
      const decreaseDisabled =
        disabled ||
        (min !== undefined && numberValue !== undefined && numberValue <= min);
      const increaseDisabled =
        disabled ||
        (max !== undefined && numberValue !== undefined && numberValue >= max);
      const buttonClass =
        'flex h-9 w-12 items-center justify-center bg-muted/30 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground disabled:cursor-not-allowed disabled:opacity-40';

      return h(
        'div',
        {
          class: [
            'border-input flex h-9 w-full items-center overflow-hidden rounded-md border bg-background shadow-xs',
            className,
          ],
          style,
        },
        [
          h(
            'button',
            {
              'aria-label': '减少',
              class: [buttonClass, 'border-r'],
              disabled: decreaseDisabled,
              onClick: () => updateValue(getNextValue(-1)),
              type: 'button',
            },
            [h(IconifyIcon, { class: 'size-4', icon: 'lucide:minus' })],
          ),
          h(InputNumber, {
            ...inputNumberAttrs,
            bordered: false,
            class: 'min-w-0 flex-1 text-center',
            controls: false,
            'onUpdate:value': updateValue,
            ref: innerRef,
            value,
            style: [{ width: '100%' }, wrapperStyle],
          }),
          h(
            'button',
            {
              'aria-label': '增加',
              class: [buttonClass, 'border-l'],
              disabled: increaseDisabled,
              onClick: () => updateValue(getNextValue(1)),
              type: 'button',
            },
            [h(IconifyIcon, { class: 'size-4', icon: 'lucide:plus' })],
          ),
        ],
      );
    };
  },
});

const withPreviewUpload = () => {
  // 检查是否为图片文件的辅助函数
  const isImageFile = (file: UploadFile): boolean => {
    const imageExtensions = new Set([
      'bmp',
      'gif',
      'jpeg',
      'jpg',
      'png',
      'svg',
      'webp',
    ]);
    if (file.url) {
      try {
        const pathname = new URL(file.url, 'http://localhost').pathname;
        const ext = pathname.split('.').pop()?.toLowerCase();
        return ext ? imageExtensions.has(ext) : false;
      } catch {
        const ext = file.url?.split('.').pop()?.toLowerCase();
        return ext ? imageExtensions.has(ext) : false;
      }
    }
    if (!file.type) {
      const ext = file.name?.split('.').pop()?.toLowerCase();
      return ext ? imageExtensions.has(ext) : false;
    }
    return file.type.startsWith('image/');
  };
  // 创建默认的上传按钮插槽
  const createDefaultSlotsWithUpload = (
    listType: string,
    placeholder: string,
  ) => {
    switch (listType) {
      case 'picture-card': {
        return {
          default: () => placeholder,
        };
      }
      default: {
        return {
          default: () =>
            h(
              Button,
              {
                icon: h(IconifyIcon, {
                  icon: 'ant-design:upload-outlined',
                  class: 'mb-1 size-4',
                }),
              },
              () => placeholder,
            ),
        };
      }
    }
  };
  // 构建预览图片组
  const previewImage = async (
    file: UploadFile,
    visible: Ref<boolean>,
    fileList: Ref<UploadProps['fileList']>,
  ) => {
    // 如果当前文件不是图片，直接打开
    if (!isImageFile(file)) {
      if (file.url) {
        window.open(file.url, '_blank');
      } else if (file.preview) {
        window.open(file.preview, '_blank');
      } else {
        message.error($t('ui.formRules.previewWarning'));
      }
      return;
    }

    // 对于图片文件，继续使用预览组
    const [ImageComponent, PreviewGroupComponent] = await Promise.all([
      Image,
      PreviewGroup,
    ]);

    const getBase64 = (file: File) => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.addEventListener('load', () => resolve(reader.result));
        reader.addEventListener('error', (error) => reject(error));
      });
    };
    // 从fileList中过滤出所有图片文件
    const imageFiles = (unref(fileList) || []).filter((element) =>
      isImageFile(element),
    );

    // 为所有没有预览地址的图片生成预览
    for (const imgFile of imageFiles) {
      if (!imgFile.url && !imgFile.preview && imgFile.originFileObj) {
        imgFile.preview = (await getBase64(imgFile.originFileObj)) as string;
      }
    }
    const container: HTMLElement | null = document.createElement('div');
    document.body.append(container);

    // 用于追踪组件是否已卸载
    let isUnmounted = false;

    const PreviewWrapper = {
      setup() {
        return () => {
          if (isUnmounted) return null;
          return h(
            PreviewGroupComponent,
            {
              class: 'hidden',
              preview: {
                open: visible.value,
                // 设置初始显示的图片索引
                current: imageFiles.findIndex((f) => f.uid === file.uid),
                onOpenChange: (value: boolean) => {
                  visible.value = value;
                  if (!value) {
                    // 延迟清理，确保动画完成
                    setTimeout(() => {
                      if (!isUnmounted && container) {
                        isUnmounted = true;
                        render(null, container);
                        container.remove();
                      }
                    }, 300);
                  }
                },
              },
            },
            () =>
              // 渲染所有图片文件
              imageFiles.map((imgFile) =>
                h(ImageComponent, {
                  key: imgFile.uid,
                  src: imgFile.url || imgFile.preview,
                }),
              ),
          );
        };
      },
    };

    render(h(PreviewWrapper), container);
  };

  // 图片裁剪操作
  const cropImage = (file: File, aspectRatio: string | undefined) => {
    return new Promise((resolve, reject) => {
      const container: HTMLElement | null = document.createElement('div');
      document.body.append(container);

      // 用于追踪组件是否已卸载
      let isUnmounted = false;
      let objectUrl: null | string = null;

      const open = ref<boolean>(true);
      const cropperRef = ref<InstanceType<typeof VCropper> | null>(null);

      const closeModal = () => {
        open.value = false;
        // 延迟清理，确保动画完成
        setTimeout(() => {
          if (!isUnmounted && container) {
            if (objectUrl) {
              URL.revokeObjectURL(objectUrl);
            }
            isUnmounted = true;
            render(null, container);
            container.remove();
          }
        }, 300);
      };

      const CropperWrapper = {
        setup() {
          return () => {
            if (isUnmounted) return null;
            if (!objectUrl) {
              objectUrl = URL.createObjectURL(file);
            }
            return h(
              Modal,
              {
                open: open.value,
                title: h('div', {}, [
                  $t('ui.crop.title'),
                  h(
                    'span',
                    {
                      class: `${aspectRatio ? '' : 'hidden'} ml-2 text-sm text-gray-400 font-normal`,
                    },
                    $t('ui.crop.titleTip', [aspectRatio]),
                  ),
                ]),
                centered: true,
                width: 548,
                keyboard: false,
                maskClosable: false,
                closable: false,
                cancelText: $t('common.cancel'),
                okText: $t('ui.crop.confirm'),
                destroyOnHidden: true,
                onOk: async () => {
                  const cropper = cropperRef.value;
                  if (!cropper) {
                    reject(new Error('Cropper not found'));
                    closeModal();
                    return;
                  }
                  try {
                    const dataUrl = await cropper.getCropImage();
                    resolve(dataUrl);
                  } catch {
                    reject(new Error($t('ui.crop.errorTip')));
                  } finally {
                    closeModal();
                  }
                },
                onCancel() {
                  resolve('');
                  closeModal();
                },
              },
              () =>
                h(VCropper, {
                  ref: (ref: any) => (cropperRef.value = ref),
                  img: objectUrl as string,
                  aspectRatio,
                }),
            );
          };
        },
      };

      render(h(CropperWrapper), container);
    });
  };

  return defineComponent({
    name: 'AUpload',
    emits: ['update:modelValue'],
    setup: (
      props: any,
      { attrs, slots, emit }: { attrs: any; emit: any; slots: any },
    ) => {
      const previewVisible = ref<boolean>(false);

      const placeholder = attrs?.placeholder || $t(`ui.placeholder.upload`);

      const listType = attrs?.listType || attrs?.['list-type'] || 'text';

      const fileList = ref<UploadProps['fileList']>(
        attrs?.fileList || attrs?.['file-list'] || [],
      );

      const maxSize = computed(() => attrs?.maxSize ?? attrs?.['max-size']);
      const aspectRatio = computed(
        () => attrs?.aspectRatio ?? attrs?.['aspect-ratio'],
      );

      const handleBeforeUpload = async (
        file: UploadFile,
        originFileList: Array<File>,
      ) => {
        if (maxSize.value && (file.size || 0) / 1024 / 1024 > maxSize.value) {
          message.error($t('ui.formRules.sizeLimit', [maxSize.value]));
          file.status = 'removed';
          return false;
        }
        // 多选或者非图片不唤起裁剪框
        if (
          attrs.crop &&
          !attrs.multiple &&
          originFileList[0] &&
          isImageFile(file)
        ) {
          file.status = 'removed';
          // antd Upload组件问题 file参数获取的是UploadFile类型对象无法取到File类型 所以通过originFileList[0]获取
          const blob = await cropImage(originFileList[0], aspectRatio.value);
          return new Promise((resolve, reject) => {
            if (!blob) {
              return reject(new Error($t('ui.crop.errorTip')));
            }
            resolve(blob);
          });
        }

        return attrs.beforeUpload?.(file) ?? true;
      };

      const handleChange = (event: UploadChangeParam) => {
        try {
          // 行内写法 handleChange: (event) => {}
          attrs.handleChange?.(event);
          // template写法 @handle-change="(event) => {}"
          attrs.onHandleChange?.(event);
        } catch (error) {
          // Avoid breaking internal v-model sync on user handler errors
          console.error(error);
        }
        fileList.value = event.fileList.filter(
          (file) => file.status !== 'removed',
        );
        emit(
          'update:modelValue',
          event.fileList?.length ? fileList.value : undefined,
        );
      };

      const handlePreview = async (file: UploadFile) => {
        previewVisible.value = true;
        await previewImage(file, previewVisible, fileList);
      };

      const renderUploadButton = (): any => {
        const isDisabled = attrs.disabled;

        // 如果禁用，不渲染上传按钮
        if (isDisabled) {
          return null;
        }

        // 否则渲染默认上传按钮
        return isEmpty(slots)
          ? createDefaultSlotsWithUpload(listType, placeholder)
          : slots;
      };

      // 可以监听到表单API设置的值
      watch(
        () => attrs.modelValue,
        (res) => {
          fileList.value = res;
        },
      );

      return () =>
        h(
          Upload,
          {
            ...props,
            ...attrs,
            fileList: fileList.value,
            beforeUpload: handleBeforeUpload,
            onChange: handleChange,
            onPreview: handlePreview,
          },
          renderUploadButton(),
        );
    },
  });
};

// 这里需要自行根据业务组件库进行适配，需要用到的组件都需要在这里类型说明
export type ComponentType =
  | 'ApiCascader'
  | 'ApiSelect'
  | 'ApiTreeSelect'
  | 'AutoComplete'
  | 'Cascader'
  | 'Checkbox'
  | 'CheckboxGroup'
  | 'DatePicker'
  | 'DefaultButton'
  | 'Divider'
  | 'IconPicker'
  | 'Input'
  | 'InputNumber'
  | 'InputPassword'
  | 'Mentions'
  | 'PrimaryButton'
  | 'Radio'
  | 'RadioGroup'
  | 'RangePicker'
  | 'Rate'
  | 'RichEditor'
  | 'Select'
  | 'Space'
  | 'Switch'
  | 'Textarea'
  | 'TimePicker'
  | 'TreeSelect'
  | 'Upload'
  | BaseFormComponentType;

/**
 * 与 {@link ComponentType} 中注册的组件名一一对应，便于 Schema 上 `component` + `componentProps` 联动提示
 */
export interface ComponentPropsMap {
  ApiCascader: ApiComponentSharedProps & CascaderProps;
  ApiSelect: ApiComponentSharedProps & SelectProps;
  ApiTreeSelect: ApiComponentSharedProps & TreeSelectProps;
  AutoComplete: AutoCompleteProps;
  Cascader: CascaderProps;
  Checkbox: CheckboxProps;
  CheckboxGroup: CheckboxGroupProps;
  DatePicker: DatePickerProps;
  DefaultButton: ButtonProps;
  Divider: DividerProps;
  IconPicker: IconPickerProps;
  Input: InputProps;
  InputNumber: InputNumberProps;
  InputPassword: InputProps;
  Mentions: MentionsProps;
  PrimaryButton: ButtonProps;
  Radio: RadioProps;
  RadioGroup: RadioGroupProps;
  RangePicker: RangePickerProps;
  Rate: RateProps;
  RichEditor: RichEditorProps;
  Select: SelectProps;
  Space: SpaceProps;
  Switch: SwitchProps;
  Textarea: TextAreaProps;
  TimePicker: TimePickerProps;
  TreeSelect: TreeSelectProps;
  Upload: AdapterUploadProps;
}

async function initComponentAdapter() {
  const components: Partial<Record<ComponentType, Component>> = {
    // 如果你的组件体积比较大，可以使用异步加载
    // Button: () =>
    // import('xxx').then((res) => res.Button),

    ApiCascader: withDefaultPlaceholder(ApiComponent, 'select', {
      component: Cascader,
      fieldNames: { label: 'label', value: 'value', children: 'children' },
      loadingSlot: 'suffixIcon',
      modelPropName: 'value',
      style: { width: '100%' },
      visibleEvent: 'onVisibleChange',
    }),
    ApiSelect: withDefaultPlaceholder(ApiComponent, 'select', {
      component: Select,
      loadingSlot: 'suffixIcon',
      modelPropName: 'value',
      style: { width: '100%' },
      visibleEvent: 'onVisibleChange',
    }),
    ApiTreeSelect: withDefaultPlaceholder(ApiComponent, 'select', {
      component: TreeSelect,
      fieldNames: { label: 'label', value: 'value', children: 'children' },
      loadingSlot: 'suffixIcon',
      modelPropName: 'value',
      optionsPropName: 'treeData',
      style: { width: '100%' },
      visibleEvent: 'onVisibleChange',
    }),
    AutoComplete: withDefaultPlaceholder(AutoComplete, 'input', {
      style: { width: '100%' },
    }),
    Cascader: withDefaultPlaceholder(Cascader, 'select', {
      style: { width: '100%' },
    }),
    Checkbox,
    CheckboxGroup,
    DatePicker: withDefaultPlaceholder(DatePicker, 'select', {
      style: { width: '100%' },
    }),
    // 自定义默认按钮
    DefaultButton: (props, { attrs, slots }) => {
      return h(Button, { ...props, attrs, type: 'default' }, slots);
    },
    Divider,
    IconPicker: withDefaultPlaceholder(IconPicker, 'select', {
      iconSlot: 'addonAfter',
      inputComponent: Input,
      modelValueProp: 'value',
    }),
    Input: withDefaultPlaceholder(Input, 'input'),
    InputNumber: withDefaultPlaceholder(StepperInputNumber, 'input', {
      style: { width: '100%' },
    }),
    InputPassword: withDefaultPlaceholder(InputPassword, 'input'),
    Mentions: withDefaultPlaceholder(Mentions, 'input'),
    // 自定义主要按钮
    PrimaryButton: (props, { attrs, slots }) => {
      return h(Button, { ...props, attrs, type: 'primary' }, slots);
    },
    Radio,
    RadioGroup,
    RangePicker: withDefaultPlaceholder(RangePicker, 'select', {
      style: { width: '100%' },
    }),
    Rate,
    RichEditor: RichEditorComponent,
    Select: withDefaultPlaceholder(Select, 'select', {
      style: { width: '100%' },
    }),
    Space,
    Switch,
    Textarea: withDefaultPlaceholder(Textarea, 'input'),
    TimePicker: withDefaultPlaceholder(TimePicker, 'select', {
      style: { width: '100%' },
    }),
    TreeSelect: withDefaultPlaceholder(TreeSelect, 'select', {
      style: { width: '100%' },
    }),
    Upload: withPreviewUpload(),
  };

  // 将组件注册到全局共享状态中
  globalShareState.setComponents(components);

  // 定义全局共享状态中的消息提示
  globalShareState.defineMessage({
    // 复制成功消息提示
    copyPreferencesSuccess: (title, content) => {
      notification.success({
        description: content,
        title,
        placement: 'bottomRight',
      });
    },
  });
}

export { initComponentAdapter, StepperInputNumber as InputNumber };
