import request from '@/utils/request'

export function pageRoles(params: any) {
  return request.get('/system/role/list', { params })
}
export function listAllRoles() {
  return request.get('/system/role/all')
}
export function createRole(data: any) {
  return request.post('/system/role', data)
}
export function updateRole(data: any) {
  return request.put('/system/role', data)
}
export function deleteRoles(ids: number[]) {
  return request.delete(`/system/role/${ids.join(',')}`)
}
export function roleMenuTree(roleId: number) {
  return request.get(`/system/menu/roleMenuTreeselect/${roleId}`)
}
export function roleAuthUsers(roleId: number, params: any) {
  return request.get(`/system/role/authUser/${roleId}`, { params })
}
export function roleAssignUsers(roleId: number, userIds: number[]) {
  return request.put('/system/role/authUser/assign', userIds, { params: { roleId } })
}