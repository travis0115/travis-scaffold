<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationLoginExpiredModal, useVbenModal } from '@vben/common-ui';
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

import { Tag } from 'antdv-next';

import {
  createWebSocketTicketApi,
  getInboxMessageDetail,
  getRecentMessages,
  markAllMessagesRead,
  markMessageRead,
  SystemMessageApi,
} from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { $t } from '#/locales';
import { useAuthStore } from '#/store';
import { messageTypeOptions } from '#/utils/business-options';
import LoginForm from '#/views/_core/authentication/login.vue';

type MessageNotification = NotificationItem & {
  content?: string;
  messageId: number;
  messageType: number;
  metadata?: Record<string, any>;
};

const notifications = ref<MessageNotification[]>([]);
const notificationPopupRef = ref<{ close: () => void }>();
const previewNotification = ref<MessageNotification>();
const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};
let notificationSocket: undefined | WebSocket;
let notificationSocketReconnectTimer: ReturnType<typeof setTimeout> | undefined;
let notificationSocketClosedByClient = false;
let notificationSocketReconnectDelay = 5000;
const messageInboxChangedEvents = new Set<string>(
  Object.values(SystemMessageApi.WebSocketEvent),
);

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { isDark } = usePreferences();
const showDot = computed(() => notifications.value.length > 0);
const [PreviewModal, previewModalApi] = useVbenModal({
  closeOnClickModal: true,
  footer: false,
});

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

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

async function handleLogout() {
  await authStore.logout(false);
}

// 监听偏好设置清除缓存后的全局事件，作为组件 emit 链的兑底
function handleClearPreferencesLogout() {
  handleLogout();
}

function refreshNotifications() {
  void loadNotifications();
}

function handleMessageRead(event: Event) {
  const { messageId } = (event as CustomEvent<{ messageId: number }>).detail;
  notifications.value = notifications.value.filter(
    (item) => item.messageId !== messageId,
  );
}

function handleAllMessagesRead() {
  notifications.value = [];
}

onMounted(async () => {
  window.addEventListener(
    'vben:clear-preferences-and-logout',
    handleClearPreferencesLogout,
  );
  window.addEventListener(
    'travis:close-notification-socket',
    closeNotificationSocket,
  );
  window.addEventListener('travis:message-inbox-changed', refreshNotifications);
  window.addEventListener('travis:message-read', handleMessageRead);
  window.addEventListener('travis:message-all-read', handleAllMessagesRead);
  await loadNotifications();
  void connectNotificationSocket();
});

onUnmounted(() => {
  window.removeEventListener(
    'vben:clear-preferences-and-logout',
    handleClearPreferencesLogout,
  );
  window.removeEventListener(
    'travis:close-notification-socket',
    closeNotificationSocket,
  );
  window.removeEventListener(
    'travis:message-inbox-changed',
    refreshNotifications,
  );
  window.removeEventListener('travis:message-read', handleMessageRead);
  window.removeEventListener('travis:message-all-read', handleAllMessagesRead);
  closeNotificationSocket();
});

async function loadNotifications() {
  try {
    const messages = await getRecentMessages();
    notifications.value = messages.map((item) => ({
      id: item.messageId,
      avatar: preferences.app.defaultAvatar,
      date: formatDateTime(item.publishTime || item.createTime),
      isRead: false,
      link: '/message',
      message: '',
      messageId: item.messageId,
      messageType: item.messageType,
      title: item.title,
    }));
  } catch {
    notifications.value = [];
  }
}

async function buildNotificationSocketUrl() {
  const wsUrl = import.meta.env.VITE_GLOB_WS_URL;
  if (!wsUrl) return '';
  const ticket = await createWebSocketTicketApi();
  const url = new URL(wsUrl, window.location.origin);
  if (url.protocol === 'http:') {
    url.protocol = 'ws:';
  }
  if (url.protocol === 'https:') {
    url.protocol = 'wss:';
  }
  url.search = '';
  url.searchParams.set('ticket', ticket.ticket);
  return url.toString();
}

async function connectNotificationSocket() {
  if (!accessStore.accessToken || notificationSocket) return;
  let socketUrl = '';
  try {
    socketUrl = await buildNotificationSocketUrl();
  } catch {
    return;
  }
  if (
    notificationSocketClosedByClient ||
    !accessStore.accessToken ||
    notificationSocket
  ) {
    return;
  }
  if (!socketUrl) return;
  notificationSocketClosedByClient = false;
  notificationSocket = new WebSocket(socketUrl);
  notificationSocket.addEventListener('open', handleNotificationSocketOpen);
  notificationSocket.addEventListener(
    'message',
    handleNotificationSocketMessage,
  );
  notificationSocket.addEventListener('close', handleNotificationSocketClose);
  notificationSocket.addEventListener('error', handleNotificationSocketError);
}

function handleNotificationSocketOpen() {
  notificationSocketReconnectDelay = 5000;
}

