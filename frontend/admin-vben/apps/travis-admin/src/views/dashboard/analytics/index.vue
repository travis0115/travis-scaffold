<script lang="ts" setup>
import type {
  AppUserLoginLogApi,
  OpsJobApi,
  SystemLoginLogApi,
  SystemNoticeApi,
  SystemUserApi,
  SystemVersionLogApi,
} from '#/api';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { EmptyIcon, IconifyIcon } from '@vben/icons';

import { Button, Card, Skeleton, Spin, Tag } from 'antdv-next';
import dayjs from 'dayjs';

import {
  getAppUserDashboard,
  getAppUserLoginDashboard,
  getErrorLogPage,
  getJobDashboard,
  getJobLogPage,
  getLoginDashboard,
  getPublishedNoticePage,
  getPublishedVersionLogs,
  getUserDashboard,
} from '#/api';
import { $t } from '#/locales';
import { OPS_PERMS, SYSTEM_PERMS } from '#/utils/permissions';

import NoticeList from '../../_core/notice/components/notice-list.vue';
import NoticePreviewModal from '../../_core/notice/components/notice-preview-modal.vue';
import VersionTimeline from '../../_core/version/components/version-timeline.vue';
import JobTrendChart from './components/job-trend-chart.vue';

interface MetricItem {
  colorClass: string;
  hint?: string;
  icon: string;
  label: string;
  surfaceClass?: string;
  suffix?: string;
  value: number;
}

const router = useRouter();
const { hasAccessByCodes } = useAccess();
const canViewUsers = hasAccessByCodes([SYSTEM_PERMS.userQuery]);
const canViewLoginLogs = hasAccessByCodes([SYSTEM_PERMS.loginLogQuery]);
const canViewJobDashboard = hasAccessByCodes([OPS_PERMS.jobQuery]);
const canViewJobLogs = hasAccessByCodes([OPS_PERMS.jobLogQuery]);
const canViewErrorLogs = hasAccessByCodes([OPS_PERMS.errorLogQuery]);

const userDashboard = ref<SystemUserApi.Dashboard>();
const appUserDashboard = ref<SystemUserApi.Dashboard>();
const loginDashboard = ref<SystemLoginLogApi.Dashboard>();
const appLoginDashboard = ref<AppUserLoginLogApi.Dashboard>();
const jobDashboard = ref<OpsJobApi.Dashboard>();
const jobTrend = ref<OpsJobApi.Dashboard['trend']>([]);
const pendingErrorTotal = ref(0);
const recentFailedJobs = ref<OpsJobApi.JobLog[]>([]);
const notices = ref<SystemNoticeApi.Notice[]>([]);
const noticeTotal = ref(0);
const noticePageNum = ref(1);
const versionLogs = ref<SystemVersionLogApi.VersionLog[]>([]);
const versionTotal = ref(0);
const versionPageNum = ref(1);
const loading = ref(true);
const jobDashboardLoading = ref(false);
const noticeLoading = ref(false);
const versionLoading = ref(false);
const homePageSize = 10;
const jobRange = ref<OpsJobApi.DashboardRange>('TODAY');
const jobRangeOptions: Array<{
  label: string;
  value: OpsJobApi.DashboardRange;
}> = [
  { label: '今日', value: 'TODAY' },
  { label: '近7日', value: 'LAST_7_DAYS' },
  { label: '近30日', value: 'LAST_30_DAYS' },
];
const noticePreviewRef = ref<{
  open: (notice: SystemNoticeApi.Notice) => void;
}>();

const hasSystemOverview = computed(() => canViewUsers || canViewLoginLogs);

const adminUserMetrics = computed<MetricItem[]>(() => {
  const items: MetricItem[] = [];
  if (canViewUsers)
    items.push(
      {
        colorClass: 'bg-blue-500/10 text-blue-500',
        hint: `今日新增 ${userDashboard.value?.newUsersToday ?? 0}`,
        icon: 'lucide:users',
        label: '用户总数',
        value: userDashboard.value?.totalUsers ?? 0,
      },
      {
        colorClass: 'bg-emerald-500/10 text-emerald-500',
        hint: '当前已建立连接',
        icon: 'lucide:wifi',
        label: '在线用户',
        value: userDashboard.value?.onlineUsers ?? 0,
      },
    );
  if (canViewLoginLogs) {
    items.push({
      colorClass: 'bg-cyan-500/10 text-cyan-500',
      hint: '成功登录用户去重',
      icon: 'lucide:log-in',
      label: '今日登录',
      value: loginDashboard.value?.todayLoginUsers ?? 0,
    });
  }
  return items;
});

