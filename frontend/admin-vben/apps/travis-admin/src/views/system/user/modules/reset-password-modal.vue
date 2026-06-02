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

    if (resetType.value === 'custom' && !customPassword.value.trim()) {
      message.warning($t('system.user.newPassword'));
      return;
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
        {{ $t('system.user.resetPasswordRandom') }}（8位，含字母和数字）
      </div>
      <div v-else>
        <InputPassword
          v-model:value="customPassword"
          :placeholder="$t('system.user.newPassword')"
        />
      </div>
    </div>
  </Modal>
</template>
