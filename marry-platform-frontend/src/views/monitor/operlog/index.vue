<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">操作日志</div></div>
    <NCard>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NTag } from 'naive-ui'
import { pageOperLog } from '@/api/monitor/operlog'

const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: '模块', key: 'title', minWidth: 120 },
  { title: '操作类型', key: 'businessType', width: 100 },
  { title: '请求方法', key: 'requestMethod', width: 90 },
  { title: '请求URL', key: 'operUrl', minWidth: 160 },
  { title: '操作人', key: 'operName', width: 120 },
  { title: 'IP', key: 'operIp', width: 130 },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '成功' : '失败') })
    }
  },
  { title: '耗时(ms)', key: 'costTime', width: 100 },
  { title: '时间', key: 'operTime', minWidth: 180 }
]

async function load() {
  loading.value = true
  try {
    const res: any = await pageOperLog(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

onMounted(load)
</script>