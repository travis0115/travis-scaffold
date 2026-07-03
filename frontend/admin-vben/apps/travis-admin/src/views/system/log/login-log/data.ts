import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { BACKEND_DATETIME_FORMAT } from '@vben/utils';

import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.loginLog.username'),
    },
    {
      component: 'Input',
      fieldName: 'ip',
      label: $t('system.loginLog.ip'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions('operation_status'),
      },
      fieldName: 'status',
      label: $t('system.loginLog.status'),
    },
    {
      component: 'RangePicker',
      componentProps: {
        showTime: true,
        valueFormat: BACKEND_DATETIME_FORMAT,
      },
      fieldName: 'loginTimeRange',
      label: $t('system.loginLog.loginTime'),
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
      field: 'loginTime',
      title: $t('system.loginLog.loginTime'),
      width: 180,
      formatter: 'formatDateTime',
      sortable: true,
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
      minWidth: 150,
      fixed: 'right',
    },

    {
      cellRender: {
        attrs: { dictCode: 'operation_status' },
        name: 'CellTag',
      },
      field: 'status',
      title: $t('system.loginLog.status'),
      width: 100,
      fixed: 'right',
    },
  ];
}
