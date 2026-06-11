<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      
    >
      <el-form-item :label="t('system.dictType.name')" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          :placeholder="t('system.dictType.placeholderName')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('system.dictType.type')" prop="type">
        <el-input
          v-model="queryParams.type"
          class="!w-240px"
          clearable
          :placeholder="t('system.dictType.placeholderType')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          class="!w-240px"
          clearable
          :placeholder="t('system.dictType.selectStatus')"
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
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          :end-placeholder="t('common.endDate')"
          :start-placeholder="t('common.startDate')"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          {{ t('common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          {{ t('common.reset') }}
        </el-button>
        <el-button
          v-hasPermi="['system:dict:create']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          {{ t('action.create') }}
        </el-button>
        <el-button
          v-hasPermi="['system:dict:export']"
          :loading="exportLoading"
          plain
          type="success"
          @click="handleExport"
        >
          <Icon class="mr-5px" icon="ep:download" />
          {{ t('action.export') }}
        </el-button>
        <el-button
          v-hasPermi="['system:dict:delete']"
          :disabled="checkedIds.length === 0"
          plain
          type="danger"
          @click="handleDeleteBatch"
        >
          <Icon class="mr-5px" icon="ep:delete" />
          {{ t('common.batchDelete') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column
        align="center"
        :label="t('system.dictType.id')"
        prop="id"
      />
      <el-table-column
        align="center"
        :label="t('system.dictType.name')"
        prop="name"
        show-overflow-tooltip
      />
      <el-table-column
        align="center"
        :label="t('system.dictType.type')"
        prop="type"
        width="300"
      />
      <el-table-column align="center" :label="t('common.status')" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('form.remark')" prop="remark" />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        :label="t('common.createTime')"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" :label="t('table.action')">
        <template #default="scope">
          <el-button
            v-hasPermi="['system:dict:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            {{ t('system.modify') }}
          </el-button>
          <router-link :to="'/dict/type/data/' + scope.row.type">
            <el-button link type="primary">{{
              t('system.dictType.data')
            }}</el-button>
          </router-link>
          <el-button
            v-hasPermi="['system:dict:delete']"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DictTypeForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'
  import * as DictTypeApi from '@/api/system/dict/dict.type'
  import DictTypeForm from './DictTypeForm.vue'
  import download from '@/utils/download'

  defineOptions({ name: 'SystemDictType' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(true) // 列表的加载中
  const total = ref(0) // 列表的总页数
  const list = ref([]) // 字典表格数据
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    name: '',
    type: '',
    status: undefined,
    createTime: []
  })
  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中

  /** 查询字典类型列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await DictTypeApi.getDictTypePage(queryParams)
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
  const openForm = (type: string, id?: number) => {
    formRef.value.open(type, id)
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起删除
      await DictTypeApi.deleteDictType(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 批量删除按钮操作 */
  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (rows: DictTypeApi.DictTypeVO[]) => {
    checkedIds.value = rows.map((row) => row.id)
  }

  const handleDeleteBatch = async () => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起批量删除
      await DictTypeApi.deleteDictTypeList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 导出按钮操作 */
  const handleExport = async () => {
    try {
      // 导出的二次确认
      await message.exportConfirm()
      // 发起导出
      exportLoading.value = true
      const data = await DictTypeApi.exportDictType(queryParams)
      download.excel(data, t('system.dictType.exportFile'))
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
