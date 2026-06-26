<script lang="ts" setup>
import type { SystemMessageApi } from '#/api';

import { ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import {
  createMessage,
  getDeptTree,
  getMessageDetail,
  getRoleList,
  getUserPage,
  updateMessage,
} from '#/api';

import { useFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<SystemMessageApi.Message>();
const [Form, formApi] = useVbenForm({ schema: useFormSchema(), showDefaultActions: false });

function buildChannelContents(values: Record<string, any>) {
  const channels: string[] = values.channels || [];
  const contents: SystemMessageApi.MessageChannelContent[] = [];
  if (channels.includes('IN_APP')) {
    contents.push({ channel: 'IN_APP', content: values.inAppContent });
  }
  if (channels.includes('APP_PUSH')) {
    contents.push({
      channel: 'APP_PUSH',
      imageUrl: values.appPushImageUrl,
      jumpUrl: values.appPushJumpUrl,
      subtitle: values.appPushSubtitle,
      title: values.appPushTitle,
    });
  }
  if (channels.includes('SMS')) {
    contents.push({
      channel: 'SMS',
      content: values.smsContent,
      wordCount: values.smsContent?.length || 0,
    });
  }
  if (channels.includes('WECHAT_MP')) {
    contents.push({
      channel: 'WECHAT_MP',
      templateParams: values.miniProgramTemplateParams,
    });
  }
  if (channels.includes('DOUYIN_MP')) {
    contents.push({
      channel: 'DOUYIN_MP',
      templateParams: values.miniProgramTemplateParams,
    });
  }
  return contents;
}

function toFormValues(detail: SystemMessageApi.Message) {
  const channelContents = detail.channelContents || [];
  const contentMap = new Map(channelContents.map((item) => [item.channel, item]));
  const channels = detail.channels
    ? detail.channels.split(',').filter(Boolean)
    : channelContents.map((item) => item.channel);
  return {
    ...detail,
    appPushImageUrl: contentMap.get('APP_PUSH')?.imageUrl,
    appPushJumpUrl: contentMap.get('APP_PUSH')?.jumpUrl,
    appPushSubtitle: contentMap.get('APP_PUSH')?.subtitle,
    appPushTitle: contentMap.get('APP_PUSH')?.title,
    channels: channels.length > 0 ? channels : ['IN_APP'],
    inAppContent: contentMap.get('IN_APP')?.content || detail.content,
    miniProgramTemplateParams:
      contentMap.get('WECHAT_MP')?.templateParams ||
      contentMap.get('DOUYIN_MP')?.templateParams,
    smsContent: contentMap.get('SMS')?.content,
  };
}

function cleanFormOnlyFields(data: Record<string, any>) {
  [
    'appPushImageUrl',
    'appPushJumpUrl',
    'appPushSubtitle',
    'appPushTitle',
    'deptIds',
    'inAppContent',
    'miniProgramTemplateParams',
    'roleIds',
    'smsContent',
    'userIds',
  ].forEach((key) => delete data[key]);
}

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();
    const targetField = ['', 'userIds', 'roleIds', 'deptIds'][values.audienceType];
    const data: Record<string, any> = {
      ...values,
      channelContents: buildChannelContents(values),
      channels: values.channels?.join(','),
      content: values.inAppContent || values.smsContent || values.title,
      targetIds: targetField ? values[targetField] : [],
    };
    if (values.pushType === 0) {
      delete data.publishTime;
    }
    cleanFormOnlyFields(data);
    await (formData.value?.id ? updateMessage(formData.value.id, data) : createMessage(data));
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
      const detail = await getMessageDetail(data.id);
      const targetField = ['', 'userIds', 'roleIds', 'deptIds'][detail.audienceType];
      await formApi.setValues({
        ...toFormValues(detail),
        ...(targetField ? { [targetField]: detail.targetIds } : {}),
      });
    } else {
      formData.value = undefined;
      await formApi.setValues({ channels: ['IN_APP'], pushType: 0, sourceType: 'MANUAL' });
    }
  },
});
</script>

<template><Drawer :title="formData?.id ? '编辑消息推送' : '新增消息推送'"><Form /></Drawer></template>
