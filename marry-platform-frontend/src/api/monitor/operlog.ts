import request from '@/utils/request'

export function pageOperLog(params: any) {
  return request.get('/monitor/operlog/list', { params })
}
export function deleteOperLog(ids: number[]) {
  return request.delete(`/monitor/operlog/${ids.join(',')}`)
}
export function cleanOperLog() {
  return request.delete('/monitor/operlog/clean')
}