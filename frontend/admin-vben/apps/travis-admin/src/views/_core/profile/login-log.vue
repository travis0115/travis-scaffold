<script setup lang="ts">
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemLoginLogApi } from '#/api';

import { useUserStore } from '@vben/stores';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getLoginLogList } from '#/api';
import { $t } from '#/locales';

import { useColumns } from './login-log-data';

const userStore = useUserStore();
const currentUsername = userStore.userInfo?.username || '';

const [Grid] = useVbenVxeGrid({
  gridOptions: {
    columns: useColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          return await getLoginLogList({
            username: currentUsername,
            pageNum: page.currentPage,
            pageSize: page.pageSize,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {},
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      zoom: true,
    },
  } as VxeTableGridOptions<SystemLoginLogApi.LoginLog>,
});
</script>
<template>
  <div class="login-log-container h-full">
    <Grid :table-title="$t('system.loginLog.list')" />
  </div>
</template>
