<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('walletRechargePackage.packageName')" prop="name">
        <el-input
          v-model="formData.name"
          :placeholder="t('walletRechargePackage.placeholderPackageName')"
        />
      </el-form-item>
      <el-form-item
        :label="t('walletRechargePackage.payPriceYuan')"
        prop="payPrice"
      >
        <el-input-number
          v-model="formData.payPrice"
          :min="0"
          :precision="2"
          :step="0.01"
        />
      </el-form-item>
      <el-form-item
        :label="t('walletRechargePackage.bonusPriceYuan')"
        prop="bonusPrice"
      >
        <el-input-number
          v-model="formData.bonusPrice"
          :min="0"
          :precision="2"
          :step="0.01"
        />
      </el-form-item>
      <el-form-item
        :label="t('walletRechargePackage.openStatus')"
        prop="status"
      >
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
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
  import * as WalletRechargePackageApi from '@/api/pay/wallet/rechargePackage'
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { fenToYuan, yuanToFen } from '@/utils'

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: undefined,
    name: undefined,
    payPrice: undefined,
    bonusPrice: undefined,
    status: undefined
  })
  const formRules = computed(() => ({
    name: [
      {
        required: true,
        message: t('walletRechargePackage.nameRequired'),
        trigger: 'blur'
      }
    ],
    payPrice: [
      {
        required: true,
        message: t('walletRechargePackage.payPriceRequired'),
        trigger: 'blur'
      }
    ],
    bonusPrice: [
      {
        required: true,
        message: t('walletRechargePackage.bonusPriceRequired'),
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
        formData.value =
          await WalletRechargePackageApi.getWalletRechargePackage(id)
        formData.value.payPrice = fenToYuan(formData.value.payPrice)
        formData.value.bonusPrice = fenToYuan(formData.value.bonusPrice)
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])
  const submitForm = async () => {
    if (!formRef) return
    const valid = await formRef.value.validate()
    if (!valid) return
    formLoading.value = true
    try {
      const data = { ...formData.value }
      data.payPrice = yuanToFen(data.payPrice)
      data.bonusPrice = yuanToFen(data.bonusPrice)
      if (formType.value === 'create') {
        await WalletRechargePackageApi.createWalletRechargePackage(data)
        message.success(t('common.createSuccess'))
      } else {
        await WalletRechargePackageApi.updateWalletRechargePackage(data)
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
      name: undefined,
      payPrice: undefined,
      bonusPrice: undefined,
      status: undefined
    }
    formRef.value?.resetFields()
  }
</script>
