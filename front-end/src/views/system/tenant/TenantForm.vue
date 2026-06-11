<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="130px">
      <el-form-item :label="t('system.tenant.formTenantName')" prop="name">
        <el-input v-model="formData.name" :placeholder="t('system.tenant.placeholderTenantName')" />
      </el-form-item>
      <el-form-item :label="t('system.tenant.formTenantCode')" prop="code">
        <el-input v-model="formData.code" :placeholder="t('system.tenant.placeholderTenantCode')" />
      </el-form-item>
      <el-form-item :label="t('system.tenant.tenantKey')" prop="code">
        <el-input v-model="formData.tenantKey" :placeholder="t('system.tenant.tenantKey')" />
      </el-form-item>
      <el-form-item :label="t('system.tenant.secretKey')" prop="code">
        <el-input v-model="formData.secretKey" :placeholder="t('system.tenant.secretKey')" />
      </el-form-item>
      <el-form-item v-if="formData.id === undefined" :label="t('system.tenant.username')" prop="username">
        <el-input v-model="formData.username" :placeholder="t('system.tenant.placeholderUsername')" />
      </el-form-item>
      <el-form-item v-if="formData.id === undefined" :label="t('system.tenant.password')" prop="password">
        <el-input v-model="formData.password" :placeholder="t('system.tenant.placeholderPassword')" show-password
          type="password" />
      </el-form-item>
      <el-form-item :label="t('system.tenant.currency')" prop="currency">
        <el-select v-model="formData.currency" :placeholder="t('system.tenant.selectCurrency')" multiple clearable>
          <el-option v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_CURRENCY_TYPE)" :key="dict.value"
            :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.tenant.venue')" prop="vendorCodes">
        <el-select v-model="formData.vendorCodes" multiple :placeholder="t('system.tenant.selectVenue')">
          <el-option v-for="item in vendorOptions" :key="item.vendorCode" :label="item.vendorName"
            :value="item.vendorCode" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.tenant.game')" prop="gameCodes">
        <el-select v-model="formData.gameCodes" multiple :placeholder="t('system.tenant.selectGame')">
          <el-option v-for="item in gameOptions" :key="item.gameCode" :label="item.gameName" :value="item.gameCode" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.tenant.callbackUrl')" prop="notifyUrl">
        <el-input v-model="formData.notifyUrl" :placeholder="t('system.tenant.placeholderCallbackUrl')"
          class="w-full" />
      </el-form-item>
      <el-form-item :label="t('system.tenant.merchantStatusSwitch')" prop="status">
        <el-switch v-model="formData.status" :active-value="0" :inactive-value="1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">{{
        t('common.ok')
      }}</el-button>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
import * as TenantApi from '@/api/system/tenant'
import { CommonStatusEnum } from '@/utils/constants'
import * as TenantPackageApi from '@/api/system/tenantPackage'
import { VendorApi } from '@/api/game/vendor'
import { InfoApi } from '@/api/game/info'
defineOptions({ name: 'SystemTenantForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  code: undefined,
  packageId: undefined,
  contactName: undefined,
  contactMobile: undefined,
  accountCount: 0,
  expireTime: undefined,
  websites: [],
  status: CommonStatusEnum.ENABLE,
  // 新增专属
  username: undefined,
  password: undefined,
  notifyUrl: undefined,
  currency: []
})
interface vendorOptions {
  vendorCode: string
  vendorName: string
}
interface GameOption {
  gameCode: string
  gameName: string
}
const vendorOptions = ref<vendorOptions[]>([])
const gameOptions = ref<GameOption[]>([])
const formRules = computed(() => ({
  name: [
    {
      required: true,
      message: t('system.tenant.formNameRequired'),
      trigger: 'blur'
    }
  ],
  code: [
    {
      required: true,
      message: t('system.tenant.formCodeRequired'),
      trigger: 'blur'
    }
  ],
  notifyUrl: [
    {
      required: true,
      message: t('system.tenant.notifyUrlRequired'),
      trigger: 'blur'
    }
  ],
  packageId: [
    {
      required: true,
      message: t('system.tenant.packageRequired'),
      trigger: 'blur'
    }
  ],
  status: [
    {
      required: true,
      message: t('system.tenant.tenantStatusRequired'),
      trigger: 'blur'
    }
  ],
  username: [
    {
      required: true,
      message: t('system.tenant.usernameRequired'),
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: t('system.tenant.passwordRequired'),
      trigger: 'blur'
    }
  ]
}))
const formRef = ref() // 表单 Ref
const packageList = ref([] as TenantPackageApi.TenantPackageVO[]) // 租户套餐
const queryParams = reactive({
  pageNo: 1,
  pageSize: 200
})
/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await TenantApi.getTenant(id)
    } finally {
      formLoading.value = false
    }
  }
  // 加载套餐列表
  packageList.value = await TenantPackageApi.getTenantPackageList()
  const vendorList = await VendorApi.getVendorPage(queryParams)
  vendorOptions.value = vendorList?.list

  const gameList = await InfoApi.getInfoPage(queryParams)
  gameOptions.value = gameList?.list
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as TenantApi.TenantVO
    if (formType.value === 'create') {
      await TenantApi.createTenant(data)
      message.success(t('common.createSuccess'))
    } else {
      await TenantApi.updateTenant(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    code: undefined,
    packageId: undefined,
    contactName: undefined,
    contactMobile: undefined,
    accountCount: 0,
    expireTime: undefined,
    websites: [],
    status: CommonStatusEnum.ENABLE,
    // 新增专属
    username: undefined,
    password: undefined,
    notifyUrl: undefined,
    currency: []
  }
  formRef.value?.resetFields()
}
</script>
