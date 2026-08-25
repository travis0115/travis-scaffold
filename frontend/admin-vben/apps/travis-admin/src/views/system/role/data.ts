import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridColumns } from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';
import { enableStatusOptions } from '#/utils/business-options';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'roleName',
      label: $t('system.role.roleName'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.role.roleName')]))
        .max(
          50,
          $t('ui.formRules.maxLength', [$t('system.role.roleName'), 50]),
        ),
    },
    {
      component: 'Input',
      fieldName: 'roleCode',
      label: $t('system.role.roleCode'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.role.roleCode')]))
        .max(50, $t('ui.formRules.maxLength', [$t('system.role.roleCode'), 50]))
        .regex(
          /^[a-zA-Z][a-zA-Z0-9_]+$/,
          '角色编码必须以字母开头，只能包含字母、数字和下划线',
        ),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.role.remark'),
      rules: z
        .string()
        .max(255, '备注长度不能超过255个字符')
        .optional()
        .or(z.literal('')),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: enableStatusOptions,
        optionType: 'button',
      },
      defaultValue: 1,
      fieldName: 'status',
      label: $t('system.role.status'),
    },
    {
      component: 'Input',
      fieldName: 'menuIds',
      formItemClass: 'items-start',
      label: $t('system.role.assignMenus'),
      modelPropName: 'modelValue',
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'roleName',
      label: $t('system.role.roleName'),
    },
    {
      component: 'Input',
      fieldName: 'roleCode',
      label: $t('system.role.roleCode'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: enableStatusOptions,
      },
      fieldName: 'status',
      label: $t('system.role.status'),
    },
  ];
}

export function useColumns<T = SystemRoleApi.SysRole>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
  const isNotModifiable = (row: Pick<SystemRoleApi.SysRole, 'modifiable'>) =>
    row.modifiable === 0;
  const isBuiltin = (row: Pick<SystemRoleApi.SysRole, 'isBuiltin'>) =>
    row.isBuiltin === 1;

  return [
    {
      field: 'roleName',
      title: $t('system.role.roleName'),
      width: 150,
    },
    {
      field: 'roleCode',
      title: $t('system.role.roleCode'),
      width: 150,
    },
    {
      cellRender: {
        attrs: {
          dictCode: 'sys_role_type',
        },
        name: 'CellTag',
      },
      field: 'isBuiltin',
      title: $t('system.role.roleType'),
      width: 110,
    },
    {
      field: 'remark',
      minWidth: 100,
      title: $t('system.role.remark'),
    },
    {
      field: 'createTime',
      title: $t('system.role.createTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      cellRender: {
        attrs: {
          beforeChange: onStatusChange,
          dictCode: 'enable_status',
          disabled: isNotModifiable,
        },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.role.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'roleName',
          nameTitle: $t('system.role.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(
          [
            {
              code: 'edit',
              show: (row: SystemRoleApi.SysRole) => !isNotModifiable(row),
            },
            {
              code: 'delete',
              show: (row: SystemRoleApi.SysRole) =>
                !isNotModifiable(row) && !isBuiltin(row),
            },
          ],
          {
            delete: SYSTEM_PERMS.roleDelete,
            edit: SYSTEM_PERMS.roleUpdate,
          },
        ),
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.role.operation'),
      width: 130,
    },
  ];
}
