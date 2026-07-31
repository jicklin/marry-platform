<template>
  <div class="login-wrapper" :class="{ 'is-dark': appStore.dark }">
    <!-- Ambient Animated Background -->
    <div class="ambient-bg">
      <div class="glow-sphere sphere-1"></div>
      <div class="glow-sphere sphere-2"></div>
      <div class="glow-sphere sphere-3"></div>
      <div class="grid-pattern"></div>
    </div>

    <div class="login-container">
      <!-- Left Section: Visual Hero Banner (Visible on Desktop >= 960px) -->
      <div class="login-hero">
        <div class="hero-content">
          <!-- Brand Badge -->
          <div class="hero-brand">
            <div class="brand-logo">
              <svg viewBox="0 0 40 40" width="40" height="40" aria-hidden="true">
                <defs>
                  <linearGradient id="hero-logo-grad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="#818cf8" />
                    <stop offset="100%" stop-color="#c084fc" />
                  </linearGradient>
                </defs>
                <rect x="2" y="2" width="36" height="36" rx="12" fill="url(#hero-logo-grad)" />
                <path d="M12 14h16M12 20h11M12 26h16" stroke="#ffffff" stroke-width="3" stroke-linecap="round" />
              </svg>
            </div>
            <span class="brand-name">Marry Platform</span>
            <span class="brand-version">v2.5</span>
          </div>

          <!-- Hero Headline -->
          <div class="hero-text">
            <h1 class="hero-title">
              企业级数字婚嫁<br />
              <span class="gradient-text">一站式管理平台</span>
            </h1>
            <p class="hero-subtitle">
              整合高性能 RBAC 权限控制系统、全链路数据协同与实时监控，赋予企业数字化转型新势能。
            </p>
          </div>

          <!-- Feature Cards Showcase -->
          <div class="feature-grid">
            <div class="feature-card">
              <div class="feature-icon flex-center">
                <NIcon size="20"><ShieldCheckmarkOutline /></NIcon>
              </div>
              <div class="feature-info">
                <h4>细粒度 RBAC 架构</h4>
                <p>多租户隔离与灵活角色权限分配</p>
              </div>
            </div>

            <div class="feature-card">
              <div class="feature-icon flex-center">
                <NIcon size="20"><SparklesOutline /></NIcon>
              </div>
              <div class="feature-info">
                <h4>实时数据看板</h4>
                <p>毫秒级响应，全面把控核心运营指标</p>
              </div>
            </div>
          </div>

          <!-- Live Metric Chip -->
          <div class="hero-footer-chip">
            <span class="pulse-dot"></span>
            <span>系统运行良好 · 服务可用率 99.99%</span>
          </div>
        </div>
      </div>

      <!-- Right Section: Interactive Login Card -->
      <div class="login-form-wrapper">
        <div class="form-card glass-panel">
          <!-- Top Utility Header: Mode / Dark Toggle -->
          <div class="card-header-actions">
            <button
              class="theme-toggle-btn"
              :title="appStore.dark ? '切换至亮色模式' : '切换至暗色模式'"
              @click="appStore.toggleDark()"
            >
              <NIcon size="18">
                <SunnyOutline v-if="appStore.dark" />
                <MoonOutline v-else />
              </NIcon>
            </button>
          </div>

          <!-- Title & Welcome Slogan -->
          <div class="form-header">
            <div class="mobile-logo flex-center">
              <svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
                <rect x="2" y="2" width="32" height="32" rx="10" fill="url(#hero-logo-grad)" />
                <path d="M10 13h16M10 18h11M10 23h16" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round" />
              </svg>
            </div>
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请输入您的管理账号以登录平台</p>
          </div>

          <!-- Login Type Tabs -->
          <div class="login-tabs">
            <button
              class="tab-item"
              :class="{ active: loginType === 'account' }"
              @click="loginType = 'account'"
            >
              <NIcon size="16" class="tab-icon"><PersonOutline /></NIcon>
              账号密码登录
            </button>
            <button
              class="tab-item"
              :class="{ active: loginType === 'mobile' }"
              @click="loginType = 'mobile'"
            >
              <NIcon size="16" class="tab-icon"><PhonePortraitOutline /></NIcon>
              手机验证登录
            </button>
          </div>

          <!-- Demo Quick Fill Badge -->
          <div class="demo-quick-bar">
            <span class="quick-label">演示账号：</span>
            <button class="quick-chip" @click="fillDemo('admin', 'admin123')">
              <span class="chip-user">admin</span> / <span class="chip-pass">admin123</span>
              <span class="chip-action">一键填入</span>
            </button>
          </div>

          <!-- Account Login Form -->
          <NForm
            v-if="loginType === 'account'"
            ref="formRef"
            :model="form"
            :rules="rules"
            size="large"
            :show-label="false"
          >
            <NFormItem path="username">
              <NInput
                v-model:value="form.username"
                placeholder="请输入账号 / 手机号"
                clearable
                autocomplete="username"
              >
                <template #prefix>
                  <NIcon class="input-icon"><PersonOutline /></NIcon>
                </template>
              </NInput>
            </NFormItem>

            <NFormItem path="password">
              <NInput
                v-model:value="form.password"
                type="password"
                placeholder="请输入密码"
                show-password-on="click"
                autocomplete="current-password"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <NIcon class="input-icon"><LockClosedOutline /></NIcon>
                </template>
              </NInput>
            </NFormItem>

            <NFormItem path="code">
              <div class="captcha-group">
                <NInput
                  v-model:value="form.code"
                  placeholder="验证码"
                  maxlength="4"
                  autocomplete="off"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <NIcon class="input-icon"><ShieldCheckmarkOutline /></NIcon>
                  </template>
                </NInput>
                <div
                  class="captcha-img-box"
                  :class="{ loading: captchaLoading }"
                  @click="refreshCaptcha"
                  title="点击刷新验证码"
                >
                  <img
                    v-if="captchaUrl"
                    :src="captchaUrl"
                    alt="captcha"
                    @error="refreshCaptcha"
                  />
                  <div v-else class="captcha-placeholder">
                    <NIcon size="18" class="spin-icon" v-if="captchaLoading"><RefreshOutline /></NIcon>
                    <span v-else>点击刷新</span>
                  </div>
                  <div class="captcha-refresh-overlay">
                    <NIcon size="16"><RefreshOutline /></NIcon>
                  </div>
                </div>
              </div>
            </NFormItem>

            <NButton
              class="submit-btn"
              type="primary"
              block
              size="large"
              :loading="loading"
              @click="handleLogin"
            >
              <template #icon v-if="!loading">
                <NIcon><CheckmarkCircleOutline /></NIcon>
              </template>
              {{ loading ? '验证身份中…' : '安全登录' }}
            </NButton>
          </NForm>

          <!-- Mobile Verification Login Form (Mock/Secondary) -->
          <NForm
            v-else
            ref="mobileFormRef"
            :model="mobileForm"
            :rules="mobileRules"
            size="large"
            :show-label="false"
          >
            <NFormItem path="mobile">
              <NInput
                v-model:value="mobileForm.mobile"
                placeholder="请输入手机号码"
                clearable
                autocomplete="tel"
              >
                <template #prefix>
                  <NIcon class="input-icon"><PhonePortraitOutline /></NIcon>
                </template>
              </NInput>
            </NFormItem>

            <NFormItem path="smsCode">
              <div class="sms-code-group">
                <NInput
                  v-model:value="mobileForm.smsCode"
                  placeholder="6 位短信验证码"
                  maxlength="6"
                  autocomplete="off"
                  @keyup.enter="handleMobileLogin"
                >
                  <template #prefix>
                    <NIcon class="input-icon"><ShieldCheckmarkOutline /></NIcon>
                  </template>
                </NInput>
                <NButton
                  class="sms-btn"
                  size="large"
                  :disabled="smsCountdown > 0"
                  @click="sendSmsCode"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : '获取验证码' }}
                </NButton>
              </div>
            </NFormItem>

            <NButton
              class="submit-btn"
              type="primary"
              block
              size="large"
              :loading="loading"
              @click="handleMobileLogin"
            >
              {{ loading ? '登录中…' : '手机号快捷登录' }}
            </NButton>
          </NForm>

          <!-- Footer Copyright -->
          <div class="form-footer">
            <p class="copyright">© 2026 Marry Platform Enterprise. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import {
  PersonOutline,
  LockClosedOutline,
  ShieldCheckmarkOutline,
  SunnyOutline,
  MoonOutline,
  SparklesOutline,
  CheckmarkCircleOutline,
  PhonePortraitOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { fetchCaptcha } from '@/api/auth'
import { message } from '@/utils/feedback'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const loginType = ref<'account' | 'mobile'>('account')
const formRef = ref<any>(null)
const mobileFormRef = ref<any>(null)

const form = ref({ username: 'admin', password: 'admin123', code: '', uuid: '' })
const mobileForm = ref({ mobile: '', smsCode: '' })

const rules = {
  username: { required: true, message: '请输入账号', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  code: { required: true, message: '请输入验证码', trigger: 'blur' }
}

const mobileRules = {
  mobile: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: { required: true, message: '请输入短信验证码', trigger: 'blur' }
}

const loading = ref(false)
const captchaLoading = ref(false)
const captchaUrl = ref('')
const smsCountdown = ref(0)
let countdownTimer: any = null

function fillDemo(user: string, pass: string) {
  loginType.value = 'account'
  form.value.username = user
  form.value.password = pass
  message.info(`已自动填入演示账号: ${user}`)
}

async function refreshCaptcha() {
  captchaLoading.value = true
  if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
  try {
    const { blobUrl, uuid } = await fetchCaptcha()
    captchaUrl.value = blobUrl
    form.value.uuid = uuid
    form.value.code = ''
  } catch (e) {
    message.error('验证码刷新失败，请检查网络设置')
  } finally {
    captchaLoading.value = false
  }
}

function sendSmsCode() {
  if (!mobileForm.value.mobile || !/^1[3-9]\d{9}$/.test(mobileForm.value.mobile)) {
    message.warning('请先输入有效的手机号码')
    return
  }
  message.success('验证码已发送（演示环境验证码：123456）')
  mobileForm.value.smsCode = '123456'
  smsCountdown.value = 60
  countdownTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

onMounted(refreshCaptcha)
onUnmounted(() => {
  if (captchaUrl.value) URL.revokeObjectURL(captchaUrl.value)
  if (countdownTimer) clearInterval(countdownTimer)
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
    message.success('登录成功，正在跳转…')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleMobileLogin() {
  try {
    await mobileFormRef.value?.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    // Fill demo account credentials for mobile login mock flow
    await userStore.login({
      username: 'admin',
      password: 'admin123',
      code: form.value.code,
      uuid: form.value.uuid
    })
    message.success('手机号快捷登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (e: any) {
    message.error('登录失败，请尝试账号密码登录')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Outer Shell */
.login-wrapper {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-body, #f8fafc);
  color: var(--fg-title, #1e293b);
  overflow: hidden;
  isolation: isolate;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* Ambient Animated Backdrop */
.ambient-bg {
  position: absolute;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

.glow-sphere {
  position: absolute;
  border-radius: 50%;
  filter: blur(110px);
  opacity: 0.45;
  will-change: transform;
}

.sphere-1 {
  width: 580px;
  height: 580px;
  background: radial-gradient(circle, #6366f1 0%, #818cf8 100%);
  top: -150px;
  left: -150px;
  animation: float-1 20s ease-in-out infinite alternate;
}

.sphere-2 {
  width: 620px;
  height: 620px;
  background: radial-gradient(circle, #a855f7 0%, #c084fc 100%);
  bottom: -200px;
  right: -100px;
  animation: float-2 24s ease-in-out infinite alternate;
}

.sphere-3 {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, #ec4899 0%, #f472b6 100%);
  top: 35%;
  left: 45%;
  opacity: 0.25;
  animation: float-3 28s ease-in-out infinite alternate;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(99, 102, 241, 0.12) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.6;
}

.is-dark .sphere-1 { opacity: 0.35; }
.is-dark .sphere-2 { opacity: 0.35; }
.is-dark .sphere-3 { opacity: 0.2; }
.is-dark .grid-pattern {
  background-image: radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px);
}

@keyframes float-1 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(60px, 80px) scale(1.08); }
}
@keyframes float-2 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(-80px, -60px) scale(0.95); }
}
@keyframes float-3 {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(50px, -70px) scale(1.1); }
}

/* Layout Container */
.login-container {
  display: flex;
  width: 100%;
  max-width: 1140px;
  min-height: 640px;
  margin: 24px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 25px 50px -12px rgba(99, 102, 241, 0.12), 0 0 0 1px rgba(99, 102, 241, 0.05);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  overflow: hidden;
  animation: container-fade-in 600ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.is-dark .login-container {
  background: rgba(15, 23, 42, 0.65);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.05);
}

@keyframes container-fade-in {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* Left Hero Section */
.login-hero {
  flex: 1;
  position: relative;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(168, 85, 247, 0.05) 100%);
  border-right: 1px solid rgba(99, 102, 241, 0.1);
}

.is-dark .login-hero {
  background: linear-gradient(135deg, rgba(30, 27, 75, 0.4) 0%, rgba(88, 28, 135, 0.2) 100%);
  border-right-color: rgba(255, 255, 255, 0.08);
}

.hero-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.brand-version {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 99px;
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
}

.is-dark .brand-version {
  background: rgba(129, 140, 248, 0.2);
  color: #a5b4fc;
}

.hero-text {
  margin-top: 40px;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -1px;
  margin: 0 0 16px 0;
}

.gradient-text {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.is-dark .gradient-text {
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
  max-width: 440px;
  margin: 0;
}

.is-dark .hero-subtitle {
  color: #94a3b8;
}

/* Feature Grid Cards */
.feature-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 36px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.12);
}

.is-dark .feature-card {
  background: rgba(30, 41, 59, 0.5);
  border-color: rgba(255, 255, 255, 0.08);
}

.feature-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  flex-shrink: 0;
}

.feature-info h4 {
  margin: 0 0 2px 0;
  font-size: 14px;
  font-weight: 700;
}

.feature-info p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.is-dark .feature-info p {
  color: #94a3b8;
}

/* Live Metric Chip */
.hero-footer-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 99px;
  background: rgba(99, 102, 241, 0.08);
  font-size: 12px;
  font-weight: 600;
  color: #4f46e5;
  width: fit-content;
  margin-top: 32px;
}

.is-dark .hero-footer-chip {
  background: rgba(129, 140, 248, 0.15);
  color: #c7d2fe;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

/* Right Section Form Wrapper */
.login-form-wrapper {
  width: 480px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px;
  position: relative;
}

.form-card {
  width: 100%;
  display: flex;
  flex-direction: column;
}

/* Card Header Action Controls */
.card-header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.theme-toggle-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--border-soft, rgba(226, 232, 240, 0.8));
  background: rgba(255, 255, 255, 0.8);
  color: var(--fg-title, #334155);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.theme-toggle-btn:hover {
  background: var(--bg-hover, #f1f5f9);
  transform: rotate(15deg) scale(1.05);
}

.is-dark .theme-toggle-btn {
  background: rgba(30, 41, 59, 0.8);
  border-color: rgba(255, 255, 255, 0.12);
  color: #f1f5f9;
}

/* Mobile Brand Logo & Header */
.mobile-logo {
  display: none;
  margin-bottom: 16px;
}

.form-header {
  margin-bottom: 24px;
}

.form-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.is-dark .form-subtitle {
  color: #94a3b8;
}

/* Login Type Tabs */
.login-tabs {
  display: flex;
  background: rgba(241, 245, 249, 0.8);
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.is-dark .login-tabs {
  background: rgba(30, 41, 59, 0.8);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item.active {
  background: #ffffff;
  color: #6366f1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.is-dark .tab-item {
  color: #94a3b8;
}

.is-dark .tab-item.active {
  background: #1e293b;
  color: #818cf8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* Demo Quick Fill Bar */
.demo-quick-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(99, 102, 241, 0.06);
  border: 1px dashed rgba(99, 102, 241, 0.2);
  font-size: 12px;
}

.is-dark .demo-quick-bar {
  background: rgba(129, 140, 248, 0.08);
  border-color: rgba(129, 140, 248, 0.25);
}

.quick-label {
  color: #64748b;
  font-weight: 500;
}

.is-dark .quick-label { color: #94a3b8; }

.quick-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(99, 102, 241, 0.2);
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
}

.quick-chip:hover {
  background: #ffffff;
  border-color: #6366f1;
  transform: translateY(-1px);
}

.chip-user { font-weight: 700; color: #4f46e5; }
.chip-pass { font-weight: 700; color: #8b5cf6; }
.chip-action {
  font-size: 10px;
  background: #6366f1;
  color: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
  margin-left: 2px;
}

.is-dark .quick-chip {
  background: rgba(30, 41, 59, 0.9);
  border-color: rgba(129, 140, 248, 0.3);
}
.is-dark .chip-user { color: #818cf8; }
.is-dark .chip-pass { color: #c084fc; }

/* Input Customization */
:deep(.n-form-item) {
  margin-bottom: 18px;
}

.input-icon {
  font-size: 18px;
  color: #94a3b8;
}

/* Captcha Group */
.captcha-group {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.captcha-group :deep(.n-input) {
  flex: 1;
}

.captcha-img-box {
  position: relative;
  width: 120px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--border-soft, #e2e8f0);
  background: rgba(241, 245, 249, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s ease;
  flex-shrink: 0;
}

.is-dark .captcha-img-box {
  background: rgba(30, 41, 59, 0.8);
  border-color: rgba(255, 255, 255, 0.12);
}

.captcha-img-box:hover {
  border-color: #6366f1;
}

.captcha-img-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-placeholder {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.captcha-refresh-overlay {
  position: absolute;
  inset: 0;
  background: rgba(99, 102, 241, 0.85);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.captcha-img-box:hover .captcha-refresh-overlay {
  opacity: 1;
}

/* SMS Button Group */
.sms-code-group {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.sms-code-group :deep(.n-input) {
  flex: 1;
}

.sms-btn {
  height: 40px;
  border-radius: 8px !important;
  font-size: 13px;
  white-space: nowrap;
}

/* Submit Action Button */
.submit-btn {
  margin-top: 10px;
  height: 48px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
  border: none !important;
  box-shadow: 0 10px 25px -5px rgba(99, 102, 241, 0.4);
  transition: transform 0.2s ease, box-shadow 0.2s ease !important;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px -5px rgba(99, 102, 241, 0.55) !important;
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

/* Footer Copyright */
.form-footer {
  margin-top: 32px;
  text-align: center;
}

.copyright {
  font-size: 11px;
  color: #94a3b8;
  margin: 0;
}

/* Spin animation helper */
.spin-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

/* Responsive Rules */
@media (max-width: 960px) {
  .login-hero {
    display: none;
  }

  .login-container {
    max-width: 440px;
    min-height: auto;
    border-radius: 24px;
    margin: 16px;
  }

  .login-form-wrapper {
    width: 100%;
    padding: 32px 28px 24px;
  }

  .mobile-logo {
    display: flex;
  }

  .form-title {
    font-size: 22px;
  }
}
</style>

