import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemNoticeApi } from '#/api';

export const useGridFormSchema = (): VbenFormSchema[] => [
  { component: 'Input', fieldName: 'title', label: '消息标题' },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      options: [
        { label: '未读', value: 0 },
        { label: '已读', value: 1 },
      ],
    },
    fieldName: 'readStatus',
    label: '阅读状态',
  },
];

export function useColumns(
  onActionClick: OnActionClickFn<SystemNoticeApi.UserMessage>,
): VxeTableGridColumns<SystemNoticeApi.UserMessage> {
  return [
    { field: 'title', minWidth: 200, title: '消息标题' },
    { field: 'content', minWidth: 300, showOverflow: true, title: '消息内容' },
    {
      field: 'noticeType',
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '通知' : '公告'),
      title: '类型',
      width: 90,
    },
    {
      field: 'readStatus',
      formatter: ({ cellValue }: any) => (cellValue === 1 ? '已读' : '未读'),
      title: '状态',
      width: 90,
    },
    { field: 'publishTime', formatter: 'formatDateTime', title: '发布时间', width: 180 },
    {
      cellRender: {
        attrs: { onClick: onActionClick },
        name: 'CellOperation',
        options: [
          { code: 'read', show: (row: SystemNoticeApi.UserMessage) => row.readStatus === 0, text: '标记已读' },
          'delete',
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: '操作',
      width: 160,
    },
  ];
}
