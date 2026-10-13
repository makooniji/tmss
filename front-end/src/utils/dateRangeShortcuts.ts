import dayjs from 'dayjs'

/** el-date-picker 日期范围快捷项 */
export interface DateRangeShortcut {
  text: string
  value: () => [Date, Date]
}

export interface DateRangeShortcutsOptions {
  /** 是否包含「明天」快捷项 */
  tomorrow?: boolean
  /** 天数范围快捷项，默认近 7/14/30/90 天 */
  ranges?: number[]
}

const DEFAULT_RANGES = [7, 14, 30, 90]

const RANGE_LABEL_KEYS: Record<number, string> = {
  7: 'game.common.tab7d',
  14: 'game.common.tab14d',
  30: 'game.common.tab30d',
  90: 'game.common.tab90d'
}

/** 近 N 天（含今天）的日期范围 */
export function getRangeByDays(days: number): [Date, Date] {
  const end = dayjs().endOf('day').toDate()
  const start = dayjs()
    .subtract(days - 1, 'day')
    .startOf('day')
    .toDate()
  return [start, end]
}

function getSingleDay(daysOffset: number): [Date, Date] {
  const day = dayjs().add(daysOffset, 'day').startOf('day').toDate()
  return [day, day]
}

/** 构建日期范围快捷项（需传入 i18n 的 t 函数） */
export function buildDateRangeShortcuts(
  t: (key: string) => string,
  options: DateRangeShortcutsOptions = {}
): DateRangeShortcut[] {
  const { tomorrow = false, ranges = DEFAULT_RANGES } = options

  const shortcuts: DateRangeShortcut[] = [
    {
      text: t('form.today'),
      value: () => getSingleDay(0)
    }
  ]

  if (tomorrow) {
    shortcuts.push({
      text: t('form.tomorrow'),
      value: () => getSingleDay(1)
    })
  }

  shortcuts.push({
    text: t('form.yesterday'),
    value: () => getSingleDay(-1)
  })

  for (const days of ranges) {
    const labelKey = RANGE_LABEL_KEYS[days]
    if (!labelKey) {
      continue
    }
    shortcuts.push({
      text: t(labelKey),
      value: () => getRangeByDays(days)
    })
  }

  return shortcuts
}
