import request from '@/utils/request'

export function pageJobs(params: any) {
  return request.get('/monitor/job/list', { params })
}
export function createJob(data: any) {
  return request.post('/monitor/job', data)
}
export function updateJob(data: any) {
  return request.put('/monitor/job', data)
}
export function deleteJobs(ids: number[]) {
  return request.delete(`/monitor/job/${ids.join(',')}`)
}
export function changeJobStatus(id: number, status: number) {
  return request.put('/monitor/job/changeStatus', null, { params: { id, status } })
}
export function runJobOnce(id: number) {
  return request.put(`/monitor/job/run/${id}`)
}
export function pageJobLogs(params: any) {
  return request.get('/monitor/job/log', { params })
}