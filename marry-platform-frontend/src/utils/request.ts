import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { message, loadingBar } from '@/utils/feedback'

const baseURL = import.meta.env.VITE_API_BASE || '/api'

const service: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  withCredentials: false
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

service.interceptors.response.use(
  async (response: AxiosResponse) => {
    endLoading()
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 0) return res.data

      // 401 → try refresh once, then retry
      if (res.code === 401) {
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
  if (location.hash !== '#/login') {
    message.warning(msg)
    location.hash = '#/login'
  }
}

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