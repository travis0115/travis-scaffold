import type { Ref } from 'vue';

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
    { color: 'success', label: $t('system.menu.typeEmbedded'), value: 3 },
    { color: 'warning', label: $t('system.menu.typeLink'), value: 4 },
  ];
}

function flattenTree(nodes: SystemMenuApi.SysMenu[]): SystemMenuApi.SysMenu[] {
  const result: SystemMenuApi.SysMenu[] = [];
  for (const node of nodes) {
    result.push(node);
    if (node.children?.length) {
      result.push(...flattenTree(node.children));
    }
  }
  return result;
}

export function useColumns(
  onActionClick: OnActionClickFn<SystemMenuApi.SysMenu>,
  gridData: Ref<SystemMenuApi.SysMenu[]>,
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
      formatter: ({ row }) => {
        switch (row.menuType) {
          case 0:
          case 1: {
            return row.component || '-';
          }
          case 3: {
            // 内嵌类型，显示iframeSrc
            try {
              const meta = row.meta ? JSON.parse(row.meta) : {};
              return meta.iframeSrc || '-';
            } catch {
              return '-';
            }
          }
          case 4: {
            // 外链类型，显示link
            try {
              const meta = row.meta ? JSON.parse(row.meta) : {};
              return meta.link || '-';
            } catch {
              return '-';
            }
          }
        }
        return '-';
      },
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
          nameField: 'menuName',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'moveUp',
            text: $t('common.moveUp'),
            disabled: (row: SystemMenuApi.SysMenu) => {
              const all = flattenTree(gridData.value);
              const siblings = all.filter(
                (n) => (n.parentId || 0) === (row.parentId || 0),
              );
              siblings.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
              const idx = siblings.findIndex((n) => n.id === row.id);
              return idx <= 0;
            },
          },
          {
            code: 'moveDown',
            text: $t('common.moveDown'),
            disabled: (row: SystemMenuApi.SysMenu) => {
              const all = flattenTree(gridData.value);
              const siblings = all.filter(
                (n) => (n.parentId || 0) === (row.parentId || 0),
              );
              siblings.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
              const idx = siblings.findIndex((n) => n.id === row.id);
              return idx >= siblings.length - 1;
            },
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
      width: 280,
    },
  ];
}
