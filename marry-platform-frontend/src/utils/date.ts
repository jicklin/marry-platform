import dayjs from 'dayjs'

/**
 * 将日期格式化为 yyyy-MM-dd HH:mm:ss
 * @param value 待格式化的值（字符串、数字、Date）
 * @param fallback 无效值时的占位符，默认 '-'
 */
export function formatDateTime(value: string | number | Date | null | undefined, fallback = '-'): string {
  if (value === null || value === undefined || value === '') return fallback
  const d = dayjs(value)
  return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : fallback
}
