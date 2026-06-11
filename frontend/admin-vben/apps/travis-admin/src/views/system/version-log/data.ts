import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemVersionLogApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.versionLog.version'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.versionLog.version')]))
        .max(50, $t('ui.formRules.maxLength', [$t('system.versionLog.version'), 50])),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.versionLog.titleField'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.versionLog.titleField')]))
        .max(200, $t('ui.formRules.maxLength', [$t('system.versionLog.titleField'), 200])),
    },
    {
      component: 'Textarea',
      fieldName: 'content',
      label: $t('system.versionLog.content'),
      rules: z.string().min(1, $t('ui.formRules.required', [$t('system.versionLog.content')])),
      componentProps: {
        rows: 8,
      },
    },
    {
      component: 'DatePicker',
      fieldName: 'publishTime',
      label: $t('system.versionLog.publishTime'),
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
          { label: $t('system.versionLog.statusDraft'), value: 0 },
          { label: $t('system.versionLog.statusPublished'), value: 1 },
        ],
        optionType: 'button',
      },
      defaultValue: 0,
      fieldName: 'status',
      label: $t('system.versionLog.status'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.versionLog.version'),
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.versionLog.titleField'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.versionLog.statusDraft'), value: 0 },
          { label: $t('system.versionLog.statusPublished'), value: 1 },
        ],
      },
      fieldName: 'status',
      label: $t('system.versionLog.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemVersionLogApi.VersionLog>,
): VxeTableGridColumns<SystemVersionLogApi.VersionLog> {
  return [
    {
      field: 'version',
      title: $t('system.versionLog.version'),
      width: 120,
    },
    {
      field: 'title',
      title: $t('system.versionLog.titleField'),
      minWidth: 200,
    },
    {
      field: 'publishTime',
      title: $t('system.versionLog.publishTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: $t('system.versionLog.createTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      fixed: 'right',
      title: $t('system.versionLog.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: $t('system.versionLog.titleField'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: ['edit', 'delete'],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.versionLog.operation'),
      width: 160,
    },
  ];
}
