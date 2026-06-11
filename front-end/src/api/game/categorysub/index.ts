import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 游戏分类信息 */
export interface CategorySub {
  id: number // 主键
  title: string // 分类名称
  code: string // 分类编码
  logo: string // 图片
  status: number // 状态
  sort: number // 排序
  /** 多语言：{ "zh-CN": { "title": "..." }, "en-US": { "title": "..." } } */
  lang?: Record<string, Record<string, string>> | string
  cateName: string // 大类名称
  cateId: number // 大类ID
}

// 游戏分类 API
export const CategorySubApi = {
  // 查询游戏分类分页
  getCategorySubPage: async (params: any) => {
    return await request.get({ url: `/game/category-sub/page`, params })
  },

  // 查询游戏分类详情
  getCategorySub: async (id: number) => {
    return await request.get({ url: `/game/category-sub/get?id=` + id })
  },

  // 新增游戏分类
  createCategorySub: async (data: CategorySub) => {
    return await request.post({ url: `/game/category-sub/create`, data })
  },

  // 修改游戏分类
  updateCategorySub: async (data: CategorySub) => {
    return await request.put({ url: `/game/category-sub/update`, data })
  },

  // 删除游戏分类
  deleteCategorySub: async (id: number) => {
    return await request.delete({ url: `/game/category-sub/delete?id=` + id })
  },

  /** 批量删除游戏分类 */
  deleteCategorySubList: async (ids: number[]) => {
    return await request.delete({
      url: `/game/category-sub/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出游戏分类 Excel
  exportCategorySub: async (params) => {
    return await request.download({
      url: `/game/category-sub/export-excel`,
      params
    })
  },

  // 查询游戏大类
  getCategoryPage: async (params: any) => {
    return await request.get({ url: `/game/category/page`, params })
  }
}
