<template>
  <div class="page-container">
    <div class="page-header"><div class="page-header-title">参数设置</div></div>
    <NCard>
      <NSpace class="mb-12">
        <NButton type="primary" v-auth="'system:config:add'" @click="openEdit()">新增参数</NButton>
      </NSpace>
      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" title="参数信息" style="width: 520px">
      <NForm :model="form" label-placement="left" label-width="80">
        <NFormItem label="名称" required><NInput v-model:value="form.name" /></NFormItem>
        <NFormItem label="Key" required><NInput v-model:value="form.configKey" /></NFormItem>
        <NFormItem label="Value"><NInput v-model:value="form.configValue" /></NFormItem>
        <NFormItem label="类型"><NSelect v-model:value="form.configType" :options="[{label:'系统',value:1},{label:'业务',value:2}]" /></NFormItem>
        <NFormItem label="备注"><NInput v-model:value="form.remark" /></NFormItem>
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
import { NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NTag, useDialog, useMessage } from 'naive-ui'
import { pageConfig, createConfig, updateConfig, deleteConfig } from '@/api/system/config'

const message = useMessage()
const dialog = useDialog()
const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

const columns = [
  { title: '名称', key: 'name', minWidth: 140 },
  { title: 'Key', key: 'configKey', minWidth: 140 },
  { title: 'Value', key: 'configValue', minWidth: 160 },
  { title: '类型', key: 'configType', width: 90 },
  { title: '备注', key: 'remark', minWidth: 140 },
  {
    title: '操作', key: 'a', width: 160, fixed: 'right',
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => remove(row) }, { default: () => '删除' })
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res: any = await pageConfig(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

const editVisible = ref(false)
const form = ref<any>({ id: null, name: '', configKey: '', configValue: '', configType: 1, remark: '' })

function openEdit(row?: any) {
  form.value = row ? { ...row } : { id: null, name: '', configKey: '', configValue: '', configType: 1, remark: '' }
  editVisible.value = true
}

async function submit() {
  if (!form.value.name || !form.value.configKey) { message.warning('请填写名称和 Key'); return }
  if (form.value.id) await updateConfig(form.value)
  else await createConfig(form.value)
  message.success('保存成功')
  editVisible.value = false
  load()
}

function remove(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除 ${row.name}?`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteConfig([row.id])
      message.success('删除成功')
      load()
    }
  })
}

onMounted(load)
</script>