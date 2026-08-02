<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-title">我的笔记</div>
    </div>

    <NCard>
      <NSpace class="mb-12" align="center">
        <NInput v-model:value="query.keyword" placeholder="搜索标题 / 内容" clearable style="width: 240px"
                @keyup.enter="search" />
        <NInput v-model:value="query.tag" placeholder="按标签过滤" clearable style="width: 160px"
                @keyup.enter="search" />
        <NSelect v-model:value="query.status" placeholder="状态" clearable style="width: 120px"
                 :options="[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]" />
        <NButton type="primary" @click="search">搜索</NButton>
        <NButton @click="reset">重置</NButton>
        <NButton type="primary" v-auth="'note:add'" @click="openEdit()">新增笔记</NButton>
      </NSpace>

      <NDataTable :columns="columns" :data="rows" :loading="loading" :pagination="pagination"
                  @update:page="(p) => { query.pageNum = p; load() }" />
    </NCard>

    <NModal v-model:show="editVisible" preset="card" :title="form.id ? '编辑笔记' : '新增笔记'"
            style="width: 900px" class="note-editor-modal">
      <NForm :model="form" label-placement="top">
        <NFormItem label="标题" required>
          <NInput v-model:value="form.title" placeholder="请输入笔记标题" />
        </NFormItem>
        <NFormItem label="标签">
          <NDynamicTags v-model:value="tagList" :max="8" />
        </NFormItem>
        <NFormItem label="内容">
          <MdEditor v-model="form.content" :theme="editorTheme" class="note-md-editor" />
        </NFormItem>
        <NFormItem label="置顶">
          <NSwitch v-model:value="pinSwitch" />
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
import { computed, h, onMounted, ref, watch } from 'vue'
import {
  NCard, NSpace, NButton, NDataTable, NModal, NForm, NFormItem,
  NInput, NSelect, NSwitch, NTag, NDynamicTags, useDialog, useMessage
} from 'naive-ui'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useAppStore } from '@/stores/app'
import { pageNotes, createNote, updateNote, deleteNotes } from '@/api/note'

const message = useMessage()
const dialog = useDialog()
const appStore = useAppStore()
const editorTheme = computed(() => (appStore.dark ? 'dark' : 'light'))

const query = ref<any>({ pageNum: 1, pageSize: 10 })
const rows = ref<any[]>([])
const loading = ref(false)
const pagination = ref({ page: 1, pageSize: 10, itemCount: 0 })

function search() {
  query.value.pageNum = 1
  load()
}

function reset() {
  query.value = { pageNum: 1, pageSize: 10 }
  load()
}

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  {
    title: '标题', key: 'title', minWidth: 200,
    render(row: any) {
      return h(NSpace, { size: 6, align: 'center' }, () => [
        row.isPinned === 1 ? h(NTag, { type: 'warning', size: 'small', bordered: false }, { default: () => '置顶' }) : null,
        h('span', { class: 'note-title' }, { default: () => row.title })
      ])
    }
  },
  {
    title: '标签', key: 'tags', minWidth: 160,
    render(row: any) {
      const tags = splitTags(row.tags)
      if (!tags.length) return null
      return h(NSpace, { size: 4, wrap: true }, () =>
        tags.map((t) => h(NTag, { size: 'small', type: 'info', bordered: false }, { default: () => t }))
      )
    }
  },
  {
    title: '状态', key: 'status', width: 90,
    render(row: any) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
    }
  },
  { title: '更新时间', key: 'updateTime', minWidth: 170 },
  {
    title: '操作', key: 'a', width: 180, fixed: 'right' as const,
    render(row: any) {
      return h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'tiny', quaternary: true, type: 'primary', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, {
          size: 'tiny', quaternary: true,
          type: row.isPinned === 1 ? 'warning' : 'default',
          onClick: () => togglePin(row)
        }, { default: () => (row.isPinned === 1 ? '取消置顶' : '置顶') }),
        h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeOne(row) }, { default: () => '删除' })
      ])
    }
  }
]

function splitTags(tags?: string): string[] {
  if (!tags) return []
  return tags.split(',').map((t) => t.trim()).filter(Boolean)
}

async function load() {
  loading.value = true
  try {
    const res: any = await pageNotes(query.value)
    rows.value = res.records || []
    pagination.value = { page: res.current || 1, pageSize: res.size || 10, itemCount: res.total || 0 }
  } finally { loading.value = false }
}

const editVisible = ref(false)
const form = ref<any>({ id: null, title: '', content: '', tags: '', isPinned: 0, status: 1 })
const tagList = ref<string[]>([])
const pinSwitch = ref(false)

watch(pinSwitch, (v) => { form.value.isPinned = v ? 1 : 0 })

function openEdit(row?: any) {
  if (row) {
    form.value = { ...row }
    tagList.value = splitTags(row.tags)
    pinSwitch.value = row.isPinned === 1
  } else {
    form.value = { id: null, title: '', content: '', tags: '', isPinned: 0, status: 1 }
    tagList.value = []
    pinSwitch.value = false
  }
  editVisible.value = true
}

async function submit() {
  if (!form.value.title?.trim()) { message.warning('请填写标题'); return }
  form.value.tags = tagList.value.join(',')
  if (form.value.id) await updateNote(form.value)
  else await createNote(form.value)
  message.success('保存成功')
  editVisible.value = false
  load()
}

function togglePin(row: any) {
  updateNote({ ...row, isPinned: row.isPinned === 1 ? 0 : 1 }).then(() => {
    message.success('操作成功')
    load()
  })
}

function removeOne(row: any) {
  dialog.warning({
    title: '确认删除', content: `确定删除笔记「${row.title}」？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await deleteNotes([row.id])
      message.success('删除成功')
      load()
    }
  })
}

onMounted(load)
</script>

<style scoped>
.note-md-editor {
  width: 100%;
  height: 420px;
}
.note-title {
  font-weight: 500;
}
</style>
