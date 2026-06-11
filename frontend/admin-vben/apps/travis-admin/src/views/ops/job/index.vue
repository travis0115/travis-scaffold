<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Result } from 'antdv-next';

import { getJobEntry } from '#/api';

const loading = ref(false);

async function openAdmin() {
  loading.value = true;
  try {
    const { url } = await getJobEntry();
    window.open(url, '_blank', 'noopener,noreferrer');
  } finally {
    loading.value = false;
  }
}

onMounted(openAdmin);
</script>

<template>
  <Page>
    <Result
      status="info"
      title="任务调度"
      sub-title="XXL-JOB Admin 作为独立系统在新页面打开。"
    >
      <template #extra>
        <Button :loading="loading" type="primary" @click="openAdmin">
          打开任务调度
        </Button>
      </template>
    </Result>
  </Page>
</template>
