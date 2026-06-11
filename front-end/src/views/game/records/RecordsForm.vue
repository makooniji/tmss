<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="70%">
    <div class="section">
      <div class="section-title">{{ t('game.records.dataDetailSection') }}</div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.vendorCodeLabel') }}
              <span>{{ currentVendorCode }}</span>
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.totalBets') }}:<span
                >{{ formData?.betCount || 0
                }}{{ t('game.records.timesUnit') }}</span
              >
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.betTotal') }}:<span
                >${{ formData?.betAmount || 0 }}</span
              >
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.payTotal') }}:<span
                >${{ formData?.winAmount || 0 }}</span
              >
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.rtp') }}:<span>{{ formData?.rtp || 0 }}%</span>
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.profitLoss') }}:<span
                >${{ formData?.winLoss || 0 }}</span
              >
            </p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-item">
            <p>
              {{ t('game.records.avgBet') }}:<span
                >${{ formData?.avgBetAmount || 0 }}</span
              >
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

    <indexList />

    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { RecordsApi } from '@/api/game/records'

  import indexList from './index.vue'

  /** 用户游戏记录 表单 */
  defineOptions({ name: 'RecordsForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改

  const formData = ref() // 统计数据
  /** 与汇总表「厂商编码」列一致，并传给投注明细列表查询 */
  const currentVendorCode = ref('')

  /** 打开弹窗 */
  const open = async (type: string, vendorCode?: string) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    currentVendorCode.value = vendorCode != null ? String(vendorCode) : ''
    // 修改时，设置数据
    if (vendorCode) {
      formLoading.value = true
      try {
        formData.value = await RecordsApi.vendorTotal(vendorCode)
      } finally {
        formLoading.value = false
      }
    }
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