function handleNotificationSocketMessage(event: MessageEvent) {
  try {
    const message = JSON.parse(event.data);
    if (message?.type === 'PING') {
      notificationSocket?.send('pong');
      return;
    }
    if (messageInboxChangedEvents.has(message?.content?.event)) {
      window.dispatchEvent(new CustomEvent('travis:message-inbox-changed'));
    }
  } catch {
    // 忽略非 JSON WebSocket 消息。
  }
}

function handleNotificationSocketClose(event: CloseEvent) {
  if (notificationSocket === event.currentTarget) {
    notificationSocket = undefined;
  }
  if (notificationSocketClosedByClient || !accessStore.accessToken) {
    return;
  }
  notificationSocketReconnectTimer = setTimeout(
    () => void connectNotificationSocket(),
    notificationSocketReconnectDelay,
  );
  notificationSocketReconnectDelay = Math.min(
    notificationSocketReconnectDelay * 2,
    30_000,
  );
}

function handleNotificationSocketError(event: Event) {
  if (event.currentTarget instanceof WebSocket) {
    event.currentTarget.close();
  }
}

function closeNotificationSocket() {
  notificationSocketClosedByClient = true;
  notificationSocketReconnectDelay = 5000;
  if (notificationSocketReconnectTimer) {
    clearTimeout(notificationSocketReconnectTimer);
    notificationSocketReconnectTimer = undefined;
  }
  if (notificationSocket) {
    notificationSocket.removeEventListener(
      'open',
      handleNotificationSocketOpen,
    );
    notificationSocket.removeEventListener(
      'message',
      handleNotificationSocketMessage,
    );
    notificationSocket.removeEventListener(
      'close',
      handleNotificationSocketClose,
    );
    notificationSocket.removeEventListener(
      'error',
      handleNotificationSocketError,
    );
    notificationSocket.close();
  }
  notificationSocket = undefined;
}

async function markRead(id: number | string) {
  await markMessageRead(id);
  const notification = notifications.value.find((item) => item.id === id);
  if (notification) {
    window.dispatchEvent(
      new CustomEvent('travis:message-read', {
        detail: { messageId: notification.messageId },
      }),
    );
  }
}

async function handleMakeAll() {
  await markAllMessagesRead();
  window.dispatchEvent(new CustomEvent('travis:message-all-read'));
}

const viewAll = () => {
  void router.push({ name: 'Message' });
};

const handleClick = async (item: NotificationItem) => {
  notificationPopupRef.value?.close();
  const notification = item as MessageNotification;
  const detail = await getInboxMessageDetail(notification.messageId);
  window.dispatchEvent(
    new CustomEvent('travis:message-read', {
      detail: { messageId: notification.messageId },
    }),
  );
  previewNotification.value = {
    ...notification,
    ...detail,
    date: formatDateTime(detail.publishTime || detail.createTime),
  };
  previewModalApi.open();
};

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
    if (token) void connectNotificationSocket();
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
        ref="notificationPopupRef"
        :dot="showDot"
        :notifications="notifications"
        :show-clear="false"
        :show-item-dot="false"
        @read="(item) => item.id && markRead(item.id)"
        @make-all="handleMakeAll"
        @on-click="handleClick"
        @view-all="viewAll"
      >
        <template #empty>
          <span class="text-sm">暂无未读消息</span>
        </template>
        <template #content="{ item }">
          <div class="min-w-0 w-full">
            <p class="line-clamp-2 text-sm font-normal leading-5">
              {{ item.title }}
            </p>
            <div class="mt-2 flex items-center justify-between gap-3">
              <Tag
                :color="
                  messageTypeOptions.find(
                    (option) => option.value === item.messageType,
                  )?.color
                "
              >
                {{
                  messageTypeOptions.find(
                    (option) => option.value === item.messageType,
                  )?.label ?? item.messageType
                }}
              </Tag>
              <span class="shrink-0 text-xs text-muted-foreground">
                {{ item.date }}
              </span>
            </div>
          </div>
        </template>
      </Notification>
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
  <PreviewModal
    class="w-[860px]"
    :fullscreen-button="false"
    :title="previewNotification?.title || '消息预览'"
  >
    <template #title>
      <div class="flex min-w-0 items-start gap-3">
        <span class="h-5 w-1.5 shrink-0 rounded-full bg-primary"></span>
        <div class="min-w-0 space-y-2">
          <div class="flex min-w-0 items-center gap-3">
            <span class="min-w-0 truncate">
              {{ previewNotification?.title || '消息预览' }}
            </span>
            <Tag
              v-if="previewNotification?.metadata?.version"
              :style="versionTagStyle"
            >
              {{ formatVersion(previewNotification.metadata.version) }}
            </Tag>
          </div>
          <div class="text-muted-foreground text-xs font-normal">
            {{ previewNotification?.date }}
          </div>
        </div>
      </div>
    </template>
    <RichTextPreview
      :content="previewNotification?.content"
      :min-height="320"
    />
  </PreviewModal>
</template>
