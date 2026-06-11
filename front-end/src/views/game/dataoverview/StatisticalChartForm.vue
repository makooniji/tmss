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
          <Echart :height="350" :options="lineOptionsData" />
        </el-skeleton>
      </el-card>

      <el-card shadow="hover" class="mb-8px">
        <el-skeleton :loading="loading" animated>
          <Echart :options="barOptionsData" :height="280" />
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

  defineOptions({ name: 'GameDataOverviewStatisticalChartForm' })

  const { t } = useI18n()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const loading = ref(false)

  const daysArr = ref<[string, string] | null>(null)
  const queryParams = reactive({
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    orderBy: '' as string,
    tenantCode: undefined as string | undefined,
    tenantName: undefined as string | undefined,
    vendorCode: undefined as string | undefined,
    vendorName: undefined as string | undefined
  })

  watchEffect(() => {
    if (Array.isArray(daysArr.value) && daysArr.value.length >= 2) {
      const [a, b] = daysArr.value
      if (a && b) {
        queryParams.days = a
        queryParams.endDays = b
      }
    }
  })

  const metricKey = ref('betAmount')
  const title = ref<string | undefined>()

  const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption
  const lineOptionsData = reactive<EChartsOption>(lineOptions) as EChartsOption

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

  function pickMetric(row: Record<string, unknown>, key: string): number {
    const raw = row[key]
    if (raw != null && raw !== '') return Number(raw)
    for (const k of [
      'betAmount',
      'payAmount',
      'winLoss',
      'betCount',
      'rtp',
      'userBetCount',
      'userCount',
      'userBetCountUserCount',
      'betCountUserBetCount'
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

  /** 游戏维度 TOP：/game/tenant-static/chartGameTotal */
  const getWeeklyUserActivity = async () => {
    const res = await GameDataOverviewApi.getchartGameInfo(queryParams)
    const data = unwrapRows(res)
    const key = metricKey.value

    set(barOptionsData, 'title', {
      text: t('game.chart.barTop10', {
        title: `${t('game.common.gamePrefix')}${title.value ?? ''}`
      }),
      subtext: '',
      left: 'center',
      top: 10,
      subtextStyle: { fontSize: 14, color: '#777' }
    })
    set(
      barOptionsData,
      'xAxis.data',
      data.map((v) => String(v.gameName ?? v.name ?? ''))
    )
    set(barOptionsData, 'series', [
      {
        name: String(title.value ?? key),
        data: data.map((v) => pickMetric(v, key)),
        type: 'bar'
      }
    ])
  }

  /** 折线：/game/tenant-static/chartGameDayTotal */
  const getMonthlySales = async () => {
    const res = await GameDataOverviewApi.getchartGameDayTotal(queryParams)
    const data = unwrapRows(res)
    const key = metricKey.value

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
        data: data.map((v) => pickMetric(v, key)),
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

  export type ChartOpenExtra = {
    days?: string
    endDays?: string
    tenantCode?: string
    tenantName?: string
    vendorCode?: string
    vendorName?: string
  }

  const open = async (
    type: string,
    key?: string,
    name?: string,
    extra?: ChartOpenExtra
  ) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    title.value = name
    metricKey.value = key || 'betAmount'
    queryParams.orderBy = key || ''

    if (extra) {
      if (extra.days && extra.endDays) {
        daysArr.value = [extra.days, extra.endDays]
        queryParams.days = extra.days
        queryParams.endDays = extra.endDays
      }
      queryParams.tenantCode = extra.tenantCode
      queryParams.tenantName = extra.tenantName
      queryParams.vendorCode = extra.vendorCode
      queryParams.vendorName = extra.vendorName
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
