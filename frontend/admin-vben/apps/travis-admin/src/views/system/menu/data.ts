import type { Ref } from 'vue';

import type {
  OnActionClickFn,
  VxeTableGridColumns,
} from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api';

import { $t } from '#/locales';
import { getDictOptions } from '#/utils/dict';
import { filterAccessOptions, SYSTEM_PERMS } from '#/utils/permissions';

export function getMenuTypeOptions() {
  return getDictOptions('sys_menu_type');
}

function flattenTree(nodes: SystemMenuApi.SysMenu[]): SystemMenuApi.SysMenu[] {
  const result: SystemMenuApi.SysMenu[] = [];
  for (const node of nodes) {
    result.push(node);
    if (node.children?.length) result.push(...flattenTree(node.children));
  }
  return result;
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemMenuApi.SysMenu>,
  gridData: Ref<SystemMenuApi.SysMenu[]>,
  onStatusChange?: (newStatus: number, row: SystemMenuApi.SysMenu) => Promise<boolean>,
): VxeTableGridColumns<SystemMenuApi.SysMenu> {
  return [
    {
      align: 'left',
      field: 'menuName',
      fixed: 'left',
      slots: { default: 'title' },
      title: $t('system.menu.menuName'),
      treeNode: true,
      width: 200,
    },
    {
      align: 'center',
      cellRender: { attrs: { dictType: 'sys_menu_type' }, name: 'CellTag' },
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
      formatter: ({ row }) => {
        if (row.menuType === 1 && row.meta) {
          try {
            const meta = JSON.parse(row.meta);
            return meta.iframeSrc || meta.link || row.component || '-';
          } catch {
            return row.component || '-';
          }
        }
        return row.component || '-';
      },
      minWidth: 200,
      title: $t('system.menu.component'),
    },
    {
      field: 'sort',
      title: $t('system.menu.sort'),
      width: 80,
    },
    {
      cellRender: {
        attrs: { beforeChange: onStatusChange, dictType: 'sys_status' },
        name: 'CellRadio',
      },
      field: 'status',
      fixed: 'right',
      title: $t('system.menu.status'),
      width: 140,
    },
    {
      align: 'center',
      cellRender: {
        attrs: {
          nameField: 'menuName',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: filterAccessOptions([
          {
            code: 'moveUp',
            disabled: (row: SystemMenuApi.SysMenu) => {
              const siblings = flattenTree(gridData.value)
                .filter((item) => (item.parentId || 0) === (row.parentId || 0))
                .toSorted((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
              return siblings.findIndex((item) => item.id === row.id) <= 0;
            },
            text: $t('common.moveUp'),
          },
          {
            code: 'moveDown',
            disabled: (row: SystemMenuApi.SysMenu) => {
              const siblings = flattenTree(gridData.value)
                .filter((item) => (item.parentId || 0) === (row.parentId || 0))
                .toSorted((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
              const index = siblings.findIndex((item) => item.id === row.id);
              return index >= siblings.length - 1;
            },
            text: $t('common.moveDown'),
          },
          'edit',
          {
            code: 'delete',
            show: (row: SystemMenuApi.SysMenu) => !row.children?.length,
          },
          {
            code: 'remove',
            danger: true,
            show: (row: SystemMenuApi.SysMenu) => Boolean(row.children?.length),
            text: $t('common.delete'),
          },
        ], {
          delete: SYSTEM_PERMS.menuDelete,
          edit: SYSTEM_PERMS.menuUpdate,
          moveDown: SYSTEM_PERMS.menuUpdate,
          moveUp: SYSTEM_PERMS.menuUpdate,
          remove: SYSTEM_PERMS.menuDelete,
        }),
      },
      field: 'operation',
      fixed: 'right',
      headerAlign: 'center',
      showOverflow: false,
      title: $t('system.menu.operation'),
      width: 280,
    },
  ];
}
