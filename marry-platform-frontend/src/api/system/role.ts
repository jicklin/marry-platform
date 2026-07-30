import request from '@/utils/request'
import type { PageQuery, PageResult, SysRole } from '@/api/types'

export function pageRoles(params: PageQuery): Promise<PageResult<SysRole>> {
  return request.get('/system/role/list', { params })
}
export function listAllRoles(): Promise<SysRole[]> {
  return request.get('/system/role/all')
}
export function createRole(data: Partial<SysRole> & { name: string; code: string }): Promise<void> {
  return request.post('/system/role', data)
}
export function updateRole(data: Partial<SysRole> & { id: number }): Promise<void> {
  return request.put('/system/role', data)
}
export function deleteRoles(ids: number[]): Promise<void> {
  return request.delete(`/system/role/${ids.join(',')}`)
}
export function roleMenuTree(roleId: number): Promise<{ menus: any[]; checkedKeys: number[] }> {
  return request.get(`/system/menu/roleMenuTreeselect/${roleId}`)
}
export function roleAuthUsers(roleId: number, params: PageQuery): Promise<PageResult<any>> {
  return request.get(`/system/role/authUser/${roleId}`, { params })
}
export function roleAssignUsers(roleId: number, userIds: number[]): Promise<void> {
  return request.put('/system/role/authUser/assign', userIds, { params: { roleId } })
}