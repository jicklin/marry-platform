<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">通知公告</div></div>
    <NCard>
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:notice:add'" @click="openEdit()">新增公告</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" title="公告信息" style="width: 600px">
      <NForm :model="form" label-placement="left" label-width="80">
        <NFormItem label="标题" required><NInput v-model:value="form.title" /></NFormItem>
        <NFormItem label="类型">
          <NSelect v-model:value="form.type" :options="[{label:'通知',value:'notice'},{label:'公告',value:'announcement'}]" />
        </NFormItem>
        <NFormItem label="内容">
          <NInput v-model:value="form.content" type="textarea" :rows="6" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="statusSwitch" />
        </NFormItem>
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
import { NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NSwitch, NTag, useDialog, useMessage } from 'naive-ui'
import { pageNotices, createNotice, updateNotice, deleteNotices } from '@/api/system/notice'

const message = useMessage()
const dialog = useDialog()
const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '标题', key: 'title', minWidth: 200 },
  {
    title: '类型', key: 'type', width: 100,
    render(row: any) {
      const t = row.type === 'announcement' ? 'announcement' : 'info'
      return h(NTag, { type: t, size: 'small' }, { default: () => (row.type === 'announcement' ? '公告' : '通知') })
    }
  },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  { title: '创建时间', key: 'createTime', minWidth: 180 },
  {
    title: '操作', key: 'a', width: 160, fixed: 'right',
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeOne(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res: any = await pageNotices(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

const editVisible = ref(false)
const form = ref<any>({ id: null, title: '', type: 'notice', content: '', status: 1 })
const statusSwitch = ref(true)

watch(statusSwitch, (v) => { form.value.status = v ? 1 : 0 })

function openEdit(row?: any) {
  if (row) {
    form.value = { ...row }
    statusSwitch.value = row.status === 1
  } else {
    form.value = { id: null, title: '', type: 'notice', content: '', status: 1 }
    statusSwitch.value = true
  }
  editVisible.value = true
}

async function submit() {
  if (!form.value.title) { message.warning('请填写标题'); return }
  if (form.value.id) await updateNotice(form.value)
  else await createNotice(form.value)
  message.success('保存成功')
  editVisible.value = false
  load()
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除 ${row.title}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteNotices([row.id])
      message.success('删除成功')
      load()
    }
  })
}

import { watch } from 'vue'
onMounted(load)
</script>