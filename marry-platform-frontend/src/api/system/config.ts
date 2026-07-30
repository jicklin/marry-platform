import request from '@/utils/request'
import type { PageQuery, PageResult, SysConfig } from '@/api/types'

export function pageConfig(params: PageQuery): Promise<PageResult<SysConfig>> {
  return request.get('/system/config/list', { params })
}
export function createConfig(data: Partial<SysConfig> & { name: string; configKey: string }): Promise<void> {
  return request.post('/system/config', data)
}
export function updateConfig(data: Partial<SysConfig> & { id: number }): Promise<void> {
  return request.put('/system/config', data)
}
export function deleteConfig(ids: number[]): Promise<void> {
  return request.delete(`/system/config/${ids.join(',')}`)
}