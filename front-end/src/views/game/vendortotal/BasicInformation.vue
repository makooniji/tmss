<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('game.common.merchantCode')" prop="code">
        <p>{{ formData.code }}</p>
      </el-form-item>
      <el-form-item :label="t('game.common.merchantAccount')" prop="name">
        <p>{{ formData.name }}</p>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <dict-tag
          :type="DICT_TYPE.TENANT_STATUS"
          :value="formData.status || 0"
        />
      </el-form-item>
      <el-form-item :label="t('game.common.playerCount')" prop="accountCount">
        <p>{{ formData.accountCount }}</p>
      </el-form-item>
      <el-form-item :label="t('game.common.openTime')" prop="createTime">
        <p>{{ formatDate(formData.createTime) }}</p>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { DICT_TYPE } from '@/utils/dict'
  import { formatDate } from '@/utils/formatTime'

  defineOptions({ name: 'BasicInformation' })

  const { t } = useI18n()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref<{
    code?: string
    name?: string
    status?: string
    accountCount?: number
    createTime?: Date
  }>({
    code: undefined,
    name: undefined,
    status: undefined,
    accountCount: undefined,
    createTime: undefined
  })
  const formRules = reactive({})
  const formRef = ref()

  const open = async (type: string, row?: object) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    resetForm()
    formData.value = Object.assign({}, row)
  }
  defineExpose({ open })

  const resetForm = () => {
    formData.value = {
      code: undefined,
      name: undefined,
      status: undefined,
      accountCount: undefined,
      createTime: undefined
    }
    formRef.value?.resetFields()
  }
</script>
