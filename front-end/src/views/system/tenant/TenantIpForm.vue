<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      label-width="120px"
    >
      <el-form-item
        :label="t('system.tenant.profileWhiteListIp')"
        prop="ipAddressText"
      >
        <el-input
          v-model="ipAddressText"
          type="textarea"
          :rows="5"
          :placeholder="t('system.tenant.placeholderWhiteListIp')"
        />
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
  import * as TenantApi from '@/api/system/tenant'

  defineOptions({ name: 'SystemTenantIpForm' })

  const { t } = useI18n()
  const message = useMessage()
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formRef = ref()
  /** 与编辑接口相同的完整租户数据 */
  const formData = ref<Record<string, unknown>>({})
  /** 仅白名单可编辑 */
  const ipAddressText = ref('')

  const normalizeIpAddressText = (value: unknown) => {
    if (Array.isArray(value)) {
      return value.filter(Boolean).join('\n')
    }
    if (value == null || value === '') {
      return ''
    }
    return String(value).replace(/,/g, '\n')
  }

  const parseIpAddressList = (text: string) =>
    text
      .split(/[\n,]/)
      .map((item) => item.trim())
      .filter(Boolean)

  const open = async (_type: string, id?: number) => {
    dialogVisible.value = true
    dialogTitle.value = t('system.tenant.updateWhiteListIp')
    formData.value = {}
    ipAddressText.value = ''
    if (!id) return
    formLoading.value = true
    try {
      const data = await TenantApi.getTenant(id)
      formData.value = { ...data }
      const raw =
        (data as Record<string, unknown>).ipAddress ??
        (data as Record<string, unknown>).ip_address
      ipAddressText.value = normalizeIpAddressText(raw)
    } finally {
      formLoading.value = false
    }
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])

  const submitForm = async () => {
    if (formData.value.id == null) return
    formLoading.value = true
    try {
      const data = {
        ...formData.value,
        ipAddress: parseIpAddressList(ipAddressText.value)
      } as unknown as TenantApi.TenantVO
      await TenantApi.updateTenant(data)
      message.success(t('common.updateSuccess'))
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }
</script>
