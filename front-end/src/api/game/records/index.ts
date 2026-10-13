import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 用户游戏记录信息 */
export interface Records {
  id: number
  userId: number
  username?: string
  action?: number
  orderNo?: string
  refOrderNo?: string
  issueNo?: string
  betAmount?: number
  betContent?: string
  winAmount?: number
  winLoss?: number
  createTime?: string
  vendorCode?: string
  gameCode?: string
  gameKind?: number
  odds?: string
}

// 用户游戏记录 API
export const RecordsApi = {
  // 查询用户游戏记录分页
  getRecordsPage: async (params: any) => {
    return await request.get({ url: `/game/records/page`, params })
  },

  getRecordsTotalPage: async (params: any) => {
    return await request.get({ url: `/game/records/pageTotal`, params })
  },

  // 查询用户游戏记录详情
  getRecords: async (id: number) => {
    return await request.get({ url: `/game/records/get?id=` + id })
  },

  // 新增用户游戏记录
  createRecords: async (data: Records) => {
    return await request.post({ url: `/game/records/create`, data })
  },

  // 修改用户游戏记录
  updateRecords: async (data: Records) => {
    return await request.put({ url: `/game/records/update`, data })
  },

  // 删除用户游戏记录
  deleteRecords: async (id: number) => {
    return await request.delete({ url: `/game/records/delete?id=` + id })
  },

  /** 批量删除用户游戏记录 */
  deleteRecordsList: async (ids: number[]) => {
    return await request.delete({
      url: `/game/records/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出用户游戏记录 Excel
  exportRecords: async (params) => {
    return await request.download({ url: `/game/records/export-excel`, params })
  },

  // 导出用户游戏记录列表 Excel（租户日统计查看弹窗等）
  exportRecordsList: async (params) => {
    return await request.download({
      url: `/game/records/export-list-excel`,
      params
    })
  },

  // 导出用户游戏记录查询页 Excel
  exportRecordsPage: async (params) => {
    return await request.download({
      url: `/game/records/export-page-excel`,
      params
    })
  },

  /** 汇总列表导出（路径需与后端一致） */
  exportTotalRecords: async (params) => {
    return await request.download({
      url: `/game/records/export-total-excel`,
      params
    })
  },

  // 查询场馆统计数据（筛选条件与 /game/records/page 一致，不含分页）
  vendorTotal: async (params: Record<string, unknown>) => {
    return await request.get({ url: `/game/records/vendorTotal`, params })
  }
}
