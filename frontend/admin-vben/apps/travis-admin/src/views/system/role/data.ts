import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'roleName',
      label: $t('system.role.roleName'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.role.roleName')]))
        .max(50, $t('ui.formRules.maxLength', [$t('system.role.roleName'), 50])),
    },
    {
      component: 'Input',
      fieldName: 'roleCode',
      label: $t('system.role.roleCode'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.role.roleCode')]))
        .max(50, $t('ui.formRules.maxLength', [$t('system.role.roleCode'), 50]))
        .regex(/^[a-zA-Z][a-zA-Z0-9_]+$/, '角色编码必须以字母开头，只能包含字母、数字和下划线'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.role.remark'),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
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
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.role.status'),
    },
  ];
}

export function useColumns<T = SystemRoleApi.SysRole>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (
    newStatus: any,
    row: T,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
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
        attrs: { beforeChange: onStatusChange },
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
          nameTitle: $t('system.role.roleName'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.role.operation'),
      width: 130,
    },
  ];
}
