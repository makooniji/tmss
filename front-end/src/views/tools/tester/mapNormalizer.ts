/**
 * GET /tester/map/game | /tester/map/sports 返回体：
 * 顶层 key = 大类名称 → 对象 → 子 key = 子场景名称 → 数组，数组元素为单条用例。
 */
export interface TesterCaseRow {
  caseCode: string
  api: string
  result: string
  header?: unknown
  body?: unknown
  remark?: string | null
  startTime?: string | null
  endTime?: string | null
  duration?: number | null
  expectedResult?: unknown
  actualResult?: unknown
  raw: Record<string, unknown>
}

export interface TesterSubgroup {
  title: string
  cases: TesterCaseRow[]
}

export interface TesterCategory {
  title: string
  subgroups: TesterSubgroup[]
}

function collectSubgroups(
  node: unknown,
  path: string[] = []
): TesterSubgroup[] {
  if (Array.isArray(node)) {
    const cases = node
      .map((row) => toCaseRow(row))
      .filter((x): x is TesterCaseRow => x != null)
    if (!cases.length) return []
    return [{ title: path.join(' / '), cases }]
  }
  if (node == null || typeof node !== 'object') return []

  const out: TesterSubgroup[] = []
  for (const [k, v] of Object.entries(node as Record<string, unknown>)) {
    out.push(...collectSubgroups(v, [...path, k]))
  }
  return out
}

function unwrapPayload(raw: unknown): unknown {
  if (typeof raw === 'string') {
    const text = raw.trim()
    if (!text) return raw
    try {
      return unwrapPayload(JSON.parse(text))
    } catch {
      return raw
    }
  }
  if (raw == null || typeof raw !== 'object') return raw
  const o = raw as Record<string, unknown>
  // 兼容后端统一返回 { code, msg, data } 以及 axios 响应壳 { data, status, ... }
  if (
    'data' in o &&
    ('code' in o ||
      'msg' in o ||
      'status' in o ||
      'statusText' in o ||
      'headers' in o ||
      'config' in o ||
      'request' in o ||
      Object.keys(o).length === 1)
  ) {
    return unwrapPayload(o.data)
  }
  return raw
}

function toCaseRow(row: unknown): TesterCaseRow | null {
  if (row == null || typeof row !== 'object' || Array.isArray(row)) return null
  const r = row as Record<string, unknown>
  return {
    caseCode: String(r.caseCode ?? ''),
    api: String(r.api ?? ''),
    result: String(r.result ?? ''),
    header: r.header,
    body: r.body,
    remark: (r.remark as string | null | undefined) ?? undefined,
    startTime: (r.startTime as string | null | undefined) ?? undefined,
    endTime: (r.endTime as string | null | undefined) ?? undefined,
    duration: typeof r.duration === 'number' ? r.duration : undefined,
    expectedResult: r.expectedResult,
    actualResult: r.actualResult,
    raw: { ...r }
  }
}

/** 是否为成功类结果文案（兼容中英文） */
export function isCaseSuccessResult(result: string): boolean {
  const s = (result || '').trim()
  if (!s) return false
  if (s === '成功') return true
  if (/^success$/i.test(s)) return true
  if (s === 'SUCCESS' || s === 'PASS' || s === 'OK') return true
  return false
}

export function countSubgroupStats(sub: TesterSubgroup): {
  pass: number
  fail: number
} {
  let pass = 0
  let fail = 0
  for (const c of sub.cases) {
    if (isCaseSuccessResult(c.result)) pass++
    else if (c.result) fail++
    else fail++
  }
  return { pass, fail }
}

/**
 * 解析嵌套地图对象 → 分类列表。
 */
export function parseTesterMapNested(raw: unknown): TesterCategory[] {
  const data = unwrapPayload(raw)
  if (data == null || typeof data !== 'object' || Array.isArray(data)) return []

  const root = data as Record<string, unknown>
  const categories: TesterCategory[] = []

  for (const [catTitle, catVal] of Object.entries(root)) {
    const subgroups = collectSubgroups(catVal)

    if (subgroups.length) {
      categories.push({ title: catTitle, subgroups })
    }
  }

  return categories
}

/** 全量是否全部成功（用于总览角标） */
export function isAllCasesSuccess(categories: TesterCategory[]): boolean {
  for (const cat of categories) {
    for (const sub of cat.subgroups) {
      for (const c of sub.cases) {
        if (!isCaseSuccessResult(c.result)) return false
      }
    }
  }
  return categories.length > 0
}
