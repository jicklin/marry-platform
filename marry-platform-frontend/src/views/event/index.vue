<template>
  <div class="event-page">
    <PigWonderland />

    <!-- Growth album header -->
    <section class="hero">
      <div class="hero-deco" aria-hidden="true">
        <span class="deco-circle c1" />
        <span class="deco-circle c2" />
        <span class="deco-circle c3" />
      </div>

      <div class="hero-copy">
        <div class="hero-top-row">
          <div class="hero-kicker"><span class="kicker-dot" /> GROWTH ALBUM</div>
          <div class="hero-counter-badge mobile-only">
            <strong>{{ totalCount }}</strong>
            <span>个成长片段</span>
          </div>
        </div>
        <div class="hero-title-row">
          <h2 class="hero-title">宝贝成长记 <span class="title-sparkle">✦</span></h2>
          <div class="hero-mini-pig mobile-only">
            <img :src="flowerPigUrl" alt="小猪" />
          </div>
        </div>
        <p class="hero-sub">把闪闪发光的小日子，认真收藏进时光里。</p>
        <div class="hero-meta desktop-only">
          <div class="hero-counter">
            <strong>{{ totalCount }}</strong>
            <span>个成长片段</span>
          </div>
          <span class="meta-divider" />
          <span class="hero-wish">每一次回看，都是温柔的相遇</span>
        </div>
      </div>

      <div class="hero-visual">
        <div class="pig-picture desktop-only">
          <span class="picture-star star-one">✦</span>
          <span class="picture-star star-two">✦</span>
          <img :src="flowerPigUrl" alt="戴小花的可爱小猪" />
        </div>
        <NButton class="hero-add-btn" type="primary" color="#f06586" size="large" round
                 v-auth="'event:add'" @click="openEdit()">
          <template #icon><NIcon><AddOutline /></NIcon></template>
          记录新故事
        </NButton>
      </div>
    </section>

    <!-- Filters -->
    <NCard class="filter-card" :bordered="false">
      <div class="filter-container">
        <div class="filter-row-top">
          <NInput v-model:value="query.keyword" placeholder="搜索标题 / 内容…" clearable class="filter-search-input"
                  @keyup.enter="search">
            <template #prefix><NIcon size="14" style="opacity:.45"><SearchOutline /></NIcon></template>
          </NInput>
          <div class="filter-selects">
            <NSelect v-model:value="query.category" placeholder="全部分类" clearable class="filter-select" :options="categoryOptions" />
            <NSelect v-model:value="query.importance" placeholder="重要程度" clearable class="filter-select" :options="importanceOptions" />
          </div>
        </div>
        <div class="filter-row-bottom">
          <NRadioGroup v-model:value="range" size="small" class="filter-range-group">
            <NRadioButton value="all">全部</NRadioButton>
            <NRadioButton value="week">近一周</NRadioButton>
            <NRadioButton value="month">本月</NRadioButton>
          </NRadioGroup>
          <div class="filter-btns">
            <NButton type="primary" class="filter-btn" @click="search">搜索</NButton>
            <NButton class="filter-btn" @click="reset">重置</NButton>
          </div>
        </div>
      </div>
    </NCard>

    <!-- Timeline -->
    <div class="timeline-wrap">
      <template v-for="group in groupedEvents" :key="group.month">
        <div class="month-divider">
          <span class="month-badge">
            {{ group.month }}
            <span class="month-count">{{ group.list.length }}</span>
          </span>
          <span class="month-line" />
        </div>

        <div class="timeline-list">
          <div class="timeline-item" v-for="ev in group.list" :key="ev.id">
            <div class="tl-dot" :class="'dot-' + (ev.category || 'other')" />
            <div class="tl-card" :class="'cat-' + (ev.category || 'other')">
              <!-- Card header: date + tags -->
              <div class="tl-head">
                <div class="tl-date">{{ formatDateFull(ev.eventDate) }}</div>
                <div class="tl-tags">
                  <NTag v-if="ev.category" size="small" :bordered="false" :color="categoryColor(ev.category)">
                    {{ ev.category }}
                  </NTag>
                  <NTag v-if="ev.importance === 1" size="small" type="warning" :bordered="false">⭐ 重要</NTag>
                  <NTag v-if="ev.importance === 2" size="small" type="error" :bordered="false">🏆 里程碑</NTag>
                </div>
              </div>

              <h3 class="tl-title">{{ ev.title }}</h3>

              <div v-if="previewContent(ev)" class="tl-content" :class="{ expanded: isExpanded(ev.id) }">
                <MdPreview :model-value="previewContent(ev)" :theme="editorTheme" class="md-preview" />
                <div v-if="contentLong(ev) && !isExpanded(ev.id)" class="tl-mask" />
              </div>
              <div v-if="contentLong(ev)" class="tl-toggle" @click="toggleExpand(ev.id)">
                {{ isExpanded(ev.id) ? '▲ 收起' : '▼ 展开全文' }}
              </div>

              <!-- Inline and attached media use fixed thumbnails; click to preview originals. -->
              <div v-if="mediaItems(ev).length" class="tl-media-section">
                <div class="tl-media-head">
                  <span>影像记录</span>
                  <small>{{ mediaItems(ev).length }} 项</small>
                </div>
                <NImageGroup>
                  <div class="tl-media-grid" :class="`grid-count-${Math.min(mediaItems(ev).length, 3)}`">
                    <template v-for="(media, i) in mediaItems(ev)" :key="`${media.type}-${media.url}-${i}`">
                      <NImage v-if="media.type === 'image'" :src="media.url" :alt="media.name || ev.title"
                              object-fit="cover" lazy class="tl-media-image" />
                      <button v-else type="button" class="tl-video-thumb"
                              :aria-label="`播放视频 ${media.name || i + 1}`" @click="openVideoPreview(media)">
                        <video :src="media.url" preload="metadata" muted />
                        <span class="video-shade" />
                        <span class="video-play"><span class="play-triangle" /></span>
                        <span class="video-label">点击播放</span>
                      </button>
                    </template>
                  </div>
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
                <div class="tl-foot-left">
                  <span v-if="ev.mood" class="tl-mood">{{ ev.mood }}</span>
                  <span v-if="ev.dirName" class="tl-dir">📁 {{ ev.dirName }}</span>
                </div>
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

      <!-- Pagination -->
      <div v-if="totalCount > 0" class="pagination-wrap">
        <NPagination
          v-model:page="query.pageNum"
          v-model:page-size="query.pageSize"
          :item-count="totalCount"
          show-size-picker
          :page-sizes="[3, 5, 10, 20, 50]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        >
          <template #prefix="{ itemCount }">
            <span class="pagination-count">共 {{ itemCount }} 条</span>
          </template>
        </NPagination>
      </div>
    </div>

    <!-- Edit modal -->
    <NModal v-model:show="editVisible" preset="card" :title="form.id ? '编辑事件' : '记录新事件'"
            style="width: min(960px, 94vw)" class="event-editor-modal">
      <NForm :model="form" label-placement="top">
        <NFormItem label="标题" required>
          <NInput v-model:value="form.title" placeholder="如：开学第一天" maxlength="60" show-count />
        </NFormItem>
        <div class="form-grid-row">
          <NFormItem label="日期" required class="form-grid-item">
            <NDatePicker v-model:value="dateTs" type="date" class="full-width" placeholder="事件发生日期" />
          </NFormItem>
          <NFormItem label="分类" class="form-grid-item">
            <NSelect v-model:value="form.category" placeholder="选择分类" :options="categoryOptions" class="full-width" />
          </NFormItem>
        </div>
        <div class="form-grid-row">
          <NFormItem label="重要程度" class="form-grid-item">
            <NRadioGroup v-model:value="form.importance" class="full-width-radio">
              <NRadioButton :value="0">普通</NRadioButton>
              <NRadioButton :value="1">重要</NRadioButton>
              <NRadioButton :value="2">里程碑</NRadioButton>
            </NRadioGroup>
          </NFormItem>
          <NFormItem label="心情" class="form-grid-item">
            <NInput v-model:value="form.mood" placeholder="如：开心 😊" maxlength="50" class="full-width" />
          </NFormItem>
        </div>
        <NFormItem label="标签">
          <NDynamicTags v-model:value="tagList" :max="8" />
        </NFormItem>
        <NFormItem label="文件目录（图片/附件将存到该目录，方便在服务器上按事件查看）">
          <div class="dir-input-row">
            <NInput v-model:value="form.dirName" placeholder="如：2026-09-01_开学第一天" class="dir-input" />
            <NButton size="small" @click="autoDir">自动生成</NButton>
          </div>
        </NFormItem>
        <NFormItem label="记录内容（文字中间可直接插入图片 / 视频）">
          <MdEditor v-model="form.content" :theme="editorTheme" :on-upload-img="handleUploadImg"
                    :toolbars="mdToolbars" ref="mdEditorRef" class="event-md-editor">
            <template #defToolbars>
              <NormalToolbar title="上传视频" :on-click="openVideoPicker">
                <NIcon><VideocamOutline /></NIcon>
              </NormalToolbar>
            </template>
          </MdEditor>
          <input ref="videoInputRef" type="file" accept="video/*" style="display: none"
                 @change="handleVideoFile" />
        </NFormItem>
        <NFormItem label="附加文件（PDF / 文档 / 视频等）">
          <NUpload accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.txt,.mp4,.webm,.mov,.m4v,.avi,.mkv,.mp3,.wav,.flac,.aac"
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

    <!-- Video preview -->
    <NModal v-model:show="videoPreviewVisible" preset="card" :title="videoPreviewTitle"
            style="width: min(900px, 94vw)" class="video-preview-modal">
      <div class="video-preview-wrap">
        <video v-if="videoPreviewVisible" :src="videoPreviewUrl" controls autoplay preload="metadata" />
      </div>
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
  NDynamicTags, NUpload, NPagination, useMessage, useDialog
} from 'naive-ui'
import { MdEditor, MdPreview, NormalToolbar } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import dayjs from 'dayjs'
import { AddOutline, DocumentAttachOutline, VideocamOutline, SearchOutline } from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'
import { pageEvents, createEvent, updateEvent, deleteEvents, attachFile, detachFile } from '@/api/event'
import { uploadFile } from '@/api/system/file'
import type { ChildEvent, ChildEventFile } from '@/api/types'
import ShareCard from './components/ShareCard.vue'
import PigWonderland from './components/PigWonderland.vue'
import flowerPigUrl from '@/assets/pig/flower-pig.svg'

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

