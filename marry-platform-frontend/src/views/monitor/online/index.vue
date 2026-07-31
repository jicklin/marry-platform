<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">在线用户</div></div>
    <NCard>
      <NDataTable :columns="columns" :data="rows" :loading="loading" />
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NButton, NSpace, useMessage } from 'naive-ui'
import { listOnline, forceLogout } from '@/api/monitor/online'

const message = useMessage()
const rows = ref<any[]>([])
const loading = ref(false)

const columns = [
  { title: '用户ID', key: 'userId', width: 120 },
  { title: '用户名', key: 'username', minWidth: 160 },
  { title: 'Token 剩余(s)', key: 'ttlSeconds', width: 160 },
  {
    title: '操作', key: 'a', width: 140, fixed: 'right' as const,
    render(row: any) {
      return h(NButton, { size: 'tiny', type: 'error', quaternary: true, onClick: () => handleForce(row) }, { default: () => '强制下线' })
    }
  }
]

async function load() {
  loading.value = true
  try { rows.value = await listOnline() } finally { loading.value = false }
}

async function handleForce(row: any) {
  await forceLogout(row.userId)
  message.success('已下线')
  load()
}

onMounted(load)
</script>