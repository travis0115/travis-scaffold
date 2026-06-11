import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'fileName', label: '文件名称' },
  { component: 'Input', fieldName: 'mimeType', label: '文件类型' },
];

export function useColumns<T>(onActionClick: OnActionClickFn<T>): VxeTableGridColumns {
  return [
    { field: 'fileName', minWidth: 220, title: '文件名' },
    { field: 'originalName', minWidth: 220, title: '原始文件名' },
    { field: 'extension', title: '文件后缀', width: 100 },
    { field: 'preview', slots: { default: 'preview' }, title: '文件预览', width: 110 },
    { field: 'size', formatter: ({ cellValue }: any) => `${Math.ceil(cellValue / 1024)} KB`, title: '大小', width: 110 },
    { field: 'creatorName', title: '上传人', width: 120 },
    {
      field: 'storageConfigName',
      formatter: ({ row }: any) => row.storageConfigName || row.storageType || '-',
      title: '服务商',
      width: 150,
    },
    { field: 'createTime', formatter: 'formatDateTime', title: '上传时间', width: 180 },
    {
      cellRender: { attrs: { onClick: onActionClick }, name: 'CellOperation', options: ['delete'] },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 100,
    },
  ];
}
