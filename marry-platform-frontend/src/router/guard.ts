import router from '@/router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { message } from '@/utils/feedback'

const whiteList = ['/login', '/404']

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (whiteList.includes(to.path)) {
    if (to.path === '/login' && userStore.token) {
      return next('/')
    }
    return next()
  }

  if (!userStore.token) {
    return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }

  try {
    if (!permissionStore.loaded) {
      await userStore.fetchInfo()
      await permissionStore.generateRoutes()
      return next({ ...to, replace: true })
    }
  } catch (e: any) {
    // Auth-related failures: silent redirect to login
    if (e?.response?.status === 401 || e?.code === 401) {
      userStore.reset()
      return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    message.error('初始化失败，请重新登录')
    userStore.reset()
    return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }

  next()
})