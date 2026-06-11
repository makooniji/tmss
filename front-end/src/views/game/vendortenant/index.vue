<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
    >
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
      <el-form-item :label="t('game.common.vendorCodeGame')" prop="vendorCode">
        <el-input
          v-model="queryParams.vendorCode"
          :placeholder="t('game.common.placeholderVendorCodeGame')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.vendorShortName')" prop="vendorName">
        <el-input
          v-model="queryParams.vendorName"
          :placeholder="t('game.common.placeholderVendorNameShort')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('game.common.selectStatus')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['game:vendor-tenant:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['game:vendor-tenant:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" />
          {{ t('game.common.batchDelete') }}
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
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column
        :label="t('game.common.merchantId')"
        align="center"
        prop="tenantId"
      />
      <el-table-column
        :label="t('game.common.merchantCode')"
        align="center"
        prop="tenantCode"
      />
      <el-table-column
        :label="t('game.common.vendorCodeGame')"
        align="center"
        prop="vendorCode"
      />
      <el-table-column
        :label="t('game.common.vendorShortName')"
        align="center"
        prop="vendorName"
      />
      <el-table-column
        :label="t('game.common.iconCol')"
        align="center"
        prop="logo"
        width="150"
      >
        <template #default="scope">
          <el-image
            v-if="scope.row.logo"
            style="width: 100px; height: 100px"
            :src="scope.row.logo"
            fit="scale-down"
          />
          <p v-else>{{ t('game.common.noCover') }}</p>
        </template>
      </el-table-column>

      <el-table-column :label="t('common.status')" align="center" prop="status">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.common.rtpInput')"
        align="center"
        prop="rtp"
      />
      <el-table-column
        :label="t('common.createTime')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <!-- <el-table-column label="回调地址" align="center" prop="callbackUrl" /> -->
      <!-- <el-table-column label="拉单地址" align="center" prop="reportUrl" /> -->
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="120px"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['game:vendor-tenant:update']"
          >
            {{ t('action.update') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['game:vendor-tenant:delete']"
          >
            {{ t('action.delete') }}
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
  <VendorTenantForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
  import { isEmpty } from '@/utils/is'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import { VendorTenantApi, VendorTenant } from '@/api/game/vendortenant'
  import VendorTenantForm from './VendorTenantForm.vue'

  /** 租户厂商 列表 */
  defineOptions({ name: 'VendorTenant' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(true) // 列表的加载中
  const list = ref<VendorTenant[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    vendorCode: undefined,
    vendorName: undefined,
    vendorChannel: undefined,
    vendorAgent: undefined,
    vendorKey: undefined,
    vendorZone: undefined,
    lobbyUrl: undefined,
    apiUrl: undefined,
    logo: undefined,
    currency: undefined,
    lang: undefined,
    status: undefined,
    remark: undefined,
    rtp: undefined,
    createTime: [],
    callbackUrl: undefined,
    reportUrl: undefined,
    tenantCode: undefined,
    tenantName: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await VendorTenantApi.getVendorTenantPage(queryParams)
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

  // 处理状态变化的方法
  const handleStatusChange = async (row) => {
    try {
      await VendorTenantApi.updateVendorTenant({
        id: row.id,
        status: row.status
      })
      message.success(t('common.updateSuccess'))
    } finally {
    }
  }

  /** 添加/修改操作 */
  const formRef = ref()
  const openForm = (type: string, id?: number) => {
    formRef.value.open(type, id)
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起删除
      await VendorTenantApi.deleteVendorTenant(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 批量删除租户厂商 */
  const handleDeleteBatch = async () => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      await VendorTenantApi.deleteVendorTenantList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: VendorTenant[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await VendorTenantApi.exportVendorTenant(queryParams)
      download.excel(data, t('game.common.exportVendorTenant'))
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
