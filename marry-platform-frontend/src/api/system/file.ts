import request from '@/utils/request'

export function uploadFile(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/system/file/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}