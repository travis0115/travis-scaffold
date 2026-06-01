<script setup lang="ts">
import type { SystemUpdateLogApi } from '#/api';

import { onMounted, ref } from 'vue';

import { Card, Empty, Spin, Tag } from 'antdv-next';

import { getPublishedUpdateLogs } from '#/api';

const loading = ref(false);
const logs = ref<SystemUpdateLogApi.UpdateLog[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    logs.value = await getPublishedUpdateLogs(20);
  } catch {
    // 错误由全局拦截器统一处理
  } finally {
    loading.value = false;
  }
});

/** 格式化日期 */
function formatDate(dateStr?: string) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
</script>

<template>
  <div class="update-log-container p-4">
    <Spin :spinning="loading">
      <Empty v-if="!loading && logs.length === 0" description="暂无更新日志" />
      <div v-else class="space-y-4">
        <Card
          v-for="log in logs"
          :key="log.id"
          size="small"
        >
          <template #title>
            <div class="flex items-center gap-2">
              <Tag color="blue">{{ log.version }}</Tag>
              <span>{{ log.title }}</span>
            </div>
          </template>
          <template #extra>
            <span class="text-gray-400 text-xs">
              {{ formatDate(log.publishTime || log.createTime) }}
            </span>
          </template>
          <div class="whitespace-pre-wrap text-gray-600 text-sm leading-relaxed">
            {{ log.content }}
          </div>
        </Card>
      </div>
    </Spin>
  </div>
</template>
