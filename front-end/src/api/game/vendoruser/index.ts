import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 厂商玩家信息 */
export interface VendorUser {
          id: number; // 主键
          vendorId?: number; // 厂商ID
          vendorCode?: string; // 厂商编码
          userId?: number; // 用户ID
          username?: string; // 用户名
          status?: number; // 状态
  }

// 厂商玩家 API
export const VendorUserApi = {
  // 查询厂商玩家分页
  getVendorUserPage: async (params: any) => {
    return await request.get({ url: `/game/vendor-user/page`, params })
  },

  // 查询厂商玩家详情
  getVendorUser: async (id: number) => {
    return await request.get({ url: `/game/vendor-user/get?id=` + id })
  },

  // 新增厂商玩家
  createVendorUser: async (data: VendorUser) => {
    return await request.post({ url: `/game/vendor-user/create`, data })
  },

  // 修改厂商玩家
  updateVendorUser: async (data: VendorUser) => {
    return await request.put({ url: `/game/vendor-user/update`, data })
  },

  // 删除厂商玩家
  deleteVendorUser: async (id: number) => {
    return await request.delete({ url: `/game/vendor-user/delete?id=` + id })
  },

  /** 批量删除厂商玩家 */
  deleteVendorUserList: async (ids: number[]) => {
    return await request.delete({ url: `/game/vendor-user/delete-list?ids=${ids.join(',')}` })
  },

  // 导出厂商玩家 Excel
  exportVendorUser: async (params) => {
    return await request.download({ url: `/game/vendor-user/export-excel`, params })
  },
}