<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('game.common.categoryName')" prop="title">
        <el-input v-model="queryParams.title" :placeholder="t('game.common.placeholderCategoryName')" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item :label="t('game.common.categoryCode')" prop="code">
        <el-input v-model="queryParams.code" :placeholder="t('game.categoryForm.placeholderCode')" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('game.common.selectStatus')" clearable class="!w-240px">
          <el-option v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :label="dict.label"
            :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" value-format="YYYY-MM-DD HH:mm:ss" type="daterange"
          :start-placeholder="t('game.common.startDate')" :end-placeholder="t('game.common.endDate')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]" class="!w-220px" />
      </el-form-item>
      <el-form-item :label="t('game.categoryForm.langConfig')" prop="lang">
        <el-input v-model="queryParams.lang" :placeholder="t('game.categoryForm.placeholderLangConfig')" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item :label="t('game.info.category')" prop="cateId">
        <el-select v-model="queryParams.cateId" :placeholder="t('game.categorySub.selectGameCategory')" clearable
          class="!w-240px">
          <el-option v-for="item in GameTypeList" :key="item.id" :label="item.title" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('game.common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['game:category-sub:create']">
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.create') }}
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading"
          v-hasPermi="['game:category-sub:export']">
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
        <el-button type="danger" plain :disabled="isEmpty(checkedIds)" @click="handleDeleteBatch"
          v-hasPermi="['game:category-sub:delete']">
          <Icon icon="ep:delete" class="mr-5px" />
          {{ t('game.common.batchDelete') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"
      @selection-change="handleRowCheckboxChange">
      <el-table-column type="selection" width="55" />
      <el-table-column :label="t('game.common.primaryKey')" align="center" prop="id" />

      <el-table-column :label="t('game.categorySub.parentCateName')" align="center" prop="cateName" />
      <el-table-column :label="t('game.common.categoryCode')" align="center" prop="code" />
      <el-table-column :label="t('game.common.categoryName')" align="center" prop="title" />

      <el-table-column :label="t('game.categoryForm.logo')" align="center" prop="logo">
        <template #default="scope">
          <el-image v-if="scope.row.logo" style="width: 100px; height: 100px" :src="scope.row.logo" fit="scale-down" />
          <p v-else>{{ t('game.common.noPhoto') }}</p>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.status')" align="center" prop="status">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1"
            @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column :label="t('game.categoryForm.sort')" align="center" prop="sort" />
      <el-table-column :label="t('common.createTime')" align="center" prop="createTime" :formatter="dateFormatter"
        width="180px" />

      <el-table-column :label="t('table.action')" align="center" min-width="120px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)"
            v-hasPermi="['game:category-sub:update']">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['game:category-sub:delete']">
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
      @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <CategorySubForm ref="formRef" @success="getList" :GameTypeList="GameTypeList" />
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { CategorySubApi, CategorySub } from '@/api/game/categorysub'
import CategorySubForm from './CategorySubForm.vue'

/** 游戏分类 列表 */
defineOptions({ name: 'CategorySub' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<CategorySub[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined,
  code: undefined,
  status: undefined,
  createTime: [],
  lang: undefined,
  cateName: undefined,
  cateId: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

interface GameType {
  id: number
  title: string
}

const GameTypeList = ref<GameType[]>([])

/** 获取游戏大类 */
const getGameTypeList = async () => {
  const qp = reactive({
    pageNo: 1,
    pageSize: 200
  })
  try {
    const data = await CategorySubApi.getCategoryPage(qp)
    GameTypeList.value = data.list
  } finally {
  }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CategorySubApi.getCategorySubPage(queryParams)
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
    await CategorySubApi.updateCategorySub(row)
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
    await message.delConfirm()
    await CategorySubApi.deleteCategorySub(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
}

/** 批量删除游戏分类 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await CategorySubApi.deleteCategorySubList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch { }
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: CategorySub[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await CategorySubApi.exportCategorySub(queryParams)
    download.excel(data, t('game.categorySub.exportFile'))
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
  getGameTypeList()
})
</script>
