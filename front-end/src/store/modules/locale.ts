import { defineStore } from 'pinia'
import { store } from '../index'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { LocaleDropdownType } from '@/types/localeDropdown'

const { wsCache } = useCache()

// 历史缓存为 `en`，与 `en-US` 等价
const rawStoredLang = wsCache.get(CACHE_KEY.LANG) as string | undefined
if (rawStoredLang === 'en') {
  wsCache.set(CACHE_KEY.LANG, 'en-US')
}

const elLocaleMap: Record<LocaleType, typeof zhCn> = {
  'zh-CN': zhCn,
  'en-US': en
}
interface LocaleState {
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const useLocaleStore = defineStore('locales', {
  state: (): LocaleState => {
    return {
      currentLocale: {
        lang:
          (wsCache.get(CACHE_KEY.LANG) as LocaleType | undefined) || 'en-US',
        elLocale:
          elLocaleMap[
            (wsCache.get(CACHE_KEY.LANG) as LocaleType | undefined) || 'en-US'
          ]
      },
      // 多语言
      localeMap: [
        {
          lang: 'zh-CN',
          name: '简体中文'
        },
        {
          lang: 'en-US',
          name: 'English'
        }
      ]
    }
  },
  getters: {
    getCurrentLocale(): LocaleDropdownType {
      return this.currentLocale
    },
    getLocaleMap(): LocaleDropdownType[] {
      return this.localeMap
    }
  },
  //  设置语言
  actions: {
    setCurrentLocale(localeMap: LocaleDropdownType) {
      // this.locale = Object.assign(this.locale, localeMap)
      this.currentLocale.lang = localeMap?.lang
      this.currentLocale.elLocale = elLocaleMap[localeMap?.lang]
      wsCache.set(CACHE_KEY.LANG, localeMap?.lang)
    }
  }
})

export const useLocaleStoreWithOut = () => {
  return useLocaleStore(store)
}
