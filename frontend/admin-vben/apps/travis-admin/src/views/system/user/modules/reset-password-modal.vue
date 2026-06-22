<script lang="ts" setup>
import { h, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { useClipboard } from '@vueuse/core';

import { App, Button } from 'antdv-next';

import { useVbenForm, z } from '#/adapter/form';
import { resetUserPassword } from '#/api';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const { copy } = useClipboard({ legacy: true });
const { message, modal: antdModal } = App.useApp();

const userId = ref<number>();
const nicknameVal = ref('');
const passwordVisible = ref(true);

function generateRandomPassword() {
  const groups = [
    'ABCDEFGHJKLMNPQRSTUVWXYZ',
    'abcdefghjkmnpqrstuvwxyz',
    '23456789',
    '~!@#',
  ];
  const chars = groups.join('');
  const password = groups.map((group) => pickChar(group));
  for (let i = password.length; i < 8; i++) {
    password.push(pickChar(chars));
  }
  return password.toSorted(() => Math.random() - 0.5).join('');
}

function pickChar(chars: string) {
  return chars.charAt(Math.floor(Math.random() * chars.length));
}

function isValidPassword(value?: string) {
  const pwd = value?.trim();
  if (!pwd) return false;
  if (pwd.length < 8 || pwd.length > 32) return false;

  let types = 0;
  if (/[a-z]/.test(pwd)) types++;
  if (/[A-Z]/.test(pwd)) types++;
  if (/\d/.test(pwd)) types++;
  if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(pwd)) types++;
  return types >= 3;
}

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  schema: [
    {
      component: 'InputPassword',
      componentProps: () => ({
        visibilityToggle: {
          visible: passwordVisible.value,
          onVisibleChange: (visible: boolean) => {
            passwordVisible.value = visible;
          },
        },
      }),
      fieldName: 'password',
      label: $t('system.user.newPassword'),
      renderComponentContent(_values, formApi) {
        return {
          addonAfter: () =>
            h(
              Button,
              {
                size: 'small',
                type: 'link',
                onClick: () => {
                  formApi.setFieldValue('password', generateRandomPassword());
                  passwordVisible.value = true;
                },
              },
              () => $t('system.user.generatePassword'),
            ),
        };
      },
      rules: z.string().refine(isValidPassword, {
        message: '密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种',
      }),
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (!userId.value) return;

    const { valid } = await formApi.validate();
    if (!valid) return;

    const { password } = await formApi.getValues();
    modalApi.lock();
    try {
      const result = await resetUserPassword(
        userId.value,
        password.trim(),
      );
      const pwd = typeof result === 'string' ? result : String(result ?? '');
      emit('success');
      modalApi.close();
      antdModal.success({
        content: h('div', { class: 'flex items-center gap-2' }, [
          h('span', $t('system.user.resetPasswordResult', { password: pwd })),
          h(
            Button,
            {
              size: 'small',
              type: 'link',
              onClick: async () => {
                await copy(pwd);
                message.success($t('ui.jsonViewer.copied'));
              },
            },
            () => $t('system.user.copyPassword'),
          ),
        ]),
        title: $t('system.user.resetPassword'),
      });
    } catch {
      modalApi.unlock();
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<{ id: number; nickname: string }>();
      userId.value = data?.id;
      nicknameVal.value = data?.nickname ?? '';
      formApi.resetForm();
      formApi.setFieldValue('password', generateRandomPassword(), false);
      passwordVisible.value = true;
    }
  },
});
</script>
<template>
  <Modal :title="$t('system.user.resetPasswordTitle', { name: nicknameVal })">
    <Form />
  </Modal>
</template>
