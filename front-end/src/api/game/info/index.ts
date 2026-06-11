import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 游戏信息信息 */
export interface Info {
  id: number // ID
  vendorCode: string // 厂商编码
  gameName: string // 游戏名称
  gameCode: string // 游戏CODE
  cateId: string // 游戏分类
  subCateId: string // 游戏小类
  logo: string // 封面
  sort: number // 排序
  maintain?: number // 维护状态
  onlineCount: number // 在线人数
  remark: string // 备注
  /** 多语言：{ "zh-CN": { "gameName": "..." }, "en-US": { "gameName": "..." } } */
  lang?: Record<string, Record<string, string>>
}

// 游戏信息 API
export const InfoApi = {
  // 查询游戏信息分页
  getInfoPage: async (params: any) => {
    return await request.get({ url: `/game/info/page`, params })
  },

  // 查询游戏信息详情
  getInfo: async (id: number) => {
    return await request.get({ url: `/game/info/get?id=` + id })
  },

  // 新增游戏信息
  createInfo: async (data: Info) => {
    return await request.post({ url: `/game/info/create`, data })
  },

  // 修改游戏信息
  updateInfo: async (data: Info) => {
    return await request.put({ url: `/game/info/update`, data })
  },

  // 删除游戏信息
  deleteInfo: async (id: number) => {
    return await request.delete({ url: `/game/info/delete?id=` + id })
  },

  /** 批量删除游戏信息 */
  deleteInfoList: async (ids: number[]) => {
    return await request.delete({
      url: `/game/info/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出游戏信息 Excel
  exportInfo: async (params) => {
    return await request.download({ url: `/game/info/export-excel`, params })
  }
}
