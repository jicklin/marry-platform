<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">代码生成</div></div>
    <NCard>
      <NTabs v-model:value="tab" type="line">
        <NTabPane name="imported" tab="已导入表">
          <NSpace class="mb-12">
            <NButton type="primary" v-auth="'tool:gen:edit'" @click="loadDb">从数据库导入</NButton>
          </NSpace>
          <NDataTable :columns="cols" :data="rows" :loading="loading" />
        </NTabPane>
        <NTabPane name="preview" tab="列预览 / 下载">
          <NSpace class="mb-12">
            <NSelect v-model:value="selectedId" :options="idOpts" placeholder="选择已导入表" />
            <NButton type="primary" :disabled="!selectedId" @click="downloadZip">下载 ZIP</NButton>
            <NButton :disabled="!selectedId" @click="syncCols">同步字段</NButton>
          </NSpace>
          <NDataTable :columns="colCols" :data="colRows" :loading="colLoading" />
        </NTabPane>
      </NTabs>
    </NCard>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { NCard, NTabs, NTabPane, NSpace, NButton, NSelect, NDataTable, NTag, useMessage } from 'naive-ui'
import { listGen, listDb, importTables, syncColumns, listColumns, downloadGenZip } from '@/api/tool/gen'

const message = useMessage()
const tab = ref<'imported' | 'preview'>('imported')
const rows = ref<any[]>([])
const loading = ref(false)
const cols = [
  { title: '表名', key: 'tableName', minWidth: 160 },
  { title: '注释', key: 'tableComment', minWidth: 200 },
  { title: '类名', key: 'className', minWidth: 120 },
  { title: '模块', key: 'moduleName', minWidth: 100 },
  { title: '生成时间', key: 'createTime', minWidth: 180 }
]

async function load() {
  loading.value = true
  try { rows.value = await listGen() } finally { loading.value = false }
}

async function loadDb() {
  const list = await listDb()
  const names = list.filter((t: any) => !rows.value.find(r => r.tableName === t.tableName)).map((t: any) => t.tableName)
  if (!names.length) return message.info('没有新表需要导入')
  await importTables(names)
  message.success(`导入 ${names.length} 张表`)
  load()
}

const selectedId = ref<number | null>(null)
const idOpts = computed(() => rows.value.map((r: any) => ({ label: r.tableName, value: r.id })))

const colRows = ref<any[]>([])
const colLoading = ref(false)
const colCols = [
  { title: '列名', key: 'columnName', minWidth: 120 },
  { title: '类型', key: 'columnType', minWidth: 100 },
  { title: 'Java', key: 'javaType', minWidth: 120 },
  { title: '字段', key: 'javaField', minWidth: 120 },
  { title: '注释', key: 'columnComment', minWidth: 160 },
  {
    title: '主键', key: 'isPk', width: 70,
    render(row: any) {
      return h(NTag, { type: row.isPk === 1 ? 'success' : 'default', size: 'small' }, { default: () => (row.isPk === 1 ? 'YES' : '-') })
    }
  }
]

async function loadColumns() {
  if (!selectedId.value) { colRows.value = []; return }
  colLoading.value = true
  try { colRows.value = await listColumns(selectedId.value) } finally { colLoading.value = false }
}

async function syncCols() {
  if (!selectedId.value) return
  await syncColumns(selectedId.value)
  message.success('已同步')
  loadColumns()
}

async function downloadZip() {
  if (!selectedId.value) return
  const blob: any = await downloadGenZip(selectedId.value)
  const url = URL.createObjectURL(new Blob([blob]))
  const a = document.createElement('a')
  a.href = url
  a.download = `code-${selectedId.value}.zip`
  a.click()
  URL.revokeObjectURL(url)
}

import { watch } from 'vue'
watch(selectedId, loadColumns)

onMounted(load)
</script>