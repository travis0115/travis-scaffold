<script setup lang="ts">
import type { SystemNoticeApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EmptyIcon } from '@vben/icons';

import { Card } from 'antdv-next';

import { getPublishedNoticePage } from '#/api';
import { $t } from '#/locales';

import NoticeList from './components/notice-list.vue';
import NoticePreviewModal from './components/notice-preview-modal.vue';

const notices = ref<SystemNoticeApi.Notice[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const total = ref(0);
const pageSize = 8;
const noticePreviewRef = ref<{
  open: (notice: SystemNoticeApi.Notice) => void;
}>();

const hasMore = computed(() => notices.value.length < total.value);

function onPreview(notice: SystemNoticeApi.Notice) {
  noticePreviewRef.value?.open(notice);
}

async function fetchNotices() {
  if (loading.value) return;
  if (pageNum.value > 1 && !hasMore.value) return;
  loading.value = true;
  try {
    const page = await getPublishedNoticePage({
      pageNum: pageNum.value,
      pageSize,
      status: 1,
    });
    notices.value =
      page.pageNum === 1 ? page.records : [...notices.value, ...page.records];
    total.value = page.total;
    pageNum.value = page.pageNum + 1;
  } finally {
    loading.value = false;
  }
}

function onScroll(event: Event) {
  const target = event.currentTarget as HTMLElement;
  const bottomDistance =
    target.scrollHeight - target.scrollTop - target.clientHeight;
  if (bottomDistance <= 48) {
    void fetchNotices();
  }
}

onMounted(() => {
  void fetchNotices();
});
</script>

<template>
  <Page>
    <NoticePreviewModal ref="noticePreviewRef" />
    <Card variant="borderless">
      <template #title>
        <div class="flex items-center gap-2">
          <span class="h-5 w-1.5 rounded-full bg-primary"></span>
          <span>系统公告</span>
        </div>
      </template>

      <div
        class="h-[calc(100vh-15rem)] overflow-y-auto pr-2"
        @scroll="onScroll"
      >
        <div
          v-if="!loading && notices.length === 0"
          class="flex h-full flex-col items-center justify-center text-muted-foreground"
        >
          <EmptyIcon class="mx-auto" />
          <div class="mt-2 text-sm">{{ $t('common.noData') }}</div>
        </div>
        <NoticeList
          v-else
          :has-more="hasMore"
          :loading="loading"
          :notices="notices"
          @preview="onPreview"
        />
      </div>
    </Card>
  </Page>
</template>
