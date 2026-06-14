<script lang="ts" setup>
import type { OpsJobApi, SystemNoticeApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { IconifyIcon } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import { Button, Card, Empty, Skeleton } from 'antdv-next';

import { getJobDashboard, getRecentMessages } from '#/api';

const router = useRouter();
const { hasAccessByCodes } = useAccess();
const canViewJobDashboard = hasAccessByCodes(['ops:job:view']);

const dashboard = ref<OpsJobApi.Dashboard>();
const messages = ref<SystemNoticeApi.UserMessage[]>([]);
const loading = ref(true);

const metricItems = computed(() => [
  {
    colorClass: 'bg-blue-500/10 text-blue-500',
    icon: 'lucide:list-todo',
    label: '任务总数',
    value: dashboard.value?.totalJobs ?? 0,
  },
  {
    colorClass: 'bg-emerald-500/10 text-emerald-500',
    icon: 'lucide:circle-play',
    label: '启用任务',
    value: dashboard.value?.enabledJobs ?? 0,
  },
  {
    colorClass: 'bg-amber-500/10 text-amber-500',
    icon: 'lucide:circle-pause',
    label: '暂停任务',
    value: dashboard.value?.pausedJobs ?? 0,
  },
  {
    colorClass: 'bg-cyan-500/10 text-cyan-500',
    icon: 'lucide:activity',
    label: '执行次数',
    value: dashboard.value?.executions ?? 0,
  },
  {
    colorClass: 'bg-rose-500/10 text-rose-500',
    icon: 'lucide:circle-x',
    label: '失败次数',
    value: dashboard.value?.failedExecutions ?? 0,
  },
  {
    colorClass: 'bg-teal-500/10 text-teal-500',
    icon: 'lucide:circle-check-big',
    label: '成功率',
    suffix: '%',
    value: dashboard.value?.successRate ?? 0,
  },
]);

onMounted(async () => {
  const requests: Promise<unknown>[] = [
    getRecentMessages(4).then((data) => {
      messages.value = data;
    }),
  ];

  if (canViewJobDashboard) {
    requests.push(
      getJobDashboard().then((data) => {
        dashboard.value = data;
      }),
    );
  }

  await Promise.allSettled(requests);
  loading.value = false;
});
</script>

<template>
  <div class="p-5">
    <Skeleton v-if="loading" active :paragraph="{ rows: 8 }" />

    <div v-else class="flex flex-col gap-5">
      <Card v-if="canViewJobDashboard" :bordered="false" title="任务调度">
        <template #extra>
          <div class="flex gap-2">
            <Button type="link" @click="router.push('/ops/job/list')">
              任务管理
            </Button>
            <Button type="link" @click="router.push('/ops/job/log')">
              执行日志
            </Button>
          </div>
        </template>

        <div
          class="grid gap-px overflow-hidden rounded-lg border border-border bg-border sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6"
        >
          <div
            v-for="item in metricItems"
            :key="item.label"
            class="group flex items-center gap-4 bg-card p-5 transition-colors hover:bg-muted"
          >
            <div
              class="flex size-12 shrink-0 items-center justify-center rounded-full"
              :class="item.colorClass"
            >
              <IconifyIcon :icon="item.icon" class="size-6" />
            </div>
            <div class="min-w-0">
              <div class="text-sm text-muted-foreground">{{ item.label }}</div>
              <div class="mt-1 text-2xl font-semibold tracking-tight">
                {{ item.value.toLocaleString() }}{{ item.suffix }}
              </div>
            </div>
          </div>
        </div>
      </Card>

      <Card :bordered="false">
        <template #title>
          <div class="flex items-center gap-2">
            <IconifyIcon class="size-5 text-blue-500" icon="lucide:bell" />
            <span>最近通知</span>
          </div>
        </template>
        <template #extra>
          <Button type="link" @click="router.push('/message')">
            查看全部
          </Button>
        </template>

        <Empty v-if="messages.length === 0" description="暂无通知" />
        <div v-else class="space-y-3">
          <button
            v-for="item in messages"
            :key="item.id"
            class="flex w-full items-start gap-3 rounded-lg border border-border/60 bg-muted/30 p-3 text-left transition-colors hover:bg-muted/70"
            type="button"
            @click="router.push('/message')"
          >
            <span
              class="mt-1 size-2 shrink-0 rounded-full"
              :class="item.readStatus === 0 ? 'bg-blue-500' : 'bg-slate-300'"
            ></span>
            <span class="min-w-0 flex-1">
              <span class="flex items-center justify-between gap-3">
                <strong class="truncate text-sm font-medium">
                  {{ item.title }}
                </strong>
                <span class="shrink-0 text-xs text-muted-foreground">
                  {{ formatDateTime(item.publishTime || item.createTime) }}
                </span>
              </span>
              <span class="mt-1 block truncate text-sm text-muted-foreground">
                {{ item.content }}
              </span>
            </span>
          </button>
        </div>
      </Card>
    </div>
  </div>
</template>
