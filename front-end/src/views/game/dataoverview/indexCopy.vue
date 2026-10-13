<template>
  <div class="title-tabs-row">
    <div class="page-title">
      <span class="material-icons"></span>
      {{ t('game.common.dataOverviewTitle') }}
    </div>
    <el-row :gutter="20" class="row-box">
      <el-col :span="6" v-for="(item, index) in tabsList" :key="item.key">
        <div
          :class="['overview-item', 'overview-item' + index]"
          @click="openChart('detail', item.key, item.name)"
        >
          <div class="item-box">
            <div class="left">
              <p><span></span> {{ item.name }} </p>
            </div>
            <div class="right" v-if="item.key == 'hotGame'">
              <div class="hotGame-title">
                <el-tag :style="tagStyle">
                  {{ tabsData?.[item.key].gameName }}
                </el-tag>
                <h3
                  >{{ item.unit }}{{ tabsData?.[item.key].betAmount || 0 }}</h3
                >
              </div>
              <p>{{ t('game.common.gamePrefix') }}{{ item.name }}</p>
            </div>
            <div class="right" v-else>
              <h3>{{ item.unit }}{{ tabsData?.[item.key] || 0 }}</h3>
              <p>{{ t('game.common.gamePrefix') }}{{ item.name }}</p>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>

  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
    >
      <el-form-item :label="t('game.common.timeRange')" prop="daysArr">
        <el-date-picker
          v-model="daysArr"
          value-format="YYYY-MM-DD"
          type="daterange"
          :shortcuts="dateRangeShortcuts"
          :range-separator="t('game.common.rangeSeparator')"
          :start-placeholder="t('game.common.startDate')"
          :end-placeholder="t('game.common.endDate')"
          clearable
          class="!w-280px"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.merchantCode')" prop="tenantCode">
        <el-input
          v-model="queryParams.tenantCode"
          :placeholder="t('game.common.placeholderMerchantCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.merchantName')" prop="tenantName">
        <el-input
          v-model="queryParams.tenantName"
          :placeholder="t('game.common.placeholderMerchantName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.vendor.vendorName')" prop="vendorName">
        <el-input
          v-model="queryParams.vendorName"
          :placeholder="t('game.vendor.placeholderVendorName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('tools.tester.gameCode')" prop="gameCode">
        <el-input
          v-model="queryParams.gameCode"
          :placeholder="t('game.recordsPage.placeholderGameCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.zoneLabel')" prop="zone">
        <el-select
          v-model="queryParams.zone"
          :placeholder="t('game.common.selectZone')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_ZONE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="w-full">
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
          v-hasPermi="['game:tenant-day-static:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column
        :label="t('game.common.merchantId')"
        align="center"
        prop="tenantCode"
      />
      <el-table-column
        :label="t('game.common.merchantName')"
        align="center"
        prop="tenantName"
      />
      <el-table-column
        :label="t('game.info.vendorCode')"
        align="center"
        prop="vendorCode"
      />
      <el-table-column
        :label="t('game.vendor.vendorName')"
        align="center"
        prop="vendorName"
      />

      <el-table-column
        :label="t('tools.tester.gameCode')"
        align="center"
        prop="gameCode"
      />

      <el-table-column
        :label="t('game.info.gameName')"
        align="center"
        prop="gameName"
      />

      <el-table-column
        :label="t('game.common.gameTypeCol')"
        align="center"
        prop="cateId"
      >
        <template #default="scope">
          {{ getCateName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.common.totalBets')"
        align="center"
        prop="betCount"
      />
      <el-table-column
        :label="t('game.common.betTotalAmount')"
        align="center"
        prop="betAmount"
      />
      <el-table-column
        :label="t('game.common.payTotalAmount')"
        align="center"
        prop="payAmount"
      />
      <el-table-column :label="t('game.common.winLossTotal')" align="center">
        <template #default="scope">
          {{ scope.row.payAmount - scope.row.betAmount }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.common.rtpColumn')"
        align="center"
        prop="rtp"
      />
      <el-table-column
        :label="t('game.common.activePlayers')"
        align="center"
        prop="userCount"
      />
      <el-table-column :label="t('game.common.perCapitaBet')" align="center">
        <template #default="scope">
          {{
            scope.row.userCount > 0
              ? (scope.row.payAmount / scope.row.userCount).toFixed(2)
              : '0.00'
          }}
        </template>
      </el-table-column>
      <el-table-column :label="t('game.common.avgPayPerUser')" align="center">
        <template #default="scope">
          {{
            scope.row.userCount > 0
              ? (scope.row.payAmount / scope.row.userCount).toFixed(2)
              : '0.00'
          }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="120px"
        fixed="right"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="
              openForm(
                'detail',
                scope.row.tenantId,
                scope.row.vendorCode,
                scope.row.gameCode
              )
            "
            v-hasPermi="['game:tenant-day-static:query']"
          >
            {{ t('game.common.view') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <TenantDayStaticForm ref="formRef" @success="getList" />
  <StatisticalChartForm ref="ChartRef" @success="getList" />
</template>

<script setup lang="ts">
  import dayjs from 'dayjs'
  import download from '@/utils/download'
  import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
  import {
    GameDataOverviewApi,
    GameDataOverview
  } from '@/api/game/dataoverview'
  import TenantDayStaticForm from './TenantDayStaticForm.vue'
  import StatisticalChartForm from './StatisticalChartForm.vue'

  /** 租户游戏日报 列表 */
  defineOptions({ name: 'GameDataOverview' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const tagStyle = {
    background: 'linear-gradient(135deg, #ff6b35, #f7931e)',
    color: '#fff'
  }

  const loading = ref(true) // 列表的加载中
  const list = ref<GameDataOverview[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const daysArr = ref<[string, string] | undefined>()
  const getRangeByDays = (days: number): [Date, Date] => {
    const end = dayjs().endOf('day').toDate()
    const start = dayjs()
      .subtract(days - 1, 'day')
      .startOf('day')
      .toDate()
    return [start, end]
  }
  const dateRangeShortcuts = computed(() => [
    {
      text: t('form.today'),
      value: () => {
        const day = dayjs().startOf('day').toDate()
        return [day, day]
      }
    },
    {
      text: t('form.yesterday'),
      value: () => {
        const day = dayjs().subtract(1, 'day').startOf('day').toDate()
        return [day, day]
      }
    },
    {
      text: t('game.common.tab7d'),
      value: () => getRangeByDays(7)
    },
    {
      text: t('game.common.tab14d'),
      value: () => getRangeByDays(14)
    },
    {
      text: t('game.common.tab30d'),
      value: () => getRangeByDays(30)
    },
    {
      text: t('game.common.tab90d'),
      value: () => getRangeByDays(90)
    }
  ])
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    tenantCode: undefined,
    tenantName: undefined,
    vendorCode: undefined,
    vendorName: undefined,
    gameCode: undefined,
    zone: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  /** 是否已选择有效日期范围 */
  const hasValidDateRange = () =>
    Array.isArray(daysArr.value) &&
    daysArr.value.length >= 2 &&
    daysArr.value[0] &&
    daysArr.value[1]

  // 日期选择器同步为接口字段 days、endDays
  watchEffect(() => {
    if (hasValidDateRange()) {
      queryParams.days = daysArr.value![0]
      queryParams.endDays = daysArr.value![1]
      return
    }
    queryParams.days = undefined
    queryParams.endDays = undefined
  })

  /** 校验搜索参数（不弹提示） */
  const checkSearchParams = () => {
    if (queryParams.zone && !hasValidDateRange()) {
      return false
    }
    if (hasValidDateRange() && !queryParams.zone) {
      return false
    }
    return true
  }

  /** 时区与日期范围需同时选择 */
  const validateSearchParams = () => {
    if (queryParams.zone && !hasValidDateRange()) {
      message.warning(t('game.common.zoneRequiresDateRange'))
      return false
    }
    if (hasValidDateRange() && !queryParams.zone) {
      message.warning(t('game.common.dateRequiresZone'))
      return false
    }
    return true
  }

  const tabsList = computed(() => [
    { name: t('game.common.betTotalAmount'), key: 'betAmount', unit: '$' },
    { name: t('game.common.payTotalAmount'), key: 'payAmount', unit: '$' },
    { name: t('game.common.winLossTotal'), key: 'winLoss', unit: '$' },
    { name: t('game.common.totalBetCount'), key: 'betCount', unit: '' },
    { name: t('game.common.rtp'), key: 'rtp', unit: '' },
    {
      name: t('game.common.gameBetRate'),
      key: 'userBetCountUserCount',
      unit: ''
    },
    {
      name: t('game.common.gameCountARPPU'),
      key: 'betCountUserBetCount',
      unit: ''
    },
    { name: t('game.common.hotGameARPPU'), key: 'hotGame', unit: '$' }
  ])

  const tabsData = ref()

  const cateNameMap = computed<Record<number, string>>(() => ({
    1: t('game.common.gameCateElectronic'),
    2: t('game.common.gameCateLive'),
    3: t('game.common.gameCateChess'),
    4: t('game.common.gameCateFishing'),
    5: t('game.common.gameCateSports'),
    6: t('game.common.gameCateLottery')
  }))

  type GameOverviewRow = GameDataOverview & {
    cateId?: number | string
    cate_id?: number | string
  }

  const buildRequestParams = () => {
    const params: Record<string, unknown> = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    }
    if (queryParams.days) params.days = queryParams.days
    if (queryParams.endDays) params.endDays = queryParams.endDays
    if (queryParams.tenantCode) params.tenantCode = queryParams.tenantCode
    if (queryParams.tenantName) params.tenantName = queryParams.tenantName
    if (queryParams.vendorCode) params.vendorCode = queryParams.vendorCode
    if (queryParams.vendorName) params.vendorName = queryParams.vendorName
    const gameCode = queryParams.gameCode?.trim()
    if (gameCode) params.gameCode = gameCode
    if (queryParams.zone) params.zone = queryParams.zone
    return params
  }

  /** 根据 cate_id 获取游戏类型名称 */
  const getCateName = (row: GameOverviewRow) => {
    const cateId = Number(row.cateId ?? row.cate_id)
    if (Number.isNaN(cateId)) {
      return '-'
    }
    return cateNameMap.value[cateId] ?? String(cateId)
  }

  /** 查询列表 */
  const getList = async () => {
    if (!checkSearchParams()) {
      return
    }
    loading.value = true
    try {
      const data =
        await GameDataOverviewApi.getGameDataOverviewPage(buildRequestParams())
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const gettotal = async () => {
    if (!checkSearchParams()) {
      return
    }
    try {
      const data =
        await GameDataOverviewApi.gettenantstatictotal(buildRequestParams())
      data.userBetCountUserCount = (data.userBetCount / data.userCount).toFixed(
        2
      )
      data.betCountUserBetCount = (data.betCount / data.userBetCount).toFixed(2)
      tabsData.value = data
    } finally {
    }
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    if (!validateSearchParams()) {
      return
    }
    queryParams.pageNo = 1
    getList()
    gettotal()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    daysArr.value = undefined
    queryFormRef.value.resetFields()
    handleQuery()
  }

  /** 查看操作 */
  const formRef = ref()
  const openForm = (
    type: string,
    tenantId?: number | string,
    vendorCode?: string,
    gameCode?: string
  ) => {
    formRef.value.open(type, tenantId, vendorCode, gameCode)
  }

  const ChartRef = ref()
  const openChart = (type: string, key?: string, name?: string) => {
    const range =
      queryParams.days && queryParams.endDays
        ? { days: queryParams.days, endDays: queryParams.endDays }
        : {}
    ChartRef.value.open(type, key, name, {
      ...range,
      tenantCode: queryParams.tenantCode,
      tenantName: queryParams.tenantName,
      vendorCode: queryParams.vendorCode,
      vendorName: queryParams.vendorName,
      gameCode: queryParams.gameCode,
      zone: queryParams.zone
    })
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: GameDataOverview[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    if (!validateSearchParams()) {
      return
    }
    try {
      await message.exportConfirm()
      exportLoading.value = true
      const data =
        await GameDataOverviewApi.exportGameDataOverview(buildRequestParams())
      download.excel(data, t('game.common.exportDataOverview'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 初始化 **/
  onMounted(() => {
    gettotal()
    getList()
  })
</script>
<style lang="scss" scoped>
  .page-title {
    font-size: 18px;
    font-weight: bold;
  }

  .overview-item {
    padding: 20px;
    margin: 10px 0;
    cursor: pointer;
    background: linear-gradient(135deg, #f8f9fa, #fff);
    border: 1px solid #e9ecef;
    border-left: 5px solid #3498db;
    border-radius: 12px;
    box-shadow: 0 1px 3px #0000001a;
    transition: all 0.3s ease;

    .item-box {
      display: flex;
      justify-content: space-between;

      .left p {
        font-size: 16px;
        font-weight: 600;
      }

      .right {
        display: flex;
        flex-direction: column;
        gap: 2px;
        align-items: end;

        p {
          text-align: right;
        }
      }
    }
  }

  .overview-item1 {
    border-left: 5px solid #2ecc71;
  }

  .overview-item2 {
    border-left: 5px solid #9b59b6;
  }

  .overview-item3 {
    border-left: 5px solid #f1c40f;
  }

  .overview-item4 {
    border-left: 5px solid #0fbdf1;
  }

  .overview-item5 {
    border-left: 5px solid #00fc1f;
  }

  .overview-item6 {
    border-left: 5px solid #fc00d1;
  }

  .overview-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px #0000001a;
  }

  .overview-item p {
    display: flex;
    font-size: 14px;
    gap: 5px;
    align-items: center;
  }

  .overview-item p span {
    font-size: 20px;
    font-weight: bold;
  }

  .row-box {
    margin: 10px 0;
  }

  .hotGame-title {
    display: flex;
  }
</style>
