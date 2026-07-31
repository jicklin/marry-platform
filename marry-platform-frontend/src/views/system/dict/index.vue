<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">字典管理</div></div>
    <NCard>
      <NTabs v-model:value="tab" type="line" animated>
        <NTabPane name="type" tab="字典类型">
          <NSpace class="mb-12">
            <NButton type="primary" v-auth="'system:dict:add'" @click="openTypeEdit()">新增类型</NButton>
          </NSpace>
          <NDataTable :columns="typeCols" :data="typeRows" :loading="typeLoading" :pagination="typePage"
                      @update:page="(p) => { typeQ.pageNum = p; loadTypes() }" />
        </NTabPane>
        <NTabPane name="data" tab="字典数据">
          <NSpace class="mb-12">
            <NButton type="primary" v-auth="'system:dict:add'" @click="openDataEdit()">新增数据</NButton>
          </NSpace>
          <NDataTable :columns="dataCols" :data="dataRows" :loading="dataLoading" :pagination="dataPage"
                      @update:page="(p) => { dataQ.pageNum = p; loadData() }" />
        </NTabPane>
      </NTabs>
    </NCard>

    <NModal v-model:show="typeEditVisible" preset="card" title="字典类型" style="width: 480px">
      <NForm :model="typeForm" label-placement="left" label-width="80">
        <NFormItem label="名称" required><NInput v-model:value="typeForm.name" /></NFormItem>
        <NFormItem label="编码" required><NInput v-model:value="typeForm.type" /></NFormItem>
        <NFormItem label="备注"><NInput v-model:value="typeForm.remark" /></NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="typeEditVisible = false">取消</NButton>
          <NButton type="primary" @click="submitType">确定</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="dataEditVisible" preset="card" title="字典数据" style="width: 520px">
      <NForm :model="dataForm" label-placement="left" label-width="80">
        <NFormItem label="字典类型" required><NInput v-model:value="dataForm.dictType" /></NFormItem>
        <NFormItem label="标签" required><NInput v-model:value="dataForm.label" /></NFormItem>
        <NFormItem label="值" required><NInput v-model:value="dataForm.value" /></NFormItem>
        <NFormItem label="样式"><NInput v-model:value="dataForm.cssClass" /></NFormItem>
        <NFormItem label="排序"><NInputNumber v-model:value="dataForm.orderNum" :min="0" /></NFormItem>
        <NFormItem label="备注"><NInput v-model:value="dataForm.remark" /></NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="dataEditVisible = false">取消</NButton>
          <NButton type="primary" @click="submitData">确定</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NCard, NTabs, NTabPane, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NInputNumber, NTag, useDialog, useMessage } from 'naive-ui'
import { pageDictTypes, createDictType, updateDictType, deleteDictTypes, pageDictData, createDictData, updateDictData, deleteDictData } from '@/api/system/dict'

const message = useMessage()
const dialog = useDialog()
const tab = ref<'type' | 'data'>('type')

// === types ===
const typeQ = ref<any>({ pageNum: 1, pageSize: 10 })
const typeRows = ref<any[]>([])
const typeLoading = ref(false)
const typePage = ref({ page: 1, pageSize: 10, itemCount: 0 })
const typeEditVisible = ref(false)
const typeForm = ref<any>({ id: null, name: '', type: '', remark: '' })

const typeCols = [
  { title: '名称', key: 'name', minWidth: 160 },
  { title: '编码', key: 'type', minWidth: 160 },
  { title: '备注', key: 'remark', minWidth: 160 },
  {
    title: '操作', key: 'a', width: 160, fixed: 'right' as const,
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openTypeEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeType(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function loadTypes() {
  typeLoading.value = true
  try {
    const res: any = await pageDictTypes(typeQ.value)
    typeRows.value = res.records || []
    typePage.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { typeLoading.value = false }
}

function openTypeEdit(row?: any) {
  typeForm.value = row ? { ...row } : { id: null, name: '', type: '', remark: '' }
  typeEditVisible.value = true
}

async function submitType() {
  if (!typeForm.value.name || !typeForm.value.type) { message.warning('请填写名称和编码'); return }
  if (typeForm.value.id) await updateDictType(typeForm.value)
  else await createDictType(typeForm.value)
  message.success('保存成功')
  typeEditVisible.value = false
  loadTypes()
}

function removeType(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDictTypes([row.id])
      message.success('删除成功')
      loadTypes()
    }
  })
}

// === data ===
const dataQ = ref<any>({ pageNum: 1, pageSize: 10 })
const dataRows = ref<any[]>([])
const dataLoading = ref(false)
const dataPage = ref({ page: 1, pageSize: 10, itemCount: 0 })
const dataEditVisible = ref(false)
const dataForm = ref<any>({ id: null, dictType: '', label: '', value: '', cssClass: '', orderNum: 0, remark: '' })

const dataCols = [
  { title: '类型', key: 'dictType', minWidth: 140 },
  { title: '标签', key: 'label', minWidth: 120 },
  { title: '值', key: 'value', minWidth: 120 },
  { title: '样式', key: 'cssClass', minWidth: 100 },
  { title: '排序', key: 'orderNum', width: 80 },
  {
    title: '状态', key: 'status', width: 80,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  {
    title: '操作', key: 'a', width: 160, fixed: 'right' as const,
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openDataEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeData(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function loadData() {
  dataLoading.value = true
  try {
    const res: any = await pageDictData(dataQ.value)
    dataRows.value = res.records || []
    dataPage.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { dataLoading.value = false }
}

function openDataEdit(row?: any) {
  dataForm.value = row ? { ...row } : { id: null, dictType: '', label: '', value: '', cssClass: '', orderNum: 0, remark: '' }
  dataEditVisible.value = true
}

async function submitData() {
  if (!dataForm.value.label || !dataForm.value.value) { message.warning('请填写标签和值'); return }
  if (dataForm.value.id) await updateDictData(dataForm.value)
  else await createDictData(dataForm.value)
  message.success('保存成功')
  dataEditVisible.value = false
  loadData()
}

function removeData(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除 ${row.label}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteDictData([row.id])
      message.success('删除成功')
      loadData()
    }
  })
}

onMounted(() => { loadTypes(); loadData() })
</script>