const appUserMetrics = computed<MetricItem[]>(() => {
  const items: MetricItem[] = [];
  if (canViewUsers)
    items.push(
      {
        colorClass: 'bg-violet-500/10 text-violet-500',
        hint: `今日新增 ${appUserDashboard.value?.newUsersToday ?? 0}`,
        icon: 'lucide:smartphone',
        label: '用户总数',
        value: appUserDashboard.value?.totalUsers ?? 0,
      },
      {
        colorClass: 'bg-emerald-500/10 text-emerald-500',
        hint: '当前已建立连接',
        icon: 'lucide:wifi',
        label: '在线用户',
        value: appUserDashboard.value?.onlineUsers ?? 0,
      },
    );
  if (canViewUsers) {
    items.push({
      colorClass: 'bg-cyan-500/10 text-cyan-500',
      hint: '成功登录用户去重',
      icon: 'lucide:log-in',
      label: '今日登录',
      value: appLoginDashboard.value?.todayLoginUsers ?? 0,
    });
  }
  return items;
});

const jobStockMetrics = computed<MetricItem[]>(() => [
  {
    colorClass: 'bg-blue-500/10 text-blue-500',
    icon: 'lucide:list-todo',
    label: '任务总数',
    value: jobDashboard.value?.totalJobs ?? 0,
  },
  {
    colorClass: 'bg-emerald-500/10 text-emerald-500',
    icon: 'lucide:circle-play',
    label: '启用任务',
    value: jobDashboard.value?.enabledJobs ?? 0,
  },
  {
    colorClass: 'bg-amber-500/10 text-amber-500',
    icon: 'lucide:circle-pause',
    label: '暂停任务',
    value: jobDashboard.value?.pausedJobs ?? 0,
  },
]);

const jobExecutionMetrics = computed<MetricItem[]>(() => [
  {
    colorClass: 'bg-cyan-500/10 text-cyan-500',
    icon: 'lucide:activity',
    label: '执行次数',
    surfaceClass: 'border-cyan-500/20 bg-cyan-500/5',
    value: jobDashboard.value?.executions ?? 0,
  },
  {
    colorClass: 'bg-violet-500/10 text-violet-500',
    icon: 'lucide:loader-circle',
    label: '运行中',
    surfaceClass: 'border-violet-500/20 bg-violet-500/5',
    value: jobDashboard.value?.runningExecutions ?? 0,
  },
  {
    colorClass: 'bg-rose-500/10 text-rose-500',
    icon: 'lucide:circle-x',
    label: '失败次数',
    surfaceClass: 'border-rose-500/20 bg-rose-500/5',
    value: jobDashboard.value?.failedExecutions ?? 0,
  },
  {
    colorClass: 'bg-emerald-500/10 text-emerald-500',
    hint: '运行中不计入成功率',
    icon: 'lucide:circle-check-big',
    label: '成功率',
    surfaceClass: 'border-emerald-500/20 bg-emerald-500/5',
    suffix: '%',
    value: jobDashboard.value?.successRate ?? 0,
  },
]);

const noticeHasMore = computed(() => notices.value.length < noticeTotal.value);
const versionHasMore = computed(
  () => versionLogs.value.length < versionTotal.value,
);

function onNoticePreview(notice: SystemNoticeApi.Notice) {
  noticePreviewRef.value?.open(notice);
}

async function fetchJobDashboard(refreshTrend = false) {
  jobDashboardLoading.value = true;
  try {
    const dashboard = await getJobDashboard(jobRange.value);
    jobDashboard.value = dashboard;
    if (refreshTrend) jobTrend.value = dashboard.trend;
  } finally {
    jobDashboardLoading.value = false;
  }
}

