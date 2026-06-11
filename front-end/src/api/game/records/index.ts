import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 用户游戏记录信息 */
export interface Records {
  id: number // ID
  userId: number // 用户ID
  action: number // 1：下注，2：派彩, 3：下注撤销 废弃 合并成了一条数据
  transNo: string // 第三方交易订单号
  balance: number // 游戏金额 单位为:厘
  drawId: string // 牌局ID
  roomKind: number // 房间ID
  betAmount: number // 下注金额 单位为:厘
  winAmount: number // 赢的金额 单位为:厘
  extraBusiness: string // 第三方游戏的扩展字段(捕鱼等游戏)
  sourceType: number // 0 正常推送 1补单
  platform: string // 所属平台
  gameCode: string // 游戏编码
  gameKind?: number // 游戏类型 1 SLOTS 2 直播类型 3 BINGO 4 POKER棋牌类 5 捕鱼 6 RNG 7 MINI 8 体育
  betContent: string // 投注内容
  issueNo: string // 局号期号
  remark: string // 备注信息，体育情况较多
  odds: string // 赔率信息
  vendorCode: string //场馆编码
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

  /** 汇总列表导出（路径需与后端一致） */
  exportTotalRecords: async (params) => {
    return await request.download({
      url: `/game/records/export-total-excel`,
      params
    })
  },

  // 查询场馆统计数据
  vendorTotal: async (vendorCode: string) => {
    return await request.get({
      url: `/game/records/vendorTotal?vendorCode=` + vendorCode
    })
  }
}
