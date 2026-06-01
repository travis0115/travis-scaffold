<script setup lang="ts">
import { ref } from 'vue';

import { VCropper } from '@vben/common-ui';
import { Profile } from '@vben/common-ui';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

import { message, Modal } from 'antdv-next';

import { uploadFileApi } from '#/api';
import { updateProfileApi } from '#/api';

import ProfileBase from './base-setting.vue';
import ProfilePasswordSetting from './password-setting.vue';

const userStore = useUserStore();

const tabsValue = ref<string>('basic');
const uploading = ref(false);
const cropperOpen = ref(false);
const cropperRef = ref<InstanceType<typeof VCropper> | null>(null);
const selectedFileUrl = ref('');
const selectedFile = ref<File | null>(null);

const tabs = ref([
  {
    label: '基本设置',
    value: 'basic',
  },
  {
    label: '修改密码',
    value: 'password',
  },
]);

const avatarUrl = ref(
  userStore.userInfo?.avatar && userStore.userInfo.avatar.trim() !== ''
    ? userStore.userInfo.avatar
    : preferences.app.defaultAvatar,
);

/** 选择图片文件 */
function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    message.error('只能上传图片文件');
    return;
  }
  // 校验文件大小
  if (file.size / 1024 / 1024 > 5) {
    message.error('图片大小不能超过 5MB');
    return;
  }

  selectedFile.value = file;
  selectedFileUrl.value = URL.createObjectURL(file);
  cropperOpen.value = true;
  // 重置 input，允许重复选择同一文件
  input.value = '';
}

/** 确认裁剪并上传 */
async function onCropConfirm() {
  const cropper = cropperRef.value;
  if (!cropper) return;

  try {
    const dataUrl = await cropper.getCropImage();
    if (!dataUrl || typeof dataUrl !== 'string') {
      message.error('裁剪失败');
      return;
    }

    // Base64 转 File
    const arr = dataUrl.split(',');
    const mime = arr[0]!.match(/:(.*?);/)?.[1] || 'image/png';
    const bstr = atob(arr[1]!);
    const n = bstr.length;
    const u8arr = new Uint8Array(n);
    for (let i = 0; i < n; i++) {
      u8arr[i] = bstr.charCodeAt(i);
    }
    const uploadFile = new File([u8arr], 'avatar.png', { type: mime });

    uploading.value = true;
    const url = await uploadFileApi(uploadFile);
    await updateProfileApi({ avatar: url });
    avatarUrl.value = url;
    if (userStore.userInfo) {
      userStore.setUserInfo({ ...userStore.userInfo, avatar: url });
    }
    message.success('头像更新成功');
  } catch {
    // 错误由全局拦截器统一处理
  } finally {
    uploading.value = false;
    cropperOpen.value = false;
    if (selectedFileUrl.value) {
      URL.revokeObjectURL(selectedFileUrl.value);
    }
  }
}

/** 取消裁剪 */
function onCropCancel() {
  cropperOpen.value = false;
  if (selectedFileUrl.value) {
    URL.revokeObjectURL(selectedFileUrl.value);
  }
}
</script>
<template>
  <Profile
    v-model:model-value="tabsValue"
    title="个人中心"
    :user-info="userStore.userInfo"
    :tabs="tabs"
  >
    <template #avatar>
      <label class="group relative size-20 cursor-pointer overflow-hidden rounded-full">
        <input
          type="file"
          accept="image/*"
          class="hidden"
          @change="onFileChange"
        />
        <img
          :src="avatarUrl"
          alt="头像"
          class="size-full rounded-full object-cover"
        />
        <div
          class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
        >
          <span class="text-xs text-white">
            {{ uploading ? '上传中...' : '更换头像' }}
          </span>
        </div>
      </label>
    </template>
    <template #content>
      <ProfileBase v-if="tabsValue === 'basic'" />
      <ProfilePasswordSetting v-if="tabsValue === 'password'" />
      <ProfileUpdateLog v-if="tabsValue === 'updateLog'" />
    </template>
  </Profile>

  <!-- 图片裁剪弹窗 -->
  <Modal
    v-model:open="cropperOpen"
    title="裁剪头像"
    centered
    :width="548"
    :keyboard="false"
    :mask-closable="false"
    ok-text="确认裁剪"
    cancel-text="取消"
    :confirm-loading="uploading"
    @ok="onCropConfirm"
    @cancel="onCropCancel"
  >
    <div v-if="selectedFileUrl" class="flex items-center justify-center">
      <VCropper
        ref="cropperRef"
        :img="selectedFileUrl"
        aspect-ratio="1:1"
      />
    </div>
  </Modal>
</template>
      />
    </div>
  </Modal>
</template>
</template>
