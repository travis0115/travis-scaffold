import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemConfigApi } from '#/api';

import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'configGroup',
      label: $t('system.config.configGroup'),
    },
    {
      component: 'Input',
      fieldName: 'configKey',
      label: $t('system.config.configKey'),
    },
    {
      component: 'Textarea',
      fieldName: 'configValue',
      label: $t('system.config.configValue'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.config.remark'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'configGroup',
      label: $t('system.config.configGroup'),
    },
    {
      component: 'Input',
      fieldName: 'configKey',
      label: $t('system.config.configKey'),
    },
  ];
}

export function useColumns<T = SystemConfigApi.SystemConfig>(
  onActionClick: OnActionClickFn<T>,
): VxeTableGridColumns {
  return [
    {
      field: 'configGroup',
      title: $t('system.config.configGroup'),
      width: 150,
    },
    {
      field: 'configKey',
      title: $t('system.config.configKey'),
      width: 200,
    },
    {
      field: 'configValue',
      minWidth: 200,
      title: $t('system.config.configValue'),
    },
    {
      field: 'remark',
      minWidth: 150,
      title: $t('system.config.remark'),
    },
    {
      field: 'createTime',
      title: $t('system.config.createTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'configKey',
          nameTitle: $t('system.config.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.config.operation'),
      width: 130,
    },
  ];
}
