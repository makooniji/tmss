<template>
  <ContentWrap>
    <el-form
      class="records-search-form -mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item :label="t('game.common.playerAccount')" prop="username">
        <el-input
          v-model="queryParams.username"
          :placeholder="t('game.common.placeholderUsername')"
          clearable
          @keyup.enter="handleQuery"
          class="records-search-control"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.betOrderNo')" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          :placeholder="t('game.recordsPage.placeholderBetOrderNo')"
          clearable
          @keyup.enter="handleQuery"
          class="records-search-control"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.gameCode')" prop="gameCode">
        <el-input
          v-model="queryParams.gameCode"
          :placeholder="t('game.recordsPage.placeholderGameCode')"
          clearable
          @keyup.enter="handleQuery"
          class="records-search-control"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.gameKind')" prop="gameKind">
        <el-select
          v-model="queryParams.gameKind"
          :placeholder="t('game.categorySub.selectGameCategory')"
          clearable
          class="records-search-control"
        >
          <el-option
            v-for="item in gameTypeList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="daysArr">
        <el-date-picker
          v-model="daysArr"
          value-format="YYYY-MM-DD"
          type="daterange"
          :shortcuts="dateRangeShortcuts"
          :disabled-date="disabledDate"
          :range-separator="t('game.common.rangeSeparator')"
          :start-placeholder="t('game.common.startDate')"
          :end-placeholder="t('game.common.endDate')"
          clearable
          class="records-search-control"
          @calendar-change="handleCalendarChange"
          @change="handleDateRangeChange"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.zoneLabel')" prop="zone">
        <el-select
          v-model="queryParams.zone"
          :placeholder="t('game.common.selectZone')"
          clearable
          class="records-search-control"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_ZONE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="records-search-form__actions w-full">
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('game.common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['game:records:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column
        :label="t('game.common.playerAccount')"
        align="center"
        prop="username"
        min-width="120"
      />
      <el-table-column
        :label="t('game.recordsPage.betOrderNo')"
        align="center"
        prop="orderNo"
        min-width="140"
      />
      <el-table-column
        :label="t('game.info.vendorCode')"
        align="center"
        prop="vendorCode"
        min-width="120"
      />
      <el-table-column
        :label="t('game.recordsPage.gameCode')"
        align="center"
        prop="gameCode"
        min-width="120"
      />
      <el-table-column
        :label="t('game.recordsPage.gameKind')"
        align="center"
        prop="gameKind"
        min-width="120"
      >
        <template #default="scope">
          {{ getGameKindName(scope.row.gameKind) }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.common.betAmount')"
        align="center"
        prop="betAmount"
        min-width="110"
      />
      <el-table-column
        :label="t('game.common.odds')"
        align="center"
        prop="odds"
        min-width="90"
      />
      <el-table-column
        :label="t('game.common.payAmount')"
        align="center"
        prop="winAmount"
        min-width="110"
      />
      <el-table-column
        :label="t('game.recordsPage.winLossCol')"
        align="center"
        prop="winLoss"
        min-width="110"
      />
      <el-table-column
        :label="t('common.status')"
        align="center"
        prop="action"
        width="100"
      >
        <template #default="scope">
          <dict-tag
            :type="DICT_TYPE.SYSTEM_GAME_RECORD_ACTION"
            :value="scope.row.action"
          />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.recordsPage.timeCol')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
  import { useDateRangeShortcuts } from '@/hooks/web/useDateRangeShortcuts'
  import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import dayjs from 'dayjs'
  import { CategoryApi } from '@/api/game/category'
  import { RecordsApi, Records } from '@/api/game/records'

  /** 玩家数据列表详情弹窗 - 投注明细（独立组件，不影响玩家游戏明细页） */
  defineOptions({ name: 'RecordsFormDetail' })

  const props = withDefaults(
    defineProps<{
      vendorCode?: string
      tenantCode?: string
      /** 列表页带入的日期/时区 */
      listDays?: string
      listEndDays?: string
      listZone?: string
    }>(),
    {
      vendorCode: '',
      tenantCode: '',
      listDays: undefined,
      listEndDays: undefined,
      listZone: undefined
    }
  )

  const emit = defineEmits<{
    filterQuery: [params: Record<string, unknown>]
  }>()

  const message = useMessage()
  const { t } = useI18n()

  const loading = ref(false)
  const list = ref<Records[]>([])
  const total = ref(0)
  const daysArr = ref<[string, string] | undefined>()
  const pickingStartDate = ref<Date | null>(null)
  const MAX_DATE_RANGE_DAYS = 90
  const gameTypeList = ref<{ id: number; title: string }[]>([])
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    username: undefined as string | undefined,
    tenantCode: undefined as string | undefined,
    orderNo: undefined as string | undefined,
    vendorCode: undefined as string | undefined,
    gameCode: undefined,
    gameKind: undefined as number | undefined,
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    zone: undefined as string | undefined
  })
  const queryFormRef = ref()
  const exportLoading = ref(false)

  const hasValidDateRange = () =>
    Array.isArray(daysArr.value) &&
    daysArr.value.length >= 2 &&
    daysArr.value[0] &&
    daysArr.value[1]

  const dateRangeShortcuts = useDateRangeShortcuts({ tomorrow: true })

  const getDateRangeSpanDays = (start: string, end: string) =>
    dayjs(end).startOf('day').diff(dayjs(start).startOf('day'), 'day') + 1

  const isDateRangeWithinLimit = () => {
    if (!hasValidDateRange()) {
      return true
    }
    return (
      getDateRangeSpanDays(daysArr.value![0], daysArr.value![1]) <=
      MAX_DATE_RANGE_DAYS
    )
  }

  const handleCalendarChange = (val: [Date, Date | null] | null) => {
    if (val?.[0] && !val[1]) {
      pickingStartDate.value = val[0]
      return
    }
    pickingStartDate.value = null
  }

  const disabledDate = (date: Date) => {
    if (!pickingStartDate.value) {
      return false
    }
    const start = dayjs(pickingStartDate.value).startOf('day')
    const current = dayjs(date).startOf('day')
    const min = start.subtract(MAX_DATE_RANGE_DAYS - 1, 'day')
    const max = start.add(MAX_DATE_RANGE_DAYS - 1, 'day')
    return current.isBefore(min) || current.isAfter(max)
  }

  const handleDateRangeChange = (val: [string, string] | null) => {
    if (!val?.[0] || !val[1]) {
      pickingStartDate.value = null
      return
    }
    if (getDateRangeSpanDays(val[0], val[1]) > MAX_DATE_RANGE_DAYS) {
      message.warning(
        t('game.common.dateRangeExceedMaxDays', { max: MAX_DATE_RANGE_DAYS })
      )
      daysArr.value = undefined
    }
    pickingStartDate.value = null
  }

  const validateSearchParams = () => {
    if (queryParams.zone && !hasValidDateRange()) {
      message.warning(t('game.common.zoneRequiresDateRange'))
      return false
    }
    if (hasValidDateRange() && !queryParams.zone) {
      message.warning(t('game.common.dateRequiresZone'))
      return false
    }
    if (hasValidDateRange() && !isDateRangeWithinLimit()) {
      message.warning(
        t('game.common.dateRangeExceedMaxDays', { max: MAX_DATE_RANGE_DAYS })
      )
      return false
    }
    return true
  }

  watchEffect(() => {
    if (hasValidDateRange()) {
      queryParams.days = daysArr.value![0]
      queryParams.endDays = daysArr.value![1]
      return
    }
    queryParams.days = undefined
    queryParams.endDays = undefined
  })

  const getGameKindName = (gameKind?: number | string) => {
    if (gameKind === undefined || gameKind === null || gameKind === '') {
      return '-'
    }
    const category = gameTypeList.value.find(
      (item) => String(item.id) === String(gameKind)
    )
    return category?.title ?? String(gameKind)
  }

  const pickPresentParams = (params: Record<string, unknown>) => {
    const result: Record<string, unknown> = {}
    for (const [key, value] of Object.entries(params)) {
      if (value === undefined || value === null || value === '') {
        continue
      }
      result[key] = value
    }
    return result
  }

  const buildFilterParams = () =>
    pickPresentParams({
      username: queryParams.username,
      tenantCode: queryParams.tenantCode,
      orderNo: queryParams.orderNo,
      vendorCode: queryParams.vendorCode,
      gameCode: queryParams.gameCode,
      gameKind: queryParams.gameKind,
      days: queryParams.days,
      endDays: queryParams.endDays,
      zone: queryParams.zone
    })

  const buildQueryParams = () => ({
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize,
    ...buildFilterParams()
  })

  const getList = async () => {
    if (!validateSearchParams()) {
      return
    }
    loading.value = true
    try {
      const filterParams = buildFilterParams()
      emit('filterQuery', filterParams)
      const data = await RecordsApi.getRecordsPage(buildQueryParams())
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => {
    if (!validateSearchParams()) {
      return
    }
    queryParams.pageNo = 1
    getList()
  }

  const resetQuery = () => {
    daysArr.value = undefined
    queryFormRef.value?.resetFields()
    queryParams.vendorCode = props.vendorCode || undefined
    queryParams.tenantCode = props.tenantCode || undefined
    list.value = []
    total.value = 0
    queryParams.pageNo = 1
  }

  const handleExport = async () => {
    if (!validateSearchParams()) {
      return
    }
    try {
      await message.exportConfirm()
      exportLoading.value = true
      const data = await RecordsApi.exportRecordsPage(buildQueryParams())
      download.excel(data, t('game.recordsPage.exportFileUserPage'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  watch(
    () =>
      [
        props.vendorCode,
        props.tenantCode,
        props.listDays,
        props.listEndDays,
        props.listZone
      ] as const,
    ([vendorCode, tenantCode, listDays, listEndDays, listZone]) => {
      queryParams.vendorCode = vendorCode || undefined
      queryParams.tenantCode = tenantCode || undefined
      queryParams.pageNo = 1
      if (listDays && listEndDays) {
        daysArr.value = [listDays, listEndDays]
        queryParams.zone = listZone || undefined
      } else {
        daysArr.value = undefined
        queryParams.zone = undefined
      }
      if (vendorCode) {
        nextTick(() => getList())
      } else {
        loading.value = false
        list.value = []
        total.value = 0
      }
    },
    { immediate: true }
  )

  const getGameTypeList = async () => {
    const data = await CategoryApi.getCategoryPage({ pageNo: 1, pageSize: 200 })
    gameTypeList.value = data.list || []
  }

  onMounted(() => {
    getGameTypeList()
  })

  defineExpose({ buildFilterParams })
</script>

<style scoped lang="scss">
  .records-search-form {
    :deep(.el-form-item:not(.records-search-form__actions)) {
      width: 352px;
      margin-right: 16px;
      vertical-align: top;
    }

    :deep(.records-search-control) {
      width: 240px;
    }
  }
</style>
