<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
    >
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

      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
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
        :label="t('game.common.indexTotalBetAmount')"
        align="center"
        prop="betAmount"
      />
      <el-table-column
        :label="t('game.common.indexTotalPayAmount')"
        align="center"
        prop="winAmount"
      />
      <el-table-column
        :label="t('game.common.indexTotalWinLoss')"
        align="center"
        prop="winLoss"
      />
      <el-table-column
        :label="t('game.common.rtpColumn')"
        align="center"
        prop="rtp"
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
            @click="openForm('detail', scope.row.vendorCode)"
            v-hasPermi="['game:records:update']"
          >
            {{ t('action.detail') }}
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
  <RecordsForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import { isEmpty } from '@/utils/is'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import { RecordsApi, Records } from '@/api/game/records'
  import RecordsForm from './RecordsForm.vue'

  /** 用户游戏记录 列表 */
  defineOptions({ name: 'Records' })

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
    orderNo: undefined,
    roundId: undefined,
    roomKind: undefined,
    betAmount: undefined,
    winAmount: undefined,
    extraBusiness: undefined,
    createTime: [],
    sourceType: undefined,
    platform: undefined,
    vendorCode: undefined,
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
      const data = await RecordsApi.getRecordsTotalPage(queryParams)
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
  const openForm = (type: string, vendorCode?: string) => {
    formRef.value.open(type, vendorCode)
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起删除
      await RecordsApi.deleteRecords(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 批量删除用户游戏记录 */
  const handleDeleteBatch = async () => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      await RecordsApi.deleteRecordsList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
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
      const data = await RecordsApi.exportTotalRecords(queryParams)
      download.excel(data, t('game.common.exportUserGameRecords'))
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
