/**
 * 按语言从 ./docs/zh-CN、./docs/en-US 聚合 .md（Vite import.meta.glob）。
 * 某语言目录下无文件则返回空数组，不再回退到其他语言。
 */
export interface DocMenuItem {
  key: string
  label: string
  content: string
}

const zhDocGlob = import.meta.glob<string>('./docs/zh-CN/*.md', {
  query: '?raw',
  import: 'default',
  eager: true
})

const enDocGlob = import.meta.glob<string>('./docs/en-US/*.md', {
  query: '?raw',
  import: 'default',
  eager: true
})

function stripYamlFrontmatter(source: string): string {
  const t = source.trimStart()
  if (!t.startsWith('---\n')) return source
  const end = t.indexOf('\n---\n', 4)
  if (end === -1) return source
  return t.slice(end + 5)
}

function extractTitle(md: string): string {
  const body = stripYamlFrontmatter(md)
  const m = body.match(/^#\s+(.+)$/m)
  return m?.[1]?.trim() ?? ''
}

function pathToKey(path: string): string {
  const base = path.match(/([^/]+)\.md$/i)?.[1] ?? ''
  return base.replace(/^\d+[-_]/, '')
}

function fallbackLabel(key: string): string {
  return key
    .split(/[-_]/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}

function buildDocMenusFromGlob(globMap: Record<string, string>): DocMenuItem[] {
  const sorted = Object.entries(globMap).sort(([pathA], [pathB]) =>
    pathA.localeCompare(pathB, undefined, { numeric: true })
  )

  return sorted
    .map(([path, content]) => {
      const key = pathToKey(path)
      const label = extractTitle(content) || fallbackLabel(key)
      return { key, label, content }
    })
    .filter((item) => Boolean(item.key))
}

/** 与 vue-i18n locale 对齐：中文走 zh-CN 目录，其余走 en-US */
export function resolveSwaggerDocLocale(locale: string): 'zh-CN' | 'en-US' {
  const l = (locale || 'zh-CN').toLowerCase()
  if (l === 'zh-cn' || l.startsWith('zh')) return 'zh-CN'
  return 'en-US'
}

/** 根据当前语言仅读取对应目录，无文件则返回 [] */
export function getDocMenus(locale: string): DocMenuItem[] {
  const target = resolveSwaggerDocLocale(locale)
  const primary = target === 'zh-CN' ? zhDocGlob : enDocGlob
  return buildDocMenusFromGlob(primary)
}
