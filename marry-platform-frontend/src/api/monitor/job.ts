import request from '@/utils/request'
import type { PageQuery, PageResult, SysJob } from '@/api/types'

export function pageJobs(params: PageQuery): Promise<PageResult<SysJob>> {
  return request.get('/monitor/job/list', { params })
}
export function createJob(data: Partial<SysJob> & { name: string; beanName: string; methodName: string; cron: string }): Promise<void> {
  return request.post('/monitor/job', data)
}
export function updateJob(data: Partial<SysJob> & { id: number }): Promise<void> {
  return request.put('/monitor/job', data)
}
export function deleteJobs(ids: number[]): Promise<void> {
  return request.delete(`/monitor/job/${ids.join(',')}`)
}
export function changeJobStatus(id: number, status: number): Promise<void> {
  return request.put('/monitor/job/changeStatus', null, { params: { id, status } })
}
export function runJobOnce(id: number): Promise<void> {
  return request.put(`/monitor/job/run/${id}`)
}
export function pageJobLogs(params: PageQuery): Promise<PageResult<any>> {
  return request.get('/monitor/job/log', { params })
}