import request from '@/utils/request'

export function pageLoginLog(params: any) {
  return request.get('/monitor/loginlog/list', { params })
}
export function deleteLoginLog(ids: number[]) {
  return request.delete(`/monitor/loginlog/${ids.join(',')}`)
}
export function cleanLoginLog() {
  return request.delete('/monitor/loginlog/clean')
}