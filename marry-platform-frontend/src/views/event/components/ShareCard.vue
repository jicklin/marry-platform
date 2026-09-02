<template>
  <!-- share card is kept rendered but hidden off-screen so html-to-image can capture it -->
  <div v-if="showCard" class="share-card-host">
    <div ref="cardRef" class="share-card">
      <div class="sc-sky">
        <span class="sc-sun" />
        <span class="sc-cloud c1" />
        <span class="sc-cloud c2" />
      </div>
      <div class="sc-body">
        <div class="sc-date">{{ shareDate }}</div>
        <div class="sc-title">{{ event?.title }}</div>
        <div class="sc-badges">
          <span v-if="event?.category" class="sc-badge cat">{{ event.category }}</span>
          <span v-if="event?.importance === 1" class="sc-badge imp">⭐ 重要</span>
          <span v-if="event?.importance === 2" class="sc-badge imp">🏆 里程碑</span>
        </div>

        <div v-if="summary" class="sc-summary">{{ summary }}</div>
        <div v-else class="sc-summary placeholder">
          <span>用文字记下此刻，未来回看依旧温暖 ✨</span>
        </div>

        <div v-if="imgs.length" class="sc-grid" :class="'n' + imgs.length">
          <img v-for="(u, i) in imgs" :key="i" :src="u" class="sc-img" />
        </div>
        <div v-else class="sc-illustration">
          <span class="sc-ill-emoji">🧸</span>
          <span class="sc-ill-text">{{ event?.mood || '每一个瞬间都值得收藏' }}</span>
        </div>
      </div>
      <div class="sc-footer">✦ 记录于 marry 成长册 ✦</div>
    </div>

    <NModal v-model:show="previewVisible" preset="card" title="分享成长图" style="width: min(440px, 92vw)" class="share-preview-modal">
      <div class="preview-wrap">
        <NImage :src="dataUrl" class="preview-img" />
      </div>
      <template #footer>
        <NSpace justify="center">
          <NButton type="primary" :loading="generating" @click="saveImage">
            <template #icon><NIcon><DownloadOutline /></NIcon></template>
            保存图片
          </NButton>
          <NButton @click="previewVisible = false">关闭</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NModal, NButton, NImage, NSpace, NIcon, useMessage } from 'naive-ui'
import { DownloadOutline } from '@vicons/ionicons5'
import { toPng } from 'html-to-image'
import dayjs from 'dayjs'
import type { ChildEvent } from '@/api/types'

const props = defineProps<{ event: ChildEvent | null }>()

const message = useMessage()
const showCard = ref(false)
const previewVisible = ref(false)
const generating = ref(false)
const dataUrl = ref('')
const cardRef = ref<HTMLElement | null>(null)

const shareDate = computed(() => (props.event?.eventDate ? dayjs(props.event.eventDate).format('YYYY年M月D日') : ''))
const summary = computed(() => stripMd(props.event?.content || ''))

const imgs = computed(() => {
  if (!props.event) return []
  const urls: string[] = []
  // images embedded inline in the markdown body (onUploadImg inserts `![..](url)`)
  const re = /!\[[^\]]*\]\(([^)\s]+)\)/g
  let m: RegExpExecArray | null
  while ((m = re.exec(props.event.content || ''))) urls.push(m[1])
  // plus explicitly attached image files
  props.event.attachFiles?.filter((f) => f.mediaType === 'image' && f.url).forEach((f) => urls.push(f.url!))
  return Array.from(new Set(urls)).slice(0, 9)
})

