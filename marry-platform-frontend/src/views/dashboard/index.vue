<template>
  <div class="dashboard">
    <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGridItem :span="'4 m:2 l:1'">
        <NCard class="stat-card" hoverable>
          <NStatistic label="注册用户" :value="stats.userCount || 0">
            <template #prefix>
              <NIcon size="22" color="#2d8cf0"><PersonOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem :span="'4 m:2 l:1'">
        <NCard class="stat-card" hoverable>
          <NStatistic label="在线用户" :value="stats.onlineCount || 0">
            <template #prefix>
              <NIcon size="22" color="#67c23a"><GlobeOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem :span="'4 m:2 l:1'">
        <NCard class="stat-card" hoverable>
          <NStatistic label="今日操作日志" :value="stats.todayOperLog || 0">
            <template #prefix>
              <NIcon size="22" color="#e6a23c"><ListOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
      <NGridItem :span="'4 m:2 l:1'">
        <NCard class="stat-card" hoverable>
          <NStatistic label="今日登录次数" :value="stats.todayLoginCount || 0">
            <template #prefix>
              <NIcon size="22" color="#f56c6c"><LogInOutline /></NIcon>
            </template>
          </NStatistic>
        </NCard>
      </NGridItem>
    </NGrid>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive class="mt-16">
      <NGridItem :span="'2 l:1'">
        <NCard title="近 7 日访问趋势">
          <VChart :option="visitOption" autoresize style="height: 320px" />
        </NCard>
      </NGridItem>
      <NGridItem :span="'2 l:1'">
        <NCard title="操作类型分布（近 7 日）">
          <VChart :option="operTypeOption" autoresize style="height: 320px" />
        </NCard>
      </NGridItem>
    </NGrid>

    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive class="mt-16">
      <NGridItem :span="'3 l:1'">
        <NCard title="登录成功/失败（近 7 日）">
          <VChart :option="loginStatusOption" autoresize style="height: 280px" />
        </NCard>
      </NGridItem>
    </NGrid>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  PersonOutline, GlobeOutline, ListOutline, LogInOutline
} from '@vicons/ionicons5'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getDashboardStats } from '@/api/dashboard'

use([
  CanvasRenderer, LineChart, BarChart, PieChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
])

const stats = ref<any>({})

async function load() {
  stats.value = await getDashboardStats()
}

onMounted(load)

const visitOption = ref<any>({
  tooltip: { trigger: 'axis' },
  grid: { left: 30, right: 16, top: 24, bottom: 30 },
  xAxis: { type: 'category', data: [], boundaryGap: false },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '访问量', type: 'line', smooth: true, areaStyle: {
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: 'rgba(45,140,240,0.45)' }, { offset: 1, color: 'rgba(45,140,240,0.05)' }]
      }
    },
    itemStyle: { color: '#2d8cf0' },
    data: []
  }]
})

const operTypeOption = ref<any>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 30, right: 16, top: 24, bottom: 30 },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{ name: '操作数', type: 'bar', itemStyle: { color: '#67c23a' }, data: [] }]
})

const loginStatusOption = ref<any>({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    avoidLabelOverlap: false,
    label: { show: true, formatter: '{b}: {c}' },
    data: []
  }]
})

function setOptionFromStats() {
  const trend = stats.value?.visitTrend || []
  visitOption.value.xAxis.data = trend.map((t: any) => t.date.slice(5))
  visitOption.value.series[0].data = trend.map((t: any) => t.count)

  const types = stats.value?.operTypeDist || []
  operTypeOption.value.xAxis.data = types.map((t: any) => t.name || 'UNKNOWN')
  operTypeOption.value.series[0].data = types.map((t: any) => t.value)

  const login = stats.value?.loginStatusDist || []
  loginStatusOption.value.series[0].data = login.map((l: any) => ({ name: l.name, value: l.value }))
}

import { watch } from 'vue'
watch(stats, setOptionFromStats, { deep: true, immediate: true })
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card :deep(.n-card__content) {
  padding: 16px;
}
</style>