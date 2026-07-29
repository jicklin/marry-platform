import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getToken,
  setToken,
  getTokenType,
  setRefreshToken,
  getRefreshToken,
  clearAuth
} from '@/utils/auth'
import { login as loginApi, refresh as refreshApi, getInfo as getInfoApi } from '@/api/auth'
import router from '@/router'

export interface UserInfo {
  userId: number
  username: string
  nickName: string
  avatar?: string
  email?: string
  phone?: string
  sex?: number
  deptId?: number
  deptName?: string
  permissions: string[]
  roles: string[]
}

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string>(getToken())
    const tokenType = ref<string>(getTokenType())
    const refreshToken = ref<string>(getRefreshToken())
    const userInfo = ref<UserInfo | null>(null)
    const perms = computed<string[]>(() => userInfo.value?.permissions || [])
    const roles = computed<string[]>(() => userInfo.value?.roles || [])

    async function login(form: { username: string; password: string }) {
      const res: any = await loginApi(form)
      token.value = res.accessToken
      tokenType.value = 'Bearer'
      refreshToken.value = res.refreshToken
      setToken(res.accessToken, 'Bearer')
      setRefreshToken(res.refreshToken)
      userInfo.value = res.userInfo
      return res
    }

    async function fetchInfo() {
      const info = await getInfoApi()
      userInfo.value = info
      return info
    }

    async function doRefresh() {
      if (!refreshToken.value) throw new Error('no refresh token')
      const res: any = await refreshApi(refreshToken.value)
      token.value = res.accessToken
      refreshToken.value = res.refreshToken
      setToken(res.accessToken, 'Bearer')
      setRefreshToken(res.refreshToken)
      return res
    }

    /** Called by axios interceptor; queued requests share one refresh call. */
    let inflight: Promise<any> | null = null
    async function silentRefresh() {
      if (!refreshToken.value) throw new Error('no refresh token')
      if (!inflight) {
        inflight = doRefresh().finally(() => (inflight = null))
      }
      return inflight
    }

    async function logout() {
      try {
        await import('@/api/auth').then((m) => m.logout())
      } catch (e) {
        /* ignore */
      }
      reset()
      router.push('/login')
    }

    function reset() {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      clearAuth()
    }

    return {
      token,
      tokenType,
      refreshToken,
      userInfo,
      perms,
      roles,
      login,
      fetchInfo,
      doRefresh,
      silentRefresh,
      logout,
      reset
    }
  },
  {
    persist: {
      pick: ['token', 'tokenType', 'refreshToken', 'userInfo']
    }
  }
)