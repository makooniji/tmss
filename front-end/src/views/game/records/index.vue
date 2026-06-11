<template>
  <ContentWrap v-if="!embedded">
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item :label="t('game.recordsPage.user')" prop="userId">
        <el-input
          v-model="queryParams.userId"
          :placeholder="t('game.recordsPage.placeholderUser')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.actionType')" prop="action">
        <el-input
          v-model="queryParams.action"
          :placeholder="t('game.recordsPage.placeholderAction')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.orderNo')" prop="transNo">
        <el-input
          v-model="queryParams.transNo"
          :placeholder="t('game.recordsPage.placeholderOrderNo')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item :label="t('game.recordsPage.drawId')" prop="drawId">
        <el-input
          v-model="queryParams.drawId"
          :placeholder="t('game.recordsPage.placeholderDrawId')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          :start-placeholder="t('game.common.startDate')"
          :end-placeholder="t('game.common.endDate')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>

      <el-form-item :label="t('game.recordsPage.platform')" prop="platform">
        <el-input
          v-model="queryParams.platform"
          :placeholder="t('game.recordsPage.placeholderPlatform')"
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
      <el-form-item :label="t('game.recordsPage.gameCode')" prop="gameCode">
        <el-input
          v-model="queryParams.gameCode"
          :placeholder="t('game.recordsPage.placeholderGameCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.gameKind')" prop="gameKind">
        <el-input
          v-model="queryParams.gameKind"
          :placeholder="t('game.recordsPage.placeholderGameKind')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.recordsPage.issueNo')" prop="issueNo">
        <el-input
          v-model="queryParams.issueNo"
          :placeholder="t('game.recordsPage.placeholderIssueNo')"
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
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column
        :label="t('game.common.playerId')"
        align="center"
        prop="id"
      />
      <el-table-column
        :label="t('game.common.playerAccount')"
        align="center"
        prop="username"
      />

      <el-table-column
        :label="t('game.common.betAmount')"
        align="center"
        prop="betAmount"
      />
      <el-table-column
        :label="t('game.common.odds')"
        align="center"
        prop="odds"
      />
      <el-table-column
        :label="t('game.common.payAmount')"
        align="center"
        prop="winAmount"
      />
      <el-table-column
        :label="t('game.recordsPage.winLossCol')"
        align="center"
        prop="winLoss"
      />
      <el-table-column
        :label="t('game.recordsPage.timeCol')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column :label="t('common.status')" align="center" prop="action">
        <template #default="scope">
          <el-tag v-if="scope.row.winLoss > 0">{{
            t('game.recordsPage.winTag')
          }}</el-tag>
          <el-tag v-else>{{ t('game.recordsPage.loseTag') }}</el-tag>
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

  <!-- 表单弹窗：添加/修改（嵌入详情弹窗内不重复挂载） -->
  <RecordsForm v-if="!embedded" ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import { DICT_TYPE } from '@/utils/dict'
  import { isEmpty } from '@/utils/is'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import { RecordsApi, Records } from '@/api/game/records'
  import RecordsForm from './RecordsForm.vue'

  /** 用户游戏记录 列表 */
  defineOptions({ name: 'Records' })

  const props = withDefaults(
    defineProps<{
      /** 嵌入汇总详情弹窗时隐藏搜索栏 */
      embedded?: boolean
      /** 带入 /game/records/page 的 vendorCode */
      initialVendorCode?: string
    }>(),
    {
      embedded: false,
      initialVendorCode: ''
    }
  )

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(true) // 列表的加载中
  const list = ref<Records[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    userId: undefined,
    action: undefined,
    transNo: undefined,
    balance: undefined,
    drawId: undefined,
    roomKind: undefined,
    betAmount: undefined,
    winAmount: undefined,
    extraBusiness: undefined,
    createTime: [],
    sourceType: undefined,
    platform: undefined,
    vendorCode: undefined as string | undefined,
    gameCode: undefined,
    gameKind: undefined,
    betContent: undefined,
    issueNo: undefined,
    remark: undefined,
    odds: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await RecordsApi.getRecordsPage(queryParams)
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

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: Records[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await RecordsApi.exportRecords(queryParams)
      download.excel(data, t('game.recordsPage.exportFileUser'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 嵌入详情：请求分页时带上 vendorCode */
  watch(
    () => [props.embedded, props.initialVendorCode] as const,
    ([embedded, code]) => {
      if (!embedded) return
      queryParams.vendorCode = code || undefined
      queryParams.pageNo = 1
      if (code) {
        getList()
      } else {
        loading.value = false
        list.value = []
        total.value = 0
      }
    },
    { immediate: true }
  )

  /** 初始化 **/
  onMounted(() => {
    if (!props.embedded) {
      getList()
    }
  })
</script>
