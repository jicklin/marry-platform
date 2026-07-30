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
    // Phase 3: refresh token is delivered via HttpOnly cookie, kept by browser.
    // We no longer mirror it in store state, but still expose a marker ref so
    // silentRefresh can distinguish "logged in" from "logged out".
    const hasRefreshCookie = ref<boolean>(false)
    const userInfo = ref<UserInfo | null>(null)
    const perms = computed<string[]>(() => userInfo.value?.permissions || [])
    const roles = computed<string[]>(() => userInfo.value?.roles || [])

    function applyTokens(res: { accessToken: string; refreshToken?: string }) {
      token.value = res.accessToken
      tokenType.value = 'Bearer'
      setToken(res.accessToken, 'Bearer')
      // refreshToken from body is transitional; once cookie is fully adopted,
      // the LoginVO field can be removed entirely.
      if (res.refreshToken) {
        hasRefreshCookie.value = true
        setRefreshToken(res.refreshToken)
      }
    }

    async function login(form: { username: string; password: string }) {
      const res: any = await loginApi(form)
      applyTokens(res)
      userInfo.value = res.userInfo
      return res
    }

    async function fetchInfo() {
      const info = await getInfoApi()
      userInfo.value = info
      return info
    }

    async function doRefresh() {
      // No body — refresh token comes from the HttpOnly cookie.
      const res: any = await refreshApi()
      applyTokens(res)
      return res
    }

    /** Called by axios interceptor; queued requests share one refresh call. */
    let inflight: Promise<any> | null = null
    async function silentRefresh() {
      if (!hasRefreshCookie.value) throw new Error('no refresh session')
      if (!inflight) {
        inflight = doRefresh().finally(() => (inflight = null))
      }
      return inflight
    }

    async function logout() {
      // Fire-and-forget the server logout (best effort — we don't want to
      // block the UX on network). The HttpOnly refresh-token cookie is
      // cleared by the server via Set-Cookie header regardless.
      try {
        const { logout: logoutApi } = await import('@/api/auth')
        await logoutApi()
      } catch (e) {
        /* ignore — local state is cleared below */
      }
      // Always clear local state and navigate to login, even if the server
      // call failed (e.g. token already expired). This guarantees the logout
      // loop cannot spin forever.
      reset()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }

    function reset() {
      token.value = ''
      hasRefreshCookie.value = false
      userInfo.value = null
      clearAuth()
    }

    return {
      token,
      tokenType,
      refreshToken: hasRefreshCookie,
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
      pick: ['token', 'tokenType', 'hasRefreshCookie', 'userInfo']
    }
  }
)