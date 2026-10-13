<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
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

      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
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
      <el-form-item
        :label="t('common.createTime')"
        prop="daysArr"
        class="records-search-form__datetime"
      >
        <el-date-picker
          v-model="daysArr"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          :shortcuts="dateRangeShortcuts"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          :range-separator="t('game.common.rangeSeparator')"
          :start-placeholder="t('game.common.startTime')"
          :end-placeholder="t('game.common.endTime')"
          clearable
          class="records-search-datetime"
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
            v-for="dict in zoneOptions"
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

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <!-- <el-table-column
        :label="t('game.recordsPage.orderId')"
        align="center"
        prop="id"
        width="100"
      /> -->
      <el-table-column
        :label="t('game.common.playerAccount')"
        align="center"
        prop="username"
        min-width="120"
      />
      <!-- <el-table-column
        :label="t('game.common.playerId')"
        align="center"
        prop="userId"
        width="100"
      /> -->
      <el-table-column
        :label="t('game.recordsPage.betOrderNo')"
        align="center"
        prop="orderNo"
        min-width="140"
      />
      <!-- <el-table-column
        :label="t('game.recordsPage.refOrderNo')"
        align="center"
        prop="refOrderNo"
        min-width="140"
      />
      <el-table-column
        :label="t('game.recordsPage.issueNo')"
        align="center"
        prop="issueNo"
        min-width="120"
      /> -->
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
      <!-- <el-table-column
        :label="t('game.recordsPage.betContent')"
        align="center"
        prop="betContent"
        min-width="120"
      /> -->
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
    <!-- 分页 -->
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
  import { CategoryApi } from '@/api/game/category'
  import { RecordsApi, Records } from '@/api/game/records'

  /** 玩家游戏明细 */
  defineOptions({ name: 'RecordsIndex' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(false) // 列表的加载中
  const list = ref<Records[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const daysArr = ref<[string, string] | undefined>()
  const gameTypeList = ref<{ id: number; title: string }[]>([])
  const zoneOptions = computed(() => getStrDictOptions(DICT_TYPE.SYSTEM_ZONE))
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    username: undefined as string | undefined,
    orderNo: undefined as string | undefined,
    vendorCode: undefined as string | undefined,
    gameCode: undefined,
    gameKind: undefined as number | undefined,
    days: undefined as string | undefined,
    endDays: undefined as string | undefined,
    zone: undefined as string | undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  const hasValidDateRange = () =>
    Array.isArray(daysArr.value) &&
    daysArr.value.length >= 2 &&
    daysArr.value[0] &&
    daysArr.value[1]

  const dateRangeShortcuts = useDateRangeShortcuts({ tomorrow: true })

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

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await RecordsApi.getRecordsPage(buildQueryParams())
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const buildQueryParams = () => ({
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize,
    username: queryParams.username,
    orderNo: queryParams.orderNo,
    vendorCode: queryParams.vendorCode,
    gameCode: queryParams.gameCode,
    gameKind: queryParams.gameKind,
    days: queryParams.days,
    endDays: queryParams.endDays,
    zone: queryParams.zone
  })

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    daysArr.value = undefined
    queryFormRef.value.resetFields()
    list.value = []
    total.value = 0
    queryParams.pageNo = 1
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await RecordsApi.exportRecordsPage(buildQueryParams())
      download.excel(data, t('game.recordsPage.exportFileUserPage'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  const getGameTypeList = async () => {
    const data = await CategoryApi.getCategoryPage({ pageNo: 1, pageSize: 10 })
    gameTypeList.value = data.list || []
  }

  /** 初始化 **/
  onMounted(() => {
    getGameTypeList()
    getList()
  })
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

    :deep(.records-search-form__datetime) {
      width: 500px;
    }

    :deep(.records-search-datetime) {
      width: 380px;
    }
  }
</style>
