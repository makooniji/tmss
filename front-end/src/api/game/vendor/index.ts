import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 游戏厂商信息 */
export interface Vendor {
          id: number; // 主键
          vendorCode?: string; // 游戏厂商编码
          vendorName: string; // 游戏厂商简称
          // logo: string; // LOGO
          // currency: string; // 货币
          // status: number; // 状态
          // logo: string; // LOGO
          // currency: string; // 货币
          // status: number; // 状态
          remark: string; // 备注
  }

// 游戏厂商 API
export const VendorApi = {
  // 查询游戏厂商分页
  getVendorPage: async (params: any) => {
    return await request.get({ url: `/game/vendor/page`, params })
  },

  // 查询游戏厂商详情
  getVendor: async (id: number) => {
    return await request.get({ url: `/game/vendor/get?id=` + id })
  },

  // 新增游戏厂商
  createVendor: async (data: Vendor) => {
    return await request.post({ url: `/game/vendor/create`, data })
  },

  // 修改游戏厂商
  updateVendor: async (data: Vendor) => {
    return await request.put({ url: `/game/vendor/update`, data })
  },

  // 删除游戏厂商
  deleteVendor: async (id: number) => {
    return await request.delete({ url: `/game/vendor/delete?id=` + id })
  },

  /** 批量删除游戏厂商 */
  deleteVendorList: async (ids: number[]) => {
    return await request.delete({ url: `/game/vendor/delete-list?ids=${ids.join(',')}` })
  },

  // 导出游戏厂商 Excel
  exportVendor: async (params) => {
    return await request.download({ url: `/game/vendor/export-excel`, params })
  }
}