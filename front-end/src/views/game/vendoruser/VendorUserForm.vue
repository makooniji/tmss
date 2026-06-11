<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('game.common.vendorId')" prop="vendorId">
        <el-input
          v-model="formData.vendorId"
          :placeholder="t('game.common.placeholderVendorId')"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-input
          v-model="formData.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.userIdShort')" prop="userId">
        <el-input
          v-model="formData.userId"
          :placeholder="t('game.common.placeholderUserId')"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.usernameLabel')" prop="username">
        <el-input
          v-model="formData.username"
          :placeholder="t('game.common.placeholderUsername')"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="formData.status"
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
  import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
  import { VendorUserApi, VendorUser } from '@/api/game/vendoruser'

  defineOptions({ name: 'VendorUserForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: undefined,
    vendorId: undefined,
    vendorCode: undefined,
    userId: undefined,
    username: undefined,
    status: undefined
  })
  const formRules = computed(() => ({
    vendorId: [
      {
        required: true,
        message: t('game.common.msgVendorIdRequired'),
        trigger: 'blur'
      }
    ],
    vendorCode: [
      {
        required: true,
        message: t('game.common.msgVendorCodeRequired'),
        trigger: 'blur'
      }
    ],
    userId: [
      {
        required: true,
        message: t('game.common.msgUserIdRequired'),
        trigger: 'blur'
      }
    ],
    username: [
      {
        required: true,
        message: t('game.common.msgUsernameRequired'),
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: t('dictData.statusRequired'),
        trigger: 'change'
      }
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
        formData.value = await VendorUserApi.getVendorUser(id)
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
      const data = formData.value as unknown as VendorUser
      if (formType.value === 'create') {
        await VendorUserApi.createVendorUser(data)
        message.success(t('common.createSuccess'))
      } else {
        await VendorUserApi.updateVendorUser(data)
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
      vendorId: undefined,
      vendorCode: undefined,
      userId: undefined,
      username: undefined,
      status: undefined
    }
    formRef.value?.resetFields()
  }
</script>