function stripMd(md: string): string {
  return md
    .replace(/<video\b[^>]*>.*?<\/video>/gis, ' ') // drop inline <video> tags entirely
    .replace(/<[^>]*>/g, ' ') // drop any remaining html tags
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[#>*`~\-_|]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

function open() {
  if (!props.event) return
  showCard.value = true
  previewVisible.value = true
  generating.value = true
  dataUrl.value = ''
  // wait a tick for card DOM to render, then wait for all images
  requestAnimationFrame(async () => {
    await waitImages()
    try {
      if (!cardRef.value) return
      dataUrl.value = await toPng(cardRef.value, { pixelRatio: 2, cacheBust: true, backgroundColor: '#fffdf7' })
    } catch (e) {
      message.error('生成失败，请重试')
    } finally {
      generating.value = false
    }
  })
}

async function waitImages() {
  if (!cardRef.value) return
  const imgsEl = Array.from(cardRef.value.querySelectorAll('img')) as HTMLImageElement[]
  await Promise.all(imgsEl.map((img) => img.decode().catch(() => {})))
}

function saveImage() {
  const a = document.createElement('a')
  a.href = dataUrl.value
  a.download = `成长记录_${props.event?.title || '瞬间'}.png`
  a.click()
  message.success('已保存')
}

// keep host mounted only while a share dialog is relevant
watch(
  () => props.event,
  (v) => {
    if (!v) showCard.value = false
  }
)

defineExpose({ open })
</script>

<style scoped>
.share-card-host {
  position: fixed;
  left: -9999px;
  top: 0;
  pointer-events: none;
}

.share-card {
  width: 750px;
  min-height: 1334px;
  background: linear-gradient(160deg, #fff7ed 0%, #ffe4e6 45%, #e0f2fe 100%);
  border-radius: 36px;
  overflow: hidden;
  position: relative;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(180, 83, 9, 0.18);
}

.sc-sky {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 260px;
  background: linear-gradient(180deg, #bae6fd 0%, #e0f2fe 100%);
}
.sc-sun {
  position: absolute;
  top: 52px;
  right: 90px;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, #fef9c3 0%, #fde047 70%, rgba(253, 224, 71, 0) 100%);
  filter: drop-shadow(0 0 24px rgba(253, 224, 71, 0.6));
}
.sc-cloud {
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
}
.sc-cloud::before,
.sc-cloud::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
}
.sc-cloud.c1 {
  top: 120px;
  left: 60px;
  width: 120px;
  height: 36px;
}
.sc-cloud.c1::before { top: -18px; left: 20px; width: 52px; height: 52px; }
.sc-cloud.c1::after { top: -10px; left: 62px; width: 38px; height: 38px; }
.sc-cloud.c2 {
  top: 180px;
  right: 220px;
  width: 90px;
  height: 28px;
  opacity: 0.7;
}
.sc-cloud.c2::before { top: -14px; left: 14px; width: 40px; height: 40px; }
.sc-cloud.c2::after { top: -8px; left: 46px; width: 30px; height: 30px; }

.sc-body {
  position: relative;
  flex: 1;
  padding: 300px 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.sc-date {
  font-size: 30px;
  font-weight: 600;
  color: #a16207;
  letter-spacing: 2px;
}
.sc-title {
  margin-top: 14px;
  font-size: 52px;
  font-weight: 800;
  color: #78350f;
  line-height: 1.3;
}
.sc-badges {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.sc-badge {
  font-size: 22px;
  padding: 6px 22px;
  border-radius: 999px;
}
.sc-badge.cat { background: #fde68a; color: #92400e; }
.sc-badge.imp { background: #fbcfe8; color: #be185d; }

.sc-summary {
  margin-top: 30px;
  font-size: 28px;
  line-height: 1.8;
  color: #57534e;
  max-width: 610px;
  min-height: 100px;
}
.sc-summary.placeholder {
  color: #a8a29e;
  font-size: 24px;
}

.sc-grid {
  margin-top: 36px;
  display: grid;
  gap: 12px;
  width: 610px;
}
.sc-grid.n1 { grid-template-columns: 1fr; }
.sc-grid.n2 { grid-template-columns: repeat(2, 1fr); }
.sc-grid.n3, .sc-grid.n4, .sc-grid.n5, .sc-grid.n6 { grid-template-columns: repeat(3, 1fr); }
.sc-grid.n7, .sc-grid.n8, .sc-grid.n9 { grid-template-columns: repeat(3, 1fr); }
.sc-img {
  width: 100%;
  height: 190px;
  object-fit: cover;
  border-radius: 20px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
}

.sc-illustration {
  margin-top: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.sc-ill-emoji { font-size: 130px; }
.sc-ill-text {
  font-size: 24px;
  color: #a16207;
}

.sc-footer {
  position: relative;
  padding: 34px 0 44px;
  text-align: center;
  font-size: 24px;
  color: #d97706;
  letter-spacing: 3px;
}

.preview-wrap {
  display: flex;
  justify-content: center;
  overflow: hidden;
}
.preview-img {
  max-width: 100%;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
.preview-img :deep(img) {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 12px;
}
</style>
