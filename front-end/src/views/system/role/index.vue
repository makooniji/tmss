<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      
    >
      <el-form-item :label="t('role.roleName')" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          :placeholder="t('role.placeholderName')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('role.roleCode')" prop="code">
        <el-input
          v-model="queryParams.code"
          class="!w-240px"
          clearable
          :placeholder="t('role.placeholderCode')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          class="!w-240px"
          clearable
          :placeholder="t('game.common.selectStatus')"
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
          :end-placeholder="t('game.common.endDate')"
          :start-placeholder="t('game.common.startDate')"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          {{ t('game.common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          {{ t('common.reset') }}
        </el-button>
        <el-button
          v-hasPermi="['system:role:create']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          {{ t('action.add') }}
        </el-button>
        <el-button
          v-hasPermi="['system:role:export']"
          :loading="exportLoading"
          plain
          type="success"
          @click="handleExport"
        >
          <Icon class="mr-5px" icon="ep:download" />
          {{ t('action.export') }}
        </el-button>
        <el-button
          v-hasPermi="['system:role:delete']"
          :disabled="checkedIds.length === 0"
          plain
          type="danger"
          @click="handleDeleteBatch"
        >
          <Icon class="mr-5px" icon="ep:delete" />
          {{ t('game.common.batchDelete') }}
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
      <el-table-column align="center" :label="t('role.roleId')" prop="id" />
      <el-table-column align="center" :label="t('role.roleName')" prop="name" />
      <el-table-column :label="t('role.roleType')" align="center" prop="type">
        <template #default="scope">
          <dict-tag
            :type="DICT_TYPE.SYSTEM_ROLE_TYPE"
            :value="scope.row.type"
          />
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('role.roleCode')" prop="code" />
      <el-table-column
        align="center"
        :label="t('role.displayOrder')"
        prop="sort"
      />
      <el-table-column align="center" :label="t('role.remark')" prop="remark" />
      <el-table-column align="center" :label="t('common.status')" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        :label="t('common.createTime')"
        prop="createTime"
        width="180"
      />
      <el-table-column :width="300" align="center" :label="t('table.action')">
        <template #default="scope">
          <el-button
            v-hasPermi="['system:role:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            {{ t('action.update') }}
          </el-button>
          <el-button
            v-hasPermi="['system:permission:assign-role-menu']"
            link
            preIcon="ep:basketball"
            :title="t('role.menuPermissionBtn')"
            type="primary"
            @click="openAssignMenuForm(scope.row)"
          >
            {{ t('role.menuPermissionBtn') }}
          </el-button>
          <el-button
            v-hasPermi="['system:permission:assign-role-data-scope']"
            link
            preIcon="ep:coin"
            :title="t('role.dataPermissionBtn')"
            type="primary"
            @click="openDataPermissionForm(scope.row)"
          >
            {{ t('role.dataPermissionBtn') }}
          </el-button>
          <el-button
            v-hasPermi="['system:role:delete']"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <RoleForm ref="formRef" @success="getList" />
  <RoleAssignMenuForm ref="assignMenuFormRef" @success="getList" />
  <RoleDataPermissionForm ref="dataPermissionFormRef" @success="getList" />
</template>
<script lang="ts" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import * as RoleApi from '@/api/system/role'
  import RoleForm from './RoleForm.vue'
  import RoleAssignMenuForm from './RoleAssignMenuForm.vue'
  import RoleDataPermissionForm from './RoleDataPermissionForm.vue'

  defineOptions({ name: 'SystemRole' })

  const message = useMessage()
  const { t } = useI18n()

  const loading = ref(true)
  const total = ref(0)
  const list = ref([])
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    code: '',
    name: '',
    status: undefined,
    createTime: []
  })
  const queryFormRef = ref()
  const exportLoading = ref(false)

  const getList = async () => {
    loading.value = true
    try {
      const data = await RoleApi.getRolePage(queryParams)
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
    queryFormRef.value.resetFields()
    handleQuery()
  }

  const formRef = ref()
  const openForm = (type: string, id?: number) => {
    formRef.value.open(type, id)
  }

  const dataPermissionFormRef = ref()
  const openDataPermissionForm = async (row: RoleApi.RoleVO) => {
    dataPermissionFormRef.value.open(row)
  }

  const assignMenuFormRef = ref()
  const openAssignMenuForm = async (row: RoleApi.RoleVO) => {
    assignMenuFormRef.value.open(row)
  }

  const handleDelete = async (id: number) => {
    try {
      await message.delConfirm()
      await RoleApi.deleteRole(id)
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (rows: RoleApi.RoleVO[]) => {
    checkedIds.value = rows.map((row) => row.id)
  }

  const handleDeleteBatch = async () => {
    try {
      await message.delConfirm()
      await RoleApi.deleteRoleList(checkedIds.value)
      checkedIds.value = []
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  const handleExport = async () => {
    try {
      await message.exportConfirm()
      exportLoading.value = true
      const data = await RoleApi.exportRole(queryParams)
      download.excel(data, t('role.exportFile'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(() => {
    getList()
  })
</script>
