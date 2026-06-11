import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemDeptApi } from '#/api';

import { z } from '#/adapter/form';
import { getDeptTree } from '#/api';
import { $t } from '#/locales';

export function useSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'deptName',
      label: $t('system.dept.deptName'),
      rules: z
        .string()
        .min(2, $t('ui.formRules.minLength', [$t('system.dept.deptName'), 2]))
        .max(
          20,
          $t('ui.formRules.maxLength', [$t('system.dept.deptName'), 20]),
        ),
    },
    {
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: getDeptTree,
        class: 'w-full',
        labelField: 'deptName',
        valueField: 'id',
        childrenField: 'children',
      },
      fieldName: 'parentId',
      label: $t('system.dept.parentDept'),
    },
    {
      component: 'Input',
      fieldName: 'leader',
      label: $t('system.dept.leader'),
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: $t('system.dept.mobile'),
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: $t('system.dept.sort'),
      defaultValue: 0,
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
      label: $t('system.dept.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemDeptApi.SysDept>,
): VxeTableGridColumns<SystemDeptApi.SysDept> {
  return [
    {
      align: 'left',
      field: 'deptName',
      fixed: 'left',
      title: $t('system.dept.deptName'),
      treeNode: true,
      width: 200,
    },
    {
      field: 'leader',
      title: $t('system.dept.leader'),
      width: 120,
    },
    {
      field: 'mobile',
      title: $t('system.dept.mobile'),
      width: 130,
    },
    {
      field: 'sort',
      title: $t('system.dept.sort'),
      width: 80,
    },
    {
      field: 'createTime',
      title: $t('system.dept.createTime'),
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      fixed: 'right',
      title: $t('system.dept.status'),
      width: 100,
    },
    {
      align: 'right',
      cellRender: {
        attrs: {
          nameField: 'deptName',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'append',
            text: $t('system.menu.appendChildren'),
          },
          'edit',
          {
            code: 'delete',
          },
        ],
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: $t('system.dept.operation'),
      width: 200,
    },
  ];
}
