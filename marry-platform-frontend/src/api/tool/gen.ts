import request from '@/utils/request'
import type { GenTable, GenTableColumn } from '@/api/types'

export function listGen(): Promise<GenTable[]> {
  return request.get('/tool/gen/list')
}
export function listDb(): Promise<{ tableName: string; tableComment: string }[]> {
  return request.get('/tool/gen/db/list')
}
export function importTables(names: string[]): Promise<void> {
  return request.post('/tool/gen/importTable', names)
}
export function syncColumns(tableId: number): Promise<void> {
  return request.put(`/tool/gen/sync/${tableId}`)
}
export function listColumns(tableId: number): Promise<GenTableColumn[]> {
  return request.get(`/tool/gen/column/${tableId}`)
}
export function downloadGenZip(tableId: number): Promise<Blob> {
  return request.get(`/tool/gen/download/${tableId}`, { responseType: 'blob' })
}