<script lang="ts" setup>
import { computed } from 'vue';

import { useRequestLoadingStore } from '@vben/stores';

import { VbenLoading } from '@vben-core/shadcn-ui';

import { useContentSpinner } from './use-content-spinner';

defineOptions({ name: 'LayoutContentSpinner' });

const { spinning: routeSpinning } = useContentSpinner();
const requestLoadingStore = useRequestLoadingStore();

// 路由切换 loading 或 HTTP 请求 loading 任一生效时显示
const spinning = computed(
  () => routeSpinning.value || requestLoadingStore.spinning,
);
</script>
<template>
  <VbenLoading :spinning="spinning" />
</template>
