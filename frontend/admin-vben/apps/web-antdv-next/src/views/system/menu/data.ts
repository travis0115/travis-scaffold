import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api';

import { $t } from '#/locales';

export function getMenuTypeOptions() {
  return [
    { color: 'processing', label: $t('system.menu.typeCatalog'), value: 0 },
    { color: 'default', label: $t('system.menu.typeMenu'), value: 1 },
    { color: 'error', label: $t('system.menu.typeButton'), value: 2 },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemMenuApi.SysMenu>,
): VxeTableGridColumns<SystemMenuApi.SysMenu> {
  return [
    {
      align: 'left',
      field: 'name',
      fixed: 'left',
      title: $t('system.menu.menuName'),
      treeNode: true,
      width: 200,
    },
    {
      align: 'center',
      cellRender: { name: 'CellTag', options: getMenuTypeOptions() },
      field: 'menuType',
      title: $t('system.menu.type'),
      width: 100,
    },
    {
      field: 'perms',
      title: $t('system.menu.perms'),
      width: 200,
    },
    {
      align: 'left',
      field: 'path',
      title: $t('system.menu.path'),
      width: 200,
    },
    {
      align: 'left',
      field: 'component',
      minWidth: 200,
      title: $t('system.menu.component'),
    },
    {
      cellRender: { name: 'CellTag' },
      field: 'status',
      title: $t('system.menu.status'),
      width: 100,
    },
    {
      field: 'sort',
      title: $t('system.menu.sort'),
      width: 80,
    },
    {
      align: 'right',
      cellRender: {
        attrs: {
          nameField: 'name',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'append',
            text: $t('system.menu.appendChildren'),
          },
          'edit',
          'delete',
        ],
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: $t('system.menu.operation'),
      width: 200,
    },
  ];
}
