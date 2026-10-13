<template>
  <div class="markdown-body gitbook-md">
    <template v-for="(item, index) in parsedBlocks" :key="index">
      <!-- 常规 Markdown 内容 -->
      <div v-if="item.type === 'markdown'" v-html="renderMarkdown(item.content)"></div>

      <!-- GitBook Tabs 渲染为 Element Plus 组件 -->
      <el-tabs v-else-if="item.type === 'tabs'" class="custom-gitbook-tabs" type="border-card">
        <el-tab-pane v-for="(tab, tIndex) in item.tabs" :key="tIndex" :label="tab.title">
          <div v-html="renderMarkdown(tab.content)"></div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

interface TabItem {
  title: string
  content: string
}

interface Block {
  type: 'markdown' | 'tabs'
  content?: string
  tabs?: TabItem[]
}

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  /** 是否去掉开头的 YAML frontmatter（--- ... ---），避免在正文重复展示 */
  stripFrontmatter: {
    type: Boolean,
    default: true
  }
})

function stripYamlFrontmatter(source: string): string {
  const t = source.trimStart()
  if (!t.startsWith('---\n')) return source
  const end = t.indexOf('\n---\n', 4)
  if (end === -1) return source
  return t.slice(end + 5)
}

// MarkdownIt 实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre><code class="hljs">${hljs.highlight(code, { language: lang }).value
        }</code></pre>`
    }
    return `<pre><code>${md.utils.escapeHtml(code)}</code></pre>`
  }
})

function renderMarkdown(text: string): string {
  return md.render(text || '')
}

/**
 * 将文本解析为 Markdown 片段与 Tabs 片段组成的数组
 */
const parsedBlocks = computed<Block[]>(() => {
  let raw = props.content
  if (props.stripFrontmatter) {
    raw = stripYamlFrontmatter(raw)
  }

  const blocks: Block[] = []

  // 匹配 {% tabs %} ... {% endtabs %}（包含跨行）
  // 注意：如果文档中没有显示的 {% endtabs %}，正则将尝试匹配到下一个 {% tab %} 的组或结尾
  const tabsBlockRegex = /{%-?\s*tabs\s*-?%}([\s\S]*?)(?:{%-?\s*endtabs\s*-?%}|$)/g

  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = tabsBlockRegex.exec(raw)) !== null) {
    // 提取 tabs 前面的普通 markdown
    if (match.index > lastIndex) {
      blocks.push({
        type: 'markdown',
        content: raw.slice(lastIndex, match.index)
      })
    }

    // 解析当前 {% tabs %} 块内部的单个 {% tab title="..." %}
    const tabsContent = match[1]
    const tabRegex = /{%-?\s*tab\s+title=["'”’](.*?)["'”’]\s*-?%}([\s\S]*?)(?:{%-?\s*endtab\s*-?%}|$)/g
    const tabsList: TabItem[] = []
    let tabMatch: RegExpExecArray | null

    while ((tabMatch = tabRegex.exec(tabsContent)) !== null) {
      tabsList.push({
        title: tabMatch[1],
        content: tabMatch[2].trim()
      })
    }

    if (tabsList.length > 0) {
      blocks.push({
        type: 'tabs',
        tabs: tabsList
      })
    }

    lastIndex = tabsBlockRegex.lastIndex
  }

  // 补全剩余的 markdown 内容
  if (lastIndex < raw.length) {
    blocks.push({
      type: 'markdown',
      content: raw.slice(lastIndex)
    })
  }

  return blocks
})
</script>

<style scoped lang="scss">
.gitbook-md {
  max-width: 900px;
  padding: 8px 24px 48px;
  margin: 0 20px;
  font-size: 15px;
  line-height: 1.75;
  color: var(--el-text-color-primary);

  /* Element Plus Tabs 样式自定义微调 */
  .custom-gitbook-tabs {
    margin: 16px 0;
    box-shadow: none;
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-tabs__content) {
      padding: 16px;
    }

    :deep(pre) {
      margin: 0;
    }
  }

  :deep(h1) {
    padding-bottom: 8px;
    margin: 0 0 16px;
    font-size: 28px;
    font-weight: 600;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(h2) {
    margin: 28px 0 12px;
    font-size: 22px;
    font-weight: 600;
  }

  :deep(h3) {
    margin: 20px 0 10px;
    font-size: 18px;
    font-weight: 600;
  }

  :deep(a) {
    color: var(--el-color-primary);
  }

  :deep(pre) {
    padding: 14px 16px;
    overflow: auto;
    font-size: 13px;
    background: var(--el-fill-color-light);
    border-radius: 8px;
  }

  :deep(code) {
    font-family:
      ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  }

  :deep(p) {
    margin: 10px 0;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 1.4em;
    margin: 8px 0;
  }

  :deep(table) {
    width: 100%;
    margin: 12px 0;
    font-size: 14px;
    border-collapse: collapse;
  }

  :deep(th),
  :deep(td) {
    padding: 8px 10px;
    text-align: left;
    border: 1px solid var(--el-border-color);
  }

  :deep(th) {
    font-weight: 600;
    background: var(--el-fill-color-lighter);
  }

  :deep(hr) {
    margin: 24px 0;
    border: none;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>