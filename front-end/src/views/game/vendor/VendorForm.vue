<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('game.vendor.vendorCode')" prop="vendorCode">
        <el-input
          v-model="formData.vendorCode"
          :placeholder="t('game.vendor.placeholderVendorCode')"
        />
      </el-form-item>
      <el-form-item :label="t('game.vendor.vendorName')" prop="vendorName">
        <el-input
          v-model="formData.vendorName"
          :placeholder="t('game.vendor.placeholderVendorName')"
        />
      </el-form-item>
      <el-form-item :label="t('game.vendor.cover')" prop="logo">
        <UploadImg v-model="formData.logo" />
      </el-form-item>
      <el-form-item :label="t('game.vendor.currency')" prop="currency">
        <el-select
          v-model="formData.currency"
          :placeholder="t('game.vendor.selectCurrency')" multiple
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
        <el-switch
          v-model="formData.status"
          :active-value="0"
          :inactive-value="1"
        />
      </el-form-item>
      <el-form-item :label="t('game.vendor.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('game.vendor.placeholderRemark')"
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
  import { getIntDictOptions, getStrDictOptions, DICT_TYPE } from '@/utils/dict'
  import { VendorApi, Vendor } from '@/api/game/vendor'

  /** 游戏厂商 表单 */
  defineOptions({ name: 'VendorForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  const formData = ref({
    id: undefined,
    vendorCode: undefined,
    vendorName: undefined,
    logo: undefined,
    currency: undefined,
    status: undefined,
    remark: undefined
  })
  const formRules = reactive({
    vendorCode: [
      {
        required: true,
        message: t('game.common.vendorCodeRequired'),
        trigger: 'blur'
      }
    ]
  })
  const formRef = ref() // 表单 Ref

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
        formData.value = await VendorApi.getVendor(id)
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open }) // 提供 open 方法，用于打开弹窗

  /** 提交表单 */
  const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
  const submitForm = async () => {
    // 校验表单
    await formRef.value.validate()
    // 提交请求
    formLoading.value = true
    try {
      const data = formData.value as unknown as Vendor
      if (formType.value === 'create') {
        await VendorApi.createVendor(data)
        message.success(t('common.createSuccess'))
      } else {
        await VendorApi.updateVendor(data)
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
      vendorCode: undefined,
      vendorName: undefined,
      logo: undefined,
      currency: undefined,
      status: undefined,
      remark: undefined
    }
    formRef.value?.resetFields()
  }
</script>
