import request from '@/utils/request'

export function listGen() { return request.get('/tool/gen/list') }
export function listDb() { return request.get('/tool/gen/db/list') }
export function importTables(names: string[]) {
  return request.post('/tool/gen/importTable', names)
}
export function syncColumns(tableId: number) {
  return request.put(`/tool/gen/sync/${tableId}`)
}
export function listColumns(tableId: number) {
  return request.get(`/tool/gen/column/${tableId}`)
}
export function downloadGenZip(tableId: number) {
  return request.get(`/tool/gen/download/${tableId}`, { responseType: 'blob' })
}