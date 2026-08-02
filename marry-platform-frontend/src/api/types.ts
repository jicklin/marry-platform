// Mirrors the backend VOs / entities. Kept narrow on purpose — extend as the
// UI needs more fields, but DO NOT add server-only columns here (e.g.
// `password`).

export interface PageQuery {
  pageNum?: number
  pageSize?: number
  [key: string]: any
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  orders?: string[]
}

// sys_*
export interface SysUser {
  id: number
  username: string
  nickName?: string
  email?: string
  phone?: string
  avatar?: string
  sex?: number
  deptId?: number
  status: number
  remark?: string
  loginIp?: string
  loginDate?: string
  roleIds?: number[]
  createTime?: string
  updateTime?: string
}

export interface SysRole {
  id: number
  name: string
  code: string
  dataScope?: number
  status: number
  remark?: string
  createTime?: string
}

export interface SysDept {
  id: number
  parentId: number
  name: string
  code?: string
  leader?: string
  phone?: string
  email?: string
  orderNum?: number
  status: number
  ancestors?: string
  children?: SysDept[]
}

export interface SysMenu {
  id: number
  parentId: number
  name: string
  menuType: 'M' | 'C' | 'F'
  path?: string
  component?: string
  perm?: string
  icon?: string
  orderNum?: number
  visible?: number
  status: number
  isCache?: number
  isFrame?: number
  children?: SysMenu[]
}

export interface SysDictType {
  id: number
  name: string
  type: string
  status: number
  remark?: string
}

export interface SysDictData {
  id: number
  dictType: string
  label: string
  value: string
  cssClass?: string
  listClass?: string
  isDefault?: number
  orderNum?: number
  status: number
  remark?: string
}

export interface SysConfig {
  id: number
  name: string
  configKey: string
  configValue?: string
  configType?: number
  isBuiltin?: number
  remark?: string
}

export interface SysNotice {
  id: number
  title: string
  type?: string
  content?: string
  status: number
  remark?: string
  createTime?: string
}

export interface SysNote {
  id: number
  title: string
  content?: string
  tags?: string
  isPinned?: number
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
}

// monitor
export interface SysJob {
  id: number
  name: string
  beanName: string
  methodName: string
  params?: string
  cron: string
  status: number
  remark?: string
  createTime?: string
}

export interface SysOperLog {
  id: number
  title?: string
  businessType?: string
  method?: string
  requestMethod?: string
  operUrl?: string
  operName?: string
  deptName?: string
  operIp?: string
  status: number
  costTime?: number
  operTime?: string
  errorMsg?: string
}

export interface SysLoginLog {
  id: number
  userName?: string
  ip?: string
  status?: string
  message?: string
  loginTime?: string
}

// gen
export interface GenTable {
  id: number
  tableName: string
  tableComment?: string
  className?: string
  tplCategory?: string
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  genType?: string
  options?: string
  createTime?: string
}

export interface GenTableColumn {
  id: number
  tableId: number
  columnName: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField: string
  isPk?: number
  isIncrement?: number
  isRequired?: number
  isInsert?: number
  isEdit?: number
  isList?: number
  isQuery?: number
  queryType?: string
  htmlType?: string
  dictType?: string
  sort?: number
}

// child growth records
export interface ChildEventFile {
  id: number
  eventId: number
  fileId: number
  mediaType: string
  sortNo?: number
  url?: string
  originalName?: string
  contentType?: string
  size?: number
}

export interface ChildEvent {
  id: number
  title: string
  content?: string
  eventDate: string
  category?: string
  tags?: string
  importance?: number
  mood?: string
  dirName?: string
  createTime?: string
  attachFiles?: ChildEventFile[]
}
