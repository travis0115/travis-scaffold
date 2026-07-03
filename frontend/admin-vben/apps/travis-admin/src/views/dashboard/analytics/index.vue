<script lang="ts" setup>
import type { OpsJobApi, SystemNoticeApi, SystemVersionLogApi } from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { EmptyIcon, IconifyIcon } from '@vben/icons';

import { Button, Card, Skeleton } from 'antdv-next';

import { getJobDashboard, getPublishedNoticePage, getPublishedVersionLogs } from '#/api';
import { $t } from '#/locales';
import { OPS_PERMS } from '#/utils/permissions';

import NoticeList from '../../_core/notice/components/notice-list.vue';
import NoticePreviewModal from '../../_core/notice/components/notice-preview-modal.vue';
import VersionTimeline from '../../_core/version/components/version-timeline.vue';

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
const noticePreviewRef = ref<{
  open: (notice: SystemNoticeApi.Notice) => void;
}>();

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

function onNoticePreview(notice: SystemNoticeApi.Notice) {
  noticePreviewRef.value?.open(notice);
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
    const page = await getPublishedNoticePage({
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
    <NoticePreviewModal ref="noticePreviewRef" />
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
            <div
              v-if="!noticeLoading && notices.length === 0"
              class="flex h-full flex-col items-center justify-center text-muted-foreground"
            >
              <EmptyIcon class="mx-auto" />
              <div class="mt-2 text-sm">{{ $t('common.noData') }}</div>
            </div>
            <NoticeList
              v-else
              compact
              :has-more="noticeHasMore"
              :loading="noticeLoading"
              :notices="notices"
              @preview="onNoticePreview"
            />
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
            <div
              v-if="!versionLoading && versionLogs.length === 0"
              class="flex h-full flex-col items-center justify-center text-muted-foreground"
            >
              <EmptyIcon class="mx-auto" />
              <div class="mt-2 text-sm">{{ $t('common.noData') }}</div>
            </div>
            <VersionTimeline
              v-else
              compact
              :has-more="versionHasMore"
              :loading="versionLoading"
              :logs="versionLogs"
            />
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>
