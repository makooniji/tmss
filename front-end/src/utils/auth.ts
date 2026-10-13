import { useCache, CACHE_KEY } from '@/hooks/web/useCache'
import { TokenType } from '@/api/login/types'
import { decrypt, encrypt } from '@/utils/jsencrypt'

const { wsCache } = useCache()

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

// 获取token
export const getAccessToken = () => {
  // 此处与TokenKey相同，此写法解决初始化时Cookies中不存在TokenKey报错
  const accessToken = wsCache.get(AccessTokenKey)
  return accessToken ? accessToken : wsCache.get('ACCESS_TOKEN')
}

// 刷新token
export const getRefreshToken = () => {
  return wsCache.get(RefreshTokenKey)
}

// 设置 token（刷新 token 时仅更新令牌，不改动 tenant-id）
export const setToken = (token: TokenType) => {
  wsCache.set(RefreshTokenKey, token.refreshToken)
  wsCache.set(AccessTokenKey, token.accessToken)
}

/** 登录成功后，用接口返回的 tenantId 覆盖本地缓存 */
export const setLoginTenantId = (tenantId: number) => {
  wsCache.set(CACHE_KEY.TenantId, tenantId)
}

/** 登录成功：保存 token + 覆盖 tenant-id */
export const applyLoginResult = (token: TokenType) => {
  setToken(token)
  if (token.tenantId != null) {
    setLoginTenantId(token.tenantId)
  }
}

// 删除token
export const removeToken = () => {
  wsCache.delete(AccessTokenKey)
  wsCache.delete(RefreshTokenKey)
  wsCache.delete(CACHE_KEY.TenantId)
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
  return 'Bearer ' + token
}
// ========== 账号相关 ==========

export type LoginFormType = {
  username: string
  password: string
  rememberMe: boolean
}

export const getLoginForm = () => {
  const loginForm: LoginFormType = wsCache.get(CACHE_KEY.LoginForm)
  if (loginForm) {
    loginForm.password = decrypt(loginForm.password) as string
  }
  return loginForm
}

export const setLoginForm = (loginForm: LoginFormType) => {
  loginForm.password = encrypt(loginForm.password) as string
  wsCache.set(CACHE_KEY.LoginForm, loginForm, { exp: 30 * 24 * 60 * 60 })
}

export const removeLoginForm = () => {
  wsCache.delete(CACHE_KEY.LoginForm)
}

// ========== 租户相关 ==========

/** 获取登录接口返回并缓存的 tenant-id */
export const getTenantId = () => {
  return wsCache.get(CACHE_KEY.TenantId)
}
