<template>
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
      <el-form-item :label="t('game.common.tenantCodeLabel')" prop="tenantCode">
        <el-input
          v-model="queryParams.tenantCode"
          :placeholder="t('game.common.placeholderTenantCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.tenantNameLabel')" prop="tenantName">
        <el-input
          v-model="queryParams.tenantName"
          :placeholder="t('game.common.placeholderTenantName')"
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
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />

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
        :label="t('game.common.betCountCol')"
        align="center"
        prop="betCount"
      />
      <el-table-column
        :label="t('game.common.betAmountCol')"
        align="center"
        prop="betAmount"
      />
      <el-table-column
        :label="t('game.common.payAmount')"
        align="center"
        prop="payAmount"
      />
      <el-table-column
        :label="t('game.common.winRate')"
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
        :label="t('game.common.participateUsers')"
        align="center"
        prop="userCount"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="120px"
      >
        <template #default="scope">
          <el-button link type="primary" @click="openForm('detail', scope.row)">
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
</template>

<script setup lang="ts">
  import { isEmpty } from '@/utils/is'
  import download from '@/utils/download'
  import { TenantDayStaticApi, TenantDayStatic } from '@/api/game/tenantstatic'
  import TenantDayStaticForm from './TenantDayStaticForm.vue'

  /** 租户游戏日报 列表 */
  defineOptions({ name: 'TenantDayStatic' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

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

  // 使用watchEffect来监听daysArr的变化，并更新queryParams
  watchEffect(() => {
    // 先检查daysArr是否是一个有效的数组
    if (Array.isArray(daysArr.value) && daysArr.value.length >= 2) {
      // 只有当daysArr有两个有效的日期时才更新queryParams
      if (daysArr.value[0] && daysArr.value[1]) {
        queryParams.days = daysArr.value[0]
        queryParams.endDays = daysArr.value[1]
      }
    }
  })

  /** 查询列表 */
  const getList = async () => {
    loading.value = true

    try {
      const data = await TenantDayStaticApi.getTenantDayStaticPage(queryParams)
      list.value = data.list
      total.value = data.total
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

  /** 添加/修改操作 */
  const formRef = ref()
  const openForm = (type: string, row?: Object) => {
    formRef.value.open(type, row)
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: TenantDayStatic[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
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
    getList()
  })
</script>
