<script setup lang="ts">
import type { SystemVersionLogApi } from '#/api';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { EmptyIcon } from '@vben/icons';

import { Card } from 'antdv-next';

import { getPublishedVersionLogs } from '#/api';
import { $t } from '#/locales';

import VersionLogTimeline from './components/version-log-timeline.vue';

const logs = ref<SystemVersionLogApi.VersionLog[]>([]);
const loading = ref(false);
const pageNum = ref(1);
const total = ref(0);
const pageSize = 8;

const hasMore = computed(() => logs.value.length < total.value);

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
        <div
          v-if="!loading && logs.length === 0"
          class="flex h-full flex-col items-center justify-center text-muted-foreground"
        >
          <EmptyIcon class="mx-auto" />
          <div class="mt-2 text-sm">{{ $t('common.noData') }}</div>
        </div>
        <VersionLogTimeline
          v-else
          :has-more="hasMore"
          :loading="loading"
          :logs="logs"
        />
      </div>
    </Card>
  </Page>
</template>
