<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-title">用户管理</div>
    </div>
    <NCard>
      <NForm inline :model="query" label-placement="left" label-width="80">
        <NFormItem label="用户名">
          <NInput v-model:value="query.username" placeholder="请输入" clearable />
        </NFormItem>
        <NFormItem label="状态">
          <NSelect v-model:value="query.status" :options="statusOpts" clearable style="width: 140px" />
        </NFormItem>
        <NFormItem>
          <NButton type="primary" @click="load(1)">搜索</NButton>
          <NButton class="ml-8" @click="reset">重置</NButton>
        </NFormItem>
      </NForm>
    </NCard>

    <NCard class="mt-16">
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:user:add'" @click="openEdit()">新增用户</NButton>
        <NButton type="error" :disabled="!selections.length" @click="batchDelete">批量删除</NButton>
      </NSpace>
      <NDataTable
        :columns="columns"
        :data="rows"
        :loading="loading"
        :pagination="pagination"
        :row-key="(r) => r.id"
        @update:checked-row-keys="(k) => (selections = k as number[])"
        @update:page="(p) => { query.pageNum = p; load() }"
        @update:page-size="(s) => { query.pageSize = s; query.pageNum = 1; load() }"
        :bordered="false"
      />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" :title="editForm.id ? '编辑用户' : '新增用户'" style="width: 540px">
      <NForm :model="editForm" label-placement="left" label-width="80">
        <NFormItem label="用户名" required>
          <NInput v-model:value="editForm.username" :disabled="!!editForm.id" />
        </NFormItem>
        <NFormItem label="昵称">
          <NInput v-model:value="editForm.nickName" />
        </NFormItem>
        <NFormItem label="密码" v-if="!editForm.id">
          <NInput v-model:value="editForm.password" type="password" show-password-on="click" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="editForm.email" />
        </NFormItem>
        <NFormItem label="电话">
          <NInput v-model:value="editForm.phone" />
        </NFormItem>
        <NFormItem label="角色">
          <NSelect multiple v-model:value="editForm.roleIds" :options="roleOpts" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="statusSwitch" />
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
import {
  NCard, NForm, NFormItem, NInput, NSelect, NButton, NSpace, NDataTable, NModal, NSwitch,
  useDialog, useMessage, NTag
} from 'naive-ui'
import { pageUsers, getUserDetail, createUser, updateUser, deleteUsers } from '@/api/system/user'
import { listAllRoles } from '@/api/system/role'

const dialog = useDialog()
const message = useMessage()
const query = ref<any>({ username: '', status: null, pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const selections = ref<number[]>([])
const roleOpts = ref<{ label: string; value: number }[]>([])

const pagination = ref({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const statusOpts = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const columns = [
  { type: 'selection', width: 48 },
  { title: '用户名', key: 'username', minWidth: 120 },
  { title: '昵称', key: 'nickName', minWidth: 120 },
  { title: '部门', key: 'deptId', minWidth: 100 },
  { title: '邮箱', key: 'email', minWidth: 180 },
  { title: '电话', key: 'phone', minWidth: 120 },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  {
    title: '创建时间', key: 'createTime', width: 180,
    render(row: any) {
      return row.createTime || '-'
    }
  },
  {
    title: '操作', key: 'actions', width: 220, fixed: 'right',
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeOne(row) }, { default: () => '删除' }),
        h(NButton, { size: 'tiny', quaternary: true, onClick: () => resetPwd(row) }, { default: () => '重置密码' })
      ])
    }
  }
]

async function load(p?: number) {
  if (p) query.value.pageNum = p
  loading.value = true
  try {
    const res: any = await pageUsers(query.value)
    rows.value = res.records || []
    total.value = res.total || 0
    pagination.value.page = res.current || query.value.pageNum
    pagination.value.pageSize = res.size || query.value.pageSize
    pagination.value.itemCount = total.value
  } finally {
    loading.value = false
  }
}

function reset() {
  query.value = { username: '', status: null, pageNum: 1, pageSize: 10 }
  load()
}

async function loadRoles() {
  const list: any[] = await listAllRoles()
  roleOpts.value = list.map((r) => ({ label: r.name, value: r.id }))
}

const editVisible = ref(false)
const editForm = ref<any>({ id: null, username: '', nickName: '', password: '', email: '', phone: '', roleIds: [], status: 1 })
const statusSwitch = ref(true)

watch(statusSwitch, (v) => { editForm.value.status = v ? 1 : 0 })

async function openEdit(row?: any) {
  if (row) {
    // List rows don't carry roleIds; fetch the detail endpoint so the role
    // <n-select multiple> can pre-select this user's currently-assigned roles.
    editForm.value = { ...row, password: '', roleIds: [] }
    statusSwitch.value = row.status === 1
    editVisible.value = true
    try {
      const detail: any = await getUserDetail(row.id)
      editForm.value = {
        ...editForm.value,
        username: detail.username ?? row.username,
        nickName: detail.nickName ?? row.nickName,
        email: detail.email ?? row.email,
        phone: detail.phone ?? row.phone,
        deptId: detail.deptId ?? row.deptId,
        roleIds: Array.isArray(detail.roleIds) ? detail.roleIds : []
      }
    } catch (e) {
      // ignore: dialog stays open with empty roleIds; user can still pick again
    }
  } else {
    editForm.value = { id: null, username: '', nickName: '', password: 'admin123', email: '', phone: '', roleIds: [], status: 1 }
    statusSwitch.value = true
    editVisible.value = true
  }
}

import { watch } from 'vue'
async function submitEdit() {
  if (!editForm.value.username) {
    message.warning('请输入用户名')
    return
  }
  try {
    if (editForm.value.id) {
      await updateUser(editForm.value)
      message.success('更新成功')
    } else {
      await createUser(editForm.value)
      message.success('创建成功')
    }
    editVisible.value = false
    load()
  } catch {}
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除',
    content: `确定删除用户 ${row.username}?`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteUsers([row.id])
      message.success('删除成功')
      load()
    }
  })
}

function batchDelete() {
  dialog.warning({
    title: '确认删除',
    content: `确定删除 ${selections.value.length} 个用户?`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteUsers(selections.value)
      message.success('删除成功')
      selections.value = []
      load()
    }
  })
}

async function resetPwd(row: any) {
  dialog.warning({
    title: '重置密码',
    content: `将 ${row.username} 的密码重置为 123456?`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const { resetUserPassword } = await import('@/api/system/user')
      await resetUserPassword(row.id, '123456')
      message.success('密码已重置')
    }
  })
}

onMounted(() => {
  loadRoles()
  load()
})
</script>

<style scoped>
.ml-8 { margin-left: 8px; }
</style>