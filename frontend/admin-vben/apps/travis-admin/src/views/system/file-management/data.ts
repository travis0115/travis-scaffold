import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';

import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export const useGridFormSchema = (
  storageConfigOptions: Array<{ label: string; value: number | string }> = [],
): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'fileName', label: '文件名称' },
  { component: 'Input', fieldName: 'originalName', label: '原始文件名' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: storageConfigOptions,
    },
    fieldName: 'storageConfigId',
    label: '服务商',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: [
        { label: '图片', value: 'image/' },
        { label: '文档', value: 'word' },
        { label: 'PDF', value: 'pdf' },
        { label: '表格', value: 'spreadsheet' },
        { label: '演示文稿', value: 'presentation' },
        { label: '压缩包', value: 'zip' },
        { label: '音频', value: 'audio/' },
        { label: '视频', value: 'video/' },
        { label: '文本', value: 'text/' },
      ],
    },
    fieldName: 'mimeType',
    label: '文件类型',
  },
];

function formatFileSize(size?: number) {
  const value = Number(size ?? 0);
  if (value >= 1024 * 1024) {
    return `${(value / 1024 / 1024).toFixed(2)} MB`;
  }
  return `${Math.ceil(value / 1024)} KB`;
}

export function useColumns<T>(onActionClick: OnActionClickFn<T>): VxeTableGridColumns {
  return [
    { field: 'fileName', minWidth: 220, title: '文件名' },
    { field: 'originalName', minWidth: 220, title: '原始文件名' },
    { field: 'extension', title: '文件后缀', width: 100 },
    { field: 'preview', slots: { default: 'preview' }, title: '文件预览', width: 110 },
    { field: 'size', formatter: ({ cellValue }: any) => formatFileSize(cellValue), sortable: true, title: '大小', width: 110 },
    {
      field: 'storageConfigName',
      formatter: ({ row }: any) => row.storageConfigName || row.storageType || '-',
      title: '服务商',
      width: 150,
    },
    { field: 'createTime', formatter: 'formatDateTime', sortable: true, title: '上传时间', width: 180 },
    {
      cellRender: {
        attrs: {
          nameField: 'fileName',
          nameTitle: '文件',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(['delete'], {
          delete: SYSTEM_PERMS.fileDelete,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 100,
    },
  ];
}
