<template>
  <div class="title-tabs-row">
    <div class="page-title">
      <p>
        <span class="material-icons"></span>
        {{ t('game.common.dataOverviewTitle') }}
      </p>
      <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
        <el-tab-pane :label="t('game.common.tenantBehavior')" name="first" />
        <el-tab-pane :label="t('game.common.tenantData')" name="second" />
      </el-tabs>
    </div>
    <el-row :gutter="20" class="row-box">
      <el-col :span="8" v-for="(item, index) in tabsList" :key="index">
        <div
          :class="['overview-item', 'overview-item' + index]"
          @click="openChart('detail', item.key, item.name, item.status)"
          v-if="item.type == activeName"
        >
          <div class="item-box">
            <div class="left">
              <p><span></span> {{ item.name }} </p>
            </div>
            <div class="right">
              <h3>{{
                item.type == 'second'
                  ? dayTotalData?.[item.key]
                  : item.type == 'first'
                    ? (TenantStatusData?.[item.key] ?? 0)
                    : 0
              }}</h3>
              <p>{{ item.title }}</p>
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
      <el-form-item :label="t('game.common.timeRange')" prop="days">
        <el-date-picker
          v-model="daysArr"
          value-format="YYYY-MM-DD"
          type="datetimerange"
          :range-separator="t('game.common.rangeSeparator')"
          :start-placeholder="t('game.common.startTime')"
          :end-placeholder="t('game.common.endTime')"
          clearable
          class="!w-240px"
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
      <el-form-item>
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
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column
        :label="t('game.common.tenantCodeCol')"
        align="center"
        :prop="activeName == 'first' ? 'code' : 'tenantCode'"
      />
      <el-table-column
        :label="t('game.common.merchantName')"
        align="center"
        :prop="activeName == 'first' ? 'name' : 'tenantName'"
      />
      <el-table-column
        :label="t('game.common.betUsers')"
        align="center"
        prop="userBetCount"
        v-if="activeName == 'second'"
      />
      <el-table-column
        :label="t('game.common.betTotalAmount')"
        align="center"
        prop="betAmount"
        v-if="activeName == 'second'"
      />
      <el-table-column
        :label="t('game.common.payTotalAmount')"
        align="center"
        prop="payAmount"
        v-if="activeName == 'second'"
      />
      <el-table-column
        :label="t('game.common.profitUsers')"
        align="center"
        prop="userWinCount"
        v-if="activeName == 'second'"
      />
      <el-table-column
        :label="t('game.common.winLossTotal')"
        align="center"
        prop="winLoss"
        v-if="activeName == 'second'"
      />
      <el-table-column
        v-if="activeName == 'first'"
        :label="t('game.common.openTime')"
        :formatter="dateFormatter"
        align="center"
        prop="createTime"
      />
      <el-table-column
        v-if="activeName == 'first'"
        :label="t('common.status')"
        align="center"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.TENANT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.common.playerCount')"
        align="center"
        prop="tenantDO.accountCount"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="120px"
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
  <VendortotalForm ref="formRef" />
  <BasicInformation ref="InfoformRef" />
  <StatisticalChartForm ref="ChartRef" @success="getList" />
</template>

