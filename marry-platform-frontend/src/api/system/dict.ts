import request from '@/utils/request'
import type { PageQuery, PageResult, SysDictType, SysDictData } from '@/api/types'

export function pageDictTypes(params: PageQuery): Promise<PageResult<SysDictType>> {
  return request.get('/system/dict/type/list', { params })
}
export function pageDictData(params: PageQuery): Promise<PageResult<SysDictData>> {
  return request.get('/system/dict/data/list', { params })
}
export function listDictDataByType(dictType: string): Promise<SysDictData[]> {
  return request.get('/system/dict/data/type', { params: { dictType } })
}
export function createDictType(data: Partial<SysDictType> & { name: string; type: string }): Promise<void> {
  return request.post('/system/dict/type', data)
}
export function updateDictType(data: Partial<SysDictType> & { id: number }): Promise<void> {
  return request.put('/system/dict/type', data)
}
export function deleteDictTypes(ids: number[]): Promise<void> {
  return request.delete(`/system/dict/type/${ids.join(',')}`)
}
export function createDictData(data: Partial<SysDictData> & { dictType: string; label: string; value: string }): Promise<void> {
  return request.post('/system/dict/data', data)
}
export function updateDictData(data: Partial<SysDictData> & { id: number }): Promise<void> {
  return request.put('/system/dict/data', data)
}
export function deleteDictData(ids: number[]): Promise<void> {
  return request.delete(`/system/dict/data/${ids.join(',')}`)
}