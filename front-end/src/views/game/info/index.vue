<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-select
          v-model="queryParams.vendorCode"
          :placeholder="t('game.common.placeholderVendorCode')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="item in vendorCodeList"
            :key="item.id"
            :label="item.vendorCode"
            :value="item.vendorCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.gameName')" prop="gameName">
        <el-input
          v-model="queryParams.gameName"
          :placeholder="t('game.info.placeholderGameName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.gameCode')" prop="gameCode">
        <el-input
          v-model="queryParams.gameCode"
          :placeholder="t('game.info.placeholderGameCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.category')" prop="cateId">
        <el-select
          v-model="queryParams.cateId"
          :placeholder="t('game.info.selectCategory')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="item in GameTypeList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.maintain')" prop="maintain">
        <el-input
          v-model="queryParams.maintain"
          :placeholder="t('game.info.placeholderMaintain')"
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
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('game.common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['game:info:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.create') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['game:info:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['game:info:delete']"
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
        :label="t('game.info.vendorCode')"
        align="center"
        prop="vendorCode"
      />
      <el-table-column
        :label="t('game.info.gameName')"
        align="center"
        prop="gameName"
      />
      <el-table-column
        :label="t('game.info.gameCode')"
        align="center"
        prop="gameCode"
      />
      <el-table-column
        :label="t('game.info.category')"
        align="center"
        prop="cateId"
      >
        <template #default="scope">
          {{ getCategoryName(scope.row.cateId) }}
        </template>
      </el-table-column>
      <el-table-column
        :label="t('game.info.currency')"
        align="center"
        prop="supportCurrency"
      />
      <el-table-column
        :label="t('game.info.lang')"
        align="center"
        prop="supportLang"
      />
      <el-table-column
        :label="t('game.common.mainCate')"
        align="center"
        prop="cateTitle"
      />
      <el-table-column
        :label="t('game.common.subCate')"
        align="center"
        prop="subCateTitle"
      />
      <el-table-column
        :label="t('game.info.cover')"
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
      <el-table-column
        :label="t('game.info.sort')"
        align="center"
        prop="sort"
      />
      <el-table-column
        :label="t('game.info.maintain')"
        align="center"
        prop="maintain"
      >
        <template #default="scope">
          <el-switch
            v-model="scope.row.maintain"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>

          <el-table-column
        :label="t('game.info.status')"
        align="center"
        prop="status"
      >
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
        :label="t('common.createTime')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        :label="t('game.info.onlineCount')"
        align="center"
        prop="onlineCount"
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
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['game:info:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['game:info:delete']"
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
  <InfoForm
    ref="formRef"
    @success="getList"
    :GameTypeList="GameTypeList"
    :vendorCodeList="vendorCodeList"
  />
</template>

<script setup lang="ts">
  import { isEmpty } from '@/utils/is'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import { InfoApi, Info } from '@/api/game/info'
  import { CategoryApi } from '@/api/game/category'
  import InfoForm from './InfoForm.vue'

  /** 游戏信息 列表 */
  defineOptions({ name: 'Info' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(true) // 列表的加载中
  const list = ref<Info[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    vendorCode: undefined,
    gameName: undefined,
    gameCode: undefined,
    cateId: undefined,
    maintain: undefined,
    createTime: []
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  interface GameType {
    id: number
    title: string
  }

  interface CodeList {
    id: number
    vendorCode: string
  }

  const GameTypeList = ref<GameType[]>([])

  const vendorCodeList = ref<CodeList[]>([])

  // 获取游戏分类名称的方法
  const getCategoryName = (cateId) => {
    const category = GameTypeList.value.find((item) => item.id === cateId)
    return category ? category.title : t('game.common.unknownCategory')
  }

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await InfoApi.getInfoPage(queryParams)
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
      await InfoApi.updateInfo(row)
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
      await InfoApi.deleteInfo(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 批量删除游戏信息 */
  const handleDeleteBatch = async () => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      await InfoApi.deleteInfoList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (records: Info[]) => {
    checkedIds.value = records.map((item) => item.id!)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await InfoApi.exportInfo(queryParams)
      download.excel(data, t('game.common.exportGameInfo'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 获取游戏分类 */
  const getGameTypeList = async () => {
    const queryParams = reactive({
      pageNo: 1,
      pageSize: 200
    })
    try {
      const data = await CategoryApi.getCategoryPage(queryParams)
      GameTypeList.value = data.list
    } finally {
    }
  }

  /** 获取厂商编码分类 */
  const getVendorCodeList = async () => {
    const queryParams = reactive({
      pageNo: 1,
      pageSize: 200
    })
    try {
      const data = await CategoryApi.getVendorCodePage(queryParams)
      vendorCodeList.value = data.list
    } finally {
    }
  }

  /** 初始化 **/
  onMounted(() => {
    getList()
    getGameTypeList()
    getVendorCodeList()
  })
</script>
