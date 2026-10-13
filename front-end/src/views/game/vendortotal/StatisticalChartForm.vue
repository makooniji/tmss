<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="70%">
    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
      >
        <el-form-item :label="t('game.common.timeRange')" prop="dateRange">
          <el-date-picker
            v-model="daysArr"
            value-format="YYYY-MM-DD"
            type="datetimerange"
            :range-separator="t('game.common.rangeSeparator')"
            :start-placeholder="t('game.common.startTime')"
            :end-placeholder="t('game.common.endTime')"
            class="!w-360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            {{ t('game.common.search') }}
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <div>
      <el-card class="mb-8px" shadow="hover">
        <el-skeleton :loading="loading" animated>
          <el-empty v-if="lineEmpty" description="暂无数据" :image-size="80" />
          <Echart v-else :height="350" :options="lineOptionsData" />
        </el-skeleton>
      </el-card>

      <el-card shadow="hover" class="mb-8px">
        <el-skeleton :loading="loading" animated>
          <el-empty v-if="barEmpty" description="暂无数据" :image-size="80" />
          <Echart v-else :options="barOptionsData" :height="280" />
        </el-skeleton>
      </el-card>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { set } from 'lodash-es'
  import { EChartsOption } from 'echarts'
  import { GameDataOverviewApi } from '@/api/game/dataoverview'
  import { lineOptions, barOptions } from './echarts-data'

  defineOptions({ name: 'StatisticalChartForm' })

  const { t } = useI18n()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const loading = ref(false)

  /** 与列表页一致：同步 days / endDays 给 chart 接口 */
  const daysArr = ref<[string, string] | null>(null)
  const queryParams = reactive({
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    orderBy: '' as string,
    status: 0
  })

  watchEffect(() => {
    if (
      Array.isArray(daysArr.value) &&
      daysArr.value.length >= 2 &&
      daysArr.value[0] &&
      daysArr.value[1]
    ) {
      queryParams.days = daysArr.value[0]
      queryParams.endDays = daysArr.value[1]
      return
    }
    queryParams.days = undefined
    queryParams.endDays = undefined
  })

  const metricKey = ref('betAmount')
  const title = ref<string | undefined>()
  const lineEmpty = ref(false)
  const barEmpty = ref(false)

  const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption
  const lineOptionsData = reactive<EChartsOption>(lineOptions) as EChartsOption

  /** 兼容接口直接返回数组或包装在 data/list 中 */
  function unwrapRows(res: unknown): Record<string, unknown>[] {
    if (Array.isArray(res)) return res as Record<string, unknown>[]
    if (res && typeof res === 'object') {
      const o = res as Record<string, unknown>
      if (Array.isArray(o.data)) return o.data as Record<string, unknown>[]
      if (Array.isArray(o.list)) return o.list as Record<string, unknown>[]
      if (Array.isArray(o.records))
        return o.records as Record<string, unknown>[]
    }
    return []
  }

  const isTenantStatusMetric = (key: string) =>
    key === 'openCounts' || key === 'operCounts' || key === 'closeCounts'

  /** 柱状图标题 */
  function getBarChartTitle(key: string): string {
    if (key === 'openCounts') {
      return '商户已开通天数'
    }
    if (key === 'operCounts') {
      return '已运营商户投注总额'
    }
    if (key === 'closeCounts') {
      return '商户投注总额'
    }
    return t('game.chart.barTop10', {
      title: `${t('game.common.gamePrefix')}${title.value ?? ''}`
    })
  }

  /** 柱状图取值字段 */
  function getBarMetricValue(
    row: Record<string, unknown>,
    key: string
  ): number {
    if (key === 'openCounts') {
      return Number(row.openDays ?? 0)
    }
    if (key === 'operCounts' || key === 'closeCounts') {
      return Number(row.betAmount ?? 0)
    }
    return pickMetric(row, key)
  }

  function pickMetric(row: Record<string, unknown>, key: string): number {
    const raw = row[key]
    if (raw != null && raw !== '') return Number(raw)
    for (const k of [
      'betAmount',
      'payAmount',
      'winLoss',
      'userBetCount',
      'userWinCount',
      'userCount',
      'openCounts',
      'operCounts',
      'closeCounts'
    ]) {
      const v = row[k]
      if (v != null && v !== '') return Number(v)
    }
    return 0
  }

  function xDayLabel(row: Record<string, unknown>): string {
    const v = row.days ?? row.day ?? row.date ?? row.statDate ?? row.time ?? ''
    return String(v)
  }

  const getWeeklyUserActivity = async () => {
    const res = await GameDataOverviewApi.getchartDayInfo(queryParams)
    const data = unwrapRows(res)
    const key = metricKey.value
    barEmpty.value = data.length === 0
    if (barEmpty.value) return

    set(barOptionsData, 'title', {
      text: getBarChartTitle(key),
      subtext: '',
      left: 'center',
      top: 10,
      subtextStyle: { fontSize: 14, color: '#777' }
    })
    set(
      barOptionsData,
      'xAxis.data',
      data.map((v) => String(v.tenantName ?? v.name ?? ''))
    )
    set(barOptionsData, 'series', [
      {
        name: isTenantStatusMetric(key)
          ? getBarChartTitle(key)
          : String(title.value ?? key),
        data: data.map((v) => getBarMetricValue(v, key)),
        type: 'bar'
      }
    ])
  }

  /** /game/tenant-day-static/chartDayTotal — 折线图按时间 */
  const getMonthlySales = async () => {
    const res = await GameDataOverviewApi.getchartDayTotal(queryParams)
    const data = unwrapRows(res)
    const key = metricKey.value
    const tenantStatus = isTenantStatusMetric(key)
    lineEmpty.value = data.length === 0
    if (lineEmpty.value) return

    set(lineOptionsData, 'title', {
      text: t('game.chart.lineTitle', { title: title.value ?? '' }),
      subtext: '',
      left: 'center',
      top: 10,
      textStyle: { color: '#f39c12' },
      subtextStyle: { fontSize: 14, color: '#777' }
    })
    set(
      lineOptionsData,
      'xAxis.data',
      data.map((v) => xDayLabel(v))
    )
    set(lineOptionsData, 'series', [
      {
        name: String(title.value ?? key),
        type: 'line',
        itemStyle: { color: '#FFA500' },
        symbol: 'circle',
        symbolSize: 5,
        // 已开通/已运营/已停用：折线图取 counts，X 轴仍用 days
        data: data.map((v) =>
          tenantStatus ? Number(v.counts ?? 0) : pickMetric(v, key)
        ),
        animationDuration: 2800,
        animationEasing: 'quadraticOut',
        areaStyle: { color: 'rgba(255, 165, 0, 0.3)' }
      }
    ])
  }

  const loadCharts = async () => {
    loading.value = true
    try {
      await Promise.all([getMonthlySales(), getWeeklyUserActivity()])
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => {
    loadCharts()
  }

  const open = async (
    type: string,
    key?: string,
    name?: string,
    status?: number,
    extra?: { days?: string; endDays?: string }
  ) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    title.value = name
    metricKey.value = key || 'betAmount'
    // 已开通/已运营/已停用商户统一按 openCounts 排序
    queryParams.orderBy =
      key === 'openCounts' || key === 'operCounts' || key === 'closeCounts'
        ? 'openCounts'
        : key || ''
    queryParams.status = status ?? 0

    if (extra?.days && extra?.endDays) {
      daysArr.value = [extra.days, extra.endDays]
      queryParams.days = extra.days
      queryParams.endDays = extra.endDays
    } else {
      daysArr.value = null
      queryParams.days = undefined
      queryParams.endDays = undefined
    }

    await nextTick()
    if (key) {
      await loadCharts()
    }
  }

  defineExpose({ open })
</script>

<style lang="scss" scoped>
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .section-title-bet {
    font-size: 18px;
    font-weight: 600;
    color: #154ec1;
  }

  .items-center {
    display: flex;
    background: #ecf0f1;

    p {
      padding: 5px 15px;
      font-size: 16px;
      cursor: pointer;
    }

    .active {
      background: #dce3e5;
    }
  }
</style>
