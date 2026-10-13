import request from '@/config/axios'

export interface TenantVO {
  id: number
  parentId?: number
  name: string
  contactName: string
  contactMobile: string
  status: number
  operateStatus?: number // 运营状态：0 开启 1 关闭
  implType?: number // 接入方式：0 免转 1 转账
  domain: string
  packageId: number
  username: string
  password: string
  expireTime: Date
  accountCount: number
  websites: string[]
  createTime: Date
  tenantKey?: string
  secretKey?: string
}

export interface TenantPageReqVO extends PageParam {
  name?: string
  contactName?: string
  contactMobile?: string
  status?: number
  operateStatus?: number
  createTime?: Date[]
  parentId?: number
}

export interface TenantExportReqVO {
  name?: string
  contactName?: string
  contactMobile?: string
  status?: number
  createTime?: Date[]
}

// 查询租户列表
export const getTenantPage = (params: TenantPageReqVO) => {
  return request.get({ url: '/system/tenant/page', params })
}

// 查询租户详情
export const getTenant = (id: number) => {
  return request.get({ url: '/system/tenant/get?id=' + id })
}

// 查询商户资料（个人中心）
export const getMerchantInformation = (id?: number) => {
  return request.get({
    url: '/system/tenant/getMerchantInformation',
    params: id ? { id } : undefined
  })
}

export interface UpdateMerchantInfoReqVO {
  notifyUrl: string
  id?: number
}

// 修改商户资料（个人中心）
export const updateMerchantInfo = (data: UpdateMerchantInfoReqVO) => {
  return request.put({ url: '/system/tenant/updateMerchantInfo', data })
}

// 获取租户精简信息列表
export const getTenantList = () => {
  return request.get({ url: '/system/tenant/simple-list' })
}

// 新增租户
export const createTenant = (data: TenantVO) => {
  return request.post({ url: '/system/tenant/create', data })
}

// 修改租户
export const updateTenant = (data: TenantVO) => {
  return request.put({ url: '/system/tenant/update', data })
}

// 修改租户状态 / 运营状态（同一接口；运营状态时多传 operateStatus）
export const updateTenantStatus = (data: {
  id: number
  status?: number
  operateStatus?: number
}) => {
  return request.put({ url: '/system/tenant/update', data })
}

// 删除租户
export const deleteTenant = (id: number) => {
  return request.delete({ url: '/system/tenant/delete?id=' + id })
}

// 批量删除租户
export const deleteTenantList = (ids: number[]) => {
  return request.delete({
    url: '/system/tenant/delete-list',
    params: { ids: ids.join(',') }
  })
}

// 导出租户
export const exportTenant = (params: TenantExportReqVO) => {
  return request.download({ url: '/system/tenant/export-excel', params })
}

// 租户管理-更新游戏配置
export const updateGameConfig = (data) => {
  return request.put({ url: '/system/tenant/updateGameConfig', data })
}

// 租户管理-查询游戏配置
export const getGameConfig = (id: number) => {
  return request.get({ url: '/system/tenant/getGameConfig/' + id })
}
