<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-blob bg-blob-1" />
      <div class="bg-blob bg-blob-2" />
      <div class="bg-blob bg-blob-3" />
    </div>

    <div class="login-card">
      <div class="brand">
        <div class="brand-mark">
          <svg viewBox="0 0 32 32" width="32" height="32" aria-hidden="true">
            <defs>
              <linearGradient id="brand-grad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#2d8cf0" />
                <stop offset="100%" stop-color="#5fa3ff" />
              </linearGradient>
            </defs>
            <rect x="2" y="2" width="28" height="28" rx="8" fill="url(#brand-grad)" />
            <path d="M10 11h12M10 16h8M10 21h12" stroke="#fff" stroke-width="2.2" stroke-linecap="round" />
          </svg>
        </div>
        <div class="brand-text">
          <div class="brand-title">marry-platform</div>
          <div class="brand-tag">MULTI-MODULE RBAC ADMIN</div>
        </div>
      </div>

      <NForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
        require-mark-placement="right-hanging"
        size="large"
      >
        <NFormItem path="username" label="账号">
          <NInput
            v-model:value="form.username"
            placeholder="请输入账号"
            clearable
            autocomplete="username"
          >
            <template #prefix>
              <NIcon class="prefix-icon"><PersonOutline /></NIcon>
            </template>
          </NInput>
        </NFormItem>

        <NFormItem path="password" label="密码">
          <NInput
            v-model:value="form.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <NIcon class="prefix-icon"><LockClosedOutline /></NIcon>
            </template>
          </NInput>
        </NFormItem>

        <NFormItem path="code" label="验证码">
          <div class="captcha-row">
            <NInput
              v-model:value="form.code"
              placeholder="4 位数字"
              maxlength="4"
              autocomplete="off"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <NIcon class="prefix-icon"><ShieldCheckmarkOutline /></NIcon>
              </template>
            </NInput>
            <div class="captcha-box" @click="refreshCaptcha" :title="'点击刷新'">
              <img
                v-if="captchaUrl"
                :src="captchaUrl"
                alt="captcha"
                @error="refreshCaptcha"
              />
              <span v-else class="captcha-placeholder">…</span>
            </div>
          </div>
        </NFormItem>

        <NButton
          class="login-button"
          type="primary"
          block
          size="large"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中…' : '登 录' }}
        </NButton>
      </NForm>

      <div class="footer-tip">
        默认账号 <b>admin</b> / 密码 <b>admin123</b>（仅开发环境）
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { PersonOutline, LockClosedOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { fetchCaptcha } from '@/api/auth'
import { message } from '@/utils/feedback'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<any>(null)
const form = ref({ username: 'admin', password: 'admin123', code: '', uuid: '' })
const rules = {
  username: { required: true, message: '请输入账号', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  code: { required: true, message: '请输入验证码', trigger: 'blur' }
}
const loading = ref(false)
const captchaUrl = ref('')

async function refreshCaptcha() {
  if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
  try {
    const { blobUrl, uuid } = await fetchCaptcha()
    captchaUrl.value = blobUrl
    form.value.uuid = uuid
    form.value.code = ''
  } catch (e) {
    message.error('验证码加载失败，请刷新重试')
  }
}

onMounted(refreshCaptcha)
onUnmounted(() => {
  if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
})

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await userStore.login(form.value)
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ------------------------------------------------------------------ *
 *  Page layout: full-viewport centred card on a glassy backdrop       *
 * ------------------------------------------------------------------ */
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg-body, #f5f7fa);
  overflow: hidden;
  isolation: isolate;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

.bg-blob {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 50%;
  filter: blur(96px);
  opacity: 0.45;
  will-change: transform;
}
.bg-blob-1 {
  background: #2d8cf0;
  top: -180px;
  left: -120px;
  animation: float-a 18s ease-in-out infinite;
}
.bg-blob-2 {
  background: #67c23a;
  bottom: -260px;
  right: -160px;
  animation: float-b 22s ease-in-out infinite;
}
.bg-blob-3 {
  background: #f5a623;
  top: 30%;
  right: 18%;
  width: 360px;
  height: 360px;
  opacity: 0.25;
  animation: float-c 26s ease-in-out infinite;
}

@keyframes float-a {
  0%,100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, 60px) scale(1.05); }
}
@keyframes float-b {
  0%,100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-60px, -30px) scale(0.95); }
}
@keyframes float-c {
  0%,100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -50px) scale(1.1); }
}

