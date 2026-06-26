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
  deleteInboxMessage,
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
let notificationSocket: WebSocket | undefined;
let notificationSocketReconnectTimer: ReturnType<typeof setTimeout> | undefined;
let notificationSocketClosedByClient = false;
let notificationSocketConnectedOnce = false;
let notificationSocketReconnectDelay = 5000;

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
    icon: 'mdi:account-circle-outline',
    text: $t('page.auth.profile'),
  },
  {
    handler: () => {
      router.push({ name: 'Version' });
    },
    icon: 'mdi:sticker-text-outline',
    text: '更新日志',
  },
  {
    handler: () => {
      router.push({ name: 'Notice' });
    },
    icon: 'mdi:bullhorn-variant-outline',
    text: '系统公告',
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
  connectNotificationSocket();
});

onUnmounted(() => {
  window.removeEventListener(
    'vben:clear-preferences-and-logout',
    handleClearPreferencesLogout,
  );
  if (notificationTimer) clearInterval(notificationTimer);
  closeNotificationSocket();
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

function buildNotificationSocketUrl() {
  const wsUrl = import.meta.env.VITE_GLOB_WS_URL;
  if (!wsUrl) return '';
  const url = new URL(wsUrl, window.location.origin);
  url.search = '';
  url.searchParams.set('loginType', 'admin');
  url.searchParams.set('token', accessStore.accessToken || '');
  return url.toString();
}

function connectNotificationSocket() {
  if (!accessStore.accessToken || notificationSocket) return;
  const socketUrl = buildNotificationSocketUrl();
  if (!socketUrl) return;
  notificationSocketClosedByClient = false;
  notificationSocket = new WebSocket(socketUrl);
  notificationSocket.onopen = () => {
    notificationSocketConnectedOnce = true;
    notificationSocketReconnectDelay = 5000;
  };
  notificationSocket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data);
      if (message?.content?.event === 'SYSTEM_MESSAGE_PUBLISHED') {
        void loadNotifications();
      }
    } catch {
      // 忽略非 JSON WebSocket 消息，保留轮询兜底。
    }
  };
  notificationSocket.onclose = () => {
    notificationSocket = undefined;
    if (
      notificationSocketClosedByClient ||
      !accessStore.accessToken ||
      !notificationSocketConnectedOnce
    ) {
      return;
    }
    notificationSocketReconnectTimer = setTimeout(
      connectNotificationSocket,
      notificationSocketReconnectDelay,
    );
    notificationSocketReconnectDelay = Math.min(
      notificationSocketReconnectDelay * 2,
      30_000,
    );
  };
  notificationSocket.onerror = () => {
    notificationSocket?.close();
  };
}

function closeNotificationSocket() {
  notificationSocketClosedByClient = true;
  notificationSocketConnectedOnce = false;
  notificationSocketReconnectDelay = 5000;
  if (notificationSocketReconnectTimer) {
    clearTimeout(notificationSocketReconnectTimer);
    notificationSocketReconnectTimer = undefined;
  }
  if (notificationSocket) {
    notificationSocket.onclose = null;
    notificationSocket.onmessage = null;
    notificationSocket.close();
  }
  notificationSocket = undefined;
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
  await deleteInboxMessage(id);
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

watch(
  () => accessStore.accessToken,
  (token) => {
    closeNotificationSocket();
    if (token) connectNotificationSocket();
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
