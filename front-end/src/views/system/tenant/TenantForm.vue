<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="130px"
    >
      <el-form-item :label="t('system.tenant.formTenantName')" prop="name">
        <el-input
          v-model="formData.name"
          :placeholder="t('system.tenant.placeholderTenantName')"
        />
      </el-form-item>
      <el-form-item :label="t('system.tenant.formTenantCode')" prop="code">
        <el-input
          v-model="formData.code"
          :placeholder="t('system.tenant.placeholderTenantCode')"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.id === undefined"
        :label="t('system.tenant.username')"
        prop="username"
      >
        <el-input
          v-model="formData.username"
          :placeholder="t('system.tenant.placeholderUsername')"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.id === undefined"
        :label="t('system.tenant.password')"
        prop="password"
      >
        <el-input
          v-model="formData.password"
          :placeholder="t('system.tenant.placeholderPassword')"
          show-password
          type="password"
        />
      </el-form-item>

      <el-form-item :label="t('system.tenant.tenantPackage')" prop="packageId">
        <el-select
          v-model="formData.packageId"
          clearable
          :placeholder="t('system.tenant.selectTenantPackage')"
        >
          <el-option
            v-for="item in packageList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('system.tenant.currency')" prop="currency">
        <el-select
          v-model="formData.currency"
          :placeholder="t('system.tenant.selectCurrency')"
          multiple
          clearable
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_CURRENCY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('system.tenant.venue')" prop="vendorCodes">
        <el-select
          v-model="formData.vendorCodes"
          multiple
          :placeholder="t('system.tenant.selectVenue')"
        >
          <el-option
            v-for="item in vendorOptions"
            :key="item.vendorCode"
            :label="item.vendorName"
            :value="item.vendorCode"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('system.tenant.callbackUrl')" prop="notifyUrl">
        <el-input
          v-model="formData.notifyUrl"
          :placeholder="t('system.tenant.placeholderCallbackUrl')"
          class="w-full"
        />
      </el-form-item>
      <el-form-item
        :label="t('system.tenant.merchantStatusSwitch')"
        prop="status"
      >
        <el-switch
          v-model="formData.status"
          :active-value="0"
          :inactive-value="1"
        />
      </el-form-item>
      <el-form-item
        :label="t('system.tenant.operateStatus')"
        prop="operateStatus"
      >
        <el-switch
          v-model="formData.operateStatus"
          :active-value="0"
          :inactive-value="1"
        />
      </el-form-item>
      <el-form-item :label="t('system.tenant.implType')" prop="implType">
        <el-radio-group v-model="formData.implType">
          <el-radio :value="0">{{ t('system.tenant.implTypeFree') }}</el-radio>
          <el-radio :value="1">{{
            t('system.tenant.implTypeTransfer')
          }}</el-radio>
        </el-radio-group>
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
  import * as TenantPackageApi from '@/api/system/tenantPackage'
  import { VendorApi } from '@/api/game/vendor'
  defineOptions({ name: 'SystemTenantForm' })

  const { t } = useI18n()
  const message = useMessage()
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: undefined,
    parentId: undefined as number | undefined,
    name: undefined,
    code: undefined,
    packageId: undefined,
    contactName: undefined,
    contactMobile: undefined,
    accountCount: 0,
    expireTime: undefined,
    websites: [],
    status: 0,
    operateStatus: 0,
    implType: 0,
    username: undefined,
    password: undefined,
    notifyUrl: undefined,
    currency: []
  })

  interface VendorOption {
    vendorCode: string
    vendorName: string
  }

  const vendorOptions = ref<VendorOption[]>([])
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

    operateStatus: [
      {
        required: true,
        message: t('system.tenant.operateStatusRequired'),
        trigger: 'change'
      }
    ],

    implType: [
      {
        required: true,
        message: t('system.tenant.implTypeRequired'),
        trigger: 'change'
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

  const formRef = ref()

  const packageList = ref([] as TenantPackageApi.TenantPackageVO[])

  const queryParams = reactive({
    pageNo: 1,
    pageSize: 200
  })
  /** 打开弹窗 */

  const open = async (type: string, id?: number, parentId?: number) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    resetForm()

    if (type === 'create' && parentId != null) {
      formData.value.parentId = parentId
    }

    if (id) {
      formLoading.value = true
      try {
        formData.value = await TenantApi.getTenant(id)
      } finally {
        formLoading.value = false
      }
    }

    packageList.value = await TenantPackageApi.getTenantPackageList()
    const vendorList = await VendorApi.getVendorPage(queryParams)
    vendorOptions.value = vendorList?.list
  }

  defineExpose({ open })

  const emit = defineEmits(['success'])

  const submitForm = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate()
    if (!valid) return

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

      emit('success')
    } finally {
      formLoading.value = false
    }
  }

  const resetForm = () => {
    formData.value = {
      id: undefined,
      parentId: undefined,
      name: undefined,
      code: undefined,
      packageId: undefined,
      contactName: undefined,
      contactMobile: undefined,
      accountCount: 0,
      expireTime: undefined,
      websites: [],

      status: 0,
      operateStatus: 0,
      implType: 0,
      username: undefined,
      password: undefined,
      notifyUrl: undefined,
      currency: []
    }
    formRef.value?.resetFields()
  }
</script>
