import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemDictApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';

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
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
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
        options: [
          { label: $t('common.enabled'), value: 1 },
          { label: $t('common.disabled'), value: 0 },
        ],
      },
      fieldName: 'status',
      label: $t('system.dict.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemDictApi.SysDict>,
): VxeTableGridColumns<SystemDictApi.SysDict> {
  return [
    {
      field: 'dictName',
      title: $t('system.dict.dictName'),
      width: 150,
    },
    {
      field: 'dictType',
      title: $t('system.dict.dictType'),
      width: 150,
    },
    {
      field: 'remark',
      minWidth: 100,
      title: $t('system.dict.remark'),
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      title: $t('system.dict.status'),
      width: 100,
    },
    {
      field: 'createTime',
      title: $t('system.dict.createTime'),
      width: 180,
      formatter: 'formatDateTime',
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
        options: [
          {
            code: 'addItem',
            text: '数据项',
          },
          'edit',
          'delete',
        ],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.dict.operation'),
      width: 200,
    },
  ];
}
