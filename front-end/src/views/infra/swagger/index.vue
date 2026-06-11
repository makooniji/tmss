<template>
  <ContentWrap :bodyStyle="{ padding: '0px' }" class="swagger-wrap !mb-0">
    <el-tabs v-model="mainTab" type="border-card" class="swagger-main-tabs">
      <el-tab-pane :label="t('infra.swagger.tabDocs')" name="docs" lazy>
        <div class="doc-gitbook">
          <el-empty
            v-if="!docMenus.length"
            class="doc-gitbook__empty"
            :description="t('infra.swagger.emptyDocsHint')"
          />
          <el-container v-else class="doc-gitbook__container">
            <Sidebar
              :menus="menuItems"
              :active-key="currentKey"
              @change="onMenuChange"
            />
            <el-container direction="vertical" class="doc-gitbook__main">
              <DocHeader :title="t('infra.swagger.docSystemTitle')" />
              <el-main class="doc-gitbook__body">
                <MarkdownView :content="currentContent" />
              </el-main>
            </el-container>
          </el-container>
        </div>
      </el-tab-pane>
      <!-- <el-tab-pane :label="t('infra.swagger.tabKnife')" name="knife" lazy>
        <IFrame v-if="!knifeLoading" v-loading="knifeLoading" :src="knifeSrc" />
      </el-tab-pane> -->
    </el-tabs>
  </ContentWrap>
</template>

<script lang="ts" setup>
  import * as ConfigApi from '@/api/infra/config'
  import { useLocaleStore } from '@/store/modules/locale'
  import { getDocMenus } from './manifest'
  import DocHeader from './components/Header.vue'
  import MarkdownView from './components/MarkdownView.vue'
  import Sidebar from './components/Sidebar.vue'

  defineOptions({ name: 'InfraSwagger' })

  const { t } = useI18n()
  const localeStore = useLocaleStore()

  /** 与顶部语言切换一致：zh-CN / en-US */
  const appLang = computed(() => localeStore.currentLocale.lang)

  const mainTab = ref<'docs' | 'knife'>('docs')
  const knifeLoading = ref(true)
  const knifeSrc = ref(import.meta.env.VITE_BASE_URL + '/doc.html')

  /** 随语言加载 docs/zh-CN 或 docs/en-US */
  const docMenus = computed(() => getDocMenus(appLang.value))

  const menuItems = computed(() =>
    docMenus.value.map(({ key, label }) => ({ key, label }))
  )

  const currentKey = ref('')

  const currentContent = computed(() => {
    const item = docMenus.value.find((d) => d.key === currentKey.value)
    return item?.content ?? ''
  })

  watch(
    [appLang, docMenus],
    () => {
      const menus = docMenus.value
      if (!menus.length) {
        currentKey.value = ''
        return
      }
      if (!menus.some((m) => m.key === currentKey.value)) {
        currentKey.value = menus[0].key
      }
    },
    { immediate: true }
  )

  function onMenuChange(key: string) {
    currentKey.value = key
  }

  /** 初始化 Knife4j 地址 */
  onMounted(async () => {
    try {
      const data = await ConfigApi.getConfigKey('url.swagger')
      if (data && data.length > 0) {
        knifeSrc.value = data
      }
    } finally {
      knifeLoading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .swagger-wrap {
    :deep(.el-card__body) {
      padding: 0;
    }
  }

  .swagger-main-tabs {
    border: none;

    :deep(.el-tabs__header) {
      padding: 0 12px;
      margin: 0;
      background: var(--el-bg-color);
    }

    :deep(.el-tabs__content) {
      padding: 0;
    }

    :deep(.el-tabs__nav-wrap) {
      padding-top: 8px;
    }
  }

  .doc-gitbook {
    height: calc(
      100vh - var(--top-tool-height) - var(--tags-view-height) - var(
          --app-content-padding
        ) - var(--app-content-padding) -
        48px
    );
    min-height: 480px;
    background: var(--el-bg-color-page);
  }

  .doc-gitbook__container {
    height: 100%;
    min-height: 0;
  }

  .doc-gitbook__main {
    min-width: 0;
    background: var(--el-bg-color);
  }

  .doc-gitbook__body {
    padding: 0;
    overflow: auto;
  }

  .doc-gitbook__empty {
    display: flex;
    height: 100%;
    min-height: 320px;
    align-items: center;
    justify-content: center;
    padding: 24px;
  }
</style>
