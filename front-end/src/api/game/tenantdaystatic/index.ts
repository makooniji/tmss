import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 租户游戏日报信息 */
export interface TenantDayStatic {
          id: number; // 主键
          days: string; // 时间维度
          tenantCode: string; // 租户编码
          tenantName: string; // 租户名称
          vendorCode: string; // 厂商编码
          vendorName: string; // 厂商名称
  }

// 租户游戏日报 API
export const TenantDayStaticApi = {
  // 查询租户游戏日报分页
  getTenantDayStaticPage: async (params: any) => {
    return await request.get({ customPath:"static-api", url: `/game/tenant-static/page`, params })
  },

  // 查询租户游戏日报详情
  getTenantDayStatic: async (id: number) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-day-static/get?id=` + id })
  },

  // 新增租户游戏日报
  createTenantDayStatic: async (data: TenantDayStatic) => {
    return await request.post({customPath:"static-api", url: `/game/tenant-day-static/create`, data })
  },

  // 修改租户游戏日报
  updateTenantDayStatic: async (data: TenantDayStatic) => {
    return await request.put({customPath:"static-api", url: `/game/tenant-day-static/update`, data })
  },

  // 删除租户游戏日报
  deleteTenantDayStatic: async (id: number) => {
    return await request.delete({customPath:"static-api", url: `/game/tenant-day-static/delete?id=` + id })
  },

  /** 批量删除租户游戏日报 */
  deleteTenantDayStaticList: async (ids: number[]) => {
    return await request.delete({customPath:"static-api", url: `/game/tenant-day-static/delete-list?ids=${ids.join(',')}` })
  },

  // 导出租户游戏日报 Excel
  exportTenantDayStatic: async (params) => {
    return await request.download({customPath:"static-api", url: `/game/tenant-day-static/export-excel`, params })
  },
}