// 精简工具栏，避免项过多被推出可视区（md-editor-v3 工具栏溢出隐藏滚动条）。
// 不用 `=` 分组：space-between 布局会把左右两组贴两端、中间留大片空白。
// 数字 0 指向 defToolbars 插槽中的自定义项（🎥 上传视频），紧跟"图片"按钮。
const mdToolbars: any[] = [
  'bold', 'underline', 'italic', 'strikeThrough', '-', 'title', 'quote',
  'unorderedList', 'orderedList', 'task', '-', 'codeRow', 'code', 'link',
  'image', 0, 'table', '-', 'revoke', 'next', 'save', 'prettier',
  'fullscreen', 'preview', 'catalog'
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
const query = ref<Record<string, any>>({ pageNum: 1, pageSize: 3 })
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
  query.value = { pageNum: 1, pageSize: 3 }
  range.value = 'all'
  load()
}

function handlePageChange(p: number) {
  query.value.pageNum = p
  load()
}

function handlePageSizeChange(size: number) {
  query.value.pageSize = size
  query.value.pageNum = 1
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

interface EventMedia {
  type: 'image' | 'video'
  url: string
  name?: string
}

function previewContent(ev: ChildEvent): string {
  return (ev.content || '')
    .replace(/<video\b[^>]*>[\s\S]*?<\/video>/gi, '')
    .replace(/<video\b[^>]*\/>/gi, '')
    .replace(/<img\b[^>]*>/gi, '')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function contentLong(ev: ChildEvent) {
  return previewContent(ev).length > 220
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

/** 当年只显示 M月D日；跨年显示 YYYY年M月D日 */
function formatDateFull(d?: string) {
  if (!d) return '-'
  const date = dayjs(d)
  return date.year() === dayjs().year() ? date.format('M月D日') : date.format('YYYY年M月D日')
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

function attachmentMediaType(file: ChildEventFile): EventMedia['type'] | null {
  const mediaType = (file.mediaType || '').toLowerCase()
  const contentType = (file.contentType || '').toLowerCase()
  if (mediaType === 'image' || contentType.startsWith('image/')) return 'image'
  if (mediaType === 'video' || contentType.startsWith('video/')) return 'video'
  return null
}

function mediaItems(ev: ChildEvent): EventMedia[] {
  const items: EventMedia[] = []
  const content = ev.content || ''
  let match: RegExpExecArray | null

  const markdownImage = /!\[([^\]]*)\]\((?:<)?([^\s)>]+)(?:>)?(?:\s+["'][^"']*["'])?\)/g
  while ((match = markdownImage.exec(content))) {
    items.push({ type: 'image', url: match[2], name: match[1] || undefined })
  }

  const htmlImage = /<img\b[^>]*\bsrc=["']([^"']+)["'][^>]*>/gi
  while ((match = htmlImage.exec(content))) {
    items.push({ type: 'image', url: match[1] })
  }

  const htmlVideo = /<video\b[^>]*\bsrc=["']([^"']+)["'][^>]*>/gi
  while ((match = htmlVideo.exec(content))) {
    items.push({ type: 'video', url: match[1] })
  }

  const videoBlock = /<video\b[^>]*>([\s\S]*?)<\/video>/gi
  while ((match = videoBlock.exec(content))) {
    const source = match[1].match(/<source\b[^>]*\bsrc=["']([^"']+)["']/i)
    if (source) items.push({ type: 'video', url: source[1] })
  }

  ev.attachFiles?.forEach((file) => {
    const type = attachmentMediaType(file)
    if (type && file.url) items.push({ type, url: file.url, name: file.originalName })
  })

  const seen = new Set<string>()
  return items.filter((item) => {
    const key = `${item.type}:${item.url}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

function fileAttachments(ev: ChildEvent): ChildEventFile[] {
  return ev.attachFiles?.filter((file) => !attachmentMediaType(file)) || []
}

const videoPreviewVisible = ref(false)
const videoPreviewUrl = ref('')
const videoPreviewTitle = ref('视频预览')

function openVideoPreview(media: EventMedia) {
  videoPreviewUrl.value = media.url
  videoPreviewTitle.value = media.name || '视频预览'
  videoPreviewVisible.value = true
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

const mdEditorRef = ref<{ getEditorView: () => any } | null>(null)
const videoInputRef = ref<HTMLInputElement | null>(null)

function openVideoPicker() {
  videoInputRef.value?.click()
}

const handleVideoFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  try {
    const res: any = await uploadFile(file, form.value.dirName)
    const view = mdEditorRef.value?.getEditorView()
    if (view) {
      const { from, to } = view.state.selection.main
      view.dispatch({
        changes: { from, to, insert: `\n\n<video src="${res.url}" controls preload="metadata"></video>\n\n` }
      })
    } else {
      form.value.content += `\n\n<video src="${res.url}" controls preload="metadata"></video>\n\n`
    }
    message.success('视频上传成功')
  } catch (err) {
    message.error('视频上传失败')
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
/* ===================== Layout ===================== */
.event-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.mobile-only {
  display: none !important;
}
.desktop-only {
  display: flex !important;
}

/* ===================== Growth album header ===================== */
.hero {
  position: relative;
  overflow: hidden;
  min-height: 184px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding: 28px 36px;
  border: 1px solid rgba(244, 114, 182, 0.14);
  border-radius: 24px;
  background:
    linear-gradient(112deg, rgba(255, 255, 255, 0.96), rgba(253, 242, 248, 0.92) 52%, rgba(239, 246, 255, 0.9));
  box-shadow: 0 12px 38px rgba(190, 78, 121, 0.09);
}
.hero::after {
  content: '';
  position: absolute;
  width: 260px;
  height: 260px;
  right: 80px;
  top: -190px;
  border: 38px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  pointer-events: none;
}

.hero-deco { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; filter: blur(2px); }
.deco-circle.c1 {
  width: 220px;
  height: 220px;
  top: -125px;
  left: 26%;
  background: radial-gradient(circle, rgba(251, 207, 232, 0.46), transparent 70%);
}
.deco-circle.c2 {
  width: 180px;
  height: 180px;
  right: -48px;
  bottom: -98px;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.34), transparent 70%);
}
.deco-circle.c3 {
  width: 110px;
  height: 110px;
  left: -42px;
  bottom: -54px;
  background: radial-gradient(circle, rgba(253, 224, 71, 0.2), transparent 70%);
}

.hero-copy {
  position: relative;
  z-index: 1;
  min-width: 0;
  flex: 1;
}

.hero-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero-mini-pig {
  width: 36px;
  height: 36px;
  filter: drop-shadow(0 4px 8px rgba(240, 101, 134, 0.25));
}
.hero-mini-pig img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.hero-counter-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  background: rgba(240, 101, 134, 0.12);
  color: #d75d83;
  padding: 2px 8px;
  border-radius: 99px;
  font-size: 11px;
}
.hero-counter-badge strong {
  font-size: 14px;
  color: #e2527c;
  font-weight: 800;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #d75d83;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 2.2px;
}
.kicker-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #fb7185;
  box-shadow: 0 0 0 5px rgba(251, 113, 133, 0.12);
}
.hero-title {
  margin: 0;
  color: #3f2b34;
  font-size: clamp(23px, 2.2vw, 32px);
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.8px;
}
.title-sparkle {
  display: inline-block;
  color: #f5b83d;
  font-size: 18px;
  vertical-align: top;
  animation: titleTwinkle 2.6s ease-in-out infinite;
}
@keyframes titleTwinkle {
  0%, 100% { opacity: 0.55; transform: rotate(0deg) scale(0.86); }
  50% { opacity: 1; transform: rotate(24deg) scale(1.08); }
}
.hero-sub {
  max-width: 520px;
  margin: 6px 0 0;
  color: #866875;
  font-size: 14px;
  line-height: 1.6;
}
.hero-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 17px;
}
.hero-counter {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: #8f7280;
  font-size: 12px;
}
.hero-counter strong {
  color: #e2527c;
  font-family: ui-rounded, 'SF Pro Rounded', 'PingFang SC', sans-serif;
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
}
.meta-divider {
  width: 1px;
  height: 18px;
  background: rgba(190, 120, 145, 0.2);
}
.hero-wish {
  color: #a08490;
  font-size: 12px;
}

.hero-visual {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}
.pig-picture {
  position: relative;
  width: 146px;
  height: 132px;
  overflow: visible;
  filter: drop-shadow(0 14px 14px rgba(167, 71, 101, 0.15));
  transform: rotate(2deg);
}
.pig-picture img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}
.picture-star {
  position: absolute;
  z-index: 2;
  color: #fff;
  text-shadow: 0 2px 8px rgba(215, 93, 131, 0.3);
}
.star-one { top: 9px; right: 12px; font-size: 17px; }
.star-two { left: 10px; bottom: 12px; font-size: 11px; }
.hero-add-btn {
  box-shadow: 0 8px 20px rgba(240, 101, 134, 0.25);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.hero-add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 11px 24px rgba(240, 101, 134, 0.32);
}

/* ===================== Filter ===================== */
.filter-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.filter-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-row-top {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-search-input {
  flex: 1.5;
  min-width: 200px;
}

.filter-selects {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 220px;
}

.filter-select {
  flex: 1;
  min-width: 100px;
}

.filter-row-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-range-group {
  display: inline-flex;
}

.filter-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===================== Timeline ===================== */
.timeline-wrap {
  padding: 4px 2px 40px;
}

.month-divider {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 28px 0 16px;
}
.month-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #92400e;
  padding: 5px 14px;
  border-radius: 999px;
  background: linear-gradient(120deg, #fef3c7, #fde68a);
  white-space: nowrap;
}
.month-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(217, 119, 6, 0.2);
  color: #b45309;
  font-size: 11px;
  font-weight: 700;
}
.month-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(217, 119, 6, 0.3), transparent);
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
  background: linear-gradient(180deg, #fcd34d 0%, #f9a8d4 50%, #93c5fd 100%);
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
  animation: fadeSlideIn 0.3s ease both;
}
@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

.tl-dot {
  position: absolute;
  left: -30px;
  top: 24px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px rgba(217, 119, 6, 0.25);
  z-index: 1;
}
.dot-学习 { background: #3b82f6; }
.dot-运动 { background: #22c55e; }
.dot-日常 { background: #f59e0b; }
.dot-纪念 { background: #ec4899; }
.dot-成长 { background: #8b5cf6; }
.dot-other { background: #a8a29e; }

/* Card + category left-border accent */
.tl-card {
  border-radius: 16px;
  padding: 18px 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border-left: 4px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.tl-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.10);
}
.cat-学习  { border-left-color: #3b82f6; }
.cat-运动  { border-left-color: #22c55e; }
.cat-日常  { border-left-color: #f59e0b; }
.cat-纪念  { border-left-color: #ec4899; }
.cat-成长  { border-left-color: #8b5cf6; }
.cat-other { border-left-color: #d1d5db; }

/* Header row */
.tl-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.tl-date {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(120deg, #d97706, #f59e0b);
  border-radius: 999px;
  padding: 3px 10px;
  flex-shrink: 0;
}
.tl-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex: 1;
}

/* Title */
.tl-title {
  margin: 12px 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1c1917;
  line-height: 1.4;
}

/* Content */
.tl-content {
  color: #57534e;
  font-size: 14px;
  line-height: 1.7;
  position: relative;
  max-height: 260px;
  overflow: hidden;
  transition: max-height 0.3s ease;
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
  left: 0; right: 0; bottom: 0;
  height: 70px;
  background: linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.96) 100%);
  pointer-events: none;
}
.tl-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  font-size: 12px;
  color: #d97706;
  cursor: pointer;
  user-select: none;
  font-weight: 600;
}
.tl-toggle:hover { color: #b45309; }

/* Fixed-size media gallery */
.tl-media-section {
  margin-top: 16px;
  padding: 12px;
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 14px;
  background: rgba(250, 250, 249, 0.7);
}
.tl-media-head {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 10px;
  color: #78716c;
  font-size: 12px;
  font-weight: 700;
}
.tl-media-head small {
  padding: 1px 6px;
  border-radius: 999px;
  background: #fce7f3;
  color: #be185d;
  font-size: 10px;
  font-weight: 700;
}
.tl-media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(128px, 1fr));
  gap: 9px;
}
.tl-media-image {
  width: 100%;
  height: 108px;
  overflow: hidden;
  border-radius: 10px;
  background: #eee;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: zoom-in;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.tl-media-image :deep(img) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover;
}
.tl-media-image:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 16px rgba(0, 0, 0, 0.13);
}
.tl-video-thumb {
  position: relative;
  width: 100%;
  height: 108px;
  padding: 0;
  overflow: hidden;
  border: 0;
  border-radius: 10px;
  background: #171717;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  font: inherit;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.tl-video-thumb:hover {
  transform: translateY(-2px);
  box-shadow: 0 7px 16px rgba(0, 0, 0, 0.18);
}
.tl-video-thumb video {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  pointer-events: none;
}
.video-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.02), rgba(0,0,0,0.45));
}
.video-play {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translate(-50%, -58%);
}
.play-triangle {
  width: 0;
  height: 0;
  margin-left: 3px;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-left: 9px solid #e8527a;
}
.video-label {
  position: absolute;
  right: 7px;
  bottom: 6px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 10px;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.5);
}

/* File attachments */
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
.file-ic { color: #f59e0b; flex-shrink: 0; }
.file-name {
  color: #1d4ed8;
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.file-name:hover { text-decoration: underline; }
.file-size { color: #a8a29e; font-size: 12px; flex-shrink: 0; }

/* Footer */
.tl-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed #e7e5e4;
  flex-wrap: wrap;
}
.tl-foot-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.tl-mood {
  font-size: 13px;
  color: #d97706;
}
.tl-dir {
  font-size: 12px;
  color: #a8a29e;
  background: #f5f5f4;
  border-radius: 6px;
  padding: 3px 8px;
}
.tl-actions { flex-shrink: 0; }

/* Editor & Modal Form Layout */
.form-grid-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.form-grid-item {
  margin-bottom: 0;
}
.full-width {
  width: 100% !important;
}
.full-width-radio {
  width: 100%;
  display: flex;
}
.full-width-radio :deep(.n-radio-button) {
  flex: 1;
  text-align: center;
}
.dir-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.dir-input {
  flex: 1;
}

.event-md-editor {
  width: 100%;
  height: 420px;
}
/* Fallback for malformed/unsupported media left inside Markdown. */
.md-preview :deep(img),
.md-preview :deep(video) {
  width: 132px;
  height: 108px;
  display: inline-block;
  margin: 4px 6px 4px 0;
  border-radius: 10px;
  object-fit: cover;
  background: #111;
}

.video-preview-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  overflow: hidden;
  border-radius: 12px;
  background: #090909;
}
.video-preview-wrap video {
  width: 100%;
  max-height: 72vh;
  display: block;
  object-fit: contain;
  background: #090909;
}

.pending-files {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* States & Pagination */
.empty-state { margin-top: 60px; }
.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  padding: 12px 0 16px;
  flex-wrap: wrap;
  gap: 10px;
}
.pagination-count {
  font-size: 13px;
  color: #78716c;
  margin-right: 6px;
}
html.dark .pagination-count {
  color: #a8a29e;
}

/* ===================== Responsive ===================== */
@media (max-width: 900px) {
  .hero { padding: 25px 28px; }
  .pig-picture { width: 106px; height: 100px; }
  .hero-visual { gap: 12px; }
}

@media (max-width: 768px) {
  .mobile-only {
    display: inline-flex !important;
  }
  .desktop-only {
    display: none !important;
  }

  .hero {
    min-height: auto;
    flex-direction: column;
    align-items: stretch;
    gap: 14px;
    padding: 18px 16px;
    border-radius: 18px;
  }
  .hero-title {
    font-size: 20px;
  }
  .hero-sub {
    font-size: 13px;
    margin: 4px 0 0;
  }
  .hero-visual {
    width: 100%;
  }
  .hero-add-btn {
    width: 100%;
    height: 42px;
    font-size: 15px;
  }

  .filter-card {
    padding: 4px;
    border-radius: 14px;
  }
  .filter-row-top {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  .filter-selects {
    width: 100%;
  }
  .filter-row-bottom {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  .filter-range-group {
    width: 100%;
    display: flex;
  }
  .filter-range-group :deep(.n-radio-button) {
    flex: 1;
    text-align: center;
  }
  .filter-btns {
    width: 100%;
  }
  .filter-btn {
    flex: 1;
  }

  .timeline-wrap {
    padding: 0 0 24px;
  }
  .timeline-list {
    padding-left: 18px;
  }
  .timeline-list::before {
    left: 5px;
  }
  .tl-dot {
    left: -18px;
    top: 18px;
    width: 12px;
    height: 12px;
  }
  .month-divider {
    margin: 18px 0 12px;
    gap: 10px;
  }
  .month-badge {
    font-size: 13px;
    padding: 3px 10px;
  }
  .tl-card {
    padding: 13px 12px;
    border-radius: 12px;
  }
  .tl-title {
    font-size: 16px;
    margin: 8px 0 6px;
  }
  .tl-content {
    font-size: 13.5px;
  }

  .tl-media-section {
    padding: 8px;
    border-radius: 10px;
    margin-top: 10px;
  }
  .tl-media-grid.grid-count-1 {
    grid-template-columns: 1fr;
  }
  .tl-media-grid.grid-count-2 {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  .tl-media-grid.grid-count-3,
  .tl-media-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }
  .tl-media-image,
  .tl-video-thumb {
    height: auto;
    aspect-ratio: 1/1;
  }
  .tl-media-grid.grid-count-1 .tl-media-image,
  .tl-media-grid.grid-count-1 .tl-video-thumb {
    aspect-ratio: 16/9;
    max-height: 180px;
  }

  .tl-foot {
    margin-top: 10px;
    padding-top: 10px;
    gap: 8px;
  }
  .tl-foot-left {
    width: 100%;
    gap: 6px;
  }
  .tl-actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    gap: 4px;
  }

  .form-grid-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .event-md-editor {
    height: 320px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .title-sparkle { animation: none; }
  .hero-add-btn { transition: none; }
}

/* ===================== Dark Mode ===================== */
html.dark .hero {
  border-color: rgba(244, 114, 182, 0.12);
  background: linear-gradient(112deg, rgba(41, 29, 36, 0.96), rgba(50, 31, 42, 0.94) 55%, rgba(28, 39, 55, 0.94));
  box-shadow: 0 12px 38px rgba(0, 0, 0, 0.26);
}
html.dark .hero::after { border-color: rgba(255, 255, 255, 0.025); }
html.dark .hero-kicker { color: #f9a8c0; }
html.dark .hero-title { color: #fff1f5; }
html.dark .hero-sub { color: #c9aeb9; }
html.dark .hero-counter { color: #bfa4af; }
html.dark .hero-counter strong { color: #fb8fab; }
html.dark .hero-counter-badge {
  background: rgba(240, 101, 134, 0.22);
  color: #f9a8c0;
}
html.dark .hero-counter-badge strong {
  color: #fb8fab;
}
html.dark .hero-wish { color: #aa929d; }
html.dark .meta-divider { background: rgba(251, 143, 171, 0.18); }
html.dark .pig-picture {
  filter: drop-shadow(0 14px 16px rgba(251, 113, 133, 0.13));
}
html.dark .deco-circle { opacity: 0.4; }

html.dark .filter-card { background: rgba(28, 25, 23, 0.7); }

html.dark .month-badge { color: #fde68a; background: linear-gradient(120deg, #78350f, #92400e); }
html.dark .month-count { background: rgba(253, 230, 138, 0.15); color: #fcd34d; }
html.dark .month-line { background: linear-gradient(90deg, rgba(253, 230, 138, 0.25), transparent); }

html.dark .tl-dot { border-color: #1c1917; }
html.dark .tl-card {
  background: rgba(28, 25, 23, 0.88);
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.3);
}
html.dark .tl-card:hover { box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4); }
html.dark .tl-title { color: #f5f5f4; }
html.dark .tl-content { color: #d6d3d1; }
html.dark .tl-mask {
  background: linear-gradient(180deg, rgba(28, 25, 23, 0) 0%, rgba(28, 25, 23, 0.95) 100%);
}
html.dark .tl-toggle { color: #fbbf24; }
html.dark .tl-toggle:hover { color: #fde68a; }
html.dark .tl-media-section {
  border-color: rgba(87, 83, 78, 0.55);
  background: rgba(28, 25, 23, 0.62);
}
html.dark .tl-media-head { color: #c8c1bd; }
html.dark .tl-media-head small { background: rgba(190, 24, 93, 0.22); color: #f9a8d4; }
html.dark .tl-media-image { background: #292524; }
html.dark .tl-file  { background: #1c1917; }
html.dark .tl-dir   { background: #1c1917; color: #a8a29e; }
html.dark .tl-foot  { border-top-color: #292524; }
</style>
