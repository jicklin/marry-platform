import request from '@/utils/request'

export function menuTree() {
  return request.get('/system/menu/tree')
}
export function getRouters() {
  return request.get('/system/menu/routers')
}
export function createMenu(data: any) {
  return request.post('/system/menu', data)
}
export function updateMenu(data: any) {
  return request.put('/system/menu', data)
}
export function deleteMenu(id: number) {
  return request.delete(`/system/menu/${id}`)
}