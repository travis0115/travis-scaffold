import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { $t } from '#/locales';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.loginLog.username'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.loginLog.success'), value: 1 },
          { label: $t('system.loginLog.fail'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.loginLog.status'),
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    {
      field: 'username',
      title: $t('system.loginLog.username'),
      width: 150,
    },
    {
      field: 'ip',
      title: $t('system.loginLog.ip'),
      width: 150,
    },
    {
      field: 'location',
      title: $t('system.loginLog.location'),
      width: 150,
    },
    {
      field: 'browser',
      title: $t('system.loginLog.browser'),
      width: 150,
    },
    {
      field: 'os',
      title: $t('system.loginLog.os'),
      width: 150,
    },
    {
      cellRender: {
        name: 'CellTag',
        props: (row: any) => ({
          color: row.status === 1 ? 'success' : 'error',
        }),
      },
      field: 'status',
      title: $t('system.loginLog.status'),
      width: 100,
      formatter({ cellValue }: { cellValue: number }) {
        return cellValue === 1
          ? $t('system.loginLog.success')
          : $t('system.loginLog.fail');
      },
    },
    {
      field: 'message',
      title: $t('system.loginLog.message'),
      width: 150,
    },
    {
      field: 'loginTime',
      title: $t('system.loginLog.loginTime'),
      width: 180,
    },
  ];
}
