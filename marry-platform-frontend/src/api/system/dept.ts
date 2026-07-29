import request from '@/utils/request'

export function deptTree(params?: any) {
  return request.get('/system/dept/tree', { params })
}
export function createDept(data: any) {
  return request.post('/system/dept', data)
}
export function updateDept(data: any) {
  return request.put('/system/dept', data)
}
export function deleteDept(id: number) {
  return request.delete(`/system/dept/${id}`)
}