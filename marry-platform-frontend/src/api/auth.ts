import request from '@/utils/request'

export interface LoginReq {
  username: string
  password: string
  code?: string
  uuid?: string
}

export function login(data: LoginReq) {
  return request.post('/auth/login', data)
}

export function refresh(refreshToken: string) {
  return request.post('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getInfo() {
  return request.get('/auth/getInfo')
}

/**
 * Fetch captcha image. The response blob URL is set on `<img src>` while
 * the X-Captcha-Id header is captured for the login submission.
 */
export async function fetchCaptcha(): Promise<{ blobUrl: string; uuid: string }> {
  const axios = (await import('axios')).default
  const res = await axios.get('/auth/captcha', { responseType: 'blob', baseURL: import.meta.env.VITE_API_BASE || '/api' })
  const uuid = res.headers['x-captcha-id'] || ''
  const blobUrl = URL.createObjectURL(res.data)
  return { blobUrl, uuid }
}