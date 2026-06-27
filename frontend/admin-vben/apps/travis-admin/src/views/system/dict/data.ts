import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'dictName',
      label: $t('system.dict.dictName'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.dict.dictName')]))
        .min(2, '字典名称长度为2-20个字符')
        .max(20, '字典名称长度为2-20个字符'),
    },
    {
      component: 'Input',
      fieldName: 'dictCode',
      label: $t('system.dict.dictCode'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.dict.dictCode')]))
        .max(100, '字典编码长度不能超过100个字符')
        .regex(/^[a-zA-Z][a-zA-Z0-9_]+$/, '字典编码必须以字母开头，只能包含字母、数字和下划线'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.dict.remark'),
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
        options: getDictOptions('sys_status'),
        optionType: 'button',
      },
      defaultValue: 1,
      fieldName: 'status',
      label: $t('system.dict.status'),
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'dictName',
      label: $t('system.dict.dictName'),
    },
    {
      component: 'Input',
      fieldName: 'dictCode',
      label: $t('system.dict.dictCode'),
    },
    {
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions('sys_status'),
      },
      fieldName: 'status',
      label: $t('system.dict.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemDictApi.SysDict>,
  onStatusChange?: (newStatus: number, row: SystemDictApi.SysDict) => Promise<boolean>,
): VxeTableGridColumns<SystemDictApi.SysDict> {
  return [
    {
      field: 'dictName',
      minWidth: 140,
      title: $t('system.dict.dictName'),
    },
    {
      field: 'dictCode',
      minWidth: 160,
      title: $t('system.dict.dictCode'),
    },
    {
      field: 'remark',
      formatter: 'emptyPlaceholder',
      minWidth: 140,
      title: $t('system.dict.remark'),
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange, dictCode: 'sys_status' },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.dict.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'dictName',
          nameTitle: $t('system.dict.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(['edit', 'delete'], {
          delete: SYSTEM_PERMS.dictDelete,
          edit: SYSTEM_PERMS.dictUpdate,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.dict.operation'),
      width: 140,
    },
  ];
}
