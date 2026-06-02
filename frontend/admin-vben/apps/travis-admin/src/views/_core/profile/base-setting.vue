<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';

import { computed, onMounted, ref } from 'vue';

import { ProfileBaseSetting } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';

import { App } from 'antdv-next';

import { getUserInfoApi, updateProfileApi } from '#/api';

const userStore = useUserStore();
const { modal } = App.useApp();
const profileBaseSettingRef = ref();

const formSchema = computed((): VbenFormSchema[] => {
  // 从用户信息中构建角色选项
  const roleNames: string[] =
    (userStore.userInfo as any)?.roleNames || [];
  const roleOptions = roleNames.map((name) => ({
    label: name,
    value: name,
  }));

  return [
    {
      fieldName: 'roles',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        options: roleOptions,
        size: 'large',
      },
      label: '角色',
      disabled: true,
    },
    {
      fieldName: 'username',
      component: 'Input',
      label: '用户名',
      componentProps: {
        size: 'large',
      },
      disabled: true,
    },
    {
      fieldName: 'nickname',
      component: 'Input',
      label: '昵称',
      componentProps: {
        size: 'large',
      },
      rules: 'required',
    },
    {
      fieldName: 'email',
      component: 'Input',
      label: '邮箱',
      componentProps: {
        size: 'large',
      },
    },
    {
      fieldName: 'mobile',
      component: 'Input',
      label: '手机号',
      componentProps: {
        size: 'large',
      },
    },
  ];
});

onMounted(async () => {
  const data = await getUserInfoApi();
  profileBaseSettingRef.value?.getFormApi().setValues({
    ...data,
    roles: (data as any)?.roleNames || [],
  });
});


/** 表单提交 */
async function handleSubmit(values: Record<string, any>) {
  try {
    await updateProfileApi(
      {
        nickname: values.nickname,
        email: values.email,
        mobile: values.mobile,
      },
    );
    modal.success({
      title: '修改成功',
      content: '您的信息已成功修改',
    });
    // 同步更新 store
    if (userStore.userInfo) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: values.nickname,
        email: values.email,
        mobile: values.mobile,
      });
    }
  } catch {
    // 错误由全局拦截器以 Modal 弹窗统一处理
  }
}
</script>
<template>
  <div class="base-form w-2/3">
    <ProfileBaseSetting
      ref="profileBaseSettingRef"
      :form-schema="formSchema"
      @submit="handleSubmit"
    />
  </div>
</template>

<style scoped>
.base-form :deep(.mt-4) {
  float: right;
}

.base-form :deep(.flex-row.pb-4) {
  padding-bottom: 1.5rem;
}
</style>