/* Dark mode: dim the blobs so they don't blow out the contrast */
:global(html.dark) .bg-blob {
  opacity: 0.18;
}

/* ------------------------------------------------------------------ *
 *  Card: glass panel with subtle border + soft shadow                  *
 * ------------------------------------------------------------------ */
.login-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 36px 40px 28px;
  border-radius: 16px;
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border-soft, #ebeef5);
  box-shadow:
    0 24px 60px -12px rgba(15, 23, 42, 0.18),
    0 6px 24px -6px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  animation: card-rise 480ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes card-rise {
  from { opacity: 0; transform: translateY(16px) scale(0.985); }
  to   { opacity: 1; transform: translateY(0)    scale(1); }
}

/* ------------------------------------------------------------------ *
 *  Brand header                                                       *
 * ------------------------------------------------------------------ */
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
  justify-content: center;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.brand-mark svg { display: block; }

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.brand-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #2d8cf0, #5fa3ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.4px;
}
.brand-tag {
  margin-top: 4px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.8px;
  color: var(--fg-muted, #909399);
}

/* ------------------------------------------------------------------ *
 *  Form                                                                 *
 * ------------------------------------------------------------------ */
.login-card :deep(.n-form-item) {
  margin-bottom: 18px;
}
.login-card :deep(.n-form-item-label) {
  font-weight: 500;
  padding-bottom: 6px;
}
.prefix-icon { font-size: 18px; color: var(--fg-muted, #909399); }

/* Subtle focus ring tuning (Naive UI applies its own; we just sharpen) */
.login-card :deep(.n-input .n-input-wrapper:focus-within) {
  box-shadow: 0 0 0 3px rgba(45, 140, 240, 0.15);
}

/* ------------------------------------------------------------------ *
 *  Captcha row: input on the left, captcha image on the right         *
 * ------------------------------------------------------------------ */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.captcha-row :deep(.n-input) { flex: 1 1 auto; min-width: 0; }

.captcha-box {
  flex: none;
  width: 110px;
  height: 36px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border-soft, #ebeef5);
  background: var(--bg-body, #f5f7fa);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: border-color 0.15s ease;
}
.captcha-box:hover { border-color: var(--border-default, #dcdfe6); }
.captcha-box img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}
.captcha-placeholder {
  color: var(--fg-muted, #909399);
  font-size: 18px;
  letter-spacing: 4px;
}

/* ------------------------------------------------------------------ *
 *  Submit button                                                        *
 * ------------------------------------------------------------------ */
.login-button {
  margin-top: 8px;
  height: 44px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2d8cf0, #5fa3ff);
  border: none;
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}
.login-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(45, 140, 240, 0.4);
}
.login-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px -2px rgba(45, 140, 240, 0.4);
}

/* ------------------------------------------------------------------ *
 *  Footer hint                                                          *
 * ------------------------------------------------------------------ */
.footer-tip {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: var(--fg-muted, #909399);
}
.footer-tip b {
  color: var(--fg-default, #303133);
  font-weight: 600;
}

/* ------------------------------------------------------------------ *
 *  Mobile: card fills the screen with breathing room                  *
 * ------------------------------------------------------------------ */
@media (max-width: 480px) {
  .login-card {
    padding: 28px 22px 22px;
    border-radius: 12px;
  }
  .login-card :deep(.n-form-item-label) {
    font-size: 13px;
  }
  .captcha-box { width: 96px; }
}
</style>
