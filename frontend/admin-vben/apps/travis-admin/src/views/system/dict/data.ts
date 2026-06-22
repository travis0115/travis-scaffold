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
        .max(50, $t('ui.formRules.maxLength', [$t('system.dict.dictName'), 50])),
    },
    {
      component: 'Input',
      fieldName: 'dictType',
      label: $t('system.dict.dictType'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.dict.dictType')]))
        .max(50, $t('ui.formRules.maxLength', [$t('system.dict.dictType'), 50]))
        .regex(/^[a-zA-Z][a-zA-Z0-9_]+$/, '字典编码必须以字母开头，只能包含字母、数字和下划线'),
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: $t('system.dict.remark'),
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
      fieldName: 'dictType',
      label: $t('system.dict.dictType'),
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
      field: 'dictType',
      minWidth: 160,
      title: $t('system.dict.dictType'),
    },
    {
      field: 'remark',
      formatter: 'emptyPlaceholder',
      minWidth: 140,
      title: $t('system.dict.remark'),
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange, dictType: 'sys_status' },
        name: 'CellRadio',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.dict.status'),
      width: 140,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'dictName',
          nameTitle: $t('system.dict.dictName'),
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
