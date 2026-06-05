import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { $t } from '#/locales';

export function useColumns(): VxeTableGridColumns {
  return [
    {
      field: 'loginTime',
      title: $t('system.loginLog.loginTime'),
      width: 180,
      formatter: 'formatDateTime',
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
      field: 'message',
      title: $t('system.loginLog.message'),
      minWidth: 200,
    },
    {
      cellRender: {
        name: 'CellTag',
        options: [
          { color: 'success', label: $t('system.loginLog.success'), value: 1 },
          { color: 'error', label: $t('system.loginLog.fail'), value: 0 },
        ],
      },
      field: 'status',
      title: $t('system.loginLog.status'),
      width: 100,
      fixed: 'right',
    },
  ];
}
