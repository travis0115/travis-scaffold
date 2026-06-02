<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { App, InputPassword, message, RadioGroup } from 'antdv-next';

import { resetUserPassword } from '#/api';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const { modal: antdModal } = App.useApp();

const userId = ref<number>();
const nicknameVal = ref('');

const resetType = ref<'custom' | 'random'>('random');
const customPassword = ref('');
const resultPassword = ref('');

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (!userId.value) return;

    if (resetType.value === 'custom') {
      const pwd = customPassword.value.trim();
      if (!pwd) {
        message.warning($t('system.user.newPassword'));
        return;
      }
      if (pwd.length < 8 || pwd.length > 32) {
        message.warning('密码长度需为8-32位');
        return;
      }
      let types = 0;
      if (/[a-z]/.test(pwd)) types++;
      if (/[A-Z]/.test(pwd)) types++;
      if (/\d/.test(pwd)) types++;
      if (/[~!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(pwd)) types++;
      if (types < 3) {
        message.warning('密码需包含大写字母、小写字母、数字、特殊符号中的至少3种');
        return;
      }
    }
    modalApi.lock();
    try {
      const password =
        resetType.value === 'custom' ? customPassword.value : undefined;
      const result = await resetUserPassword(userId.value, password);
      const pwd = typeof result === 'string' ? result : String(result ?? '');
      resultPassword.value = pwd;
      emit('success');
      modalApi.close();
      antdModal.success({
        content: $t('system.user.resetPasswordResult', { password: pwd }),
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
      resetType.value = 'random';
      customPassword.value = '';
      resultPassword.value = '';
    }
  },
});
</script>
<template>
  <Modal :title="$t('system.user.resetPasswordTitle', { name: nicknameVal })">
    <div class="flex flex-col gap-4">
      <RadioGroup
        v-model:value="resetType"
        :options="[
          { label: $t('system.user.resetPasswordRandom'), value: 'random' },
          { label: $t('system.user.resetPasswordCustom'), value: 'custom' },
        ]"
        option-type="button"
        button-style="solid"
      />
      <div v-if="resetType === 'random'" class="text-muted-foreground text-sm">
        {{ $t('system.user.resetPasswordRandom') }}（8-32位，含大小写字母、数字和特殊符号中的至少3种）
      </div>
      <div v-else class="flex flex-col gap-1">
        <InputPassword
          v-model:value="customPassword"
          :placeholder="$t('system.user.newPassword')"         
        />
        <span class="text-muted-foreground text-xs">
          密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种
        </span>
      </div>
    </div>
  </Modal>
</template>
