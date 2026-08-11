<script setup lang="ts">
import { ref } from 'vue';

import {
  Profile,
  useVbenModal,
  VbenAvatar,
  VbenButton,
  VCropper,
} from '@vben/common-ui';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

import { message } from 'antdv-next';

import {
  updateAvatarApi,
  UPLOAD_FILE_MAX_SIZE_MB,
  UPLOAD_FILE_MAX_SIZE_TEXT,
  uploadAvatarApi,
} from '#/api';

import ProfileBase from './base-setting.vue';
import ProfileLoginLog from './login-log.vue';
import ProfilePasswordSetting from './password-setting.vue';

const userStore = useUserStore();

const tabsValue = ref<string>('basic');
const cropperRef = ref<InstanceType<typeof VCropper> | null>(null);
const selectedFileUrl = ref('');
const selectedFile = ref<File | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const previewUrl = ref('');
let previewTimer: null | ReturnType<typeof setTimeout> = null;

const tabs = ref([
  {
    label: '账号资料',
    value: 'basic',
  },
  {
    label: '修改密码',
    value: 'password',
  },
  {
    label: '登录日志',
    value: 'loginLog',
  },
]);

const avatarUrl = ref(
  userStore.userInfo?.avatar && userStore.userInfo.avatar.trim() !== ''
    ? userStore.userInfo.avatar
    : preferences.app.defaultAvatar,
);

const [AvatarModal, avatarModalApi] = useVbenModal({
  centered: true,
  class: 'w-[640px]',
  closeOnClickModal: false,
  closeOnPressEscape: false,
  confirmText: '确定',
  cancelText: '取消',
  async onConfirm() {
    await onConfirm();
  },
  onOpenChange(open) {
    if (!open) onModalCancel();
  },
});

/** 打开修改头像弹窗 */
function openAvatarModal() {
  avatarModalApi.open();
}

/** 触发文件选择 */
function triggerFileInput() {
  fileInputRef.value?.click();
}

/** 选择图片文件 */
function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    message.error('只能上传图片文件');
    input.value = '';
    return;
  }
  // 校验文件大小
  if (file.size / (1024 * 1024) > UPLOAD_FILE_MAX_SIZE_MB) {
    message.error(`图片大小不能超过 ${UPLOAD_FILE_MAX_SIZE_TEXT}MB`);
    input.value = '';
    return;
  }

  // 清理旧文件
  if (selectedFileUrl.value) {
    URL.revokeObjectURL(selectedFileUrl.value);
  }
  selectedFile.value = file;
  selectedFileUrl.value = URL.createObjectURL(file);
  previewUrl.value = '';
  // 重置 input，允许重复选择同一文件
  input.value = '';
}

/** 裁剪区域变化时更新预览 */
function onCropChange() {
  if (previewTimer) clearTimeout(previewTimer);
  previewTimer = setTimeout(async () => {
    const cropper = cropperRef.value;
    if (!cropper) return;
    try {
      const dataUrl = await cropper.getCropImage('image/png', 1, 'base64');
      if (dataUrl && typeof dataUrl === 'string') {
        previewUrl.value = dataUrl;
      }
    } catch {
      // 忽略预览更新失败
    }
  }, 80);
}

function clearAvatarSelection() {
  if (selectedFileUrl.value) {
    URL.revokeObjectURL(selectedFileUrl.value);
  }
  selectedFileUrl.value = '';
  selectedFile.value = null;
  previewUrl.value = '';
  if (previewTimer) {
    clearTimeout(previewTimer);
    previewTimer = null;
  }
}

