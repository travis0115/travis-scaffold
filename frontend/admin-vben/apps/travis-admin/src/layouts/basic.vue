<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationLoginExpiredModal } from '@vben/common-ui';
import { useWatermark } from '@vben/hooks';
import {
  BasicLayout,
  LockScreen,
  Notification,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { formatDateTime } from '@vben/utils';

import {
  clearMessages,
  deleteMessage,
  getRecentMessages,
  getUnreadMessageCount,
  markAllMessagesRead,
  markMessageRead,
} from '#/api';
import { $t } from '#/locales';
import { useAuthStore } from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';

const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
let notificationTimer: ReturnType<typeof setInterval> | undefined;

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { isDark } = usePreferences();
const showDot = computed(() => unreadCount.value > 0);

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    text: $t('page.auth.profile'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar && userStore.userInfo.avatar.trim() !== ''
    ? userStore.userInfo.avatar
    : preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

// 监听偏好设置清除缓存后的全局事件，作为组件 emit 链的兑底
function handleClearPreferencesLogout() {
  handleLogout();
}

onMounted(async () => {
  window.addEventListener(
    'vben:clear-preferences-and-logout',
    handleClearPreferencesLogout,
  );
  await loadNotifications();
  notificationTimer = setInterval(loadNotifications, 60_000);
});

onUnmounted(() => {
  window.removeEventListener(
    'vben:clear-preferences-and-logout',
    handleClearPreferencesLogout,
  );
  if (notificationTimer) clearInterval(notificationTimer);
});

async function loadNotifications() {
  const [messages, unread] = await Promise.all([
    getRecentMessages(),
    getUnreadMessageCount(),
  ]);
  notifications.value = messages.map((item) => ({
    id: item.id,
    avatar: preferences.app.defaultAvatar,
    date: formatDateTime(item.publishTime || item.createTime),
    isRead: item.readStatus === 1,
    link: '/message',
    message: item.content,
    title: item.title,
  }));
  unreadCount.value = unread.count;
}

async function handleNoticeClear() {
  await clearMessages();
  await loadNotifications();
}

async function markRead(id: number | string) {
  await markMessageRead(id);
  await loadNotifications();
}

async function remove(id: number | string) {
  await deleteMessage(id);
  await loadNotifications();
}

async function handleMakeAll() {
  await markAllMessagesRead();
  await loadNotifications();
}

const viewAll = () => router.push('/message');

const handleClick = async (item: NotificationItem) => {
  if (item.id && !item.isRead) await markRead(item.id);
  if (item.link) {
    navigateTo(item.link, item.query, item.state);
  }
};

function navigateTo(
  link: string,
  query?: Record<string, any>,
  state?: Record<string, any>,
) {
  if (link.startsWith('http://') || link.startsWith('https://')) {
    // 外部链接，在新标签页打开
    window.open(link, '_blank');
  } else {
    // 内部路由链接，支持 query 参数和 state
    router.push({
      path: link,
      query: query || {},
      state,
    });
  }
}

watch(
  () => ({
    enable: preferences.app.watermark,
    content: preferences.app.watermarkContent,
    isDark: isDark.value,
  }),
  async ({ enable, content, isDark: isDarkValue }) => {
    if (enable) {
      const watermarkColor = isDarkValue
        ? 'rgba(255, 255, 255, 0.12)'
        : 'rgba(0, 0, 0, 0.12)';

      await updateWatermark({
        advancedStyle: {
          colorStops: [
            {
              color: watermarkColor,
              offset: 0,
            },
            {
              color: watermarkColor,
              offset: 1,
            },
          ],
          type: 'linear',
        },
        content:
          content ||
          `${userStore.userInfo?.username} - ${userStore.userInfo?.nickname}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.nickname"
        :description="userStore.userInfo?.username"
        tag-text="Pro"
        @logout="handleLogout"
        @clear-preferences-and-logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNoticeClear"
        @read="(item) => item.id && markRead(item.id)"
        @remove="(item) => item.id && remove(item.id)"
        @make-all="handleMakeAll"
        @on-click="handleClick"
        @view-all="viewAll"
      />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
</template>
