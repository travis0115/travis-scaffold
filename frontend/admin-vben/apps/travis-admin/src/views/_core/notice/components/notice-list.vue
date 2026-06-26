<script setup lang="ts">
import type { SystemNoticeApi } from '#/api';

import { formatDate } from '@vben/utils';

import { Spin, Tag } from 'antdv-next';

const props = withDefaults(
  defineProps<{
    compact?: boolean;
    hasMore: boolean;
    loading: boolean;
    notices: SystemNoticeApi.Notice[];
  }>(),
  {
    compact: false,
  },
);

const emit = defineEmits<{
  preview: [notice: SystemNoticeApi.Notice];
}>();

const pinnedTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};
</script>

<template>
  <div class="space-y-2">
    <button
      v-for="notice in props.notices"
      :key="notice.id"
      class="group block w-full rounded-lg border border-border/60 bg-card text-left transition-colors hover:border-primary/30 hover:bg-muted/20"
      :class="props.compact ? 'p-4' : 'p-5'"
      type="button"
      @click="emit('preview', notice)"
    >
      <div class="flex min-w-0 items-start justify-between gap-4">
        <div class="min-w-0 flex-1">
          <div class="flex min-w-0 flex-wrap items-center gap-x-3 gap-y-1">
            <Tag
              v-if="notice.isPinned === 1"
              class="text-[10px] leading-4"
              :style="pinnedTagStyle"
            >
              置顶
            </Tag>
            <h3
              class="text-foreground group-hover:text-primary min-w-0 truncate text-sm transition-colors"
            >
              {{ notice.title }}
            </h3>
          </div>
        </div>
        <span class="shrink-0 pt-0.5 text-muted-foreground text-xs">
          {{ formatDate(notice.publishTime || notice.createTime) }}
        </span>
      </div>
    </button>

    <div v-if="props.loading" class="flex justify-center py-3">
      <Spin size="small" />
    </div>
    <div
      v-else-if="props.notices.length > 0 && !props.hasMore"
      class="py-3 text-center text-muted-foreground text-xs"
    >
      已加载全部系统公告
    </div>
  </div>
</template>
