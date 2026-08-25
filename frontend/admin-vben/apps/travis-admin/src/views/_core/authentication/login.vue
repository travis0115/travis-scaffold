<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';

import { computed, markRaw } from 'vue';

import { AuthenticationLogin, SliderCaptcha, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useAuthStore } from '#/store';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();

const formSchema = computed((): VbenFormSchema[] => {
  const usernameRule = z
    .string()
    .min(1, { message: '用户名不能为空' })
    .refine((value) => !value || (value.length >= 6 && value.length <= 16), {
      message: '用户名长度为6-16个字符',
    })
    .refine(
      (value) =>
        !value ||
        value.length < 6 ||
        value.length > 16 ||
        /^[a-zA-Z][a-zA-Z0-9_]*$/.test(value),
      {
        message: '用户名格式不正确，需以字母开头，仅支持字母、数字和下划线',
      },
    );

  const passwordRule = z
    .string()
    .min(1, { message: '密码不能为空' })
    .refine((value) => !value || (value.length >= 8 && value.length <= 32), {
      message:
        '密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种',
    })
    .refine(
      (value) => {
        if (!value || value.length < 8 || value.length > 32) return true;
        let types = 0;
        if (/[a-z]/.test(value)) types++;
        if (/[A-Z]/.test(value)) types++;
        if (/\d/.test(value)) types++;
        if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(value)) types++;
        return types >= 3;
      },
      {
        message:
          '密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种',
      },
    );

  return [
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.usernameTip'),
      },
      fieldName: 'username',
      label: $t('authentication.username'),
      rules: usernameRule,
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.password'),
      },
      fieldName: 'password',
      label: $t('authentication.password'),
      rules: passwordRule,
    },
    {
      component: markRaw(SliderCaptcha),
      fieldName: 'captcha',
      formFieldProps: {
        validateOnModelUpdate: true,
      },
      rules: z.boolean().refine((value) => value, {
        message: $t('authentication.verifyRequiredTip'),
      }),
    },
  ];
});
</script>

<template>
  <AuthenticationLogin
    :form-schema="formSchema"
    :loading="authStore.loginLoading"
    @submit="authStore.authLogin"
  />
</template>
