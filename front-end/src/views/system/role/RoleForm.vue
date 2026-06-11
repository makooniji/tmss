<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item :label="t('role.roleName')" prop="name">
        <el-input
          v-model="formData.name"
          :placeholder="t('role.placeholderName')"
        />
      </el-form-item>
      <el-form-item :label="t('role.roleCode')" prop="code">
        <el-input
          v-model="formData.code"
          :placeholder="t('role.placeholderCode')"
        />
      </el-form-item>
      <el-form-item :label="t('role.displayOrder')" prop="sort">
        <el-input
          v-model="formData.sort"
          :placeholder="t('role.placeholderSort')"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="formData.status"
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
      <el-form-item :label="t('role.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('role.placeholderRemark')"
          type="textarea"
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
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { CommonStatusEnum } from '@/utils/constants'
  import * as RoleApi from '@/api/system/role'

  defineOptions({ name: 'SystemRoleForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: undefined,
    name: '',
    code: '',
    sort: undefined,
    status: CommonStatusEnum.ENABLE,
    remark: ''
  })
  const formRules = computed(() => ({
    name: [
      { required: true, message: t('role.nameRequired'), trigger: 'blur' }
    ],
    code: [
      { required: true, message: t('role.codeRequired'), trigger: 'change' }
    ],
    sort: [
      { required: true, message: t('role.sortRequired'), trigger: 'change' }
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
        formData.value = await RoleApi.getRole(id)
      } finally {
        formLoading.value = false
      }
    }
  }

  const resetForm = () => {
    formData.value = {
      id: undefined,
      name: '',
      code: '',
      sort: undefined,
      status: CommonStatusEnum.ENABLE,
      remark: ''
    }
    formRef.value?.resetFields()
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])
  const submitForm = async () => {
    if (!formRef) return
    const valid = await formRef.value.validate()
    if (!valid) return
    formLoading.value = true
    try {
      const data = formData.value as unknown as RoleApi.RoleVO
      if (formType.value === 'create') {
        await RoleApi.createRole(data)
        message.success(t('common.createSuccess'))
      } else {
        await RoleApi.updateRole(data)
        message.success(t('common.updateSuccess'))
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }
</script>
