/**
 * 与 LangJsonForm 配套：仅处理「system_lang」多语言字典与 JSON 结构的校验/解析。
 * 业务侧不要直接 import @/utils/dict 的 SYSTEM_LANG，统一走本文件。
 */
import { i18n } from '@/plugins/vueI18n'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'

function tt(key: string) {
  const g = i18n?.global
  if (g && typeof g.t === 'function') {
    return String(g.t(key))
  }
  return key
}

/** 本组件固定使用的字典类型 */
export const LANG_JSON_DICT_TYPE = DICT_TYPE.SYSTEM_LANG

export function getSystemLangDictOptions() {
  return getStrDictOptions(LANG_JSON_DICT_TYPE)
}

export type LangJsonMessages = {
  empty?: string
  noneFilled?: string
}

function defaultLangJsonMessages(): Required<LangJsonMessages> {
  return {
    empty: tt('langJson.empty'),
    noneFilled: tt('langJson.noneFilled')
  }
}

/** 供 el-form 规则使用 */
export function validateLangJsonValue(
  value: Record<string, Record<string, string>> | undefined,
  fieldKey: string,
  callback: (e?: Error) => void,
  messages: LangJsonMessages = {}
) {
  const m = { ...defaultLangJsonMessages(), ...messages }
  if (!value || typeof value !== 'object' || Object.keys(value).length === 0) {
    callback(new Error(m.empty))
    return
  }
  const ok = getSystemLangDictOptions().some((d) =>
    (value[d.value]?.[fieldKey] ?? '').trim()
  )
  if (!ok) {
    callback(new Error(m.noneFilled))
    return
  }
  callback()
}

/** 生成 Element Plus 表单 validator */
export function createLangJsonValidator(
  fieldKey: string,
  messages?: LangJsonMessages
) {
  return (_rule: unknown, value: unknown, callback: (e?: Error) => void) => {
    validateLangJsonValue(value as any, fieldKey, callback, messages)
  }
}

/** 按字典顺序优先，从多语言对象中取某一字段的首个非空值 */
export function resolvePrimaryFieldFromLang(
  lang: Record<string, Record<string, string>> | undefined,
  fieldKey: string
): string {
  if (!lang) return ''
  for (const d of getSystemLangDictOptions()) {
    const n = lang[d.value]?.[fieldKey]?.trim()
    if (n) return n
  }
  for (const k of Object.keys(lang)) {
    const n = lang[k]?.[fieldKey]?.trim()
    if (n) return n
  }
  return ''
}
