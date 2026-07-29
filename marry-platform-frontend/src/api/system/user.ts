import request from '@/utils/request'

export function pageUsers(params: any) {
  return request.get('/system/user/list', { params })
}
export function createUser(data: any) {
  return request.post('/system/user', data)
}
export function updateUser(data: any) {
  return request.put('/system/user', data)
}
export function deleteUsers(ids: number[]) {
  return request.delete(`/system/user/${ids.join(',')}`)
}
export function resetUserPassword(id: number, password: string) {
  return request.put(`/system/user/resetPwd/${id}`, null, { params: { password } })
}
export function changeUserStatus(id: number, status: number) {
  return request.put(`/system/user/changeStatus/${id}`, null, { params: { status } })
}