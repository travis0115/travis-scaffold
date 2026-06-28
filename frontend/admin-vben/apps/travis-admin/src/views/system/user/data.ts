import type { VbenFormSchema } from '#/adapter/form';
import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api';

import { h } from 'vue';

import { formatDateTime } from '@vben/utils';

import { Button } from 'antdv-next';

import { z } from '#/adapter/form';
import { getRoleList } from '#/api';
import { isDeptEnabled } from '#/features';
import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

function generateRandomPassword() {
  const groups = [
    'ABCDEFGHJKLMNPQRSTUVWXYZ',
    'abcdefghjkmnpqrstuvwxyz',
    '23456789',
    '~!@#$%^&*',
  ];
  const chars = groups.join('');
  const password = groups.map((group) => pickChar(group));
  for (let i = password.length; i < 8; i++) {
    password.push(pickChar(chars));
  }
  return password.toSorted(() => Math.random() - 0.5).join('');
}

function pickChar(chars: string) {
  return chars.charAt(Math.floor(Math.random() * chars.length));
}

function formatIp(ip?: string, location?: string) {
  if (!ip && !location) {
    return '-';
  }
  return `${ip || '-'}${location ? `（${location}）` : ''}`;
}

function formatLastOnline(row: SystemUserApi.SysUser) {
  if (!row.lastOnlineTime && !row.lastOnlineIp && !row.lastOfflineTime) {
    return '-';
  }
  const timeLabel = row.online ? '上线' : '离线';
  const time = row.online ? row.lastOnlineTime : row.lastOfflineTime;
  const formattedTime = formatDateTime(time);
  return [
    `${timeLabel}：${formattedTime || '-'}`,
    `IP：${formatIp(row.lastOnlineIp, row.lastOnlineLocation)}`,
  ].join('\n');
}

function formatContactInfo(row: SystemUserApi.SysUser) {
  if (!row.mobile && !row.email) {
    return '-';
  }
  return [
    `${$t('system.user.mobile')}：${row.mobile || '-'}`,
    `${$t('system.user.email')}：${row.email || '-'}`,
  ].join('\n');
}

export function useFormSchema(deptTreeData?: any[]): VbenFormSchema[] {
  const schemas: VbenFormSchema[] = [
    {
      component: 'Input',
      fieldName: 'username',
      label: $t('system.user.username'),
      rules: z
        .string()
        .regex(
          /^[a-zA-Z][a-zA-Z0-9_]{5,15}$/,
          '用户名需以字母开头，长度为6-16位，仅支持字母、数字和下划线',
        ),
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
      component: 'InputPassword',
      fieldName: 'password',
      label: $t('system.user.password'),
      renderComponentContent(_values, formApi) {
        return {
          addonAfter: () =>
            h(
              Button,
              {
                size: 'small',
                type: 'link',
                onClick: () => {
                  formApi.setFieldValue('password', generateRandomPassword());
                },
              },
              () => $t('system.user.generatePassword'),
            ),
        };
      },
      rules: z
        .string()
        .min(8, '密码长度不能少于8位')
        .max(32, '密码长度不能超过32位')
        .refine(
          (value) => {
            let types = 0;
            if (/[a-z]/.test(value)) types++;
            if (/[A-Z]/.test(value)) types++;
            if (/\d/.test(value)) types++;
            if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value)) types++;
            return types >= 3;
          },
          {
            message: '密码需包含大写字母、小写字母、数字、特殊符号中的至少3种',
          },
        ),
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
      rules: z
        .string()
        .regex(/^$|^1[3-9]\d{9}$/, '请输入有效的手机号')
        .optional(),
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: $t('system.user.email'),
      rules: z.string().email('请输入有效的邮箱地址').or(z.literal('')).optional(),
    },
    {
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: getDictOptions('status'),
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
        options: getDictOptions('status'),
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
      align: 'center',
      cellRender: { name: 'CellAvatar' },
      field: 'avatar',
      title: $t('system.user.avatar'),
      width: 70,
    },
    {
      field: 'username',
      title: $t('system.user.username'),
      width: 140,
    },
    {
      field: 'nickname',
      title: $t('system.user.nickname'),
      width: 140,
    },
    {
      align: 'center',
      cellRender: {
        attrs: { dictCode: 'online_status' },
        name: 'CellTag',
      },
      field: 'onlineStatus',
      title: $t('system.user.onlineStatus'),
      width: 100,
    },
    {
      field: 'roleNames',
      title: $t('system.user.roles'),
      width: 120,
      formatter: ({ row }) => {
        if (row.roleNames && row.roleNames.length > 0) {
          return row.roleNames.join(', ');
        }
        return '-';
      },
    },
    {
      field: 'deptName',
      title: $t('system.user.dept'),
      width: 140,
      visible: isDeptEnabled(),
      formatter: 'emptyPlaceholder',
    },
    {
      className: 'whitespace-pre-line text-center leading-6',
      field: 'contactInfo',
      formatter: ({ row }) => formatContactInfo(row),
      headerAlign: 'center',
      showOverflow: false,
      title: $t('system.user.contactInfo'),
      minWidth: 210,
    },
    {
      className: 'whitespace-pre-line text-center leading-6',
      field: 'lastOnlineTime',
      formatter: ({ row }) => formatLastOnline(row),
      headerAlign: 'center',
      showOverflow: false,
      sortable: true,
      title: $t('system.user.lastLogin'),
      minWidth: 260,
    },
    
    {
      field: 'createTime',
      title: $t('system.user.createTime'),
      sortable: true,
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange, dictCode: 'status' },
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
          nameTitle: $t('system.user.name'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions(['edit', 'resetPassword', 'delete'], {
          delete: SYSTEM_PERMS.userDelete,
          edit: SYSTEM_PERMS.userUpdate,
          resetPassword: SYSTEM_PERMS.userUpdate,
        }),
      },
      field: 'operation',
      fixed: 'right',
      title: $t('system.user.operation'),
      width: 200,
    },
  ];
}
