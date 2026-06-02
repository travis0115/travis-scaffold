import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemUpdateLogApi } from '#/api/system/updateLog';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'version',
      label: $t('system.updateLog.version'),
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'title',
      label: $t('system.updateLog.updateTitle'),
      rules: 'required',
    },
    {
      component: 'Textarea',
      componentProps: { rows: 6 },
      fieldName: 'content',
      label: $t('system.updateLog.content'),
      rules: 'required',
    },
    {
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'YYYY-MM-DDTHH:mm:ss',
      },
      fieldName: 'publishTime',
      label: $t('system.updateLog.publishTime'),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('system.updateLog.published'), value: 1 },
          { label: $t('system.updateLog.draft'), value: 0 },
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
      label: $t('system.updateLog.updateTitle'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: $t('system.updateLog.published'), value: 1 },
          { label: $t('system.updateLog.draft'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.updateLog.status'),
    },
  ];
}

export function useColumns<T = SystemUpdateLogApi.UpdateLog>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridColumns {
  return [
    {
      field: 'version',
      title: $t('system.updateLog.version'),
      width: 120,
    },
    {
      field: 'title',
      title: $t('system.updateLog.updateTitle'),
      minWidth: 200,
    },
    {
      cellRender: {
        name: 'CellTag',
        props: (row: any) => ({
          color: row.status === 1 ? 'success' : 'default',
        }),
      },
      field: 'status',
      title: $t('system.updateLog.status'),
      width: 100,
      formatter({ cellValue }: { cellValue: number }) {
        return cellValue === 1
          ? $t('system.updateLog.published')
          : $t('system.updateLog.draft');
      },
    },
    {
      field: 'publishTime',
      title: $t('system.updateLog.publishTime'),
      width: 180,
    },
    {
      field: 'createTime',
      title: $t('system.updateLog.createTime'),
      width: 180,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'title',
          nameTitle: $t('system.updateLog.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.updateLog.operation'),
      width: 130,
    },
  ];
}
