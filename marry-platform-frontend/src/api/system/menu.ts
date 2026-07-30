import request from '@/utils/request'
import type { SysMenu } from '@/api/types'

export function menuTree(): Promise<SysMenu[]> {
  return request.get('/system/menu/tree')
}
export function getRouters(): Promise<SysMenu[]> {
  return request.get('/system/menu/routers')
}
export function createMenu(data: Partial<SysMenu> & { name: string; menuType: 'M' | 'C' | 'F' }): Promise<void> {
  return request.post('/system/menu', data)
}
export function updateMenu(data: Partial<SysMenu> & { id: number }): Promise<void> {
  return request.put('/system/menu', data)
}
export function deleteMenu(id: number): Promise<void> {
  return request.delete(`/system/menu/${id}`)
}