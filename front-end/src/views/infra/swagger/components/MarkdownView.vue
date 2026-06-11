<template>
  <div class="markdown-body gitbook-md" v-html="html"></div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue'
  import MarkdownIt from 'markdown-it'
  import hljs from 'highlight.js'
  import 'highlight.js/styles/github.css'

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

  const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight(code, lang) {
      if (lang && hljs.getLanguage(lang)) {
        return `<pre><code class="hljs">${
          hljs.highlight(code, { language: lang }).value
        }</code></pre>`
      }
      return `<pre><code>${md.utils.escapeHtml(code)}</code></pre>`
    }
  })

  const html = computed(() => {
    let raw = props.content
    if (props.stripFrontmatter) {
      raw = stripYamlFrontmatter(raw)
    }
    return md.render(raw)
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
