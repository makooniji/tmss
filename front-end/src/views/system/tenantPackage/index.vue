<template>
  <ContentWrap :title="t('tenantPackage.moduleTitle')">
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      
    >
      <el-form-item :label="t('tenantPackage.packageName')" prop="name">
        <el-input
          v-model="queryParams.name"
          :placeholder="t('tenantPackage.placeholderPackageName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('common.selectStatus')"
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
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          :start-placeholder="t('common.startDate')"
          :end-placeholder="t('common.endDate')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['system:tenant-package:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          {{ t('action.add') }}
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="checkedIds.length === 0"
          @click="handleDeleteBatch"
          v-hasPermi="['system:tenant-package:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" />
          {{ t('common.batchDelete') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column
        :label="t('tenantPackage.packageId')"
        align="center"
        prop="id"
        width="120"
      />
      <el-table-column
        :label="t('tenantPackage.packageName')"
        align="center"
        prop="name"
      />
      <el-table-column
        :label="t('common.status')"
        align="center"
        prop="status"
        width="100"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('tenantPackage.remark')"
        align="center"
        prop="remark"
      />
      <el-table-column
        :label="t('common.createTime')"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['system:tenant-package:update']"
          >
            {{ t('action.update') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['system:tenant-package:delete']"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <TenantPackageForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'
  import * as TenantPackageApi from '@/api/system/tenantPackage'
  import TenantPackageForm from './TenantPackageForm.vue'

  defineOptions({ name: 'SystemTenantPackage' })

  const message = useMessage()
  const { t } = useI18n()

  const loading = ref(true)
  const total = ref(0)
  const list = ref([])
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    name: undefined,
    status: undefined,
    remark: undefined,
    createTime: []
  })
  const queryFormRef = ref()

  const getList = async () => {
    loading.value = true
    try {
      const data = await TenantPackageApi.getTenantPackagePage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  const resetQuery = () => {
    queryFormRef.value?.resetFields()
    handleQuery()
  }

  const formRef = ref()
  const openForm = (type: string, id?: number) => {
    formRef.value.open(type, id)
  }

  const handleDelete = async (id: number) => {
    try {
      await message.delConfirm()
      await TenantPackageApi.deleteTenantPackage(id)
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (
    rows: TenantPackageApi.TenantPackageVO[]
  ) => {
    checkedIds.value = rows.map((row) => row.id)
  }

  const handleDeleteBatch = async () => {
    try {
      await message.delConfirm()
      await TenantPackageApi.deleteTenantPackageList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  onMounted(() => {
    getList()
  })
</script>
