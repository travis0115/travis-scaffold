<script setup lang="ts">
import type { SystemVersionLogApi } from '#/api';

import { formatDate } from '@vben/utils';

import { Spin, Tag } from 'antdv-next';

import RichTextPreview from '#/components/rich-text-preview/index.vue';

const props = withDefaults(
  defineProps<{
    compact?: boolean;
    hasMore: boolean;
    loading: boolean;
    logs: SystemVersionLogApi.VersionLog[];
  }>(),
  {
    compact: false,
  },
);

const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}
</script>

<template>
  <div class="relative space-y-6">
    <article
      v-for="(log, index) in props.logs"
      :key="log.id"
      class="relative pl-10"
    >
      <div
        v-if="index < props.logs.length - 1"
        class="absolute left-[9px] top-3 h-[calc(100%+1.5rem)] w-px bg-primary/20"
      ></div>
      <div
        class="absolute left-0 top-0 flex size-5 items-center justify-center rounded-full border border-primary/25 bg-primary/10"
      >
        <span class="size-2 rounded-full bg-primary"></span>
      </div>

      <div class="text-muted-foreground text-xs">
        {{ formatDate(log.publishTime || log.createTime) }}
      </div>

      <div
        class="mt-3 rounded-lg border border-border/60 bg-card shadow-sm transition-colors hover:border-primary/30 hover:bg-muted/20"
        :class="props.compact ? 'p-4' : 'p-6'"
      >
        <div class="flex min-w-0 flex-wrap items-center gap-x-3 gap-y-1">
          <Tag :style="versionTagStyle">
            {{ formatVersion(log.version) }}
          </Tag>
          <component
            :is="props.compact ? 'h3' : 'h2'"
            class="text-foreground min-w-0 text-sm font-semibold"
          >
            {{ log.title }}
          </component>
        </div>
        <div :class="props.compact ? 'mt-3' : 'mt-4'">
          <RichTextPreview
            class="text-sm"
            :content="log.content"
            :min-height="0"
          />
        </div>
      </div>
    </article>

    <div v-if="props.loading" class="flex justify-center py-3">
      <Spin size="small" />
    </div>
    <div
      v-else-if="props.logs.length > 0 && !props.hasMore"
      class="py-3 text-center text-muted-foreground text-xs"
    >
      已加载全部更新日志
    </div>
  </div>
</template>
