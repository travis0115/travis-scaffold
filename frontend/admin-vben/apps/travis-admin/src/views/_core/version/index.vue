<script setup lang="ts">
import type { SystemVersionLogApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import { Card, Empty, Spin, Tag } from 'antdv-next';

import { getPublishedVersionLogs } from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';

const logs = ref<SystemVersionLogApi.VersionLog[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const total = ref(0);
const pageSize = 8;
const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

const hasMore = computed(() => logs.value.length < total.value);

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

async function fetchLogs() {
  if (loading.value) return;
  if (pageNum.value > 1 && !hasMore.value) return;
  loading.value = true;
  try {
    const page = await getPublishedVersionLogs({
      pageNum: pageNum.value,
      pageSize,
    });
    logs.value =
      page.pageNum === 1 ? page.records : [...logs.value, ...page.records];
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
    void fetchLogs();
  }
}

onMounted(() => {
  void fetchLogs();
});
</script>

<template>
  <Page>
    <Card variant="borderless">
      <template #title>
        <div class="flex items-center gap-2">
          <span class="h-5 w-1.5 rounded-full bg-primary"></span>
          <span>更新日志</span>
        </div>
      </template>

      <div
        class="h-[calc(100vh-15rem)] overflow-y-auto pr-2"
        @scroll="onScroll"
      >
        <Empty
          v-if="!loading && logs.length === 0"
          description="暂无更新日志"
        />
        <div v-else class="space-y-4">
          <article
            v-for="log in logs"
            :key="log.id"
            class="rounded-lg border border-border/60 bg-muted/20 p-5"
          >
            <div class="flex items-center gap-3 text-muted-foreground text-xs">
              <Tag :style="versionTagStyle">
                {{ formatVersion(log.version) }}
              </Tag>
              <span>{{ formatDate(log.publishTime || log.createTime) }}</span>
            </div>
            <h2 class="mt-4 text-foreground text-lg font-semibold">
              {{ log.title }}
            </h2>
            <div class="mt-4">
              <RichTextPreview :content="log.content" :min-height="0" />
            </div>
          </article>
          <div v-if="loading" class="flex justify-center py-4">
            <Spin size="small" />
          </div>
          <div
            v-else-if="logs.length > 0 && !hasMore"
            class="py-3 text-center text-muted-foreground text-xs"
          >
            已加载全部更新日志
          </div>
        </div>
      </div>
    </Card>
  </Page>
</template>