async function onJobRangeChange(range: OpsJobApi.DashboardRange) {
  if (jobDashboardLoading.value || range === jobRange.value) return;
  const previousRange = jobRange.value;
  jobRange.value = range;
  try {
    await fetchJobDashboard();
  } catch {
    jobRange.value = previousRange;
  }
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
  if (target.scrollHeight - target.scrollTop - target.clientHeight <= 48) {
    void fetchNotices();
  }
}

function onVersionScroll(event: Event) {
  const target = event.currentTarget as HTMLElement;
  if (target.scrollHeight - target.scrollTop - target.clientHeight <= 48) {
    void fetchVersionLogs();
  }
}

const navigation = {
  errorLogs: () =>
    void router.push({
      path: '/ops/error-log',
      query: { pageKey: '/ops/error-log', status: '0' },
    }),
  failedJobLogs: () =>
    void router.push({
      path: '/ops/job/log',
      query: { pageKey: '/ops/job/log', status: '2' },
    }),
  jobList: () => void router.push('/ops/job/list'),
  jobLogs: () => void router.push('/ops/job/log'),
  notices: () => void router.push({ name: 'Notice' }),
  versions: () => void router.push({ name: 'Version' }),
};

onMounted(async () => {
  const requests: Promise<unknown>[] = [fetchNotices(), fetchVersionLogs()];
  if (canViewUsers) {
    requests.push(
      Promise.all([
        getUserDashboard(),
        getAppUserDashboard(),
        getAppUserLoginDashboard(),
      ]).then(([adminData, appData, appLoginData]) => {
        userDashboard.value = adminData;
        appUserDashboard.value = appData;
        appLoginDashboard.value = appLoginData;
      }),
    );
  }
  if (canViewLoginLogs) {
    requests.push(
      getLoginDashboard().then((data) => {
        loginDashboard.value = data;
      }),
    );
  }
  if (canViewJobDashboard) requests.push(fetchJobDashboard(true));
  if (canViewJobLogs) {
    requests.push(
      getJobLogPage({ pageNum: 1, pageSize: 5, status: 2 }).then((page) => {
        recentFailedJobs.value = page.records;
      }),
    );
  }
  if (canViewErrorLogs) {
    requests.push(
      getErrorLogPage({ pageNum: 1, pageSize: 1, status: 0 }).then((page) => {
        pendingErrorTotal.value = page.total;
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
    <Skeleton v-if="loading" active :paragraph="{ rows: 12 }" />

    <div v-else class="flex flex-col gap-5">
      <Card
        v-if="canViewErrorLogs && pendingErrorTotal > 0"
        class="border border-rose-500/20"
        variant="borderless"
      >
        <div class="flex items-center justify-between gap-4">
          <div class="flex min-w-0 items-center gap-3">
            <span
              class="flex size-10 shrink-0 items-center justify-center rounded-full bg-rose-500/10 text-rose-500"
            >
              <IconifyIcon icon="lucide:triangle-alert" class="size-5" />
            </span>
            <span class="font-medium">待处理异常</span>
            <Tag color="error">{{ pendingErrorTotal }}</Tag>
          </div>
          <Button
            class="shrink-0 border-rose-500/20 bg-rose-500/10 text-rose-500 hover:!border-rose-500/40 hover:!text-rose-600"
            @click="navigation.errorLogs"
          >
            <template #icon>
              <IconifyIcon icon="lucide:arrow-up-right" class="size-4" />
            </template>
            立即查看
          </Button>
        </div>
      </Card>

      <Card v-if="hasSystemOverview" variant="borderless">
        <template #title>
          <div class="flex items-center gap-2">
            <span class="h-5 w-1.5 rounded-full bg-primary"></span>
            <span>系统概览</span>
          </div>
        </template>
        <div class="grid gap-4" :class="canViewUsers ? 'xl:grid-cols-2' : ''">
          <section class="rounded-xl border border-border p-5">
            <div class="mb-4 flex items-center gap-2">
              <span class="size-2 rounded-full bg-blue-500"></span>
              <span class="font-medium">后台用户</span>
              <span class="text-xs text-muted-foreground">管理端</span>
            </div>
            <div class="grid gap-3 sm:grid-cols-3">
              <div
                v-for="item in adminUserMetrics"
                :key="item.label"
                class="rounded-lg bg-muted/45 p-4"
              >
                <div
                  class="flex items-center gap-2 text-xs text-muted-foreground"
                >
                  <span
                    class="flex size-7 items-center justify-center rounded-md"
                    :class="item.colorClass"
                  >
                    <IconifyIcon :icon="item.icon" class="size-4" />
                  </span>
                  {{ item.label }}
                </div>
                <div class="mt-3 text-2xl font-semibold tracking-tight">
                  {{ item.value.toLocaleString() }}
                </div>
                <div
                  v-if="item.hint"
                  class="mt-1 text-xs text-muted-foreground"
                >
                  {{ item.hint }}
                </div>
              </div>
            </div>
          </section>

          <section
            v-if="canViewUsers"
            class="rounded-xl border border-border p-5"
          >
            <div class="mb-4 flex items-center gap-2">
              <span class="size-2 rounded-full bg-violet-500"></span>
              <span class="font-medium">客户端用户</span>
              <span class="text-xs text-muted-foreground">App 端</span>
            </div>
            <div class="grid gap-3 sm:grid-cols-3">
              <div
                v-for="item in appUserMetrics"
                :key="item.label"
                class="rounded-lg bg-muted/45 p-4"
              >
                <div
                  class="flex items-center gap-2 text-xs text-muted-foreground"
                >
                  <span
                    class="flex size-7 items-center justify-center rounded-md"
                    :class="item.colorClass"
                  >
                    <IconifyIcon :icon="item.icon" class="size-4" />
                  </span>
                  {{ item.label }}
                </div>
                <div class="mt-3 text-2xl font-semibold tracking-tight">
                  {{ item.value.toLocaleString() }}
                </div>
                <div
                  v-if="item.hint"
                  class="mt-1 text-xs text-muted-foreground"
                >
                  {{ item.hint }}
                </div>
              </div>
            </div>
          </section>
        </div>
      </Card>

      <Card v-if="canViewJobDashboard" variant="borderless">
        <template #title>
          <div class="flex items-center gap-2">
            <span class="h-5 w-1.5 rounded-full bg-primary"></span>
            <span>任务调度</span>
          </div>
        </template>
        <template #extra>
          <div class="flex gap-2">
            <Button
              class="border-blue-500/20 bg-blue-500/10 text-blue-500 hover:!border-blue-500/40 hover:!text-blue-600"
              @click="navigation.jobList"
            >
              <template #icon>
                <IconifyIcon icon="lucide:list-checks" class="size-4" />
              </template>
              任务管理
            </Button>
            <Button
              v-if="canViewJobLogs"
              class="border-violet-500/20 bg-violet-500/10 text-violet-500 hover:!border-violet-500/40 hover:!text-violet-600"
              @click="navigation.jobLogs"
            >
              <template #icon>
                <IconifyIcon icon="lucide:scroll-text" class="size-4" />
              </template>
              执行日志
            </Button>
          </div>
        </template>

        <div class="grid gap-4 xl:grid-cols-2">
          <section class="rounded-xl border border-border p-5">
            <div class="mb-4 font-medium">当前任务状态</div>
            <div class="grid gap-3 sm:grid-cols-3">
              <div
                v-for="item in jobStockMetrics"
                :key="item.label"
                class="flex items-center gap-3 rounded-lg border border-border bg-background p-4 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
              >
                <span
                  class="flex size-10 items-center justify-center rounded-full"
                  :class="item.colorClass"
                >
                  <IconifyIcon :icon="item.icon" class="size-5" />
                </span>
                <span>
                  <span class="block text-xs text-muted-foreground">{{
                    item.label
                  }}</span>
                  <span class="block text-xl font-semibold">{{
                    item.value.toLocaleString()
                  }}</span>
                </span>
              </div>
            </div>
          </section>

          <section
            class="relative rounded-xl border border-border p-5"
            :aria-busy="jobDashboardLoading"
          >
            <div
              v-if="jobDashboardLoading"
              class="absolute inset-0 z-20 flex items-center justify-center rounded-xl bg-background/75 backdrop-blur-[1px]"
            >
              <Spin />
            </div>
            <div
              :class="{
                'pointer-events-none select-none': jobDashboardLoading,
              }"
            >
              <div class="mb-4 flex items-center justify-between gap-3">
                <span class="font-medium">执行概况</span>
                <div
                  class="flex rounded-lg border border-border bg-muted/50 p-1"
                >
                  <button
                    v-for="option in jobRangeOptions"
                    :key="option.value"
                    class="rounded-md px-3 py-1.5 text-xs font-medium transition-all"
                    :class="
                      jobRange === option.value
                        ? 'bg-primary text-primary-foreground shadow-sm shadow-primary/20'
                        : 'text-muted-foreground hover:text-foreground'
                    "
                    :disabled="jobDashboardLoading"
                    type="button"
                    @click="onJobRangeChange(option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
                <div
                  v-for="item in jobExecutionMetrics"
                  :key="item.label"
                  class="rounded-lg border p-4 transition-all hover:-translate-y-0.5 hover:shadow-sm"
                  :class="item.surfaceClass"
                >
                  <div class="flex items-center gap-2 text-xs font-medium">
                    <span
                      class="flex size-7 items-center justify-center rounded-md"
                      :class="item.colorClass"
                    >
                      <IconifyIcon :icon="item.icon" class="size-4" />
                    </span>
                    {{ item.label }}
                  </div>
                  <div class="mt-2 text-xl font-semibold">
                    {{ item.value.toLocaleString() }}{{ item.suffix }}
                  </div>
                  <div
                    v-if="item.hint"
                    class="mt-1 text-xs text-muted-foreground"
                  >
                    {{ item.hint }}
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div
          class="mt-4 grid gap-4"
          :class="canViewJobLogs ? 'xl:grid-cols-2' : ''"
        >
          <section class="rounded-xl border border-border p-4">
            <div class="mb-2 flex items-start justify-between gap-3">
              <div>
                <div class="font-medium">执行趋势</div>
                <div class="mt-1 text-xs text-muted-foreground">
                  近 7 日成功与失败执行次数
                </div>
              </div>
            </div>
            <JobTrendChart :data="jobTrend" />
          </section>

          <section
            v-if="canViewJobLogs"
            class="rounded-xl border border-border p-4"
          >
            <div class="flex items-center justify-between gap-3">
              <div class="font-medium">最近失败任务</div>
              <Button
                class="border-rose-500/20 bg-rose-500/10 text-rose-500 hover:!border-rose-500/40 hover:!text-rose-600"
                size="small"
                @click="navigation.failedJobLogs"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:arrow-up-right" class="size-4" />
                </template>
                立即查看
              </Button>
            </div>
            <div
              v-if="recentFailedJobs.length === 0"
              class="flex h-[260px] items-center justify-center"
            >
              <div class="text-center text-muted-foreground">
                <EmptyIcon class="mx-auto" />
                <div class="mt-2 text-sm">暂无失败任务</div>
              </div>
            </div>
            <div v-else class="divide-y divide-border">
              <div
                v-for="item in recentFailedJobs"
                :key="item.id"
                class="flex w-full items-center gap-3 py-3 text-left"
              >
                <span
                  class="flex size-9 shrink-0 items-center justify-center rounded-full bg-amber-500/10 text-amber-500"
                >
                  <IconifyIcon icon="lucide:timer-off" class="size-4" />
                </span>
                <span class="min-w-0 flex-1">
                  <span class="block truncate text-sm font-medium">{{
                    item.jobName
                  }}</span>
                  <span
                    class="mt-1 block truncate text-xs text-muted-foreground"
                  >
                    {{ item.handlerName }}
                  </span>
                </span>
                <span class="shrink-0 text-xs text-muted-foreground">
                  {{
                    item.startTime
                      ? dayjs(item.startTime).format('MM-DD HH:mm')
                      : '-'
                  }}
                </span>
              </div>
            </div>
          </section>
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
            <Button type="link" @click="navigation.notices">查看全部</Button>
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
            <Button type="link" @click="navigation.versions">查看全部</Button>
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
