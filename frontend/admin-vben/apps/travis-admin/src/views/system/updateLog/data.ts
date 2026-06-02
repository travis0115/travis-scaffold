import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemUpdateLogApi } from '#/api';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.updateLog.version'),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.updateLog.titleField'),
    },
    {
      component: 'Textarea',
      fieldName: 'content',
      label: $t('system.updateLog.content'),
      componentProps: {
        rows: 8,
      },
    },
    {
      component: 'DatePicker',
      fieldName: 'publishTime',
      label: $t('system.updateLog.publishTime'),
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        style: { width: '100%' },
      },
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('system.updateLog.statusDraft'), value: 0 },
          { label: $t('system.updateLog.statusPublished'), value: 1 },
        ],
        optionType: 'button',
      },
      defaultValue: 0,
      fieldName: 'status',
      label: $t('system.updateLog.status'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.updateLog.version'),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.updateLog.titleField'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.updateLog.statusDraft'), value: 0 },
          { label: $t('system.updateLog.statusPublished'), value: 1 },
        ],
      },
      fieldName: 'status',
      label: $t('system.updateLog.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemUpdateLogApi.UpdateLog>,
): VxeTableGridColumns<SystemUpdateLogApi.UpdateLog> {
  return [
    {
      field: 'version',
      title: $t('system.updateLog.version'),
      width: 120,
    },
    {
      field: 'title',
      title: $t('system.updateLog.titleField'),
      minWidth: 200,
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      title: $t('system.updateLog.status'),
      width: 100,
    },
    {
      field: 'publishTime',
      title: $t('system.updateLog.publishTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: $t('system.updateLog.createTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: $t('system.updateLog.titleField'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: ['edit', 'delete'],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.updateLog.operation'),
      width: 160,
    },
  ];
}
