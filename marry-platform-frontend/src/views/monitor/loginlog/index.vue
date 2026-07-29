<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">登录日志</div></div>
    <NCard>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NDataTable, NTag } from 'naive-ui'
import { pageLoginLog } from '@/api/monitor/loginlog'

const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: '用户', key: 'userName', width: 120 },
  { title: 'IP', key: 'ip', width: 130 },
  { title: '浏览器', key: 'browser', width: 100 },
  { title: '操作系统', key: 'os', width: 100 },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      const isOk = row.status === 'SUCCESS'
      return h(NTag, { type: isOk ? 'success' : 'error', size: 'small' }, { default: () => (isOk ? '成功' : '失败') })
    }
  },
  { title: '消息', key: 'message', minWidth: 160 },
  { title: '时间', key: 'loginTime', minWidth: 180 }
]

async function load() {
  loading.value = true
  try {
    const res: any = await pageLoginLog(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

onMounted(load)
</script>