<template>
  <!-- 租户管理 -->
  <!-- 搜索 -->
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
    >
      <el-form-item :label="t('system.tenant.merchantName')" prop="name">
        <el-input
          v-model="queryParams.name"
          :placeholder="t('system.tenant.placeholderMerchantName')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('system.tenant.merchantCode')" prop="code">
        <el-input
          v-model="queryParams.code"
          :placeholder="t('system.tenant.placeholderMerchantCode')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item :label="t('system.tenant.merchantStatus')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('system.tenant.selectMerchantStatus')"
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
      <el-form-item
        :label="t('system.tenant.operateStatus')"
        prop="operateStatus"
      >
        <el-select
          v-model="queryParams.operateStatus"
          :placeholder="t('system.tenant.selectOperateStatus')"
          clearable
          class="!w-240px"
        >
          <el-option :label="t('common.on')" :value="0" />
          <el-option :label="t('common.off')" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
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

      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          {{ t('common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['system:tenant:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          {{ t('action.create') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['system:tenant:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          {{ t('action.export') }}
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="checkedIds.length === 0"
          @click="handleDeleteBatch"
          v-hasPermi="['system:tenant:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" />
          {{ t('common.batchDelete') }}
        </el-button>
        <el-button v-if="!isRootList" plain @click="handleBackToParent">
          <Icon icon="ep:back" class="mr-5px" />
          {{ t('system.tenant.backToParent') }}
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
      <!-- <el-table-column label="主键" align="center" prop="id" /> -->
      <el-table-column
        :label="t('system.tenant.merchantName')"
        align="center"
        prop="name"
      />
      <el-table-column
        :label="t('system.tenant.merchantId')"
        align="center"
        prop="code"
      />
      <el-table-column
        :label="t('system.tenant.tenantKey')"
        align="center"
        prop="tenantKey"
      />
      <el-table-column
        :label="t('system.tenant.secretKey')"
        align="center"
        min-width="100"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openSecretKeyDialog(scope.row)"
          >
            {{ t('system.tenant.viewSecretKey') }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('system.tenant.tenantPackage')"
        align="center"
        prop="packageId"
        min-width="120"
      >
        <template #default="scope">
          {{ getPackageName(scope.row) }}
        </template>
      </el-table-column>

      <el-table-column
        :label="t('system.tenant.merchantStatus')"
        align="center"
        prop="status"
      >
        <template #default="scope">
          <el-switch
            v-if="isSuperAdmin"
            v-model="scope.row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
          <dict-tag
            v-else
            :type="DICT_TYPE.COMMON_STATUS"
            :value="scope.row.status"
          />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('system.tenant.operateStatus')"
        align="center"
        prop="operateStatus"
      >
        <template #default="scope">
          <el-switch
            v-if="isSuperAdmin"
            v-model="scope.row.operateStatus"
            :active-value="0"
            :inactive-value="1"
            @change="handleOperateStatusChange(scope.row)"
          />
          <dict-tag
            v-else
            :type="DICT_TYPE.COMMON_STATUS"
            :value="scope.row.operateStatus"
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
      <el-table-column
        :label="t('common.updateTime')"
        align="center"
        prop="updateTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        min-width="280"
        fixed="right"
      >
        <template #default="scope">
          <!-- <el-button link type="primary" @click="openPermissionsForm('update', scope.row.packageId)">
            权限配置
          </el-button> -->
          <el-button
            v-if="isRootList"
            link
            type="primary"
            @click="handleViewChildAgents(scope.row.id)"
            v-hasPermi="['system:tenant:agent']"
          >
            {{ t('system.tenant.viewChildAgent') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="openGameForm('update', scope.row.id)"
            v-hasPermi="['system:tenant:game']"
          >
            {{ t('system.tenant.gameConfig') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="openIpForm('update', scope.row.id)"
            v-hasPermi="['system:tenant:ip']"
          >
            {{ t('system.tenant.updateWhiteListIp') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['system:tenant:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['system:tenant:delete']"
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
  <TenantForm ref="formRef" @success="getList" />

  <!-- 表单弹窗：权限配置 -->
  <TenantPackageForm ref="PermissionsRef" @success="getList" />
  <GameconfigurationForm ref="GameFormRef" />
  <TenantIpForm ref="IpFormRef" @success="getList" />

  <!-- 查看私钥 -->
  <Dialog
    v-model="secretKeyDialogVisible"
    :title="t('system.tenant.secretKey')"
    width="520px"
  >
    <div class="secret-key-text">{{ secretKeyDialogValue }}</div>
    <template #footer>
      <el-button @click="secretKeyDialogVisible = false">{{
        t('common.close')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { dateFormatter } from '@/utils/formatTime'
  import download from '@/utils/download'
  import { useUserStore } from '@/store/modules/user'
  import { getTenantId } from '@/utils/auth'
  import * as TenantApi from '@/api/system/tenant'
  import * as TenantPackageApi from '@/api/system/tenantPackage'
  import TenantForm from './TenantForm.vue'
  import GameconfigurationForm from './GameconfigurationForm.vue'
  import TenantIpForm from './TenantIpForm.vue'
  import TenantPackageForm from './TenantPackageForm.vue'

  defineOptions({ name: 'SystemTenant' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化
  const userStore = useUserStore()

  const loading = ref(true) // 列表的加载中
  const total = ref(0) // 列表的总页数
  const list = ref([]) // 列表的数据
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    name: undefined,
    contactName: undefined,
    contactMobile: undefined,
    status: undefined,
    operateStatus: undefined as number | undefined,
    createTime: [],
    code: undefined,
    parentId: undefined as number | undefined
  })

  /** 是否超级管理员 */
  const isSuperAdmin = computed(() =>
    userStore.getRoles.includes('super_admin')
  )

  /** 根级列表的 parentId：超管传 0（仅一级租户）；租户用户传当前租户 id */
  const rootParentId = computed((): number | undefined => {
    if (isSuperAdmin.value) {
      return 0
    }
    const tenantId = getTenantId()
    if (tenantId == null || tenantId === '') {
      return undefined
    }
    const id = Number(tenantId)
    return id > 0 ? id : undefined
  })

  /** 是否在根级列表 */
  const isRootList = computed(() => queryParams.parentId === rootParentId.value)

  /** 应用根级 parentId（租户用户为当前租户 id） */
  const applyRootParentId = () => {
    queryParams.parentId = rootParentId.value
  }

  const queryFormRef = ref() // 搜索的表单
  const exportLoading = ref(false) // 导出的加载中
  const packageList = ref([] as TenantPackageApi.TenantPackageVO[]) // 租户套餐列表

  /** 套餐 id -> 名称 */
  const packageNameMap = computed(() => {
    const map = new Map<number, string>()
    packageList.value.forEach((item) => {
      if (item.id != null) {
        map.set(item.id, item.name)
      }
    })
    return map
  })

  type TenantListRow = TenantApi.TenantVO & {
    packageId?: number | string
    packageName?: string
    tenantPackageName?: string
    tenantPackage?: { name?: string }
    package?: { name?: string }
  }

  /** 列表展示套餐名称 */
  const getPackageName = (row: TenantListRow) => {
    const packageName =
      row.packageName ||
      row.tenantPackageName ||
      row.tenantPackage?.name ||
      row.package?.name
    if (packageName) {
      return packageName
    }
    if (row.packageId == null || row.packageId === '') {
      return '-'
    }
    const packageId = Number(row.packageId)
    if (!Number.isNaN(packageId)) {
      const name = packageNameMap.value.get(packageId)
      if (name) {
        return name
      }
    }
    return String(row.packageId)
  }

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await TenantApi.getTenantPage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  /** 修改状态（仅传 id、status） */
  const handleStatusChange = async (row: { id: number; status: number }) => {
    const prevStatus = row.status === 0 ? 1 : 0
    try {
      await TenantApi.updateTenantStatus({ id: row.id, status: row.status })
      message.success(t('common.updateSuccess'))
    } catch {
      row.status = prevStatus
    }
  }

  /** 修改运营状态（与商户状态同一接口，额外传 operateStatus） */
  const handleOperateStatusChange = async (row: {
    id: number
    status: number
    operateStatus: number
  }) => {
    const prevStatus = row.operateStatus === 0 ? 1 : 0
    try {
      await TenantApi.updateTenantStatus({
        id: row.id,
        status: row.status,
        operateStatus: row.operateStatus
      })
      message.success(t('common.updateSuccess'))
    } catch {
      row.operateStatus = prevStatus
    }
  }

  /** 查看私钥弹窗 */
  const secretKeyDialogVisible = ref(false)
  const secretKeyDialogValue = ref('')
  const openSecretKeyDialog = (row: TenantApi.TenantVO) => {
    secretKeyDialogValue.value = row.secretKey || ''
    secretKeyDialogVisible.value = true
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    queryFormRef.value.resetFields()
    applyRootParentId()
    handleQuery()
  }

  /** 查询子代理 */
  const handleViewChildAgents = (parentId: number) => {
    queryParams.parentId = parentId
    queryParams.pageNo = 1
    getList()
  }

  /** 返回根级列表 */
  const handleBackToParent = () => {
    applyRootParentId()
    queryParams.pageNo = 1
    getList()
  }

  /** 添加/修改操作 */
  const formRef = ref()
  const openForm = (type: string, id?: number) => {
    const parentId = type === 'create' ? queryParams.parentId : undefined
    formRef.value.open(type, id, parentId)
  }

  /** 权限配追操作 */
  const PermissionsRef = ref()
  const openPermissionsForm = (type: string, id?: number) => {
    PermissionsRef.value.open(type, id)
  }

  /** 游戏配追操作 */
  const GameFormRef = ref()
  const openGameForm = (type: string, id?: number) => {
    GameFormRef.value.open(type, id)
  }

  /** 白名单 */
  const IpFormRef = ref()
  const openIpForm = (type: string, id?: number) => {
    IpFormRef.value.open(type, id)
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起删除
      await TenantApi.deleteTenant(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 批量删除按钮操作 */
  const checkedIds = ref<number[]>([])
  const handleRowCheckboxChange = (rows: TenantApi.TenantVO[]) => {
    checkedIds.value = rows.map((row) => row.id)
  }

  const handleDeleteBatch = async () => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起批量删除
      await TenantApi.deleteTenantList(checkedIds.value)
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
      const data = await TenantApi.exportTenant(queryParams)
      download.excel(data, t('system.tenant.exportFile'))
    } catch {
    } finally {
      exportLoading.value = false
    }
  }

  /** 初始化 **/
  onMounted(async () => {
    if (!userStore.getIsSetUser) {
      await userStore.setUserInfoAction()
    }
    packageList.value = await TenantPackageApi.getTenantPackageList()
    applyRootParentId()
    await getList()
  })
</script>

<style scoped>
  .secret-key-text {
    font-size: 14px;
    line-height: 1.6;
    word-break: break-all;
  }
</style>
