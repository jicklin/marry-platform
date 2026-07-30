import request from '@/utils/request'
import type { SysDept } from '@/api/types'

export function deptTree(params?: { name?: string; status?: number }): Promise<SysDept[]> {
  return request.get('/system/dept/tree', { params })
}
export function createDept(data: Partial<SysDept> & { name: string; parentId: number }): Promise<void> {
  return request.post('/system/dept', data)
}
export function updateDept(data: Partial<SysDept> & { id: number }): Promise<void> {
  return request.put('/system/dept', data)
}
export function deleteDept(id: number): Promise<void> {
  return request.delete(`/system/dept/${id}`)
}