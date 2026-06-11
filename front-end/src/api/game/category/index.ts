import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 游戏大类信息 */
export interface Category {
  id: number // 主键
  title: string // 分类名称（展示用，可与多语言首选项一致）
  logo: string // 图片
  code: string // 分类编码
  status: number // 状态
  sort: number // 排序
  /** 多语言：{ "zh-CN": { "title": "..." }, "en-US": { "title": "..." } } */
  lang?: Record<string, Record<string, string>> | string
}

// 游戏大类 API
export const CategoryApi = {
  // 查询游戏大类分页
  getCategoryPage: async (params: any) => {
    return await request.get({ url: `/game/category/page`, params })
  },

  // 查询游戏大类详情
  getCategory: async (id: number) => {
    return await request.get({ url: `/game/category/get?id=` + id })
  },

  // 新增游戏大类
  createCategory: async (data: Category) => {
    return await request.post({ url: `/game/category/create`, data })
  },

  // 修改游戏大类
  updateCategory: async (data: Category) => {
    return await request.put({ url: `/game/category/update`, data })
  },

  // 删除游戏大类
  deleteCategory: async (id: number) => {
    return await request.delete({ url: `/game/category/delete?id=` + id })
  },

  /** 批量删除游戏大类 */
  deleteCategoryList: async (ids: number[]) => {
    return await request.delete({
      url: `/game/category/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出游戏大类 Excel
  exportCategory: async (params) => {
    return await request.download({
      url: `/game/category/export-excel`,
      params
    })
  },

  // 查询厂商编码
  getVendorCodePage: async (params: any) => {
    return await request.get({ url: `/game/vendor-tenant/page`, params })
  }
}
