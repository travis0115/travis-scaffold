<script lang="ts" setup>
import type { SystemMessageApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import {
  createMessagePush,
  getDeptTree,
  getMessagePushDetail,
  getRoleList,
  getUserPage,
  updateMessagePush,
} from '#/api';

import { useFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.Message>();
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });
const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const targetField = ['', 'userIds', 'roleIds', 'deptIds'][values.audienceType];
    const data: Record<string, any> = {
      ...values,
      targetIds: targetField ? values[targetField] : [],
    };
    delete data.userIds;
    delete data.roleIds;
    delete data.deptIds;
    if (formData.value?.id) delete data.status;
    await (formData.value?.id ? updateMessagePush(formData.value.id, data) : createMessagePush(data));
    emit('success');
    drawerApi.close();
  },
  async onOpenChange(open) {
    if (!open) return;
    const data = drawerApi.getData<SystemMessageApi.Message>();
    formApi.resetForm();
    formData.value = data;
    formApi.updateSchema([
      {
        fieldName: 'status',
        hide: Boolean(data?.id),
      },
    ]);
    const [userPage, roles, departments] = await Promise.all([
      getUserPage({ pageNum: 1, pageSize: 500, status: 1 }),
      getRoleList(),
      getDeptTree(),
    ]);
    formApi.updateSchema([
      {
        componentProps: {
          options: userPage.records.map((item) => ({ label: `${item.nickname}（${item.username}）`, value: item.id })),
        },
        fieldName: 'userIds',
      },
      {
        componentProps: { options: roles.map((item) => ({ label: item.roleName, value: item.id })) },
        fieldName: 'roleIds',
      },
      { componentProps: { treeData: departments }, fieldName: 'deptIds' },
    ]);
    if (data?.id) {
      const detail = await getMessagePushDetail(data.id);
      const targetField = ['', 'userIds', 'roleIds', 'deptIds'][detail.audienceType];
      await formApi.setValues({
        ...detail,
        ...(targetField ? { [targetField]: detail.targetIds } : {}),
      });
    }
  },
});
</script>

<template><Drawer :title="formData?.id ? '编辑消息推送' : '新增消息推送'"><Form /></Drawer></template>
