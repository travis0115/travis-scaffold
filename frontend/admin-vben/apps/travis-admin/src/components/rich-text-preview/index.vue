<script setup lang="ts">
import type { TipTapPreviewProps } from '@vben/plugins/tiptap';

import { ref } from 'vue';

import { VbenTiptapPreview } from '@vben/plugins/tiptap';

import { Image } from 'antdv-next';

defineProps<TipTapPreviewProps>();

const previewImageOpen = ref(false);
const previewImageSrc = ref('');

function onPreviewClick(event: MouseEvent) {
  const target = event.target;
  if (!(target instanceof HTMLImageElement) || !target.currentSrc) {
    return;
  }

  previewImageSrc.value = target.currentSrc;
  previewImageOpen.value = true;
}

function onPreviewOpenChange(open: boolean) {
  previewImageOpen.value = open;
}
</script>

<template>
  <VbenTiptapPreview v-bind="$props" @click="onPreviewClick" />
  <Image
    :preview="{
      src: previewImageSrc,
      open: previewImageOpen,
      onOpenChange: onPreviewOpenChange,
    }"
    :src="previewImageSrc"
    class="hidden"
  />
</template>