<script setup lang="ts">
  import download from '@/utils/download'
  import { TenantDayStaticApi, TenantDayStatic } from '@/api/game/vendortotal'
  import VendortotalForm from './VendortotalForm.vue'
  import BasicInformation from './BasicInformation.vue'
  import StatisticalChartForm from './StatisticalChartForm.vue'
  import { dateFormatter } from '@/utils/formatTime'
  import { DICT_TYPE } from '@/utils/dict'
  import type { TabsPaneContext } from 'element-plus'

  /** 租户游戏日报 列表 */
  defineOptions({ name: 'TenantDayStatic' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const activeName = ref('first')

  const loading = ref(true) // 列表的加载中
  const list = ref<TenantDayStatic[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const daysArr = ref()
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    days: undefined,
    endDays: undefined,
    tenantCode: undefined,
    tenantName: undefined,
    vendorCode: undefined,
    vendorName: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  const dayTotalData = ref()
  const TenantStatusData = ref()
  watchEffect(() => {
    if (Array.isArray(daysArr.value) && daysArr.value.length >= 2) {
      if (daysArr.value[0] && daysArr.value[1]) {
        queryParams.days = daysArr.value[0]
        queryParams.endDays = daysArr.value[1]
      }
    }
  })

  const tabsList = computed(() => [
    {
      name: t('game.common.statOpenedTenant'),
      key: 'openCounts',
      title: t('game.common.statOpenedTenantTotal'),
      type: 'first',
      status: 0
    },
    {
      name: t('game.common.statActiveTenant'),
      key: 'operCounts',
      title: t('game.common.statActiveTenantTotal'),
      type: 'first',
      status: 1
    },
    {
      name: t('game.common.statDisabledTenant'),
      key: 'closeCounts',
      title: t('game.common.statDisabledTenantTotal'),
      type: 'first',
      status: 2
    },
    {
      name: t('game.common.betTotalAmount'),
      key: 'betAmount',
      title: t('game.common.statBetTotalTitle'),
      type: 'second'
    },
    {
      name: t('game.common.payTotalAmount'),
      key: 'payAmount',
      title: t('game.common.statPayTotalTitle'),
      type: 'second'
    },
    {
      name: t('game.common.winLossTotal'),
      key: 'winLoss',
      title: t('game.common.statWinLossTotalTitle'),
      type: 'second'
    },
    {
      name: t('game.common.betUsers'),
      key: 'userBetCount',
      title: t('game.common.statTenantUserBetCountTitle'),
      type: 'second'
    },
    {
      name: t('game.common.profitUsers'),
      key: 'userWinCount',
      title: t('game.common.statTenantUserWinCountTitle'),
      type: 'second'
    },
    {
      name: t('game.common.playerCount'),
      key: 'userCount',
      title: t('game.common.statTenantUserCountTitle'),
      type: 'second'
    }
  ])

  const handleClick = (tab: TabsPaneContext, event: Event) => {
    nextTick(() => {
      getList()
      getOverview()
    })
  }

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      if (activeName.value == 'first') {
        const data =
          await TenantDayStaticApi.getTenantDayStaticPage(queryParams)
        list.value = data.list
        total.value = data.total
      } else {
        const data = await TenantDayStaticApi.getTenantdayPage(queryParams)
        list.value = data.list
        total.value = data.total
      }
    } finally {
      loading.value = false
    }
  }

  /** 查询数据总览 */
  const getOverview = async () => {
    loading.value = true
    try {
      if (activeName.value == 'first') {
        const data = await TenantDayStaticApi.getTenantStatus({})
        TenantStatusData.value = data
      } else {
        const data = await TenantDayStaticApi.getTenantdayTotal({})
        dayTotalData.value = data
      }
    } finally {
      loading.value = false
    }
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    queryFormRef.value.resetFields()
    handleQuery()
  }

  /** 查看操作 */
  const formRef = ref()
  const InfoformRef = ref()
  const openForm = (type: string, row?: Object) => {
    if (activeName.value == 'first') {
      InfoformRef.value.open(type, row)
    } else {
      formRef.value.open(type, row)
    }
  }

  const ChartRef = ref()
  const openChart = (
    type: string,
    key?: string,
    name?: string,
    status?: number
  ) => {
    ChartRef.value.open(type, key, name, status, {
      days: queryParams.days,
      endDays: queryParams.endDays
    })
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: TenantDayStatic[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      await message.exportConfirm()
      exportLoading.value = true
      const data = await TenantDayStaticApi.exportTenantDayStatic(queryParams)
      download.excel(data, t('game.common.exportTenantDay'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 初始化 **/
  onMounted(() => {
    getOverview()
    getList()
  })
</script>
<style lang="scss" scoped>
  .page-title {
    display: flex;
    font-size: 18px;
    font-weight: bold;
    justify-content: space-between;
    align-items: center;
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
</style>
