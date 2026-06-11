import request from '@/config/axios'

/**
 * 单元测试接口挂在站点根路径，不带 /admin-api。
 * 默认 axios baseURL = VITE_BASE_URL + VITE_API_URL，此处单独指定 baseURL 仅为 VITE_BASE_URL。
 */
function getTesterBaseURL(): string {
  return String(import.meta.env.VITE_BASE_URL || '').replace(/\/$/, '')
}

/** 与后端「单元测试入参模型」一致的可选字段 */
export interface TesterRunBody {
  gameCode?: string
  currency?: string
  username?: string
  [key: string]: unknown
}

/** 查询游戏单元测试地图 → GET {VITE_BASE_URL}/tester/map/game */
export const getGameTesterMap = () => {
  return request.getOriginal<unknown>({
    url: '/tester/map/game',
    baseURL: getTesterBaseURL()
  })
}

/** 查询体育单元测试地图 → GET {VITE_BASE_URL}/tester/map/sports */
export const getSportsTesterMap = () => {
  return request.getOriginal<unknown>({
    url: '/tester/map/sports',
    baseURL: getTesterBaseURL()
  })
}

/** 运行验证案例 → POST {VITE_BASE_URL}/tester/map/run */
export const runTesterMap = (data: TesterRunBody) => {
  return request.postOriginal({
    url: '/tester/map/run',
    data,
    baseURL: getTesterBaseURL()
  })
}
