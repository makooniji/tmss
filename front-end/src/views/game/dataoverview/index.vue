<template>
  <div class="title-tabs-row">
    <div class="page-title">
      <span class="material-icons"></span>
      {{ t('game.common.dataOverviewTitle') }}
    </div>
    <el-row :gutter="20" class="row-box">
      <el-col :span="8" v-for="(item, index) in tabsList" :key="item.key">
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
              <p>{{ item.desc }}</p>
            </div>
            <div class="right" v-else>
              <h3>{{ item.unit }}{{ tabsData?.[item.key] || 0 }}</h3>
              <p>{{ item.desc }}</p>
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
      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
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
      row-key="vendorCode"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
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
        :label="t('game.common.rtpColumn')"
        align="center"
        prop="rtp"
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
        :label="t('table.action')"
        align="center"
        min-width="120px"
        fixed="right"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('detail', scope.row)"
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
  <VendorTenantDayStaticForm ref="formRef" />
  <StatisticalChartForm ref="ChartRef" @success="getList" />
</template>

<script setup lang="ts">
  import download from '@/utils/download'
  import { useDateRangeShortcuts } from '@/hooks/web/useDateRangeShortcuts'
  import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
  import { GameDataOverviewApi, VendorDayStatic } from '@/api/game/dataoverview'
  import VendorTenantDayStaticForm from './VendorTenantDayStaticForm.vue'
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
  const list = ref<VendorDayStatic[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const daysArr = ref<[string, string] | undefined>()
  const dateRangeShortcuts = useDateRangeShortcuts()
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    vendorCode: undefined,
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

  const tabsList = computed(() => {
    const prefix = t('game.common.gamePrefix')
    return [
      {
        name: t('game.common.betTotalAmount'),
        key: 'betAmount',
        unit: '$',
        desc: prefix + t('game.common.betTotalAmount')
      },
      {
        name: t('game.common.payTotalAmount'),
        key: 'payAmount',
        unit: '$',
        desc: prefix + t('game.common.payTotalAmount')
      },
      {
        name: t('game.common.winLossTotal'),
        key: 'winLoss',
        unit: '$',
        desc: t('game.common.gameTotalWinLossDesc')
      },
      {
        name: t('game.common.totalBetCount'),
        key: 'betCount',
        unit: '',
        desc: prefix + t('game.common.totalBetCount')
      },
      {
        name: t('game.common.rtp'),
        key: 'rtp',
        unit: '',
        desc: t('game.common.gameRtpRateDesc')
      },
      {
        name: t('game.common.gameBetRate'),
        key: 'userBetCountUserCount',
        unit: '',
        desc: t('game.common.gameBetRateDesc')
      },
      {
        name: t('game.common.gameCountARPPU'),
        key: 'betCountUserBetCount',
        unit: '',
        desc: t('game.common.gameCountARPPUDesc')
      },
      {
        name: t('game.common.hotGameARPPU'),
        key: 'hotGame',
        unit: '$',
        desc: t('game.common.hotGameARPPUDesc')
      }
    ]
  })

  const tabsData = ref()

  const buildRequestParams = () => {
    const params: Record<string, unknown> = {
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize
    }
    if (queryParams.days) params.days = queryParams.days
    if (queryParams.endDays) params.endDays = queryParams.endDays
    if (queryParams.vendorCode) params.vendorCode = queryParams.vendorCode
    if (queryParams.zone) params.zone = queryParams.zone
    return params
  }

  /** 查询列表 */
  const getList = async () => {
    if (!checkSearchParams()) {
      return
    }
    loading.value = true
    try {
      const data =
        await GameDataOverviewApi.getVendorStaticPage(buildRequestParams())
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
        await GameDataOverviewApi.getVendorStaticTotal(buildRequestParams())
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
  const openForm = (type: string, row: VendorDayStatic) => {
    formRef.value.open(type, row, {
      days: queryParams.days,
      endDays: queryParams.endDays,
      zone: queryParams.zone
    })
  }

  const ChartRef = ref()
  const openChart = (type: string, key?: string, name?: string) => {
    const range =
      queryParams.days && queryParams.endDays
        ? { days: queryParams.days, endDays: queryParams.endDays }
        : {}
    ChartRef.value.open(type, key, name, {
      ...range,
      vendorCode: queryParams.vendorCode,
      zone: queryParams.zone
    })
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
        await GameDataOverviewApi.exportVendorStatic(buildRequestParams())
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
      align-items: center;
      gap: 8px;

      .left {
        flex: 0 0 auto;

        p {
          font-size: 16px;
          font-weight: 600;
          white-space: nowrap;
        }
      }

      .right {
        display: flex;
        min-width: 240px;
        flex: 1 1 auto;
        flex-direction: column;
        gap: 2px;
        align-items: end;

        p {
          display: block;
          text-align: right;
          white-space: nowrap;
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
