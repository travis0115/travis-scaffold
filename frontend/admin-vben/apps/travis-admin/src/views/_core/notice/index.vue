<script setup lang="ts">
import type { SystemNoticeApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EmptyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import { Card, Spin, Tag } from 'antdv-next';

import { getNoticePage } from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { $t } from '#/locales';

const notices = ref<SystemNoticeApi.Notice[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const total = ref(0);
const pageSize = 8;
const pinnedTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

const hasMore = computed(() => notices.value.length < total.value);

async function fetchNotices() {
  if (loading.value) return;
  if (pageNum.value > 1 && !hasMore.value) return;
  loading.value = true;
  try {
    const page = await getNoticePage({
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
        <div v-else class="space-y-4">
          <article
            v-for="notice in notices"
            :key="notice.id"
            class="rounded-lg border border-border/60 bg-muted/20 p-5"
          >
            <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
              <Tag v-if="notice.isPinned === 1" :style="pinnedTagStyle">
                置顶
              </Tag>
              <h2 class="text-foreground text-lg font-semibold">
                {{ notice.title }}
              </h2>
              <span class="text-muted-foreground text-xs">
                {{ formatDate(notice.publishTime || notice.createTime) }}
              </span>
            </div>
            <div class="mt-4">
              <RichTextPreview :content="notice.content" :min-height="0" />
            </div>
          </article>
          <div v-if="loading" class="flex justify-center py-4">
            <Spin size="small" />
          </div>
          <div
            v-else-if="notices.length > 0 && !hasMore"
            class="py-3 text-center text-muted-foreground text-xs"
          >
            已加载全部系统公告
          </div>
        </div>
      </div>
    </Card>
  </Page>
</template>
