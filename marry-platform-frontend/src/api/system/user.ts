import request from '@/utils/request'
import type { PageQuery, PageResult, SysUser } from '@/api/types'

export function pageUsers(params: PageQuery): Promise<PageResult<SysUser>> {
  return request.get('/system/user/list', { params })
}
/** User detail incl. roleIds — used by the edit dialog to pre-select roles. */
export function getUserDetail(id: number): Promise<SysUser> {
  return request.get(`/system/user/${id}`)
}
export function createUser(data: Partial<SysUser> & { password: string; roleIds?: number[] }): Promise<void> {
  return request.post('/system/user', data)
}
export function updateUser(data: Partial<SysUser> & { id: number; roleIds?: number[] }): Promise<void> {
  return request.put('/system/user', data)
}
export function deleteUsers(ids: number[]): Promise<void> {
  return request.delete(`/system/user/${ids.join(',')}`)
}
export function resetUserPassword(id: number, password: string): Promise<void> {
  return request.put(`/system/user/resetPwd/${id}`, null, { params: { password } })
}
export function changeUserStatus(id: number, status: number): Promise<void> {
  return request.put(`/system/user/changeStatus/${id}`, null, { params: { status } })
}