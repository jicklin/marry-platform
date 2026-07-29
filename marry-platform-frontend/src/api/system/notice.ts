import request from '@/utils/request'

export function pageNotices(params: any) {
  return request.get('/system/notice/list', { params })
}
export function createNotice(data: any) {
  return request.post('/system/notice', data)
}
export function updateNotice(data: any) {
  return request.put('/system/notice', data)
}
export function deleteNotices(ids: number[]) {
  return request.delete(`/system/notice/${ids.join(',')}`)
}