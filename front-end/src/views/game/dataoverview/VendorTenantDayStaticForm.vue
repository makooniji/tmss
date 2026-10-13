<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="78%">
    <div class="section">
      <div class="section-title">{{ t('game.records.dataDetailSection') }}</div>
      <el-row :gutter="14" class="detail-overview-row">
        <el-col :span="4" v-for="(item, index) in DataDetails" :key="index">
          <div class="overview-item">
            <p>
              {{ item.name }}
              <span>
                <b v-if="item.location == 'left'">{{ item.unit }}</b>
                {{ totalData?.[item.value] ?? 0 }}
                <b v-if="item.location == 'right'">{{ item.unit }}</b>
              </span>
            </p>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="betting-details-section">
      <div class="section-header">
        <h3 class="section-title-bet">{{
          t('game.common.merchantDetailSection')
        }}</h3>
      </div>
    </div>

    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
      >
        <el-form-item label="" prop="tenantName">
          <el-input
            v-model="queryParams.tenantName"
            :placeholder="t('game.common.placeholderMerchantName')"
            clearable
            class="!w-240px"
            @keyup.enter="handleQuery"
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            {{ t('game.common.search') }}
          </el-button>
          <el-button
            type="success"
            plain
            @click="handleExport"
            :loading="exportLoading"
            v-hasPermi="['game:tenant-day-static:export']"
          >
            <Icon icon="ep:download" class="mr-5px" />
            {{ t('action.export') }}
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="detail-table-wrap">
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        row-key="rowKey"
      >
        <el-table-column
          :label="t('game.common.merchantCode')"
          align="center"
          prop="tenantCode"
        />
        <el-table-column
          :label="t('game.common.merchantName')"
          align="center"
          prop="tenantName"
        />
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
        <el-table-column
          :label="t('game.common.winLossTotal')"
          align="center"
          prop="winLoss"
        />
        <el-table-column
          :label="t('game.common.betUsers')"
          align="center"
          prop="userBetCount"
        />
        <el-table-column
          :label="t('game.common.profitUsers')"
          align="center"
          prop="userWinCount"
        />
        <el-table-column
          :label="t('game.common.activePlayers')"
          align="center"
          prop="userCount"
        />
        <el-table-column
          :label="t('game.common.rtpColumn')"
          align="center"
          prop="rtp"
        />
      </el-table>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
  import download from '@/utils/download'
  import {
    GameDataOverviewApi,
    VendorDayStatic,
    VendorTenantDayStatic
  } from '@/api/game/dataoverview'

  defineOptions({ name: 'VendorTenantDayStaticForm' })

  const message = useMessage()
  const { t } = useI18n()

  type SearchContext = {
    days?: string
    endDays?: string
    zone?: string
  }

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const loading = ref(false)
  const exportLoading = ref(false)
  const list = ref<(VendorTenantDayStatic & { rowKey: string })[]>([])
  const total = ref(0)
  const searchContext = ref<SearchContext>({})
  const selectedVendorCode = ref<string>()
  const selectedVendorName = ref<string>()
  const totalData = ref<Record<string, unknown>>()
  const queryFormRef = ref()

  const DataDetails = computed(() => [
    { name: `${t('game.common.gameVendor')}:`, value: 'vendorName' },
    {
      name: `${t('game.common.totalBets')}:`,
      value: 'betCount',
      unit: t('game.records.timesUnit'),
      location: 'right' as const
    },
    {
      name: `${t('game.common.betTotalAmount')}:`,
      value: 'betAmount',
      unit: '$',
      location: 'left' as const
    },
    {
      name: `${t('game.common.payTotalAmount')}:`,
      value: 'payAmount',
      unit: '$',
      location: 'left' as const
    },
    {
      name: `${t('game.common.winLossTotal')}:`,
      value: 'winLoss',
      unit: '$',
      location: 'left' as const
    },
    {
      name: `${t('game.common.betUsers')}:`,
      value: 'userBetCount',
      location: 'right' as const
    },
    {
      name: `${t('game.common.profitUsers')}:`,
      value: 'userWinCount',
      location: 'right' as const
    },
    {
      name: `${t('game.common.activePlayers')}:`,
      value: 'userCount',
      location: 'right' as const
    },
    {
      name: `${t('game.common.rtp')}:`,
      value: 'rtp',
      unit: '%',
      location: 'right' as const
    },
    {
      name: `${t('game.common.perCapitaBet')}:`,
      value: 'avgBetAmount',
      unit: '$',
      location: 'left' as const
    }
  ])

  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    tenantName: undefined as string | undefined
  })

  const buildFilterParams = () => {
    const params: Record<string, unknown> = {
      vendorCode: selectedVendorCode.value
    }
    if (searchContext.value.days) params.days = searchContext.value.days
    if (searchContext.value.endDays)
      params.endDays = searchContext.value.endDays
    if (searchContext.value.zone) params.zone = searchContext.value.zone
    const tenantName = queryParams.tenantName?.trim()
    if (tenantName) params.tenantName = tenantName
    return params
  }

  const buildRequestParams = () => ({
    ...buildFilterParams(),
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize
  })

  const getTotal = async () => {
    try {
      const data =
        await GameDataOverviewApi.getVendorTenantStaticTotal(
          buildFilterParams()
        )
      const userBetCount = Number(data.userBetCount) || 0
      const betAmount = Number(data.betAmount) || 0
      totalData.value = {
        ...data,
        vendorName: selectedVendorName.value,
        avgBetAmount:
          data.avgBetAmount ??
          (userBetCount > 0 ? (betAmount / userBetCount).toFixed(2) : '0.00')
      }
    } catch {
      totalData.value = { vendorName: selectedVendorName.value }
    }
  }

  const getList = async () => {
    loading.value = true
    try {
      const data =
        await GameDataOverviewApi.getVendorTenantStaticPage(
          buildRequestParams()
        )
      list.value = (data.list || []).map((item: VendorTenantDayStatic) => ({
        ...item,
        rowKey: `${item.vendorCode}-${item.tenantCode}`
      }))
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => {
    queryParams.pageNo = 1
    getTotal()
    getList()
  }

  const handleExport = async () => {
    try {
      await message.exportConfirm()
      exportLoading.value = true
      const data =
        await GameDataOverviewApi.exportVendorTenantStatic(buildFilterParams())
      download.excel(data, t('game.common.exportVendorTenant'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  const open = (
    type: string,
    row: VendorDayStatic,
    context: SearchContext = {}
  ) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    selectedVendorCode.value = row.vendorCode
    selectedVendorName.value = row.vendorName
    searchContext.value = context
    queryParams.pageNo = 1
    queryParams.tenantName = undefined
    getTotal()
    getList()
  }

  defineExpose({ open })
</script>

<style lang="scss" scoped>
  .detail-table-wrap :deep(.el-table .el-table__cell) {
    font-size: 16px;
    user-select: text;
  }

  .section {
    margin-bottom: 20px;
  }

  .detail-overview-row {
    margin-right: -4px !important;
    margin-left: -4px !important;
  }

  .overview-item {
    min-height: 52px;
    padding: 14px 18px;
    margin: 6px 0;
    background: linear-gradient(135deg, #f8f9fa, #fff);
    border: 1px solid #e9ecef;
    border-radius: 10px;
    box-shadow: 0 3px 5px #0000000d;
    transition: all 0.3s ease;
  }

  .overview-item:hover {
    border-color: #3498db;
    transform: translateY(-2px);
    box-shadow: 0 5px 10px #00000014;
  }

  .overview-item p {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    margin: 0;
    font-size: 14px;
    line-height: 1.5;
  }

  .overview-item p span {
    font-size: 18px;
    font-weight: bold;
    white-space: nowrap;
  }

  .section-title {
    margin-bottom: 5px;
    font-size: 18px;
    font-weight: 600;
    color: #154ec1;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .section-title-bet {
    font-size: 18px;
    font-weight: 600;
    color: #154ec1;
  }
</style>
