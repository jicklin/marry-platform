<template>
  <div class="dashboard">
    <!-- Welcome Header Banner -->
    <div class="welcome-banner glass-card mb-20">
      <div class="welcome-text">
        <h2 class="welcome-title">欢迎回来，{{ userStore.userInfo?.nickName || userStore.userInfo?.username || '管理员' }} 👋</h2>
        <p class="welcome-subtitle">系统运行正常，今天又是充满活力的一天！</p>
      </div>
      <div class="welcome-tag">
        <span class="status-dot" />
        <span>系统状态: 良好</span>
      </div>
    </div>

    <!-- Stat Cards Grid -->
    <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGridItem :span="'4 m:2 l:1'">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-header">
            <div class="stat-icon-wrap icon-indigo">
              <NIcon size="22"><PersonOutline /></NIcon>
            </div>
            <span class="stat-badge badge-up">
              <NIcon size="12"><TrendingUpOutline /></NIcon> 12.5%
            </span>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">注册用户总数</div>
          </div>
        </div>
      </NGridItem>

      <NGridItem :span="'4 m:2 l:1'">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-header">
            <div class="stat-icon-wrap icon-emerald">
              <NIcon size="22"><GlobeOutline /></NIcon>
            </div>
            <span class="stat-badge badge-up">
              <NIcon size="12"><TrendingUpOutline /></NIcon> 5.2%
            </span>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.onlineCount || 0 }}</div>
            <div class="stat-label">当前在线用户</div>
          </div>
        </div>
      </NGridItem>

      <NGridItem :span="'4 m:2 l:1'">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-header">
            <div class="stat-icon-wrap icon-amber">
              <NIcon size="22"><ListOutline /></NIcon>
            </div>
            <span class="stat-badge badge-neutral">今日</span>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.todayOperLog || 0 }}</div>
            <div class="stat-label">今日操作日志</div>
          </div>
        </div>
      </NGridItem>

      <NGridItem :span="'4 m:2 l:1'">
        <div class="stat-card glass-card hover-lift">
          <div class="stat-header">
            <div class="stat-icon-wrap icon-rose">
              <NIcon size="22"><LogInOutline /></NIcon>
            </div>
            <span class="stat-badge badge-up">
              <NIcon size="12"><TrendingUpOutline /></NIcon> 8.4%
            </span>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.todayLoginCount || 0 }}</div>
            <div class="stat-label">今日登录次数</div>
          </div>
        </div>
      </NGridItem>
    </NGrid>

    <!-- Main Charts Section -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive class="mt-20">
      <NGridItem :span="'3 l:2'">
        <NCard title="近 7 日访问趋势" class="chart-card glass-card">
          <template #header-extra>
            <NTag size="small" round type="primary">实时统计</NTag>
          </template>
          <VChart :option="visitOption" autoresize style="height: 320px" />
        </NCard>
      </NGridItem>

      <NGridItem :span="'3 l:1'">
        <NCard title="操作类型分布" class="chart-card glass-card">
          <VChart :option="operTypeOption" autoresize style="height: 320px" />
        </NCard>
      </NGridItem>
    </NGrid>

    <!-- Secondary Widgets Section -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive class="mt-20">
      <NGridItem :span="'3 l:1'">
        <NCard title="登录成功/失败分布" class="chart-card glass-card">
          <VChart :option="loginStatusOption" autoresize style="height: 260px" />
        </NCard>
      </NGridItem>

      <NGridItem :span="'3 l:2'">
        <NCard title="快捷快捷导航 & 系统状态" class="glass-card">
          <div class="quick-nav-grid">
            <div class="quick-nav-item" @click="router.push('/system/user')">
              <div class="quick-icon icon-indigo"><NIcon size="20"><PersonOutline /></NIcon></div>
              <div class="quick-title">用户管理</div>
            </div>
            <div class="quick-nav-item" @click="router.push('/system/role')">
              <div class="quick-icon icon-emerald"><NIcon size="20"><PeopleCircleOutline /></NIcon></div>
              <div class="quick-title">角色权限</div>
            </div>
            <div class="quick-nav-item" @click="router.push('/monitor/operlog')">
              <div class="quick-icon icon-amber"><NIcon size="20"><ListOutline /></NIcon></div>
              <div class="quick-title">操作日志</div>
            </div>
            <div class="quick-nav-item" @click="router.push('/tool/gen')">
              <div class="quick-icon icon-purple"><NIcon size="20"><CodeSlashOutline /></NIcon></div>
              <div class="quick-title">代码生成</div>
            </div>
          </div>
        </NCard>
      </NGridItem>
    </NGrid>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  PersonOutline, GlobeOutline, ListOutline, LogInOutline, TrendingUpOutline, PeopleCircleOutline, CodeSlashOutline
} from '@vicons/ionicons5'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

