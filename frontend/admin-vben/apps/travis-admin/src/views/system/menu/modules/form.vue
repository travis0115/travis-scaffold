<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { SystemMenuApi } from '#/api';

import { computed, h, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { getPopupContainer } from '@vben/utils';

import { breakpointsTailwind, useBreakpoints } from '@vueuse/core';

import { useVbenForm, z } from '#/adapter/form';
import { createMenu, getMenuDetail, getMenuTree, updateMenu } from '#/api';
import { $t } from '#/locales';
import { componentKeys } from '#/router/routes';

import { getMenuTypeOptions } from '../data';

const emit = defineEmits<{
  success: [];
}>();

// 注意：不可使用 `class`/`className` 作为键名，antd Select 会将其作为保留字段
// 应用到选中项容器上（SingleContent 读取 option.data.class），导致选中后输入框
// 出现背景色块。改用 `dotClass` 仅供圆点渲染读取。
const badgeVariantOptions = [
  { dotClass: 'bg-green-500', label: 'default', value: 'default' },
  { dotClass: 'bg-destructive', label: 'destructive', value: 'destructive' },
  { dotClass: 'bg-primary', label: 'primary', value: 'primary' },
  { dotClass: 'bg-green-500', label: 'success', value: 'success' },
  { dotClass: 'bg-yellow-500', label: 'warning', value: 'warning' },
];

function renderBadgeVariant(value?: string, label?: string) {
  const option = badgeVariantOptions.find((item) => item.value === value);
  return h('span', { class: 'flex items-center gap-2' }, [
    h('span', {
      class: ['inline-block size-3 rounded-full', option?.dotClass],
    }),
    h('span', label ?? option?.label ?? value),
  ]);
}

const defaultBadgeVariant = 'default';

const formData = ref<SystemMenuApi.SysMenu>();

/** 解析 meta JSON 为对象 */
function parseMeta(metaStr?: string) {
  if (!metaStr) return {};
  try {
    return JSON.parse(metaStr);
  } catch {
    return {};
  }
}

function flattenMenus(menus: SystemMenuApi.SysMenu[]) {
  const result: SystemMenuApi.SysMenu[] = [];
  for (const menu of menus) {
    result.push(menu);
    if (menu.children?.length) {
      result.push(...flattenMenus(menu.children));
    }
  }
  return result;
}

async function isMenuPathExists(path: string, excludeId?: number) {
  const menus = await getMenuTree();
  return flattenMenus(menus).some(
    (menu) => menu.path === path && menu.id !== excludeId,
  );
}

const schema: VbenFormSchema[] = [
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      options: getMenuTypeOptions(),
      optionType: 'button',
    },
    defaultValue: 1,
    fieldName: 'menuType',
    formItemClass: 'col-span-2 md:col-span-2',
    label: $t('system.menu.type'),
  },
  {
    component: 'Input',
    fieldName: 'menuName',
    label: $t('system.menu.menuName'),
    rules: 'required',
  },
  {
    component: 'ApiTreeSelect',
    componentProps: {
      api: getMenuTree,
      class: 'w-full',
      allowClear: true,
      filterTreeNode(input: string, node: any) {
        if (!input || input.length === 0) {
          return true;
        }
        const name: string = node.menuName ?? '';
        return name.includes(input);
      },
      getPopupContainer,
      labelField: 'menuName',
      showSearch: true,
      treeDefaultExpandAll: true,
      valueField: 'id',
      childrenField: 'children',
    },
    fieldName: 'parentId',
    label: $t('system.menu.parent'),
    renderComponentContent() {
      return {
        title({ label, menuName }: { label: string; menuName: string }) {
          const coms = [];
          if (!label && !menuName) return '';
          coms.push(h('span', { class: '' }, menuName || label));
          return h('div', { class: 'flex items-center gap-1' }, coms);
        },
      };
    },
  },
  {
    component: 'Input',
    componentProps: (values) => ({
      disabled: values.menuType === 2,
    }),
    dependencies: {
      rules: (values) => {
        return [0, 1].includes(values.menuType)
          ? z
              .string()
              .min(1, $t('ui.formRules.required', [$t('system.menu.path')]))
              .max(
                100,
                $t('ui.formRules.maxLength', [$t('system.menu.path'), 100]),
              )
              .refine(
                (value: string) => value.startsWith('/'),
                $t('ui.formRules.startWith', [$t('system.menu.path'), '/']),
              )
              .refine(
                async (value: string) => {
                  return !(await isMenuPathExists(value, formData.value?.id));
                },
                (value) => ({
                  message: $t('ui.formRules.alreadyExists', [
                    $t('system.menu.path'),
                    value,
                  ]),
                }),
              )
          : null;
      },
      show: (values) => [0, 1].includes(values.menuType),
      triggerFields: ['menuType'],
    },
    fieldName: 'path',
    label: $t('system.menu.path'),
  },
  {
    component: 'AutoComplete',
    componentProps: (values) => ({
      allowClear: true,
      class: 'w-full',
      disabled: values.isExternal === 1,
      filterOption(input: string, option: { value: string }) {
        return option.value.toLowerCase().includes(input.toLowerCase());
      },
      options: componentKeys.map((v) => ({ value: v })),
    }),
    dependencies: {
      rules: (values) => {
        return values.menuType === 1 && values.isExternal !== 1
          ? 'required'
          : null;
      },
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType', 'isExternal'],
    },
    fieldName: 'component',
    label: $t('system.menu.component'),
  },

  {
    component: 'Input',
    dependencies: {
      rules: (values) => {
        return values.menuType === 2 ? 'required' : null;
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'perms',
    label: $t('system.menu.perms'),
  },
  {
    component: 'IconPicker',
    componentProps: {
      prefix: 'carbon',
    },
    dependencies: {
      show: (values) => [0, 1].includes(values.menuType),
      triggerFields: ['menuType'],
    },
    fieldName: 'icon',
    label: $t('system.menu.icon'),
  },
  {
    component: 'IconPicker',
    componentProps: {
      prefix: 'carbon',
    },
    dependencies: {
      show: (values) => [0, 1].includes(values.menuType),
      triggerFields: ['menuType'],
    },
    fieldName: '_activeIcon',
    label: $t('system.menu.activeIcon'),
  },
  {
    component: 'RadioGroup',
    componentProps: {
      options: [
        { label: '否', value: 0 },
        { label: '是', value: 1 },
      ],
    },
    defaultValue: 0,
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: 'isExternal',
    label: '是否外链',
  },
  {
    component: 'Input',
    componentProps: (values) => ({
      disabled: values.isExternal !== 1,
    }),
    dependencies: {
      rules: (values) =>
        values.menuType === 1 && values.isExternal === 1
          ? z
              .string()
              .min(1, $t('ui.formRules.required', [$t('system.menu.linkSrc')]))
              .url($t('ui.formRules.invalidURL'))
          : null,
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType', 'isExternal'],
    },
    fieldName: 'linkSrc',
    label: $t('system.menu.linkSrc'),
  },
  {
    component: 'RadioGroup',
    componentProps: (values) => ({
      disabled: values.isExternal !== 1,
      options: [
        { label: '内嵌', value: 'iframe' },
        { label: '新窗口', value: 'newWindow' },
      ],
    }),
    defaultValue: 'iframe',
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: 'externalOpenMode',
    label: '打开方式',
  },
  {
    component: 'Select',
    componentProps: {
      allowClear: true,
      class: 'w-full',
      options: [
        {
          label: $t('system.menu.badgeType.dot'),
          value: 'dot',
        },
        {
          label: $t('system.menu.badgeType.normal'),
          value: 'normal',
        },
      ],
    },
    dependencies: {
      show: (values) => values.menuType !== 2,
      triggerFields: ['menuType'],
    },
    fieldName: '_badgeType',
    label: $t('system.menu.badgeType.title'),
  },
  {
    component: 'Input',
    componentProps: (values) => ({
      allowClear: true,
      class: 'w-full',
      disabled: values._badgeType !== 'normal',
    }),
    dependencies: {
      rules: (values) =>
        values.menuType !== 2 && values._badgeType === 'normal'
          ? 'required'
          : null,
      show: (values) => values.menuType !== 2,
      triggerFields: ['menuType', '_badgeType'],
    },
    fieldName: '_badge',
    label: $t('system.menu.badge'),
  },
  {
    component: 'Select',
    componentProps: (values) => ({
      class: 'w-full',
      disabled: !values._badgeType,
      options: badgeVariantOptions,
      labelRender: ({ label, value }: { label?: string; value?: string }) =>
        renderBadgeVariant(value, label),
      optionRender: ({
        option,
      }: {
        option: { data: { label: string; value: string } };
      }) => renderBadgeVariant(option.data.value, option.data.label),
    }),
    defaultValue: defaultBadgeVariant,
    dependencies: {
      rules: (values) =>
        values.menuType !== 2 && values._badgeType ? 'selectRequired' : null,
      show: (values) => values.menuType !== 2,
      triggerFields: ['menuType', '_badgeType'],
    },
    fieldName: '_badgeVariants',
    label: $t('system.menu.badgeVariants'),
  },
  {
    component: 'InputNumber',
    defaultValue: 0,
    fieldName: 'sort',
    label: $t('system.menu.sort'),
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
    formItemClass: 'col-span-2 md:col-span-2',
    label: $t('system.menu.status'),
  },
  {
    component: 'Divider',
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: 'divider1',
    formItemClass: 'col-span-2 md:col-span-2 pb-0',
    hideLabel: true,
    renderComponentContent() {
      return {
        default: () => $t('system.menu.advancedSettings'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: '_affixTab',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.affixTab'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: '_hideInMenu',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.hideInMenu'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => values.menuType === 1,
      triggerFields: ['menuType'],
    },
    fieldName: '_hideInTab',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.hideInTab'),
      };
    },
  },
];

const breakpoints = useBreakpoints(breakpointsTailwind);
const isHorizontal = computed(() => breakpoints.greaterOrEqual('md').value);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    colon: true,
    formItemClass: 'col-span-2 md:col-span-1',
  },
  schema,
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2 gap-x-4',
});

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: onSubmit,
  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemMenuApi.SysMenu>();
      formApi.resetForm();
      if (data?.id) {
        // 编辑时加载完整详情
        const detail = await getMenuDetail(data.id);
        formData.value = detail;
        // 解析 meta JSON
        const metaObj = parseMeta(detail.meta);

        // 回填表单值
        const formValues: Record<string, any> = {
          ...detail,
          _affixTab: metaObj.affixTab ?? false,
          _activeIcon: metaObj.activeIcon,
          _badge: metaObj.badge,
          _badgeType: metaObj.badgeType,
          _badgeVariants: metaObj.badgeType
            ? (metaObj.badgeVariants ?? defaultBadgeVariant)
            : undefined,
          _hideInMenu: metaObj.hideInMenu ?? false,
          _hideInTab: metaObj.hideInTab ?? false,
          externalOpenMode: metaObj.iframeSrc ? 'iframe' : 'newWindow',
          isExternal: metaObj.iframeSrc || metaObj.link ? 1 : 0,
        };

        if (metaObj.iframeSrc && detail.menuType === 1) {
          formValues.linkSrc = metaObj.iframeSrc;
        } else if (metaObj.link && detail.menuType === 1) {
          formValues.linkSrc = metaObj.link;
        }

        formApi.setValues(formValues);
      } else {
        formData.value = undefined;
      }
    }
  },
});

