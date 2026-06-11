<template>
  <ContentWrap>
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

  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column
        :label="t('game.common.tenantCodeCol')"
        align="center"
        prop="tenantCode"
      />
      <el-table-column
        :label="t('game.common.merchantName')"
        align="center"
        prop="tenantName"
      />
      <el-table-column
        :label="t('game.common.vendorCodeCol')"
        align="center"
        prop="vendorCode"
      />
      <el-table-column
        :label="t('game.vendor.vendorName')"
        align="center"
        prop="vendorName"
      />
      <el-table-column
        :label="t('game.info.gameName')"
        align="center"
        prop="gameName"
      />
      <el-table-column
        :label="t('game.common.gameTypeCol')"
        align="center"
        prop="gameCode"
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
          {{ scope.row.betAmount / scope.row.userCount }}
        </template>
      </el-table-column>
      <el-table-column :label="t('game.common.avgPayPerUser')" align="center">
        <template #default="scope">
          {{ scope.row.payAmount / scope.row.userCount }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="120px"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="
              openForm('detail', scope.row.gameCode, scope.row.vendorCode)
            "
            v-hasPermi="['game:tenant-day-static:query']"
          >
            {{ t('game.common.view') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <TenantDayStaticForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import download from '@/utils/download'
  import {
    TenantDayStaticApi,
    TenantDayStatic
  } from '@/api/game/tenantdaystatic'
  import TenantDayStaticForm from './TenantDayStaticForm.vue'

  defineOptions({ name: 'TenantDayStatic' })

  const message = useMessage()
  const { t } = useI18n()

  const loading = ref(true)
  const list = ref<TenantDayStatic[]>([])
  const total = ref(0)
  const daysArr = ref()
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    days: undefined,
    endDays: undefined,
    tenantCode: undefined,
    tenantName: undefined,
    vendorCode: undefined,
    vendorName: undefined,
    gameCode: undefined
  })
  const queryFormRef = ref()
  const exportLoading = ref(false)

  watchEffect(() => {
    if (Array.isArray(daysArr.value) && daysArr.value.length >= 2) {
      if (daysArr.value[0] && daysArr.value[1]) {
        queryParams.days = daysArr.value[0]
        queryParams.endDays = daysArr.value[1]
      }
    }
  })

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

  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  const resetQuery = () => {
    queryFormRef.value.resetFields()
    handleQuery()
  }

  const formRef = ref()
  const openForm = (type: string, gameCode?: string, vendorCode?: string) => {
    formRef.value.open(type, gameCode, vendorCode)
  }

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

  onMounted(() => {
    getList()
  })
</script>
