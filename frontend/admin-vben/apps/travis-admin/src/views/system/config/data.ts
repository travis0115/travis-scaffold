import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemConfigApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'configKey',
      label: $t('system.config.configKey'),
      rules: z
        .string()
        .min(1, '配置键不能为空')
        .max(255, '配置键长度不能超过255个字符'),
    },
    {
      component: 'Textarea',
      fieldName: 'configValue',
      label: $t('system.config.configValue'),
      rules: z
        .string()
        .max(1000, '配置值长度不能超过1000个字符')
        .optional()
        .or(z.literal('')),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.config.remark'),
      rules: z
        .string()
        .max(255, '备注长度不能超过255个字符')
        .optional()
        .or(z.literal('')),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
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
  const isBuiltin = (row: Pick<SystemConfigApi.SystemConfig, 'isBuiltin'>) =>
    row.isBuiltin === 1;

  return [
    {
      field: 'configKey',
      title: $t('system.config.configKey'),
      width: 200,
    },
    {
      cellRender: {
        attrs: {
          dictCode: 'sys_config_type',
        },
        name: 'CellTag',
      },
      field: 'isBuiltin',
      title: $t('system.config.configType'),
      width: 110,
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
        options: filterAccessOptions(
          [
            'edit',
            {
              code: 'delete',
              show: (row: SystemConfigApi.SystemConfig) => !isBuiltin(row),
            },
          ],
          {
            delete: SYSTEM_PERMS.configDelete,
            edit: SYSTEM_PERMS.configUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.config.operation'),
      width: 130,
    },
  ];
}
