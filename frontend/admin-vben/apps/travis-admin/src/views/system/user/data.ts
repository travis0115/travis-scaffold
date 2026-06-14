import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { z } from '#/adapter/form';
import { getRoleList } from '#/api';
import { isDeptEnabled } from '#/features';
import { $t } from '#/locales';

export function useFormSchema(deptTreeData?: any[]): VbenFormSchema[] {
  const schemas: VbenFormSchema[] = [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
      rules: z
        .string()
        .min(6, $t('ui.formRules.minLength', [$t('system.user.username'), 6]))
        .max(16, $t('ui.formRules.maxLength', [$t('system.user.username'), 16]))
        .regex(
          /^[a-zA-Z][a-zA-Z0-9_]{5,15}$/,
          '用户名需以字母开头，长度为6-16位，仅支持字母、数字和下划线',
        ),
      description:
        '用户名需以字母开头，长度为6-16位，仅支持字母、数字和下划线',
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: $t('system.user.nickname'),
      rules: z
        .string()
        .min(1, $t('ui.formRules.required', [$t('system.user.nickname')]))
        .min(2, '昵称长度为2-20个字符')
        .max(20, '昵称长度为2-20个字符'),
    },
    {
      component: 'Input',
      componentProps: {
        type: 'password',
      },
      dependencies: {
        rules: (values) => {
          if (values.id) return null;
          return z
            .string()
            .min(8, '密码长度不能少于8位')
            .max(32, '密码长度不能超过32位')
            .refine(
              (value) => {
                let types = 0;
                if (/[a-z]/.test(value)) types++;
                if (/[A-Z]/.test(value)) types++;
                if (/\d/.test(value)) types++;
                if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value))
                  types++;
                return types >= 3;
              },
              {
                message:
                  '密码需包含大写字母、小写字母、数字、特殊符号中的至少3种',
              },
            );
        },
        show: (values) => {
          // 编辑时隐藏密码字段
          return !values.id;
        },
        triggerFields: ['id'],
      },
      fieldName: 'password',
      label: $t('system.user.password'),
      description:
        '密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种',
    },
    {
      component: 'ApiSelect',
      componentProps: {
        api: getRoleList,
        class: 'w-full',
        labelField: 'roleName',
        valueField: 'id',
        mode: 'multiple',
        placeholder: '请选择角色',
      },
      fieldName: 'roleIds',
      label: $t('system.user.roles'),
    },
  ];

  if (isDeptEnabled()) {
    schemas.push({
      component: 'TreeSelect',
      componentProps: {
        allowClear: true,
        fieldNames: { label: 'deptName', value: 'id', children: 'children' },
        placeholder: '请选择部门',
        treeData: deptTreeData ?? [],
      },
      fieldName: 'deptId',
      label: $t('system.user.dept'),
    });
  }

  schemas.push(
    {
      component: 'Input',
      fieldName: 'mobile',
      label: $t('system.user.mobile'),
      rules: z.string().regex(/^$|^1[3-9]\d{9}$/, '请输入有效的手机号').optional().or(z.literal('')),
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: $t('system.user.email'),
      rules: z.string().email('请输入有效的邮箱地址').optional().or(z.literal('')),
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
      label: $t('system.user.status'),
    },
  );

  return schemas;
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: $t('system.user.mobile'),
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
      label: $t('system.user.status'),
    },
  ];
}

export function useColumns<T = SystemUserApi.SysUser>(
  onActionClick: OnActionClickFn<T>,
  onStatusChange?: (
    newStatus: any,
    row: T,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
  return [
    {
      cellRender: { name: 'CellAvatar' },
      field: 'avatar',
      title: $t('system.user.avatar'),
      width: 70,
    },
    {
      field: 'username',
      title: $t('system.user.username'),
      width: 120,
    },
    {
      field: 'nickname',
      title: $t('system.user.nickname'),
      width: 120,
    },
    {
      field: 'mobile',
      title: $t('system.user.mobile'),
      width: 130,
    },
    {
      field: 'deptName',
      title: $t('system.user.dept'),
      width: 120,
      visible: isDeptEnabled(),
      formatter: 'emptyPlaceholder',
    },
    {
      field: 'roleNames',
      title: $t('system.user.roles'),
      minWidth: 120,
      formatter: ({ row }) => {
        if (row.roleNames && row.roleNames.length > 0) {
          return row.roleNames.join(', ');
        }
        return '-';
      },
    },
    {
      field: 'createTime',
      title: $t('system.user.createTime'),
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
      title: $t('system.user.status'),
      width: 100,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'username',
          nameTitle: $t('system.user.username'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: ['edit', 'resetPassword', 'delete'],
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.user.operation'),
      width: 200,
    },
  ];
}
