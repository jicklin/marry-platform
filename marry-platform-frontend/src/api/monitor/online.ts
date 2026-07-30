import request from '@/utils/request'

interface OnlineUser {
  userId: number
  username: string
  deptName?: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  loginTime?: string
}

export function listOnline(): Promise<OnlineUser[]> {
  return request.get('/monitor/online/list')
}
export function forceLogout(userId: number): Promise<void> {
  return request.delete(`/monitor/online/${userId}`)
}