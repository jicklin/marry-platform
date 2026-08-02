import request from '@/utils/request'
import type { PageQuery, PageResult, SysNote } from '@/api/types'

export function pageNotes(params: PageQuery): Promise<PageResult<SysNote>> {
  return request.get('/note/list', { params })
}
export function createNote(data: Partial<SysNote> & { title: string }): Promise<void> {
  return request.post('/note', data)
}
export function updateNote(data: Partial<SysNote> & { id: number }): Promise<void> {
  return request.put('/note', data)
}
export function deleteNotes(ids: number[]): Promise<void> {
  return request.delete(`/note/${ids.join(',')}`)
}
