<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, ref, watch } from 'vue';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import dayjs from 'dayjs';

interface TrendPoint {
  date: string;
  failed: number;
  success: number;
}

const props = defineProps<{ data: TrendPoint[] }>();
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

function render() {
  void renderEcharts({
    grid: {
      bottom: 4,
      containLabel: true,
      left: 4,
      right: 12,
      top: 38,
    },
    legend: {
      itemHeight: 8,
      itemWidth: 8,
      right: 8,
      top: 0,
    },
    series: [
      {
        barCategoryGap: '40%',
        barGap: '20%',
        barMaxWidth: 20,
        barMinHeight: 2,
        data: props.data.map((item) => item.success),
        itemStyle: { borderRadius: [3, 3, 0, 0], color: '#10b981' },
        name: '成功',
        type: 'bar',
      },
      {
        barMaxWidth: 20,
        barMinHeight: 2,
        data: props.data.map((item) => item.failed),
        itemStyle: { borderRadius: [3, 3, 0, 0], color: '#f43f5e' },
        name: '失败',
        type: 'bar',
      },
    ],
    tooltip: { trigger: 'axis' },
    xAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      data: props.data.map((item) => dayjs(item.date).format('MM-DD')),
      type: 'category',
    },
    yAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      minInterval: 1,
      splitLine: { lineStyle: { color: 'rgba(127, 127, 127, 0.16)' } },
      type: 'value',
    },
  });
}

onMounted(render);
watch(() => props.data, render, { deep: true });
</script>

<template>
  <EchartsUI ref="chartRef" height="260px" />
</template>