async function onSubmit() {
  const { valid } = await formApi.validate();
  if (valid) {
    drawerApi.lock();
    const values =
      await formApi.getValues<
        Omit<SystemMenuApi.SysMenu, 'children' | 'createTime'> & {
          _activeIcon?: string;
          _affixTab?: boolean;
          _badge?: string;
          _badgeType?: 'dot' | 'normal';
          _badgeVariants?: string;
          _hideInMenu?: boolean;
          _hideInTab?: boolean;
          externalOpenMode?: 'iframe' | 'newWindow';
          isExternal?: number;
          linkSrc?: string;
        }
      >();

    // 构建 meta JSON
    const metaObj: Record<string, any> = parseMeta(values.meta);
    const badgeType = values.menuType === 2 ? undefined : values._badgeType;
    if (values.menuType !== 2 && values._activeIcon) {
      metaObj.activeIcon = values._activeIcon;
    }
    if (badgeType) metaObj.badgeType = badgeType;
    if (badgeType === 'normal' && values._badge) {
      metaObj.badge = values._badge;
    }
    if (badgeType) {
      metaObj.badgeVariants = values._badgeVariants || defaultBadgeVariant;
    }
    if (values.menuType === 1 && values._affixTab) metaObj.affixTab = true;
    if (values.menuType === 1 && values._hideInMenu) metaObj.hideInMenu = true;
    if (values.menuType === 1 && values._hideInTab) metaObj.hideInTab = true;

    // 清理开关未勾选的字段
    if (values.menuType === 2 || !values._activeIcon) delete metaObj.activeIcon;
    if (!badgeType) delete metaObj.badgeType;
    if (badgeType !== 'normal' || !values._badge) delete metaObj.badge;
    if (!badgeType) delete metaObj.badgeVariants;
    if (values.menuType !== 1 || !values._affixTab) delete metaObj.affixTab;
    if (values.menuType !== 1 || !values._hideInMenu) delete metaObj.hideInMenu;
    if (values.menuType !== 1 || !values._hideInTab) delete metaObj.hideInTab;
    delete metaObj.hideChildrenInMenu;
    delete metaObj.hideInBreadcrumb;
    delete metaObj.keepAlive;
    delete metaObj.openInNewWindow;

    if (values.menuType === 1 && values.isExternal === 1) {
      if (values.externalOpenMode === 'iframe') {
        metaObj.iframeSrc = values.linkSrc;
        delete metaObj.link;
      } else {
        metaObj.link = values.linkSrc;
        delete metaObj.iframeSrc;
      }
    } else {
      delete metaObj.iframeSrc;
      delete metaObj.link;
    }

    const data: Partial<SystemMenuApi.SysMenu> = {
      ...values,
      parentId: values.parentId ?? 0,
      component:
        values.menuType === 1 && values.isExternal !== 1
          ? values.component || ''
          : '',
      icon: values.menuType === 2 ? '' : values.icon,
      menuType: values.menuType,
      menuName: values.menuName,
      path: values.menuType === 2 ? '' : values.path,
      meta: Object.keys(metaObj).length > 0 ? JSON.stringify(metaObj) : '{}',
    };
    // 移除内部辅助字段
    delete (data as any)._affixTab;
    delete (data as any)._activeIcon;
    delete (data as any)._badge;
    delete (data as any)._badgeType;
    delete (data as any)._badgeVariants;
    delete (data as any)._hideInMenu;
    delete (data as any)._hideInTab;
    delete (data as any).externalOpenMode;
    delete (data as any).isExternal;
    delete (data as any).linkSrc;

    try {
      await (formData.value?.id
        ? updateMenu(formData.value.id, data)
        : createMenu(data));
      drawerApi.close();
      emit('success');
    } finally {
      drawerApi.unlock();
    }
  }
}

const getDrawerTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('system.menu.name')])
    : $t('ui.actionTitle.create', [$t('system.menu.name')]),
);
</script>
<template>
  <Drawer class="w-full max-w-200" :title="getDrawerTitle">
    <Form :layout="isHorizontal ? 'horizontal' : 'vertical'" />
  </Drawer>
</template>
