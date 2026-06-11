<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="70%">
    <div class="section-title">{{ t('game.common.tenantBasicSection') }}</div>
    <el-form ref="formRef" :model="formData" label-width="100px">
      <el-form-item :label="t('game.common.merchantCode')" prop="code">
        <p>{{ formData?.tenantDO?.code }}</p>
      </el-form-item>
      <el-form-item :label="t('game.common.merchantAccount')" prop="name">
        <p>{{ formData?.tenantDO?.name }}</p>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <dict-tag
          :type="DICT_TYPE.TENANT_STATUS"
          :value="formData?.tenantDO?.status || 0"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.playerCount')" prop="accountCount">
        <p>{{ formData?.tenantDO?.accountCount }}</p>
      </el-form-item>
      <el-form-item :label="t('game.common.openTime')" prop="createTime">
        <p>{{ formatDate(formData?.tenantDO?.createTime) }}</p>
      </el-form-item>
    </el-form>

    <div class="section">
      <div class="section-title">{{ t('game.common.financeDataSection') }}</div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in DataDetails" :key="index">
          <div class="overview-item">
            <p
              >{{ item.name }} <span>{{ formData?.[item.value] || 0 }}</span></p
            >
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="betting-details-section">
      <div class="section-header">
        <h3 class="section-title-bet">{{
          t('game.common.tenantPlayerDataSection')
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
        <el-form-item label="" prop="name">
          <el-input
            v-model="queryParams.name"
            :placeholder="t('game.common.placeholderSearchAccount')"
            clearable
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            {{ t('game.common.search') }}
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
          :label="t('game.common.playerId')"
          align="center"
          prop="userId"
        />
        <el-table-column
          :label="t('game.common.playerAccount')"
          align="center"
          prop="username"
        />
        <el-table-column
          :label="t('game.common.betTotalTimes')"
          align="center"
          prop="betCount"
        />
        <el-table-column
          :label="t('game.common.betTotalAmount')"
          align="center"
          prop="betCount"
        />
        <el-table-column
          :label="t('game.common.winLossTotal')"
          align="center"
          prop="winLoss"
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
  import { TenantDayStaticApi } from '@/api/game/vendortotal'
  import { DICT_TYPE } from '@/utils/dict'
  import { formatDate } from '@/utils/formatTime'

  defineOptions({ name: 'TenantStaticDetailForm' })

  const { t } = useI18n()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formType = ref('')

  interface TenantDO {
    code?: string
    name?: string
    status?: string
    accountCount?: number
    createTime?: Date
  }
  const formData = ref<{
    tenantCode?: string
    tenantName?: string
    status?: string
    accountCount?: number
    createTime?: string
    tenantDO?: TenantDO
    [key: string]: unknown
  }>({
    tenantCode: undefined,
    tenantName: undefined,
    status: undefined,
    accountCount: undefined,
    createTime: undefined,
    tenantDO: {
      code: undefined,
      name: undefined,
      status: undefined,
      accountCount: undefined,
      createTime: undefined
    }
  })

  const DataDetails = computed(() => [
    { name: `${t('game.common.betUsers')}:`, value: 'userBetCount' },
    { name: `${t('game.common.betTotalAmount')}:`, value: 'betAmount' },
    { name: `${t('game.common.payTotalAmount')}:`, value: 'payAmount' },
    { name: `${t('game.common.profitUsers')}:`, value: 'userWinCount' },
    { name: `${t('game.common.winLossTotal')}:`, value: 'winLoss' }
  ])

  const loading = ref(false)
  const total = ref(0)

  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    name: ''
  })

  const list = ref([])

  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  const getList = async () => {
    loading.value = true
    try {
      const data =
        await TenantDayStaticApi.getTenantDaypageUserPage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const open = async (type: string, row?: object) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    formData.value = Object.assign({}, row)
    getList()
  }
  defineExpose({ open })
</script>

<style lang="scss" scoped>
  .section {
    margin-bottom: 20px;
  }

  .overview-item {
    padding: 20px;
    margin: 10px 0;
    background: linear-gradient(135deg, #f8f9fa, #fff);
    border: 1px solid #e9ecef;
    border-radius: 12px;
    box-shadow: 0 4px 6px #0000000d;
    transition: all 0.3s ease;
  }

  .overview-item:hover {
    border-color: #3498db;
    transform: translateY(-2px);
    box-shadow: 0 6px 12px #0000001a;
  }

  .overview-item p {
    display: flex;
    gap: 10px;
    align-items: center;
    font-size: 14px;
  }

  .overview-item p span {
    font-size: 20px;
    font-weight: bold;
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

  .items-center {
    display: flex;
    background: #ecf0f1;

    p {
      padding: 5px 15px;
      font-size: 16px;
      cursor: pointer;
    }

    .active {
      background: #dce3e5;
    }
  }
</style>
