import request from '@/utils/request'

export function pageDictTypes(params: any) {
  return request.get('/system/dict/type/list', { params })
}
export function pageDictData(params: any) {
  return request.get('/system/dict/data/list', { params })
}
export function listDictDataByType(dictType: string) {
  return request.get('/system/dict/data/type', { params: { dictType } })
}
export function createDictType(data: any) {
  return request.post('/system/dict/type', data)
}
export function updateDictType(data: any) {
  return request.put('/system/dict/type', data)
}
export function deleteDictTypes(ids: number[]) {
  return request.delete(`/system/dict/type/${ids.join(',')}`)
}
export function createDictData(data: any) {
  return request.post('/system/dict/data', data)
}
export function updateDictData(data: any) {
  return request.put('/system/dict/data', data)
}
export function deleteDictData(ids: number[]) {
  return request.delete(`/system/dict/data/${ids.join(',')}`)
}