/** 确定：裁剪并上传 */
async function onConfirm() {
  if (!selectedFileUrl.value) {
    message.warning('请先上传图片');
    return;
  }

  const cropper = cropperRef.value;
  if (!cropper) {
    message.error('裁剪失败');
    return;
  }

  try {
    const dataUrl = await cropper.getCropImage('image/jpeg', 0.8, 'base64');
    if (!dataUrl || typeof dataUrl !== 'string') {
      message.error('裁剪失败');
      return;
    }

    // Base64 转 File
    const arr = dataUrl.split(',');
    const mime = arr[0]?.match(/:(.*?);/)?.[1] || 'image/jpeg';
    const bstr = atob(arr[1] ?? '');
    const n = bstr.length;
    const u8arr = new Uint8Array(n);
    for (let i = 0; i < n; i++) {
      u8arr[i] = bstr.codePointAt(i) ?? 0;
    }
    const uploadFile = new File([u8arr], 'avatar.jpg', { type: mime });

    avatarModalApi.lock();
    const result = await uploadAvatarApi(uploadFile);
    await updateAvatarApi(result.id);
    avatarUrl.value = result.url;
    if (userStore.userInfo) {
      userStore.setUserInfo({ ...userStore.userInfo, avatar: result.url });
    }
    message.success('头像更新成功');
    avatarModalApi.close();
    clearAvatarSelection();
  } catch {
    avatarModalApi.unlock();
    clearAvatarSelection();
    // 错误由全局拦截器统一处理
  } finally {
    if (previewTimer) {
      clearTimeout(previewTimer);
      previewTimer = null;
    }
  }
}

/** 关闭弹窗 */
function onModalCancel() {
  clearAvatarSelection();
}
</script>
<template>
  <div>
    <Profile
      v-model:model-value="tabsValue"
      title="个人中心"
      :user-info="userStore.userInfo"
      :tabs="tabs"
    >
      <template #avatar>
        <div
          class="group relative size-20 cursor-pointer overflow-hidden rounded-full"
          @click="openAvatarModal"
        >
          <VbenAvatar
            :fallback-src="preferences.app.defaultAvatar"
            :src="avatarUrl"
            alt="头像"
            class="size-20"
          />
          <div
            class="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
          >
            <span class="text-xs text-white">更换头像</span>
          </div>
        </div>
      </template>
      <template #content>
        <ProfileBase v-if="tabsValue === 'basic'" />
        <ProfilePasswordSetting v-if="tabsValue === 'password'" />
        <ProfileLoginLog v-if="tabsValue === 'loginLog'" />
      </template>
    </Profile>

    <!-- 修改头像弹窗 -->
    <AvatarModal title="修改头像">
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="hidden"
        @change="onFileChange"
      />

      <div class="flex gap-6 px-2 pt-2">
        <!-- 左：裁剪区域 -->
        <div class="flex-shrink-0">
          <div v-if="selectedFileUrl">
            <VCropper
              ref="cropperRef"
              :img="selectedFileUrl"
              aspect-ratio="1:1"
              :width="300"
              :height="300"
              @cropchange="onCropChange"
            />
          </div>
          <div
            v-else
            class="flex size-[300px] cursor-pointer items-center justify-center rounded border-2 border-dashed border-gray-300 bg-gray-50 transition-colors hover:border-blue-400 hover:bg-blue-50"
            @click="triggerFileInput"
          >
            <div class="flex flex-col items-center gap-2 text-gray-400">
              <span class="text-sm">点击上传图片</span>
            </div>
          </div>
        </div>

        <!-- 右：预览头像 -->
        <div class="flex flex-1 flex-col items-center justify-center gap-3">
          <span class="text-sm text-gray-500">预览头像</span>
          <img
            :src="previewUrl || selectedFileUrl || avatarUrl"
            alt="头像预览"
            class="size-[120px] rounded-full object-cover ring-2 ring-gray-200"
          />
        </div>
      </div>

      <!-- 底部：重新上传图片按钮（选择图片后显示） -->
      <div v-if="selectedFileUrl" class="mt-4 px-2">
        <VbenButton variant="secondary" @click="triggerFileInput">
          重新上传图片
        </VbenButton>
      </div>
    </AvatarModal>
  </div>
</template>
