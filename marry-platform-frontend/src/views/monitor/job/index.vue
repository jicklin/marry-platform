<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">定时任务</div></div>
    <NCard>
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'monitor:job:add'" @click="openEdit()">新增任务</NButton>
        <NButton @click="load">刷新</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" title="任务信息" style="width: 540px">
      <NForm :model="form" label-placement="left" label-width="100">
        <NFormItem label="任务名" required><NInput v-model:value="form.name" /></NFormItem>
        <NFormItem label="Bean 名" required><NInput v-model:value="form.beanName" placeholder="e.g. demoJob" /></NFormItem>
        <NFormItem label="方法名"><NInput v-model:value="form.methodName" placeholder="execute" /></NFormItem>
        <NFormItem label="Cron" required><NInput v-model:value="form.cron" placeholder="0/10 * * * * ?" /></NFormItem>
        <NFormItem label="参数"><NInput v-model:value="form.params" /></NFormItem>
        <NFormItem label="备注"><NInput v-model:value="form.remark" type="textarea" /></NFormItem>
        <NFormItem label="状态"><NSwitch v-model:value="statusSwitch" /></NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="editVisible = false">取消</NButton>
          <NButton type="primary" @click="submit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSwitch, NTag, useDialog, useMessage } from 'naive-ui'
import { pageJobs, createJob, updateJob, deleteJobs, changeJobStatus, runJobOnce } from '@/api/monitor/job'

const message = useMessage()
const dialog = useDialog()
const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: '任务名', key: 'name', minWidth: 140 },
  { title: 'Bean', key: 'beanName', minWidth: 140 },
  { title: '方法', key: 'methodName', minWidth: 120 },
  { title: 'Cron', key: 'cron', minWidth: 140 },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '运行' : '暂停') })
    }
  },
  {
    title: '操作', key: 'a', width: 260, fixed: 'right' as const,
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'warning', onClick: () => runOnce(row) }, { default: () => '执行' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeOne(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res: any = await pageJobs(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

const editVisible = ref(false)
const form = ref<any>({ id: null, name: '', beanName: '', methodName: 'execute', params: '', cron: '', status: 1, remark: '' })
const statusSwitch = ref(true)
watch(statusSwitch, (v) => { form.value.status = v ? 1 : 0 })

function openEdit(row?: any) {
  if (row) { form.value = { ...row }; statusSwitch.value = row.status === 1 }
  else { form.value = { id: null, name: '', beanName: '', methodName: 'execute', params: '', cron: '', status: 1, remark: '' }; statusSwitch.value = true }
  editVisible.value = true
}

async function submit() {
  if (!form.value.name || !form.value.beanName || !form.value.cron) {
    message.warning('请填写必填字段'); return
  }
  if (form.value.id) await updateJob(form.value)
  else await createJob(form.value)
  message.success('保存成功')
  editVisible.value = false
  load()
}

async function runOnce(row: any) {
  await runJobOnce(row.id)
  message.success('已触发')
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除任务 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteJobs([row.id])
      message.success('删除成功')
      load()
    }
  })
}

import { watch } from 'vue'
onMounted(load)
</script>