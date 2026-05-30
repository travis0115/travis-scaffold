<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { SystemMenuApi } from '#/api';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { createMenu, getMenuTree, updateMenu } from '#/api';
import { $t } from '#/locales';

import { getMenuTypeOptions } from '../data';

const emit = defineEmits<{
  success: [];
}>();

const formData = ref<SystemMenuApi.SysMenu>();

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
    fieldName: 'name',
    label: $t('system.menu.menuName'),
    rules: 'required',
  },
  {
    component: 'ApiTreeSelect',
    componentProps: {
      api: getMenuTree,
      class: 'w-full',
      labelField: 'name',
      valueField: 'id',
      childrenField: 'children',
      treeDefaultExpandAll: true,
    },
    fieldName: 'parentId',
    label: $t('system.menu.parent'),
  },
  {
    component: 'Input',
    dependencies: {
      show: (values) => {
        return [0, 1].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'path',
    label: $t('system.menu.path'),
  },
  {
    component: 'Input',
    dependencies: {
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
        return [0, 1, 2].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
    fieldName: 'perms',
    label: $t('system.menu.perms'),
  },
  {
    component: 'Input',
    fieldName: 'icon',
    label: $t('system.menu.icon'),
    dependencies: {
      show: (values) => {
        return [0, 1].includes(values.menuType);
      },
      triggerFields: ['menuType'],
    },
  },
  {
    component: 'InputNumber',
    fieldName: 'sort',
    label: $t('system.menu.sort'),
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
    label: $t('system.menu.status'),
  },
];

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
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemMenuApi.SysMenu>();
      if (data) {
        formData.value = data;
        formApi.setValues(formData.value);
      } else {
        formData.value = undefined;
        formApi.resetForm();
      }
    }
  },
});

async function onSubmit() {
  const { valid } = await formApi.validate();
  if (valid) {
    drawerApi.lock();
    const data =
      await formApi.getValues<
        Omit<SystemMenuApi.SysMenu, 'children' | 'createTime'>
      >();
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
    <Form class="mx-4" layout="horizontal" />
  </Drawer>
</template>
