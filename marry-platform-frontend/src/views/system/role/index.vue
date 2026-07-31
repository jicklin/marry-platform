<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">角色管理</div></div>
    <NCard>
      <NForm inline :model="query" label-placement="left" label-width="80">
        <NFormItem label="角色名">
          <NInput v-model:value="query.name" placeholder="请输入" clearable />
        </NFormItem>
        <NFormItem>
          <NButton type="primary" @click="load(1)">搜索</NButton>
        </NFormItem>
      </NForm>
    </NCard>
    <NCard class="mt-16">
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:role:add'" @click="openEdit()">新增角色</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }"
                  @update:page-size="(s) => { query.pageSize = s; query.pageNum = 1; load() }" />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" :title="editForm.id ? '编辑角色' : '新增角色'" style="width: 640px">
      <NForm :model="editForm" label-placement="left" label-width="80">
        <NFormItem label="角色名" required>
          <NInput v-model:value="editForm.name" />
        </NFormItem>
        <NFormItem label="角色编码" required>
          <NInput v-model:value="editForm.code" />
        </NFormItem>
        <NFormItem label="数据权限">
          <NSelect v-model:value="editForm.dataScope" :options="dataScopeOpts" />
        </NFormItem>
        <NFormItem label="菜单权限">
          <NTree
            block-line
            checkable
            cascade
            check-strategy="child"
            :data="menuTreeData"
            :default-checked-keys="checkedKeys"
            @update:checked-keys="(k) => (checkedKeys = k as number[])"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="editForm.remark" type="textarea" />
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
import { NCard, NForm, NFormItem, NInput, NSelect, NButton, NSpace, NDataTable, NModal, NTree, NTag, useDialog, useMessage } from 'naive-ui'
import { pageRoles, createRole, updateRole, deleteRoles, roleMenuTree } from '@/api/system/role'
import { menuTree } from '@/api/system/menu'

const dialog = useDialog()
const message = useMessage()
const query = ref<any>({ name: '', pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const dataScopeOpts = [
  { label: '全部数据', value: 1 },
  { label: '本部门数据', value: 2 },
  { label: '本部门及子部门', value: 3 },
  { label: '仅本人数据', value: 4 },
  { label: '自定义', value: 5 }
]

const columns = [
  { title: '角色名', key: 'name', minWidth: 140 },
  { title: '编码', key: 'code', minWidth: 140 },
  {
    title: '数据权限', key: 'dataScope', width: 140,
    render(row: any) {
      const map: any = { 1: '全部', 2: '本部门', 3: '本部门及子', 4: '仅本人', 5: '自定义' }
      return h(NTag, { size: 'small', type: 'info' }, { default: () => map[row.dataScope] || '-' })
    }
  },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  { title: '备注', key: 'remark', minWidth: 160 },
  {
    title: '操作', key: 'a', width: 200, fixed: 'right' as const,
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeOne(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function load(p?: number) {
  if (p) query.value.pageNum = p
  loading.value = true
  try {
    const res: any = await pageRoles(query.value)
    rows.value = res.records || []
    total.value = res.total || 0
    pagination.value.page = res.current || query.value.pageNum
    pagination.value.pageSize = res.size || query.value.pageSize
    pagination.value.itemCount = total.value
  } finally {
    loading.value = false
  }
}

const editVisible = ref(false)
const editForm = ref<any>({ id: null, name: '', code: '', dataScope: 1, remark: '', menuIds: [] })
const menuTreeData = ref<any[]>([])
const checkedKeys = ref<number[]>([])

function buildTreeOpts(items: any[]): any[] {
  return items.map((m) => {
    const node: any = { key: m.id, label: m.name }
    if (m.children?.length) node.children = buildTreeOpts(m.children)
    return node
  })
}

async function openEdit(row?: any) {
  if (row) {
    editForm.value = { id: row.id, name: row.name, code: row.code, dataScope: row.dataScope, remark: row.remark, menuIds: [] }
  } else {
    editForm.value = { id: null, name: '', code: '', dataScope: 1, remark: '', menuIds: [] }
    checkedKeys.value = []
  }
  const tree: any = await menuTree()
  menuTreeData.value = buildTreeOpts(tree || [])
  if (row) {
    const tree2: any = await roleMenuTree(row.id)
    checkedKeys.value = (tree2?.checkedKeys || []).map((n: any) => Number(n))
  }
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.name || !editForm.value.code) {
    message.warning('请填写角色名和编码')
    return
  }
  editForm.value.menuIds = checkedKeys.value
  try {
    if (editForm.value.id) {
      await updateRole(editForm.value)
      message.success('更新成功')
    } else {
      await createRole(editForm.value)
      message.success('创建成功')
    }
    editVisible.value = false
    load()
  } catch {}
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除',
    content: `确定删除角色 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteRoles([row.id])
      message.success('删除成功')
      load()
    }
  })
}

onMounted(load)
</script>