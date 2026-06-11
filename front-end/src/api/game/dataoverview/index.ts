import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 租户游戏日报信息 */
export interface GameDataOverview {
          id: number; // 主键
          days: string; // 时间维度
          tenantCode: string; // 租户编码
          tenantName: string; // 租户名称
          vendorCode: string; // 厂商编码
          vendorName: string; // 厂商名称
  }

// 租户游戏日报 API
export const GameDataOverviewApi = {
  // 查询租户游戏日报分页
  getGameDataOverviewPage: async (params: any) => {
    return await request.get({ url: `/game/tenant-static/page`, params, customPath:"static-api" })
  },

  // 查询租户游戏日报详情
  getGameDataOverview: async (id: number) => {
    return await request.get({ url: `/game/tenant-day-static/get?id=` + id,customPath:"static-api" })
  },

  // 新增租户游戏日报
  createGameDataOverview: async (data: GameDataOverview) => {
    return await request.post({ url: `/game/tenant-day-static/create`, data,customPath:"static-api" })
  },

  // 修改租户游戏日报
  updateGameDataOverview: async (data: GameDataOverview) => {
    return await request.put({ url: `/game/tenant-day-static/update`, data,customPath:"static-api" })
  },

  // 删除租户游戏日报
  deleteGameDataOverview: async (id: number) => {
    return await request.delete({ url: `/game/tenant-day-static/delete?id=` + id ,customPath:"static-api"})
  },

  /** 批量删除租户游戏日报 */
  deleteGameDataOverviewList: async (ids: number[]) => {
    return await request.delete({customPath:"static-api", url: `/game/tenant-day-static/delete-list?ids=${ids.join(',')}` })
  },

  // 导出租户游戏日报 Excel
  exportGameDataOverview: async (params) => {
    return await request.download({customPath:"static-api", url: `/game/tenant-day-static/export-excel`, params })
  },


  // 查询游戏总计数据
  gettenantstatictotal: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-static/total`, params })
  },
  
  // 商户柱状图
  getchartDayInfo: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-day-static/chartDayInfo`, params })
  },
  
  // 商户曲线图
  getchartDayTotal: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-day-static/chartDayTotal`, params })
  },

  // 查询投注明细分页
  getrecordsPage: async (params: any) => {
    return await request.get({ url: `/game/records/page`, params })
  },

  // 查询游戏总计数据
  gettenantstaticTotal: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-static/gameTotal`, params })
  },


  // 游戏柱状图
  getchartGameInfo: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-static/chartGameTotal`, params })
  },
  
  // 游戏曲线图
  getchartGameDayTotal: async (params: any) => {
    return await request.get({customPath:"static-api", url: `/game/tenant-static/chartGameDayTotal`, params })
  },

  
}
