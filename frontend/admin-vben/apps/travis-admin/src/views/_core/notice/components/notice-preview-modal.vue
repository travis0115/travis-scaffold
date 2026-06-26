<script setup lang="ts">
import type { SystemNoticeApi } from '#/api';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import { Tag } from 'antdv-next';

import RichTextPreview from '#/components/rich-text-preview/index.vue';

const notice = ref<SystemNoticeApi.Notice>();
const pinnedTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

const [PreviewModal, previewModalApi] = useVbenModal({
  closeOnClickModal: true,
  footer: false,
});

function open(row: SystemNoticeApi.Notice) {
  notice.value = row;
  previewModalApi.open();
}

defineExpose({ open });
</script>

<template>
  <PreviewModal
    class="w-[860px]"
    :fullscreen-button="false"
    :title="notice?.title || '公告详情'"
  >
    <template #title>
      <div class="flex min-w-0 items-start gap-3">
        <span class="h-5 w-1.5 shrink-0 rounded-full bg-primary"></span>
        <div class="min-w-0 space-y-2">
          <div class="flex min-w-0 items-center gap-3">
            <span class="min-w-0 truncate">
              {{ notice?.title || '公告详情' }}
            </span>
            <Tag
              v-if="notice?.isPinned === 1"
              class="shrink-0 text-[10px] leading-4"
              :style="pinnedTagStyle"
            >
              置顶
            </Tag>
          </div>
          <div class="text-muted-foreground text-xs font-normal">
            {{ formatDate(notice?.publishTime || notice?.createTime) }}
          </div>
        </div>
      </div>
    </template>
    <RichTextPreview :content="notice?.content" :min-height="320" />
  </PreviewModal>
</template>
