import request from '@/utils/request'

export function pageConfig(params: any) {
  return request.get('/system/config/list', { params })
}
export function createConfig(data: any) {
  return request.post('/system/config', data)
}
export function updateConfig(data: any) {
  return request.put('/system/config', data)
}
export function deleteConfig(ids: number[]) {
  return request.delete(`/system/config/${ids.join(',')}`)
}