import request from '@/utils/request'
import type { PageQuery, PageResult, SysOperLog } from '@/api/types'

export function pageOperLog(params: PageQuery): Promise<PageResult<SysOperLog>> {
  return request.get('/monitor/operlog/list', { params })
}
export function deleteOperLog(ids: number[]): Promise<void> {
  return request.delete(`/monitor/operlog/${ids.join(',')}`)
}
export function cleanOperLog(): Promise<void> {
  return request.delete('/monitor/operlog/clean')
}