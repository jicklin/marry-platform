<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">部门管理</div></div>
    <NCard>
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:dept:add'" @click="openEdit()">新增部门</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :row-key="(r) => r.id" default-expand-all />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" title="部门信息" style="width: 480px">
      <NForm :model="editForm" label-placement="left" label-width="80">
        <NFormItem label="上级部门">
          <NTreeSelect :options="parentOpts" v-model:value="editForm.parentId" />
        </NFormItem>
        <NFormItem label="部门名称" required>
          <NInput v-model:value="editForm.name" />
        </NFormItem>
        <NFormItem label="编码">
          <NInput v-model:value="editForm.code" />
        </NFormItem>
        <NFormItem label="负责人">
          <NInput v-model:value="editForm.leader" />
        </NFormItem>
        <NFormItem label="电话">
          <NInput v-model:value="editForm.phone" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="editForm.email" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="editForm.orderNum" :min="0" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="editVisible = false">取消</NButton>
          <NButton type="primary" @click="submitEdit">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NInputNumber, NTreeSelect, NTag, useDialog, useMessage } from 'naive-ui'
import { deptTree, createDept, updateDept, deleteDept } from '@/api/system/dept'

const message = useMessage()
const dialog = useDialog()

const rows = ref<any[]>([])
const loading = ref(false)

const columns = [
  { title: '部门名称', key: 'name', minWidth: 180 },
  { title: '编码', key: 'code', minWidth: 100 },
  { title: '负责人', key: 'leader', minWidth: 100 },
  { title: '电话', key: 'phone', minWidth: 120 },
  { title: '邮箱', key: 'email', minWidth: 160 },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  {
    title: '操作', key: 'a', width: 160, fixed: 'right' as const,
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
  try { rows.value = await deptTree() } finally { loading.value = false }
}

const editVisible = ref(false)
const editForm = ref<any>({ id: null, parentId: 0, name: '', code: '', leader: '', phone: '', email: '', orderNum: 0 })
const parentOpts = ref<any[]>([])

function buildParentOpts(items: any[]): any[] {
  const opts: any[] = [{ label: '顶级', value: 0 }]
  for (const m of items) {
    opts.push({ label: m.name, value: m.id })
    if (m.children?.length) opts.push(...buildParentOpts(m.children))
  }
  return opts
}

function openEdit(row?: any) {
  if (parentOpts.value.length === 0) parentOpts.value = buildParentOpts(rows.value)
  if (row) editForm.value = { ...row }
  else editForm.value = { id: null, parentId: 0, name: '', code: '', leader: '', phone: '', email: '', orderNum: 0 }
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.name) { message.warning('请填写部门名称'); return }
  try {
    if (editForm.value.id) await updateDept(editForm.value)
    else await createDept(editForm.value)
    message.success('保存成功')
    editVisible.value = false
    load()
  } catch {}
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDept(row.id)
        message.success('删除成功')
        load()
      } catch {}
    }
  })
}

onMounted(load)
</script>