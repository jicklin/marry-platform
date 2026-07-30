import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import router from '@/router'
import { message, loadingBar } from '@/utils/feedback'

const baseURL = import.meta.env.VITE_API_BASE || '/api'

const service: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  // Phase 3: refresh token is delivered as an HttpOnly cookie, so we need to
  // include credentials on every request.
  withCredentials: true
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token && config.headers) {
      config.headers.Authorization = `${userStore.tokenType} ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ---------- 401 auto-refresh ----------
let refreshing: Promise<string> | null = null
let pendingQueue: Array<(token: string | null) => void> = []

function flushQueue(token: string | null) {
  pendingQueue.forEach((cb) => cb(token))
  pendingQueue = []
}

/**
 * Auth-flow endpoints (login/refresh/logout/etc.) must never trigger
 * silentRefresh — a refresh loop on /auth/logout would keep the user
 * seemingly "logged in" forever. These endpoints manage their own state.
 */
function isAuthFlowEndpoint(url: string = ''): boolean {
  return /\/auth\/(login|refresh|logout|captcha)\b/.test(url)
}

service.interceptors.response.use(
  async (response: AxiosResponse) => {
    endLoading()
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 0) return res.data

      // 401 → try refresh once, then retry (skip for auth-flow endpoints)
      if (res.code === 401) {
        if (isAuthFlowEndpoint(response.config?.url)) {
          return Promise.reject(new Error(res.msg || '认证失败'))
        }
        const retried = await tryRefresh()
        if (retried) {
          // Retry the original request
          const cfg = response.config as InternalAxiosRequestConfig & { _retried?: boolean }
          if (!cfg._retried) {
            cfg._retried = true
            cfg.headers!.Authorization = `${useUserStore().tokenType} ${useUserStore().token}`
            return service.request(cfg)
          }
        }
        handleUnauthorized(res.msg)
        return Promise.reject(new Error(res.msg || '未授权'))
      }

      message.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  async (error) => {
    endLoading()
    if (error.response?.status === 401) {
      if (isAuthFlowEndpoint(error.config?.url)) {
        // Don't trigger refresh on auth-flow endpoints — let the caller's
        // catch (e.g. user.ts logout()) decide what to do.
        return Promise.reject(error)
      }
      const retried = await tryRefresh()
      if (retried) {
        const cfg = error.config as InternalAxiosRequestConfig & { _retried?: boolean }
        if (!cfg._retried) {
          cfg._retried = true
          cfg.headers!.Authorization = `${useUserStore().tokenType} ${useUserStore().token}`
          return service.request(cfg)
        }
      }
      handleUnauthorized(error.response?.data?.msg || '认证失败')
    } else {
      message.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

async function tryRefresh(): Promise<boolean> {
  const userStore = useUserStore()
  if (!userStore.refreshToken) return false
  if (!refreshing) {
    refreshing = userStore
      .silentRefresh()
      .then(() => userStore.token)
      .catch(() => {
        flushQueue(null)
        return ''
      })
      .finally(() => {
        refreshing = null
      })
  }
  const t = await refreshing
  return !!t
}

function handleUnauthorized(msg: string) {
  const userStore = useUserStore()
  const appStore = useAppStore()
  userStore.reset()
  appStore.clearCache()

  // Skip if we already landed on (or are en route to) the login page.
  const here = router.currentRoute.value.fullPath
  if (here.startsWith('/login')) return

  // Idempotency: ignore subsequent 401s while a redirect is in flight so we
  // don't fire multiple message.warning toasts or races on router.push.
  if (redirecting) return
  redirecting = true

  // Capture the current location so after login the user returns to where
  // they were (excluding the auth endpoints themselves).
  const target = here.startsWith('/login') ? '/' : here
  const redirect = encodeURIComponent(target)
  message.warning(msg)
  // Use router.push (not location.hash=...) so Vue Router reliably picks up
  // the navigation even when we are inside an async error interceptor.
  router.push(`/login?redirect=${redirect}`).finally(() => {
    redirecting = false
  })
}

let redirecting = false

let loadingCount = 0
function startLoading() {
  if (loadingCount === 0) loadingBar.start()
  loadingCount++
}
function endLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    loadingBar.finish()
  }
}

export default service