import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 租户厂商信息 */
export interface VendorTenant {
  id: number // 主键
  vendorCode?: string // 游戏厂商编码
  vendorName: string // 游戏厂商简称
  vendorChannel: string // 通道
  vendorAgent: string // 账号
  vendorKey: string // 私钥
  vendorZone: string // 源时区
  lobbyUrl: string // 游戏大厅
  apiUrl: string // API地址
  logo: string // 图标
  currency: string // 货币类型
  lang: string // 语言
  status?: number // 状态
  remark: string // 备注
  rtp: number // RTP
  callbackUrl: string // 回调地址
  reportUrl: string // 拉单地址
  supportLang: undefined //语言
}

// 租户厂商 API
export const VendorTenantApi = {
  // 查询租户厂商分页
  getVendorTenantPage: async (params: any) => {
    return await request.get({ url: `/game/vendor-tenant/page`, params })
  },

  // 查询租户厂商详情
  getVendorTenant: async (id: number) => {
    return await request.get({ url: `/game/vendor-tenant/get?id=` + id })
  },

  // 新增租户厂商
  createVendorTenant: async (data: VendorTenant) => {
    return await request.post({ url: `/game/vendor-tenant/create`, data })
  },

  // 修改租户厂商
  updateVendorTenant: async (data: VendorTenant) => {
    return await request.put({ url: `/game/vendor-tenant/update`, data })
  },

  // 删除租户厂商
  deleteVendorTenant: async (id: number) => {
    return await request.delete({ url: `/game/vendor-tenant/delete?id=` + id })
  },

  /** 批量删除租户厂商 */
  deleteVendorTenantList: async (ids: number[]) => {
    return await request.delete({
      url: `/game/vendor-tenant/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出租户厂商 Excel
  exportVendorTenant: async (params) => {
    return await request.download({
      url: `/game/vendor-tenant/export-excel`,
      params
    })
  }
}
