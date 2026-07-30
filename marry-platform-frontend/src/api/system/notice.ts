import request from '@/utils/request'
import type { PageQuery, PageResult, SysNotice } from '@/api/types'

export function pageNotices(params: PageQuery): Promise<PageResult<SysNotice>> {
  return request.get('/system/notice/list', { params })
}
export function createNotice(data: Partial<SysNotice> & { title: string }): Promise<void> {
  return request.post('/system/notice', data)
}
export function updateNotice(data: Partial<SysNotice> & { id: number }): Promise<void> {
  return request.put('/system/notice', data)
}
export function deleteNotices(ids: number[]): Promise<void> {
  return request.delete(`/system/notice/${ids.join(',')}`)
}