import request from '@/utils/request'

export interface DashboardStats {
  userCount: number
  onlineCount: number
  todayOperLog: number
  todayLoginCount: number
  visitTrend: { date: string; count: number }[]
  operTypeDist: { name: string; value: number }[]
  loginStatusDist: { name: string; value: number }[]
}

export function getDashboardStats(): Promise<DashboardStats> {
  return request.get('/dashboard/stats')
}