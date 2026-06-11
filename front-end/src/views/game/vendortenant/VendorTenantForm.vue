<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('game.common.vendorCodeGame')" prop="vendorCode">
        <el-input
          v-model="formData.vendorCode"
          :placeholder="t('game.common.placeholderVendorCodeGame')"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.vendorShortName')" prop="vendorName">
        <el-input
          v-model="formData.vendorName"
          :placeholder="t('game.common.placeholderVendorNameShort')"
        />
      </el-form-item>

      <el-form-item :label="t('game.common.langLabel')" prop="supportLang">
        <el-select
          v-model="formData.supportLang"
          :placeholder="t('game.common.placeholderLang')"
          multiple
          clearable
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_LANG)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('system.tenant.currency')" prop="currency">
        <el-select
          v-model="formData.currency"
          multiple
          clearable
          :placeholder="t('system.tenant.selectCurrency')"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_CURRENCY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('game.vendor.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('game.vendor.placeholderRemark')"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.rtpInput')" prop="rtp">
        <el-input
          v-model="formData.rtp"
          :placeholder="t('game.common.placeholderRtp')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">{{
        t('common.ok')
      }}</el-button>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { getIntDictOptions, DICT_TYPE, getStrDictOptions } from '@/utils/dict'
  import { VendorTenantApi, VendorTenant } from '@/api/game/vendortenant'

  defineOptions({ name: 'VendorTenantForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: undefined,
    vendorCode: undefined,
    vendorName: undefined,
    vendorChannel: undefined,
    vendorAgent: undefined,
    vendorKey: undefined,
    vendorZone: undefined,
    lobbyUrl: undefined,
    apiUrl: undefined,
    logo: undefined,
    currency: undefined,
    supportLang: undefined,
    status: undefined,
    remark: undefined,
    rtp: undefined,
    callbackUrl: undefined,
    reportUrl: undefined
  })
  const formRules = computed(() => ({
    vendorCode: [
      {
        required: true,
        message: t('game.common.vendorCodeRequired'),
        trigger: 'blur'
      }
    ],
    status: [
      { required: true, message: t('dictData.statusRequired'), trigger: 'blur' }
    ]
  }))
  const formRef = ref()

  const open = async (type: string, id?: number) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    resetForm()
    if (id) {
      formLoading.value = true
      try {
        const res = await VendorTenantApi.getVendorTenant(id)

        formData.value = res

        // 在这里处理 currency 回显
        formData.value.currency = (res.currency || '')
          .replace(/\[|\]/g, '')
          .split(',')
          .map((i) => i.trim())
          .filter(Boolean)
        // 在这里处理 supportLang 回显
        formData.value.supportLang = (res.supportLang || '')
          .replace(/\[|\]/g, '')
          .split(',')
          .map((i) => i.trim())
          .filter(Boolean)
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])
  const submitForm = async () => {
    await formRef.value.validate()
    formLoading.value = true
    try {
      const data = formData.value as unknown as VendorTenant
      if (formType.value === 'create') {
        await VendorTenantApi.createVendorTenant(data)
        message.success(t('common.createSuccess'))
      } else {
        await VendorTenantApi.updateVendorTenant(data)
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
      vendorCode: undefined,
      vendorName: undefined,
      vendorChannel: undefined,
      vendorAgent: undefined,
      vendorKey: undefined,
      vendorZone: undefined,
      lobbyUrl: undefined,
      apiUrl: undefined,
      logo: undefined,
      currency: undefined,
      supportLang: undefined,
      status: undefined,
      remark: undefined,
      rtp: undefined,
      callbackUrl: undefined,
      reportUrl: undefined
    }
    formRef.value?.resetFields()
  }
</script>
