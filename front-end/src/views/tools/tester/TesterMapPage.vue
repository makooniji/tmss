<template>
  <div v-loading="mapLoading" class="tester-page">
    <!-- 游戏验证：仅用户名 / 游戏代码 / 货币（无游戏代币） -->
    <el-card shadow="never" class="tester-card">
      <template #header>
        <span class="card-title">{{ t('tools.tester.verifyTitle') }}</span>
      </template>
      <el-form
        ref="formRef"
        :model="runForm"
        :rules="formRules"
        label-width="160px"
        class="verify-form"
        @submit.prevent
      >
        <el-form-item :label="t('tools.tester.usernameLabel')" prop="username">
          <el-input
            v-model="runForm.username"
            clearable
            :placeholder="t('tools.tester.usernamePlaceholder')"
            class="!w-360px"
          />
        </el-form-item>
        <el-form-item :label="t('tools.tester.gameCodeLabel')" prop="gameCode">
          <el-input
            v-model="runForm.gameCode"
            clearable
            :placeholder="t('tools.tester.gameCodePlaceholder')"
            class="!w-360px"
          />
        </el-form-item>
        <el-form-item :label="t('tools.tester.currencyLabel')" prop="currency">
          <el-input
            v-model="runForm.currency"
            clearable
            :placeholder="t('tools.tester.currencyPlaceholder')"
            class="!w-360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="runLoading"
            @click="handleStartTest"
          >
            {{ t('tools.tester.startTest') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 测试结果：左分类 + 右详情（与图2 一致） -->
    <el-card shadow="never" class="tester-card result-card">
      <template #header>
        <div class="result-card__header">
          <span class="card-title">{{ t('tools.tester.testResults') }}</span>
          <el-tag
            v-if="categories.length"
            :type="suiteAllSuccess ? 'success' : 'danger'"
            effect="dark"
            round
          >
            {{
              suiteAllSuccess
                ? t('tools.tester.suitePass')
                : t('tools.tester.suiteFail')
            }}
          </el-tag>
        </div>
      </template>

      <el-empty
        v-if="!categories.length"
        :description="t('tools.tester.emptyMap')"
      />

      <el-row v-else :gutter="16" class="result-body">
        <el-col :span="7" :xs="24" class="result-cats">
          <el-scrollbar class="cat-scroll">
            <div
              v-for="(cat, idx) in categories"
              :key="'cat-' + idx"
              class="cat-item"
              :class="{ 'is-active': activeCatIndex === idx }"
              @click="activeCatIndex = idx"
            >
              <Icon
                v-if="categoryAllSuccess(cat)"
                icon="ep:circle-check-filled"
                class="cat-item__icon cat-item__icon--ok"
              />
              <Icon
                v-else
                icon="ep:circle-close-filled"
                class="cat-item__icon cat-item__icon--bad"
              />
              <span class="cat-item__text">{{ cat.title }}</span>
            </div>
          </el-scrollbar>
        </el-col>

        <el-col :span="17" :xs="24" class="result-detail">
          <template v-if="activeCategory">
            <el-collapse v-model="expandedSubNames" class="sub-collapse">
              <el-collapse-item
                v-for="(sub, si) in activeCategory.subgroups"
                :key="subCollapseKey(activeCatIndex, si)"
                :name="subCollapseKey(activeCatIndex, si)"
                class="sub-block"
              >
                <template #title>
                  <div class="sub-head">
                    <span class="sub-title">{{
                      sub.title || activeCategory.title
                    }}</span>
                    <span class="sub-stats">
                      <span class="stat-ok"
                        >✔ {{ countSubgroupStats(sub).pass }}</span
                      >
                      <span class="stat-bad"
                        >✕ {{ countSubgroupStats(sub).fail }}</span
                      >
                    </span>
                  </div>
                </template>

                <el-collapse v-model="expandedCaseNames" class="case-collapse">
                  <el-collapse-item
                    v-for="(c, ci) in sub.cases"
                    :key="caseCollapseKey(activeCatIndex, si, ci)"
                    :name="caseCollapseKey(activeCatIndex, si, ci)"
                  >
                    <template #title>
                      <div class="case-title-row">
                        <span class="case-code">{{ c.caseCode }}</span>
                        <span class="case-api">{{ c.api }}</span>
                        <el-tag
                          :type="
                            isCaseSuccessResult(c.result) ? 'success' : 'danger'
                          "
                          size="small"
                          round
                          class="case-tag"
                        >
                          {{ c.result || t('tools.tester.unknown') }}
                        </el-tag>
                      </div>
                    </template>
                    <div class="case-detail">
                      <el-descriptions
                        :column="3"
                        border
                        size="small"
                        class="mb-12px"
                      >
                        <el-descriptions-item
                          :label="t('tools.tester.startTime')"
                        >
                          {{ c.startTime ?? '—' }}
                        </el-descriptions-item>
                        <el-descriptions-item
                          :label="t('tools.tester.endTime')"
                        >
                          {{ c.endTime ?? '—' }}
                        </el-descriptions-item>
                        <el-descriptions-item
                          :label="t('tools.tester.duration')"
                        >
                          {{ c.duration != null ? `${c.duration} ms` : '—' }}
                        </el-descriptions-item>
                      </el-descriptions>
                      <div class="kv-block">
                        <div class="kv-label">API</div>
                        <pre class="kv-pre">{{ c.api || '—' }}</pre>
                      </div>
                      <div class="kv-block">
                        <div class="kv-label">{{
                          t('tools.tester.requestHeaders')
                        }}</div>
                        <pre class="kv-pre">{{ formatJson(c.header) }}</pre>
                      </div>
                      <div class="kv-block">
                        <div class="kv-label">{{
                          t('tools.tester.requestBody')
                        }}</div>
                        <pre class="kv-pre">{{ formatJson(c.body) }}</pre>
                      </div>
                      <div class="kv-block">
                        <div class="kv-label">{{
                          t('tools.tester.remark')
                        }}</div>
                        <pre class="kv-pre">{{ c.remark ?? '—' }}</pre>
                      </div>
                      <el-row :gutter="12" class="diff-row">
                        <el-col :span="12" :xs="24">
                          <div class="diff-title">{{
                            t('tools.tester.expected')
                          }}</div>
                          <pre class="diff-pre">{{
                            formatJson(c.expectedResult)
                          }}</pre>
                        </el-col>
                        <el-col :span="12" :xs="24">
                          <div class="diff-title">{{
                            t('tools.tester.actual')
                          }}</div>
                          <pre class="diff-pre">{{
                            formatJson(c.actualResult)
                          }}</pre>
                        </el-col>
                      </el-row>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </el-collapse-item>
            </el-collapse>
          </template>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
  import type { FormInstance, FormRules } from 'element-plus'
  import type { TesterRunBody } from '@/api/tester/map'
  import { runTesterMap } from '@/api/tester/map'
  import {
    countSubgroupStats,
    isAllCasesSuccess,
    isCaseSuccessResult,
    parseTesterMapNested,
    type TesterCategory
  } from './mapNormalizer'

  const props = defineProps<{
    loadMap: () => Promise<unknown>
  }>()

  const { t } = useI18n()
  const message = useMessage()

  const mapLoading = ref(true)
  const runLoading = ref(false)
  const categories = ref<TesterCategory[]>([])
  const activeCatIndex = ref(0)
  const expandedSubNames = ref<string[]>([])
  const expandedCaseNames = ref<string[]>([])
  const formRef = ref<FormInstance>()

  const runForm = reactive<TesterRunBody>({
    username: '',
    gameCode: '',
    currency: ''
  })

  const formRules = computed<FormRules>(() => ({
    username: [
      {
        required: true,
        message: t('tools.tester.ruleUsername'),
        trigger: 'blur'
      }
    ],
    gameCode: [
      {
        required: true,
        message: t('tools.tester.ruleGameCode'),
        trigger: 'blur'
      }
    ],
    currency: [
      {
        required: true,
        message: t('tools.tester.ruleCurrency'),
        trigger: 'blur'
      }
    ]
  }))

  const activeCategory = computed(
    () => categories.value[activeCatIndex.value] ?? null
  )

  const suiteAllSuccess = computed(() => isAllCasesSuccess(categories.value))

  function categoryAllSuccess(cat: TesterCategory): boolean {
    for (const sub of cat.subgroups) {
      for (const c of sub.cases) {
        if (!isCaseSuccessResult(c.result)) return false
      }
    }
    return cat.subgroups.some((s) => s.cases.length > 0)
  }

  function subCollapseKey(ci: number, si: number) {
    return `sub-${ci}-${si}`
  }

  function caseCollapseKey(ci: number, si: number, ii: number) {
    return `${ci}-${si}-${ii}`
  }

  function formatJson(v: unknown): string {
    if (v === undefined || v === null) return '—'
    if (typeof v === 'string') {
      try {
        return JSON.stringify(JSON.parse(v), null, 2)
      } catch {
        return v
      }
    }
    try {
      return JSON.stringify(v, null, 2)
    } catch {
      return String(v)
    }
  }

  /** 若 POST 返回与地图同结构则直接替换，否则重新 GET */
  function tryApplyMapPayload(raw: unknown) {
    const parsed = parseTesterMapNested(raw)
    if (parsed.length) {
      categories.value = parsed
      activeCatIndex.value = 0
      return true
    }
    return false
  }

  async function loadMapData() {
    mapLoading.value = true
    try {
      const raw = await props.loadMap()
      categories.value = parseTesterMapNested(raw)
      if (activeCatIndex.value >= categories.value.length) {
        activeCatIndex.value = 0
      }
      expandedSubNames.value = (
        categories.value[activeCatIndex.value]?.subgroups ?? []
      ).map((_, si) => subCollapseKey(activeCatIndex.value, si))
      expandedCaseNames.value = []
    } catch {
      categories.value = []
      message.error(t('tools.tester.loadMapFail'))
    } finally {
      mapLoading.value = false
    }
  }

  async function handleStartTest() {
    await formRef.value?.validate().catch(() => Promise.reject())
    runLoading.value = true
    try {
      const body: TesterRunBody = {
        username: runForm.username?.trim(),
        gameCode: runForm.gameCode?.trim(),
        currency: runForm.currency?.trim()
      }
      const res = await runTesterMap(body)
      if (!tryApplyMapPayload(res)) {
        await loadMapData()
      }
      message.success(t('tools.tester.runDone'))
    } catch {
      /* 校验失败或请求失败 */
    } finally {
      runLoading.value = false
    }
  }

  onMounted(() => {
    loadMapData()
  })

  watch(activeCategory, (cat) => {
    expandedSubNames.value = (cat?.subgroups ?? []).map((_, si) =>
      subCollapseKey(activeCatIndex.value, si)
    )
    expandedCaseNames.value = []
  })
</script>

<style scoped lang="scss">
  .tester-page {
    padding: 0 0 16px;
  }

  .tester-card {
    margin-bottom: 16px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .verify-form {
    max-width: 720px;
  }

  .result-card__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .result-body {
    min-height: 420px;
  }

  .result-cats {
    padding-right: 8px;
    border-right: 1px solid var(--el-border-color-lighter);
  }

  .cat-scroll {
    max-height: calc(100vh - 360px);
    min-height: 320px;
  }

  .cat-item {
    display: flex;
    padding: 10px 12px;
    margin-bottom: 4px;
    font-size: 14px;
    line-height: 1.45;
    color: var(--el-text-color-primary);
    cursor: pointer;
    border-radius: 8px;
    align-items: flex-start;
    gap: 8px;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.is-active {
      font-weight: 600;
      background: var(--el-color-primary-light-9);
    }
  }

  .cat-item__icon {
    flex-shrink: 0;
    margin-top: 2px;
  }

  .cat-item__icon--ok {
    color: var(--el-color-success);
  }

  .cat-item__icon--bad {
    color: var(--el-color-danger);
  }

  .cat-item__text {
    flex: 1;
    min-width: 0;
  }

  .result-detail {
    min-width: 0;
  }

  .sub-block {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .sub-head {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    width: 100%;
    padding: 6px 0;
  }

  .sub-title {
    font-size: 15px;
    font-weight: 600;
  }

  .sub-stats {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .stat-ok {
    margin-right: 12px;
    color: var(--el-color-success);
  }

  .stat-bad {
    color: var(--el-color-danger);
  }

  .sub-collapse {
    border-top: none;
    border-bottom: none;

    :deep(.el-collapse-item__header) {
      height: auto;
      min-height: 52px;
      padding: 0 12px;
      line-height: 1.4;
    }

    :deep(.el-collapse-item__wrap) {
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    :deep(.el-collapse-item__content) {
      padding-bottom: 12px;
    }
  }

  .case-collapse {
    border-top: 1px solid var(--el-border-color-lighter);
    border-bottom: none;

    :deep(.el-collapse-item__header) {
      height: auto;
      min-height: 48px;
      padding: 8px 12px;
      line-height: 1.4;
    }

    :deep(.el-collapse-item__wrap) {
      border-bottom: 1px solid var(--el-border-color-lighter);
    }
  }

  .case-title-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
    width: 100%;
    padding-right: 8px;
  }

  .case-code {
    font-family: ui-monospace, monospace;
    font-weight: 600;
  }

  .case-api {
    min-width: 0;
    font-family: ui-monospace, monospace;
    color: var(--el-color-primary);
    flex: 1;
  }

  .case-tag {
    flex-shrink: 0;
  }

  .case-detail {
    padding: 8px 0 4px;
  }

  .kv-block {
    margin-bottom: 12px;
  }

  .kv-label {
    margin-bottom: 6px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-regular);
  }

  .kv-pre {
    padding: 10px 12px;
    margin: 0;
    overflow: auto;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-all;
    white-space: pre-wrap;
    background: var(--el-fill-color-light);
    border-radius: 8px;
  }

  .diff-row {
    margin-top: 8px;
  }

  .diff-title {
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 600;
  }

  .diff-pre {
    min-height: 120px;
    padding: 10px 12px;
    margin: 0;
    overflow: auto;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-all;
    white-space: pre-wrap;
    background: var(--el-fill-color-lighter);
    border-radius: 8px;
  }

  .mb-12px {
    margin-bottom: 12px;
  }
</style>
