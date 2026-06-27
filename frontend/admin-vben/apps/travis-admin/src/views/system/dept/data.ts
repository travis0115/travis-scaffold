import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemDeptApi } from '#/api';

import { z } from '#/adapter/form';
import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export function useSchema(
  getParentDeptTree: () => Promise<SystemDeptApi.SysDept[]>,
): VbenFormSchema[] {
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
        alwaysLoad: true,
        api: getParentDeptTree,
        class: 'w-full',
        labelField: 'deptName',
        treeDefaultExpandAll: false,
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
      rules: z
        .string()
        .min(2, '负责人长度为2-20个字符')
        .max(20, '负责人长度为2-20个字符')
        .optional()
        .or(z.literal('')),
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: $t('system.dept.mobile'),
      rules: z
        .string()
        .regex(/^$|^1[3-9]\d{9}$/, '请输入有效的手机号')
        .optional()
        .or(z.literal('')),
    },
    {
      component: 'InputNumber',
      componentProps: { max: 9999, min: 0 },
      fieldName: 'sort',
      label: $t('system.dept.sort'),
      defaultValue: 1,
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
      label: $t('system.dept.status'),
    },
  ];
}

export function useColumns(
  onActionClick?: OnActionClickFn<SystemDeptApi.SysDept>,
  onStatusChange?: (newStatus: number, row: SystemDeptApi.SysDept) => Promise<boolean>,
): VxeTableGridColumns<SystemDeptApi.SysDept> {
  return [
    {
      align: 'left',
      field: 'deptName',
      fixed: 'left',
      title: $t('system.dept.deptName'),
      treeNode: true,
      minWidth: 200,
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
      cellRender: {
        attrs: { beforeChange: onStatusChange, dictCode: 'sys_status' },
        name: onStatusChange ? 'CellSwitch' : 'CellTag',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.dept.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'deptName',
          nameTitle: $t('system.dept.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions([
          {
            code: 'append',
            text: $t('system.menu.appendChildren'),
          },
          'edit',
          {
            code: 'delete',
            show: (row: SystemDeptApi.SysDept) => !row.children?.length,
          },
          {
            code: 'remove',
            danger: true,
            show: (row: SystemDeptApi.SysDept) => Boolean(row.children?.length),
            text: $t('common.delete'),
          },
        ], {
          append: SYSTEM_PERMS.deptCreate,
          delete: SYSTEM_PERMS.deptDelete,
          edit: SYSTEM_PERMS.deptUpdate,
          remove: SYSTEM_PERMS.deptDelete,
        }),
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
