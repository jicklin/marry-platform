<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">🍵</div>
        <div class="brand-text">
          <div class="brand-title">marry-platform</div>
          <div class="brand-tag">Multi-module RBAC Admin Platform</div>
        </div>
      </div>

      <NForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="left"
        label-width="80"
        require-mark-placement="right-hanging"
        size="large"
      >
        <NFormItem path="username" label="账号">
          <NInput v-model:value="form.username" placeholder="admin" clearable>
            <template #prefix><NIcon><PersonOutline /></NIcon></template>
          </NInput>
        </NFormItem>
        <NFormItem path="password" label="密码">
          <NInput
            v-model:value="form.password"
            type="password"
            placeholder="admin123"
            show-password-on="click"
            @keyup.enter="handleLogin"
          >
            <template #prefix><NIcon><LockClosedOutline /></NIcon></template>
          </NInput>
        </NFormItem>
        <NFormItem path="code" label="验证码">
          <NInput v-model:value="form.code" placeholder="4位数字" maxlength="4" @keyup.enter="handleLogin">
            <template #prefix><NIcon><ShieldCheckmarkOutline /></NIcon></template>
          </NInput>
          <div class="captcha-box" @click="refreshCaptcha" :title="'点击刷新'">
            <img v-if="captchaUrl" :src="captchaUrl" alt="captcha" />
          </div>
        </NFormItem>
        <NButton type="primary" block size="large" :loading="loading" @click="handleLogin">
          登录
        </NButton>
      </NForm>

      <div class="footer-tip">
        默认账号：<b>admin</b> / 默认密码：<b>admin123</b>
      </div>
    </div>
    <div class="bg-decoration bg-decoration-1"></div>
    <div class="bg-decoration bg-decoration-2"></div>
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
  const { blobUrl, uuid } = await fetchCaptcha()
  captchaUrl.value = blobUrl
  form.value.uuid = uuid
  form.value.code = ''
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
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.login-card {
  position: relative;
  z-index: 2;
  width: 460px;
  background: #ffffff;
  padding: 36px 40px 32px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
  justify-content: center;
}

.brand-icon { font-size: 36px; }
.brand-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #2d8cf0, #5fa3ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}
.brand-tag {
  font-size: 11px;
  color: #909399;
  letter-spacing: 1px;
}

.footer-tip {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
}

.captcha-box {
  width: 110px;
  height: 36px;
  margin-left: 8px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  display: inline-block;
}
.captcha-box img { width: 100%; height: 100%; display: block; }

.bg-decoration {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}
.bg-decoration-1 {
  width: 500px;
  height: 500px;
  background: #ffd166;
  top: -200px;
  left: -200px;
}
.bg-decoration-2 {
  width: 600px;
  height: 600px;
  background: #06d6a0;
  bottom: -300px;
  right: -200px;
}
</style>