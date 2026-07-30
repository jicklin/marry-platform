import request from '@/utils/request'
import type { PageQuery, PageResult, SysLoginLog } from '@/api/types'

export function pageLoginLog(params: PageQuery): Promise<PageResult<SysLoginLog>> {
  return request.get('/monitor/loginlog/list', { params })
}
export function deleteLoginLog(ids: number[]): Promise<void> {
  return request.delete(`/monitor/loginlog/${ids.join(',')}`)
}
export function cleanLoginLog(): Promise<void> {
  return request.delete('/monitor/loginlog/clean')
}