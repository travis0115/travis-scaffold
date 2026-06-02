<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Card, Empty, Spin, Tag, Timeline, TimelineItem } from 'antdv-next';

import { getPublishedUpdateLogs } from '#/api/system/updateLog';
import { $t } from '#/locales';

interface UpdateLogItem {
  content?: string;
  id: string;
  publishTime?: string;
  title?: string;
  version?: string;
}

const loading = ref(false);
const logs = ref<UpdateLogItem[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    logs.value = await getPublishedUpdateLogs(50);
  } finally {
    loading.value = false;
  }
});
</script>
<template>
  <Page auto-content-height :title="$t('system.updateLog.viewUpdateLog')">
    <Spin :spinning="loading">
      <div v-if="logs.length === 0 && !loading" class="flex justify-center py-20">
        <Empty :description="$t('common.noData')" />
      </div>
      <Timeline v-else mode="left">
        <TimelineItem v-for="item in logs" :key="item.id">
          <Card class="mb-4 max-w-2xl">
            <template #title>
              <div class="flex items-center gap-2">
                <Tag color="blue">{{ item.version }}</Tag>
                <span>{{ item.title }}</span>
              </div>
            </template>
            <template #extra>
              <span class="text-sm text-gray-400">{{ item.publishTime }}</span>
            </template>
            <div class="whitespace-pre-wrap text-sm">{{ item.content }}</div>
          </Card>
        </TimelineItem>
      </Timeline>
    </Spin>
  </Page>
</template>
