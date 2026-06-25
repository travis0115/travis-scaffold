<script lang="ts" setup>
import type { OpsJobApi, SystemNoticeApi, SystemVersionLogApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import { Button, Card, Empty, Skeleton, Spin, Tag } from 'antdv-next';

import { getJobDashboard, getNoticePage, getPublishedVersionLogs } from '#/api';
import RichTextPreview from '#/components/rich-text-preview/index.vue';
import { OPS_PERMS } from '#/utils/permissions';

const router = useRouter();
const { hasAccessByCodes } = useAccess();
const canViewJobDashboard = hasAccessByCodes([OPS_PERMS.jobQuery]);

const dashboard = ref<OpsJobApi.Dashboard>();
const notices = ref<SystemNoticeApi.Notice[]>([]);
const noticeTotal = ref(0);
const noticePageNum = ref(1);
const versionLogs = ref<SystemVersionLogApi.VersionLog[]>([]);
const versionTotal = ref(0);
const versionPageNum = ref(1);
const loading = ref(true);
const noticeLoading = ref(false);
const versionLoading = ref(false);
const homePageSize = 10;
const versionTagStyle = {
  backgroundColor: 'hsl(var(--primary) / 10%)',
  borderColor: 'hsl(var(--primary) / 20%)',
  color: 'hsl(var(--primary))',
};

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

const noticeHasMore = computed(() => notices.value.length < noticeTotal.value);
const versionHasMore = computed(
  () => versionLogs.value.length < versionTotal.value,
);

function formatVersion(value?: null | string) {
  if (!value) return '-';
  return value.toLowerCase().startsWith('v') ? value : `v${value}`;
}

async function fetchVersionLogs() {
  if (versionLoading.value) return;
  if (versionPageNum.value > 1 && !versionHasMore.value) return;
  versionLoading.value = true;
  try {
    const page = await getPublishedVersionLogs({
      pageNum: versionPageNum.value,
      pageSize: homePageSize,
    });
    versionLogs.value =
      page.pageNum === 1
        ? page.records
        : [...versionLogs.value, ...page.records];
    versionTotal.value = page.total;
    versionPageNum.value = page.pageNum + 1;
  } finally {
    versionLoading.value = false;
  }
}

async function fetchNotices() {
  if (noticeLoading.value) return;
  if (noticePageNum.value > 1 && !noticeHasMore.value) return;
  noticeLoading.value = true;
  try {
    const page = await getNoticePage({
      pageNum: noticePageNum.value,
      pageSize: homePageSize,
      status: 1,
    });
    notices.value =
      page.pageNum === 1 ? page.records : [...notices.value, ...page.records];
    noticeTotal.value = page.total;
    noticePageNum.value = page.pageNum + 1;
  } finally {
    noticeLoading.value = false;
  }
}

function onNoticeScroll(event: Event) {
  const target = event.currentTarget as HTMLElement;
  const bottomDistance =
    target.scrollHeight - target.scrollTop - target.clientHeight;
  if (bottomDistance <= 48) {
    void fetchNotices();
  }
}

function onVersionScroll(event: Event) {
  const target = event.currentTarget as HTMLElement;
  const bottomDistance =
    target.scrollHeight - target.scrollTop - target.clientHeight;
  if (bottomDistance <= 48) {
    void fetchVersionLogs();
  }
}

onMounted(async () => {
  const requests: Promise<unknown>[] = [fetchNotices(), fetchVersionLogs()];

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
      <Card v-if="canViewJobDashboard" variant="borderless">
        <template #title>
          <div class="flex items-center gap-2">
            <span class="h-5 w-1.5 rounded-full bg-primary"></span>
            <span>任务调度</span>
          </div>
        </template>
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

      <div class="grid gap-5 xl:grid-cols-2">
        <Card variant="borderless">
          <template #title>
            <div class="flex items-center gap-2">
              <span class="h-5 w-1.5 rounded-full bg-primary"></span>
              <span>系统公告</span>
            </div>
          </template>
          <template #extra>
            <Button type="link" @click="router.push({ name: 'Notice' })">
              查看全部
            </Button>
          </template>

          <div class="h-[460px] overflow-y-auto pr-1" @scroll="onNoticeScroll">
            <Empty
              v-if="!noticeLoading && notices.length === 0"
              description="暂无系统公告"
            />
            <div v-else class="space-y-4">
              <article
                v-for="notice in notices"
                :key="notice.id"
                class="rounded-lg border border-border/60 bg-muted/20 p-4"
              >
                <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
                  <h3 class="text-foreground text-base font-semibold">
                    {{ notice.title }}
                  </h3>
                  <span class="text-muted-foreground text-xs">{{
                    formatDate(notice.publishTime || notice.createTime)
                  }}</span>
                  <Tag v-if="notice.pinned === 1" :style="versionTagStyle">
                    置顶
                  </Tag>
                </div>
                <div class="mt-3">
                  <RichTextPreview :content="notice.content" :min-height="0" />
                </div>
              </article>
              <div v-if="noticeLoading" class="flex justify-center py-3">
                <Spin size="small" />
              </div>
              <div
                v-if="!noticeLoading && !noticeHasMore"
                class="py-3 text-center text-muted-foreground text-xs"
              >
                已加载全部系统公告
              </div>
            </div>
          </div>
        </Card>

        <Card variant="borderless">
          <template #title>
            <div class="flex items-center gap-2">
              <span class="h-5 w-1.5 rounded-full bg-primary"></span>
              <span>更新日志</span>
            </div>
          </template>
          <template #extra>
            <Button type="link" @click="router.push({ name: 'Version' })">
              查看全部
            </Button>
          </template>

          <div class="h-[460px] overflow-y-auto pr-1" @scroll="onVersionScroll">
            <Empty
              v-if="!versionLoading && versionLogs.length === 0"
              description="暂无更新日志"
            />
            <div v-else class="space-y-4">
              <article
                v-for="log in versionLogs"
                :key="log.id"
                class="rounded-lg border border-border/60 bg-muted/20 p-4"
              >
                <div
                  class="flex items-center gap-3 text-muted-foreground text-xs"
                >
                  <Tag :style="versionTagStyle">
                    {{ formatVersion(log.version) }}
                  </Tag>
                  <span>{{
                    formatDate(log.publishTime || log.createTime)
                  }}</span>
                </div>
                <h3 class="mt-3 text-foreground text-base font-semibold">
                  {{ log.title }}
                </h3>
                <div class="mt-3">
                  <RichTextPreview :content="log.content" :min-height="0" />
                </div>
              </article>
              <div v-if="versionLoading" class="flex justify-center py-3">
                <Spin size="small" />
              </div>
              <div
                v-if="!versionLoading && !versionHasMore"
                class="py-3 text-center text-muted-foreground text-xs"
              >
                已加载全部更新日志
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>
