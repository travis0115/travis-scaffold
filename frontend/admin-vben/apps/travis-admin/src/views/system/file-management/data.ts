import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'fileName', label: '文件名称' },
  { component: 'Input', fieldName: 'mimeType', label: '文件类型' },
];

export function useColumns<T>(onActionClick: OnActionClickFn<T>): VxeTableGridColumns {
  return [
    { field: 'originalName', minWidth: 220, title: '原始文件名' },
    { field: 'mimeType', title: '文件类型', width: 160 },
    { field: 'size', formatter: ({ cellValue }: any) => `${Math.ceil(cellValue / 1024)} KB`, title: '大小', width: 110 },
    { field: 'url', minWidth: 240, title: '访问地址' },
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
