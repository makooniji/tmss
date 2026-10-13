import {
  buildDateRangeShortcuts,
  type DateRangeShortcutsOptions
} from '@/utils/dateRangeShortcuts'

/**
 * 日期范围快捷选项（适用于 el-date-picker :shortcuts）
 *
 * @example
 * const dateRangeShortcuts = useDateRangeShortcuts()
 * const dateRangeShortcuts = useDateRangeShortcuts({ tomorrow: true })
 */
export function useDateRangeShortcuts(options?: DateRangeShortcutsOptions) {
  const { t } = useI18n()
  return computed(() => buildDateRangeShortcuts(t, options))
}
