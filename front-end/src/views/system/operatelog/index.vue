<template>
  <!-- 操作日志 -->
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      
    >
      <el-form-item :label="t('system.operateLog.operator')" prop="userId">
        <el-select
          v-model="queryParams.userId"
          clearable
          filterable
          :placeholder="t('system.operateLog.placeholderOperator')"
          class="!w-240px"
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.nickname"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.operateLog.module')" prop="type">
        <el-input
          v-model="queryParams.type"
          :placeholder="t('system.operateLog.placeholderModule')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('system.operateLog.name')" prop="subType">
        <el-input
          v-model="queryParams.subType"
          :placeholder="t('system.operateLog.placeholderName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('system.operateLog.content')" prop="action">
        <el-input
          v-model="queryParams.action"
          :placeholder="t('system.operateLog.placeholderContent')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item
        :label="t('system.operateLog.operateTime')"
        prop="createTime"
      >
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          :start-placeholder="t('common.startDate')"
          :end-placeholder="t('common.endDate')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('system.operateLog.bizId')" prop="bizId">
        <el-input
          v-model="queryParams.bizId"
          :placeholder="t('system.operateLog.placeholderBizId')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"
          ><Icon icon="ep:search" class="mr-5px" />
          {{ t('common.search') }}</el-button
        >
        <el-button @click="resetQuery"
          ><Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}</el-button
        >
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['system:operate-log:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column
        :label="t('system.operateLog.logId')"
        align="center"
        prop="id"
        width="100"
      />
      <el-table-column
        :label="t('system.operateLog.operator')"
        align="center"
        prop="userName"
        width="120"
      />
      <el-table-column
        :label="t('system.operateLog.module')"
        align="center"
        prop="type"
        width="120"
      />
      <el-table-column
        :label="t('system.operateLog.name')"
        align="center"
        prop="subType"
        width="160"
      />
      <el-table-column
        :label="t('system.operateLog.content')"
        align="center"
        prop="action"
      />
      <el-table-column
        :label="t('system.operateLog.operateTime')"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column
        :label="t('system.operateLog.bizId')"
        align="center"
        prop="bizId"
        width="120"
      />
      <el-table-column
        :label="t('system.operateLog.userIp')"
        align="center"
        prop="userIp"
        width="120"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        fixed="right"
        width="60"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row)"
            v-hasPermi="['system:operate-log:query']"
          >
            {{ t('system.operateLog.detail') }}
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

  <!-- 表单弹窗：详情 -->
  <OperateLogDetail ref="detailRef" />
</template>
<script lang="ts" setup>
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import * as OperateLogApi from '@/api/system/operatelog'
  import OperateLogDetail from './OperateLogDetail.vue'
  import * as UserApi from '@/api/system/user'
  const userList = ref<UserApi.UserVO[]>([]) // 用户列表

  defineOptions({ name: 'SystemOperateLog' })

  const { t } = useI18n()
  const message = useMessage() // 消息弹窗

  const loading = ref(true) // 列表的加载中
  const total = ref(0) // 列表的总页数
  const list = ref([]) // 列表的数据
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    userId: undefined,
    type: undefined,
    subType: undefined,
    action: undefined,
    createTime: [],
    bizId: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await OperateLogApi.getOperateLogPage(queryParams)
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

  /** 详情操作 */
  const detailRef = ref()
  const openDetail = (data: OperateLogApi.OperateLogVO) => {
    detailRef.value.open(data)
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await OperateLogApi.exportOperateLog(queryParams)
      download.excel(data, t('system.operateLog.exportFile'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 初始化 **/
  onMounted(async () => {
    await getList()
    // 获得用户列表
    userList.value = await UserApi.getSimpleUserList()
  })
</script>
