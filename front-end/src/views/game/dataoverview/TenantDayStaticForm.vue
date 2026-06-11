<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="70%">
    <div class="section">
      <div class="section-title">{{ t('game.records.dataDetailSection') }}</div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in DataDetails" :key="index">
          <div class="overview-item">
            <p>
              {{ item.name }}
              <span
                ><b v-if="item.location == 'left'">{{ item.unit }}</b
                >{{ totalData?.[item.value] || 0 }}
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
          t('game.records.betDetailSection')
        }}</h3>
      </div>
    </div>

    <ContentWrap>
      <!-- 搜索工作栏 -->
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

    <!-- 列表 -->
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
          :label="t('game.common.winLossCol')"
          align="center"
          prop="winLoss"
        />
        <el-table-column
          :label="t('common.status')"
          align="center"
          prop="action"
        >
          <template #default="scope">
            <dict-tag
              :type="DICT_TYPE.SYSTEM_GAME_RECORD_ACTION"
              :value="scope.row.action"
            />
          </template>
        </el-table-column>
        <el-table-column
          :label="t('common.createTime')"
          align="center"
          prop="createTime"
          width="180"
          :formatter="dateFormatter"
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

    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { GameDataOverviewApi } from '@/api/game/dataoverview'
  import { DICT_TYPE } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'

  /** 租户游戏日报 表单 */
  defineOptions({ name: 'TenantDayStaticForm' })

  const { t } = useI18n() // 国际化

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  const formData = ref({
    id: undefined,
    days: undefined,
    tenantCode: undefined,
    tenantName: undefined,
    vendorCode: undefined,
    vendorName: undefined
  })

  const DataDetails = computed(() => [
    { name: `${t('game.info.gameName')}:`, value: 'gameName' },
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
      name: `${t('game.common.rtp')}:`,
      value: 'rtp',
      unit: '%',
      location: 'right' as const
    },
    {
      name: `${t('game.common.winLossTotal')}:`,
      value: 'winLoss',
      unit: '$',
      location: 'left' as const
    },
    {
      name: `${t('game.common.perCapitaBet')}:`,
      value: 'avgBetAmount',
      unit: '$',
      location: 'left' as const
    }
  ])
  const totalData = ref()

  const loading = ref(false) // 列表的加载中
  const total = ref(0) // 列表的总页数

  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    name: ''
  })

  const list = ref([]) // 列表的数据

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await GameDataOverviewApi.getrecordsPage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const getTotal = async (gameCode) => {
    try {
      const data = await GameDataOverviewApi.gettenantstaticTotal({ gameCode })
      totalData.value = data
    } finally {
    }
  }

  /** 打开弹窗 */
  const open = async (type: string, gameCode?: string) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    getList()
    getTotal(gameCode)
  }
  defineExpose({ open }) // 提供 open 方法，用于打开弹窗
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
    font-size: 14px;
    gap: 10px;
    align-items: center;
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
