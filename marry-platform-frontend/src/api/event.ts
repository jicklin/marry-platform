import request from '@/utils/request'
import type { PageQuery, PageResult, ChildEvent } from '@/api/types'

export function pageEvents(params: PageQuery): Promise<PageResult<ChildEvent>> {
  return request.get('/event/list', { params })
}

export function getEvent(id: number): Promise<ChildEvent> {
  return request.get(`/event/${id}`)
}

export function createEvent(data: Partial<ChildEvent> & { title: string; eventDate: string }): Promise<number> {
  return request.post('/event', data)
}

export function updateEvent(data: Partial<ChildEvent> & { id: number }): Promise<void> {
  return request.put('/event', data)
}

export function deleteEvents(ids: number[]): Promise<void> {
  return request.delete(`/event/${ids.join(',')}`)
}

export function attachFile(eventId: number, fileId: number, mediaType: string): Promise<void> {
  return request.post(`/event/${eventId}/attach`, null, { params: { fileId, mediaType } })
}

export function detachFile(eventId: number, fileId: number): Promise<void> {
  return request.delete(`/event/${eventId}/attach/${fileId}`)
}
