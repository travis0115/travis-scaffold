<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';

import { computed } from 'vue';

import { ProfilePasswordSetting, z } from '@vben/common-ui';

import { Modal } from 'antdv-next';

import { changePasswordApi } from '#/api';
import { useAuthStore } from '#/store/auth';

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      fieldName: 'oldPassword',
      label: '旧密码',
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: '请输入旧密码',
      },
      rules: z.string().min(1, { message: '请输入旧密码' }),
    },
    {
      fieldName: 'newPassword',
      label: '新密码',
      component: 'VbenInputPassword',
      labelClass: '-translate-y-[8px]',
      componentProps: {
        passwordStrength: true,
        placeholder: '请输入新密码',
      },
      rules: z
        .string()
        .min(8, { message: '密码长度不能少于8位' })
        .max(32, { message: '密码长度不能超过32位' })
        .refine(
          (value) => {
            let types = 0;
            if (/[a-z]/.test(value)) types++;
            if (/[A-Z]/.test(value)) types++;
            if (/\d/.test(value)) types++;
            if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value))
              types++;
            return types >= 3;
          },
          {
            message:
              '密码需包含大写字母、小写字母、数字、特殊符号中的至少3种',
          },
        ),
    },
    {
      fieldName: 'confirmPassword',
      label: '确认密码',
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: '请再次输入新密码',
      },
      dependencies: {
        rules(values) {
          const { newPassword } = values;
          return z
            .string({ required_error: '请再次输入新密码' })
            .min(1, { message: '请再次输入新密码' })
            .refine((value) => value === newPassword, {
              message: '两次输入的密码不一致',
            });
        },
        triggerFields: ['newPassword'],
      },
    },
  ];
});

async function handleSubmit(values: Record<string, any>) {
  try {
    await changePasswordApi({
      oldPassword: values.oldPassword,
      newPassword: values.newPassword,
    });
    Modal.success({
      title: '密码修改成功',
      content: '请重新登录',
      okText: '确定',
      cancelText: '取消',
      centered: true,
      onOk: async () => {
        const authStore = useAuthStore();
        await authStore.logout();
      },
    });
  } catch {
    // 错误由全局拦截器统一处理
  }
}
</script>
<template>
  <div class="password-form w-2/3">
    <ProfilePasswordSetting
      :form-schema="formSchema"
      @submit="handleSubmit"
    />
  </div>
</template>

<style scoped>
.password-form :deep(.mt-4) {
  float: right;
}

.password-form :deep(.flex-row.pb-4) {
  padding-bottom: 1.5rem;
}
</style>
