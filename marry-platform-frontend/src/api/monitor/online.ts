import request from '@/utils/request'

export function listOnline() {
  return request.get('/monitor/online/list')
}
export function forceLogout(userId: number) {
  return request.delete(`/monitor/online/${userId}`)
}