use([
  CanvasRenderer, LineChart, BarChart, PieChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
])

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const stats = ref<any>({})

async function load() {
  stats.value = await getDashboardStats()
}

onMounted(load)

const visitOption = ref<any>({
  tooltip: { trigger: 'axis', backgroundColor: 'rgba(17, 24, 39, 0.85)', textStyle: { color: '#fff' }, borderColor: 'transparent' },
  grid: { left: 40, right: 20, top: 24, bottom: 30 },
  xAxis: {
    type: 'category',
    data: [],
    boundaryGap: false,
    axisLine: { lineStyle: { color: '#94a3b8' } }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)', type: 'dashed' } }
  },
  series: [{
    name: '访问量',
    type: 'line',
    smooth: 0.4,
    showSymbol: false,
    lineStyle: { width: 3.5, color: '#6366f1' },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(99, 102, 241, 0.45)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0.02)' }
        ]
      }
    },
    itemStyle: { color: '#6366f1' },
    data: []
  }]
})

const operTypeOption = ref<any>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(17, 24, 39, 0.85)', textStyle: { color: '#fff' } },
  grid: { left: 40, right: 20, top: 24, bottom: 30 },
  xAxis: { type: 'category', data: [], axisLine: { lineStyle: { color: '#94a3b8' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)', type: 'dashed' } } },
  series: [{
    name: '操作数',
    type: 'bar',
    barWidth: '40%',
    itemStyle: {
      borderRadius: [6, 6, 0, 0],
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#10b981' },
          { offset: 1, color: '#059669' }
        ]
      }
    },
    data: []
  }]
})

const loginStatusOption = ref<any>({
  tooltip: { trigger: 'item', backgroundColor: 'rgba(17, 24, 39, 0.85)', textStyle: { color: '#fff' } },
  legend: { bottom: 0, textStyle: { color: '#94a3b8' } },
  series: [{
    type: 'pie',
    radius: ['50%', '75%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 8, borderColor: 'transparent', borderWidth: 2 },
    label: { show: true, formatter: '{b}: {c}' },
    color: ['#6366f1', '#ef4444', '#f59e0b', '#10b981'],
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

watch(stats, setOptionFromStats, { deep: true, immediate: true })
</script>

<style scoped>
.dashboard {
  max-width: 1600px;
  margin: 0 auto;
}

.welcome-banner {
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.05) 100%);
  border: 1px solid var(--border-soft);
}

.welcome-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--fg-title);
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--fg-muted);
}

.welcome-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 99px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-soft);
  font-size: 13px;
  font-weight: 600;
  color: var(--fg-default);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 8px #10b981;
}

.stat-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: var(--shadow-soft);
}

.icon-indigo { background: linear-gradient(135deg, #6366f1, #4f46e5); }
.icon-emerald { background: linear-gradient(135deg, #10b981, #059669); }
.icon-amber { background: linear-gradient(135deg, #f59e0b, #d97706); }
.icon-rose { background: linear-gradient(135deg, #f43f5e, #e11d48); }
.icon-purple { background: linear-gradient(135deg, #a855f7, #7c3aed); }

.stat-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.badge-up {
  background: rgba(16, 185, 129, 0.12);
  color: #10b981;
}

.badge-neutral {
  background: rgba(99, 102, 241, 0.12);
  color: var(--primary-color);
}

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--fg-title);
  letter-spacing: -0.8px;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: var(--fg-muted);
  margin-top: 4px;
}

.chart-card {
  height: 100%;
}

.quick-nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 16px;
  padding: 10px 0;
}

.quick-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-radius: 12px;
  background-color: var(--bg-hover);
  border: 1px solid var(--border-soft);
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-nav-item:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-soft);
}

.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 8px;
}

.quick-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--fg-default);
}
</style>