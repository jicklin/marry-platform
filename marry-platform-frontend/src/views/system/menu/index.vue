<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">菜单管理</div></div>
    <NCard>
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:menu:add'" @click="openEdit()">新增菜单</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :row-key="(r) => r.id" default-expand-all />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" :title="editForm.id ? '编辑菜单' : '新增菜单'" style="width: 600px">
      <NForm :model="editForm" label-placement="left" label-width="100">
        <NFormItem label="上级菜单">
          <NTreeSelect :options="parentOpts" v-model:value="editForm.parentId" />
        </NFormItem>
        <NFormItem label="菜单类型">
          <NRadioGroup v-model:value="editForm.menuType">
            <NRadio value="M">目录</NRadio>
            <NRadio value="C">菜单</NRadio>
            <NRadio value="F">按钮</NRadio>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="菜单名称" required>
          <NInput v-model:value="editForm.name" />
        </NFormItem>
        <NFormItem label="权限标识" v-if="editForm.menuType === 'F'">
          <NInput v-model:value="editForm.perm" placeholder="如 system:user:add" />
        </NFormItem>
        <NFormItem label="路由路径" v-if="editForm.menuType !== 'F'">
          <NInput v-model:value="editForm.path" placeholder="如 user" />
        </NFormItem>
        <NFormItem label="组件路径" v-if="editForm.menuType === 'C'">
          <NInput v-model:value="editForm.component" placeholder="如 system/user/index" />
        </NFormItem>
        <NFormItem label="图标" v-if="editForm.menuType !== 'F'">
          <NInput v-model:value="editForm.icon" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="editForm.orderNum" :min="0" />
        </NFormItem>
        <NFormItem label="显示">
          <NSwitch v-model:value="visibleSwitch" />
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
  NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NInputNumber,
  NRadioGroup, NRadio, NSwitch, NTreeSelect, NTag, useDialog, useMessage
} from 'naive-ui'
import { menuTree, createMenu, updateMenu, deleteMenu } from '@/api/system/menu'

const message = useMessage()
const dialog = useDialog()

const rows = ref<any[]>([])
const loading = ref(false)

const columns = [
  { title: '菜单名称', key: 'name', minWidth: 180 },
  {
    title: '类型', key: 'menuType', width: 90,
    render(row: any) {
      const map: any = { M: ['info', '目录'], C: ['success', '菜单'], F: ['warning', '按钮'] }
      return h(NTag, { type: map[row.menuType]?.[0], size: 'small' }, { default: () => map[row.menuType]?.[1] })
    }
  },
  { title: '权限标识', key: 'perm', minWidth: 160 },
  { title: '路径', key: 'path', minWidth: 100 },
  { title: '组件', key: 'component', minWidth: 160 },
  { title: '排序', key: 'orderNum', width: 80 },
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
  try {
    rows.value = await menuTree()
  } finally { loading.value = false }
}

const editVisible = ref(false)
const editForm = ref<any>({ id: null, parentId: 0, name: '', menuType: 'C', path: '', component: '', perm: '', icon: '', orderNum: 0, visible: 1 })
const visibleSwitch = ref(true)
const parentOpts = ref<any[]>([])

watch(visibleSwitch, (v) => { editForm.value.visible = v ? 1 : 0 })

function buildParentOpts(items: any[]): any[] {
  const opts: any[] = [{ label: '顶级', value: 0 }]
  for (const m of items) {
    opts.push({ label: m.name, value: m.id })
    if (m.children?.length) opts.push(...buildParentOpts(m.children))
  }
  return opts
}

async function openEdit(row?: any) {
  if (parentOpts.value.length === 0) {
    parentOpts.value = buildParentOpts(rows.value)
  }
  if (row) {
    editForm.value = { ...row }
    visibleSwitch.value = row.visible !== 0
  } else {
    editForm.value = { id: null, parentId: 0, name: '', menuType: 'C', path: '', component: '', perm: '', icon: '', orderNum: 0, visible: 1 }
    visibleSwitch.value = true
  }
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.name) { message.warning('请填写菜单名称'); return }
  try {
    if (editForm.value.id) {
      await updateMenu(editForm.value)
      message.success('更新成功')
    } else {
      await createMenu(editForm.value)
      message.success('创建成功')
    }
    editVisible.value = false
    load()
  } catch {}
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除',
    content: `确定删除菜单 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMenu(row.id)
        message.success('删除成功')
        load()
      } catch (e: any) {
        /* message handled by interceptor */
      }
    }
  })
}

import { watch } from 'vue'
onMounted(load)
</script>