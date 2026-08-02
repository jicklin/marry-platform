<template>
  <div class="event-page">
    <!-- Welcome banner -->
    <div class="hero glass-card">
      <div class="hero-emoji">🎒</div>
      <div class="hero-text">
        <h2 class="hero-title">宝贝成长记</h2>
        <p class="hero-sub">记录每一个值得珍藏的瞬间，共 {{ totalCount }} 个成长片段</p>
      </div>
      <NButton type="primary" size="large" round v-auth="'event:add'" @click="openEdit()">
        <template #icon><NIcon><AddOutline /></NIcon></template>
        记录新事件
      </NButton>
    </div>

    <!-- Filters -->
    <NCard class="filter-card" :bordered="false">
      <NSpace align="center" wrap :size="12">
        <NInput v-model:value="query.keyword" placeholder="搜索标题 / 内容" clearable style="width: 220px"
                @keyup.enter="search" />
        <NSelect v-model:value="query.category" placeholder="分类" clearable style="width: 130px" :options="categoryOptions" />
        <NSelect v-model:value="query.importance" placeholder="重要程度" clearable style="width: 130px" :options="importanceOptions" />
        <NRadioGroup v-model:value="range" size="small">
          <NRadioButton value="all">全部</NRadioButton>
          <NRadioButton value="week">近一周</NRadioButton>
          <NRadioButton value="month">本月</NRadioButton>
        </NRadioGroup>
        <NButton type="primary" @click="search">搜索</NButton>
        <NButton @click="reset">重置</NButton>
      </NSpace>
    </NCard>

    <!-- Timeline -->
    <div class="timeline-wrap">
      <template v-for="group in groupedEvents" :key="group.month">
        <div class="month-divider">
          <span class="month-badge">{{ group.month }}</span>
          <span class="month-line" />
        </div>

        <div class="timeline-list">
          <div class="timeline-item" v-for="ev in group.list" :key="ev.id">
            <div class="tl-dot" :class="'dot-' + (ev.category || 'other')" />
            <div class="tl-card glass-card">
              <div class="tl-head">
                <div class="tl-tags">
                  <NTag v-if="ev.category" size="small" :bordered="false" :color="categoryColor(ev.category)">
                    {{ ev.category }}
                  </NTag>
                  <NTag v-if="ev.importance === 1" size="small" type="warning" :bordered="false">⭐ 重要</NTag>
                  <NTag v-if="ev.importance === 2" size="small" type="error" :bordered="false">🏆 里程碑</NTag>
                </div>
                <div class="tl-date">{{ formatDate(ev.eventDate) }}</div>
              </div>

              <h3 class="tl-title">{{ ev.title }}</h3>

              <div v-if="ev.content" class="tl-content" :class="{ expanded: isExpanded(ev.id) }">
                <MdPreview :model-value="ev.content" :theme="editorTheme" class="md-preview" />
                <div v-if="contentLong(ev) && !isExpanded(ev.id)" class="tl-mask" />
              </div>
              <div v-if="contentLong(ev)" class="tl-toggle" @click="toggleExpand(ev.id)">
                <NIcon size="13"><span>{{ isExpanded(ev.id) ? '▲' : '▼' }}</span></NIcon>
                {{ isExpanded(ev.id) ? '收起' : '展开全文' }}
              </div>

              <!-- image grid from attachments (inline images already inside content) -->
              <div v-if="imageUrls(ev).length" class="tl-imgs">
                <NImageGroup>
                  <NImage v-for="(u, i) in imageUrls(ev)" :key="i" :src="u" width="110" height="110"
                          object-fit="cover" class="tl-img" />
                </NImageGroup>
              </div>

              <!-- download-type attachments -->
              <div v-if="fileAttachments(ev).length" class="tl-files">
                <div v-for="f in fileAttachments(ev)" :key="f.id" class="tl-file">
                  <NIcon size="16" class="file-ic"><DocumentAttachOutline /></NIcon>
                  <a :href="f.url" target="_blank" class="file-name">{{ f.originalName }}</a>
                  <span class="file-size">{{ formatSize(f.size) }}</span>
                </div>
              </div>

              <div class="tl-foot">
                <span v-if="ev.dirName" class="tl-dir">📁 {{ ev.dirName }}</span>
                <span v-if="ev.mood" class="tl-mood">{{ ev.mood }}</span>
                <NSpace class="tl-actions" :size="4">
                  <NButton size="tiny" quaternary type="primary" @click="openShare(ev)">分享成长图</NButton>
                  <NButton size="tiny" quaternary type="primary" v-auth="'event:edit'" @click="openEdit(ev)">编辑</NButton>
                  <NButton size="tiny" quaternary type="error" v-auth="'event:remove'" @click="removeOne(ev)">删除</NButton>
                </NSpace>
              </div>
            </div>
          </div>
        </div>
      </template>

      <NEmpty v-if="!loading && !rows.length" description="还没有记录，快记录第一个成长瞬间吧 🧸" class="empty-state" />
      <div v-if="loading" class="loading-state">
        <NSpin size="small" />
      </div>
    </div>

    <!-- Edit modal -->
    <NModal v-model:show="editVisible" preset="card" :title="form.id ? '编辑事件' : '记录新事件'"
            style="width: 960px" class="event-editor-modal">
      <NForm :model="form" label-placement="top">
        <NFormItem label="标题" required>
          <NInput v-model:value="form.title" placeholder="如：开学第一天" maxlength="60" show-count />
        </NFormItem>
        <NFormItem label="日期" required>
          <NDatePicker v-model:value="dateTs" type="date" style="width: 180px" placeholder="事件发生日期" />
        </NFormItem>
        <NFormItem label="分类">
          <NSelect v-model:value="form.category" placeholder="选择分类" :options="categoryOptions" style="width: 180px" />
        </NFormItem>
        <NFormItem label="重要程度">
          <NRadioGroup v-model:value="form.importance">
            <NRadioButton :value="0">普通</NRadioButton>
            <NRadioButton :value="1">重要</NRadioButton>
            <NRadioButton :value="2">里程碑</NRadioButton>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="心情">
          <NInput v-model:value="form.mood" placeholder="如：开心 😊" maxlength="50" style="width: 260px" />
        </NFormItem>
        <NFormItem label="标签">
          <NDynamicTags v-model:value="tagList" :max="8" />
        </NFormItem>
        <NFormItem label="文件目录（图片/附件将存到该目录，方便在服务器上按事件查看）">
          <NSpace align="center">
            <NInput v-model:value="form.dirName" placeholder="如：2026-09-01_开学第一天" style="width: 340px" />
            <NButton size="small" @click="autoDir">自动生成</NButton>
          </NSpace>
        </NFormItem>
        <NFormItem label="记录内容（文字中间可直接插入图片）">
          <MdEditor v-model="form.content" :theme="editorTheme" :on-upload-img="handleUploadImg"
                    class="event-md-editor" />
        </NFormItem>
        <NFormItem label="附加文件（PDF / 文档等）">
          <NUpload accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.txt"
                   :show-file-list="false" :default-upload="false" :custom-request="customUpload">
            <NButton size="small">选择文件上传</NButton>
          </NUpload>
          <div v-if="pendingFiles.length" class="pending-files">
            <div v-for="(f, i) in pendingFiles" :key="i" class="tl-file">
              <NIcon size="16" class="file-ic"><DocumentAttachOutline /></NIcon>
              <span class="file-name">{{ f.originalName }}</span>
              <span class="file-size">{{ formatSize(f.size) }}</span>
              <NButton size="tiny" text type="error" @click="pendingFiles.splice(i, 1)">移除</NButton>
            </div>
          </div>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="editVisible = false">取消</NButton>
          <NButton type="primary" :loading="submitting" @click="submit">确定</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Share growth image -->
    <ShareCard ref="shareCardRef" :event="shareEvent" />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NCard, NSpace, NButton, NModal, NForm, NFormItem, NInput, NSelect, NRadioGroup,
  NRadioButton, NDatePicker, NTag, NImage, NImageGroup, NIcon, NSpin, NEmpty,
  NDynamicTags, NUpload, useMessage, useDialog
} from 'naive-ui'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import dayjs from 'dayjs'
import { AddOutline, DocumentAttachOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { pageEvents, createEvent, updateEvent, deleteEvents, attachFile, detachFile } from '@/api/event'
import { uploadFile } from '@/api/system/file'
import type { ChildEvent, ChildEventFile } from '@/api/types'
import ShareCard from './components/ShareCard.vue'

const message = useMessage()
const dialog = useDialog()
const appStore = useAppStore()
const editorTheme = computed(() => (appStore.dark ? 'dark' : 'light'))

const categoryOptions = [
  { label: '学习', value: '学习' },
  { label: '运动', value: '运动' },
  { label: '日常', value: '日常' },
  { label: '纪念', value: '纪念' },
  { label: '成长', value: '成长' }
]
const importanceOptions = [
  { label: '普通', value: 0 },
  { label: '重要', value: 1 },
  { label: '里程碑', value: 2 }
]
const categoryColors: Record<string, { color: string; textColor: string }> = {
  学习: { color: '#dbeafe', textColor: '#1d4ed8' },
  运动: { color: '#dcfce7', textColor: '#15803d' },
  日常: { color: '#fef3c7', textColor: '#b45309' },
  纪念: { color: '#fce7f3', textColor: '#be185d' },
  成长: { color: '#ede9fe', textColor: '#6d28d9' }
}
function categoryColor(cat: string) {
  const c = categoryColors[cat]
  return c ? { color: c.color, textColor: c.textColor } : undefined
}

const rows = ref<ChildEvent[]>([])
const loading = ref(false)
const totalCount = ref(0)
const query = ref<Record<string, any>>({ pageNum: 1, pageSize: 100 })
const range = ref<'all' | 'week' | 'month'>('all')

async function load() {
  loading.value = true
  try {
    const params: Record<string, any> = { ...query.value }
    if (range.value === 'week') params.startDate = dayjs().subtract(7, 'day').format('YYYY-MM-DD')
    if (range.value === 'month') params.startDate = dayjs().startOf('month').format('YYYY-MM-DD')
    const res: any = await pageEvents(params)
    rows.value = res.records || []
    totalCount.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.value.pageNum = 1
  load()
}

function reset() {
  query.value = { pageNum: 1, pageSize: 100 }
  range.value = 'all'
  load()
}

const groupedEvents = computed(() => {
  const map = new Map<string, ChildEvent[]>()
  for (const ev of rows.value) {
    const month = dayjs(ev.eventDate).format('YYYY年M月')
    if (!map.has(month)) map.set(month, [])
    map.get(month)!.push(ev)
  }
  return Array.from(map.entries()).map(([month, list]) => ({ month, list }))
})

const expandedIds = ref<Set<number>>(new Set())

function contentLong(ev: ChildEvent) {
  return (ev.content || '').length > 220
}

function isExpanded(id: number) {
  return expandedIds.value.has(id)
}

function toggleExpand(id: number) {
  const next = new Set(expandedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expandedIds.value = next
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('M月D日') : '-'
}

function formatSize(size?: number) {
  if (size == null) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

function imageUrls(ev: ChildEvent): string[] {
  const urls: string[] = []
  ev.attachFiles?.filter((f) => f.mediaType === 'image' && f.url).forEach((f) => urls.push(f.url!))
  return urls.slice(0, 9)
}

function fileAttachments(ev: ChildEvent): ChildEventFile[] {
  return ev.attachFiles?.filter((f) => f.mediaType !== 'image') || []
}

// ---------------- edit ----------------
const editVisible = ref(false)
const submitting = ref(false)
const form = ref<any>({ id: null, title: '', content: '', eventDate: '', category: null, importance: 0, mood: '', dirName: '', tags: '' })
const dateTs = ref<number | null>(null)
const tagList = ref<string[]>([])
const pendingFiles = ref<{ id: number; originalName: string; size: number }[]>([])

function openEdit(row?: ChildEvent) {
  if (row) {
    form.value = {
      id: row.id, title: row.title, content: row.content || '', eventDate: row.eventDate,
      category: row.category || null, importance: row.importance ?? 0, mood: row.mood || '',
      dirName: row.dirName || ''
    }
    dateTs.value = row.eventDate ? dayjs(row.eventDate).valueOf() : null
    tagList.value = row.tags ? row.tags.split(',').map((t) => t.trim()).filter(Boolean) : []
  } else {
    form.value = { id: null, title: '', content: '', eventDate: '', category: null, importance: 0, mood: '', dirName: '' }
    dateTs.value = null
    tagList.value = []
  }
  pendingFiles.value = []
  editVisible.value = true
}

function autoDir() {
  const d = dateTs.value ? dayjs(dateTs.value).format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD')
  const t = form.value.title.trim()
  if (t) form.value.dirName = `${d}_${t.replace(/[\\/:*?"<>|\s]+/g, '_')}`
  else form.value.dirName = d
}

const handleUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  try {
    const urls: string[] = []
    for (const f of files) {
      const res: any = await uploadFile(f, form.value.dirName)
      urls.push(res.url)
    }
    callback(urls)
  } catch (e) {
    message.error('图片上传失败')
  }
}

const customUpload = async ({ file, onFinish, onError }: any) => {
  try {
    const res: any = await uploadFile(file.file, form.value.dirName)
    pendingFiles.value.push({ id: res.id, originalName: res.originalName, size: res.size })
    onFinish()
  } catch (e) {
    onError()
    message.error('文件上传失败')
  }
}

async function submit() {
  if (!form.value.title?.trim()) { message.warning('请填写标题'); return }
  if (!dateTs.value) { message.warning('请选择日期'); return }
  form.value.eventDate = dayjs(dateTs.value).format('YYYY-MM-DD')
  form.value.tags = tagList.value.join(',')

  submitting.value = true
  try {
    let eventId: number
    if (form.value.id) {
      await updateEvent(form.value)
      eventId = form.value.id
    } else {
      eventId = await createEvent({ ...form.value, dirName: form.value.dirName || undefined })
    }
    for (const f of pendingFiles.value) {
      await attachFile(eventId, f.id, 'file')
    }
    message.success('保存成功')
    editVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

function removeOne(row: ChildEvent) {
  dialog.warning({
    title: '确认删除',
    content: `确定删除「${row.title}」吗？关联的附件记录也会一并移除。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteEvents([row.id])
      message.success('删除成功')
      load()
    }
  })
}

// ---------------- share ----------------
const shareCardRef = ref<{ open: () => void } | null>(null)
const shareEvent = ref<ChildEvent | null>(null)

function openShare(row: ChildEvent) {
  shareEvent.value = row
  shareCardRef.value?.open()
}

onMounted(load)
</script>

<style scoped>
.event-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 26px 30px;
  background: linear-gradient(120deg, #fff7ed 0%, #fef3c7 45%, #fde68a 100%);
  border-radius: 18px;
}
.hero-emoji {
  font-size: 44px;
  filter: drop-shadow(0 4px 8px rgba(217, 119, 6, 0.25));
}
.hero-text {
  flex: 1;
}
.hero-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #92400e;
}
.hero-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #b45309;
}

.filter-card {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.7);
}

.timeline-wrap {
  padding: 4px 2px 40px;
}

.month-divider {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 26px 0 16px;
}
.month-badge {
  font-size: 17px;
  font-weight: 700;
  color: #92400e;
  padding: 4px 14px;
  border-radius: 999px;
  background: linear-gradient(120deg, #fef3c7, #fde68a);
}
.month-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(217, 119, 6, 0.35), transparent);
}

.timeline-list {
  position: relative;
  padding-left: 30px;
}
.timeline-list::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  border-radius: 2px;
  background: linear-gradient(180deg, #fcd34d, #f9a8d4, #93c5fd);
}

.timeline-item {
  position: relative;
  margin-bottom: 18px;
}
.tl-dot {
  position: absolute;
  left: -30px;
  top: 26px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.3);
}
.dot-学习 { background: #3b82f6; }
.dot-运动 { background: #22c55e; }
.dot-日常 { background: #f59e0b; }
.dot-纪念 { background: #ec4899; }
.dot-成长 { background: #8b5cf6; }
.dot-other { background: #a8a29e; }

.tl-card {
  border-radius: 16px;
  padding: 18px 22px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.tl-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.09);
}

.tl-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.tl-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tl-date {
  font-size: 13px;
  color: #a8a29e;
  font-weight: 600;
}

.tl-title {
  margin: 10px 0 8px;
  font-size: 19px;
  font-weight: 700;
  color: #292524;
}

.tl-content {
  color: #57534e;
  font-size: 14px;
  line-height: 1.7;
  position: relative;
  max-height: 260px;
  overflow: hidden;
  transition: max-height 0.25s ease;
}
.tl-content.expanded {
  max-height: none;
  overflow: visible;
}
.tl-content :deep(.md-editor),
.tl-content :deep(.md-editor-previewOnly),
.tl-content :deep(.md-editor-content),
.tl-content :deep(.md-editor-preview-wrapper),
.tl-content :deep(.md-editor-html),
.tl-content :deep(.md-editor-preview) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}
/* 让 markdown 内容跟随卡片配色，保证可读性 */
.tl-content :deep(.md-editor) {
  --md-bk-color: transparent;
  --md-bk-color-outstand: rgba(0, 0, 0, 0.05);
  --md-bk-hover-color: transparent;
  --md-border-color: transparent;
  --md-border-hover-color: transparent;
  --md-border-active-color: transparent;
  --md-color: #57534e;
  --md-theme-color: #57534e;
  --md-theme-bg-color: transparent;
  --md-theme-bg-color-inset: rgba(0, 0, 0, 0.04);
  color: #57534e;
}
html.dark .tl-content :deep(.md-editor) {
  --md-bk-color-outstand: rgba(255, 255, 255, 0.06);
  --md-color: #d6d3d1;
  --md-theme-color: #d6d3d1;
  --md-theme-bg-color-inset: rgba(255, 255, 255, 0.06);
  color: #d6d3d1;
}
.tl-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 70px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.95) 100%);
  pointer-events: none;
}
.tl-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #d97706;
  cursor: pointer;
  user-select: none;
}
.tl-toggle:hover {
  color: #b45309;
}

.tl-imgs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}
.tl-img {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tl-files {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.tl-file {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  background: #fafaf9;
  border-radius: 8px;
  padding: 6px 10px;
  max-width: 480px;
}
.file-ic { color: #f59e0b; }
.file-name {
  color: #1d4ed8;
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-size { color: #a8a29e; font-size: 12px; }

.tl-foot {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed #e7e5e4;
}
.tl-dir {
  font-size: 12px;
  color: #a8a29e;
  background: #fafaf9;
  border-radius: 6px;
  padding: 3px 8px;
}
.tl-mood {
  font-size: 13px;
  color: #d97706;
}
.tl-actions {
  margin-left: auto;
}

.event-md-editor {
  width: 100%;
  height: 420px;
}

.pending-files {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.empty-state {
  margin-top: 60px;
}
.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px;
}

html.dark .hero {
  background: linear-gradient(120deg, #451a03, #78350f 60%, #92400e);
}
html.dark .hero-title { color: #fde68a; }
html.dark .hero-sub { color: #fcd34d; }
html.dark .month-badge { color: #fde68a; background: linear-gradient(120deg, #78350f, #92400e); }
html.dark .tl-card { background: rgba(41, 37, 36, 0.85); }
html.dark .tl-title { color: #f5f5f4; }
html.dark .tl-content { color: #d6d3d1; }
html.dark .tl-mask { background: linear-gradient(180deg, rgba(41, 37, 36, 0) 0%, rgba(41, 37, 36, 0.9) 100%); }
html.dark .tl-toggle:hover { color: #fbbf24; }
html.dark .tl-file { background: #292524; }
html.dark .tl-dir { background: #292524; color: #a8a29e; }
</style>
