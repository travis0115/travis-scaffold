<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { SystemMenuApi } from '#/api';

import { computed, h, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $te } from '@vben/locales';
import { getPopupContainer } from '@vben/utils';

import { breakpointsTailwind, useBreakpoints } from '@vueuse/core';

import { useVbenForm } from '#/adapter/form';
import { createMenu, getMenuDetail, getMenuTree, updateMenu } from '#/api';
import { $t } from '#/locales';

import { getMenuTypeOptions } from '../data';

const emit = defineEmits<{
  success: [];
}>();

const formData = ref<SystemMenuApi.SysMenu>();
const titleSuffix = ref<string>();

/** 解析 meta JSON 为对象 */
function parseMeta(metaStr?: string) {
  if (!metaStr) return {};
  try {
    return JSON.parse(metaStr);
  } catch {
    return {};
  }
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
    fieldName: 'menuTitle',
    label: $t('system.menu.menuTitle'),
    componentProps() {
      return {
        ...(titleSuffix.value && { addonAfter: titleSuffix.value }),
        onChange({ target: { value } }: { target: { value: string } }) {
          titleSuffix.value =
            value && $te(value) ? $t(value) : undefined;
        },
      };
    },
    help: $t('system.menu.menuTitle'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => {
        return [0, 1, 3, 4].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'path',
    label: $t('system.menu.path'),
  },
  {
    component: 'IconPicker',
    componentProps: {
      prefix: 'carbon',
    },
    dependencies: {
      show: (values) => {
        return [0, 1, 3, 4].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'icon',
    label: $t('system.menu.icon'),
  },
  {
    component: 'Input',
    dependencies: {
      rules: (values) => {
        return values.menuType === 1 ? 'required' : null;
      },
      show: (values) => {
        return values.menuType === 1;
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'component',
    label: $t('system.menu.component'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => {
        return [3, 4].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'linkSrc',
    label: $t('system.menu.linkSrc'),
  },
  {
    component: 'Input',
    dependencies: {
      rules: (values) => {
        return values.menuType === 2 ? 'required' : null;
      },
      show: (values) => {
        return [0, 1, 2].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'perms',
    label: $t('system.menu.perms'),
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
    label: $t('system.menu.status'),
  },
  {
    component: 'Divider',
    dependencies: {
      show: (values) => {
        return ![2].includes(values.menuType);
      },
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
      show: (values) => {
        return values.menuType === 1;
      },
      triggerFields: ['menuType'],
    },
    fieldName: '_keepAlive',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.keepAlive'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => {
        return [1, 3].includes(values.menuType);
      },
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
      show: (values) => {
        return ![2].includes(values.menuType);
      },
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
      show: (values) => {
        return [0, 1].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: '_hideChildrenInMenu',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.hideChildrenInMenu'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => {
        return ![2, 4].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: '_hideInBreadcrumb',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.hideInBreadcrumb'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => {
        return ![2, 4].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: '_hideInTab',
    renderComponentContent() {
      return {
        default: () => $t('system.menu.hideInTab'),
      };
    },
  },
  {
    component: 'Checkbox',
    dependencies: {
      show: (values) => {
        return values.menuType === 4;
      },
      triggerFields: ['menuType'],
    },
    fieldName: '_openInNewWindow',
    renderComponentContent() {
      return {
        default: () => '在新窗口打开',
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
          _keepAlive: metaObj.keepAlive ?? false,
          _affixTab: metaObj.affixTab ?? false,
          _hideInMenu: metaObj.hideInMenu ?? false,
          _hideChildrenInMenu: metaObj.hideChildrenInMenu ?? false,
          _hideInBreadcrumb: metaObj.hideInBreadcrumb ?? false,
          _hideInTab: metaObj.hideInTab ?? false,
          _openInNewWindow: metaObj.openInNewWindow ?? false,
        };

        // 从 meta 推断前端菜单类型
        if (metaObj.iframeSrc && detail.menuType === 1) {
          formValues.menuType = 3; // 内嵌
          formValues.linkSrc = metaObj.iframeSrc;
        } else if (metaObj.link && detail.menuType === 1) {
          formValues.menuType = 4; // 外链
          formValues.linkSrc = metaObj.link;
        }

        // 回填 menuTitle
        formValues.menuTitle = detail.menuName;
        titleSuffix.value = detail.menuName;

        formApi.setValues(formValues);
      } else {
        formData.value = undefined;
        titleSuffix.value = '';
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
          _affixTab?: boolean;
          _hideChildrenInMenu?: boolean;
          _hideInBreadcrumb?: boolean;
          _hideInMenu?: boolean;
          _hideInTab?: boolean;
          _keepAlive?: boolean;
          _openInNewWindow?: boolean;
          linkSrc?: string;
          menuTitle?: string;
        }
      >();

    // 构建 meta JSON
    const metaObj: Record<string, any> = parseMeta(values.meta);
    if (values._keepAlive) metaObj.keepAlive = true;
    if (values._affixTab) metaObj.affixTab = true;
    if (values._hideInMenu) metaObj.hideInMenu = true;
    if (values._hideChildrenInMenu) metaObj.hideChildrenInMenu = true;
    if (values._hideInBreadcrumb) metaObj.hideInBreadcrumb = true;
    if (values._hideInTab) metaObj.hideInTab = true;
    if (values._openInNewWindow) metaObj.openInNewWindow = true;

    // 清理开关未勾选的字段
    if (!values._keepAlive) delete metaObj.keepAlive;
    if (!values._affixTab) delete metaObj.affixTab;
    if (!values._hideInMenu) delete metaObj.hideInMenu;
    if (!values._hideChildrenInMenu) delete metaObj.hideChildrenInMenu;
    if (!values._hideInBreadcrumb) delete metaObj.hideInBreadcrumb;
    if (!values._hideInTab) delete metaObj.hideInTab;
    if (!values._openInNewWindow) delete metaObj.openInNewWindow;

    // 处理内嵌/外链类型
    let actualMenuType = values.menuType;
    if (values.menuType === 3) {
      // 内嵌类型 → 存储为 menuType=1 + meta.iframeSrc
      actualMenuType = 1 as any;
      if (values.linkSrc) metaObj.iframeSrc = values.linkSrc;
      delete metaObj.link;
    } else if (values.menuType === 4) {
      // 外链类型 → 存储为 menuType=1 + meta.link
      actualMenuType = 1 as any;
      if (values.linkSrc) metaObj.link = values.linkSrc;
      delete metaObj.iframeSrc;
    } else {
      delete metaObj.iframeSrc;
      delete metaObj.link;
    }

    const data: Partial<SystemMenuApi.SysMenu> = {
      ...values,
      menuType: actualMenuType as any,
      menuName: values.menuTitle || values.menuName,
      meta: Object.keys(metaObj).length > 0 ? JSON.stringify(metaObj) : undefined,
    };
    // 移除内部辅助字段
    delete (data as any)._keepAlive;
    delete (data as any)._affixTab;
    delete (data as any)._hideInMenu;
    delete (data as any)._hideChildrenInMenu;
    delete (data as any)._hideInBreadcrumb;
    delete (data as any)._hideInTab;
    delete (data as any)._openInNewWindow;
    delete (data as any).linkSrc;
    delete (data as any).menuTitle;

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
    <Form class="mx-4" :layout="isHorizontal ? 'horizontal' : 'vertical'" />
  </Drawer>
</template>
