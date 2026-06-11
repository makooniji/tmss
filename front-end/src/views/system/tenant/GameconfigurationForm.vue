<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
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
      <el-form-item :label="t('system.tenant.game')" prop="gameCodes">
        <el-select
          v-model="formData.gameCodes"
          multiple
          :placeholder="t('system.tenant.selectGame')"
        >
          <el-option
            v-for="item in gameOptions"
            :key="item.gameCode"
            :label="item.gameName"
            :value="item.gameCode"
          />
        </el-select>
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
  import { VendorApi } from '@/api/game/vendor'
  import { InfoApi } from '@/api/game/info'

  defineOptions({ name: 'SystemGameconfigurationForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗
  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  const formData = ref({
    id: undefined,
    tenantId: undefined,
    vendorCodes: [],
    gameCodes: []
  })
  const formRules = computed(() => ({
    vendorCodes: [
      {
        required: true,
        message: t('system.tenant.venueRequired'),
        trigger: 'change'
      }
    ],
    gameCodes: [
      {
        required: true,
        message: t('system.tenant.gameRequired'),
        trigger: 'change'
      }
    ]
  }))
  const formRef = ref() // 表单 Ref
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
        formData.value = await TenantApi.getGameConfig(id)
      } finally {
        formLoading.value = false
      }
    }
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
      const data = formData.value
      data.tenantId = data.id // 将 tenantId 设置为表单的 id
      await TenantApi.updateGameConfig(data)
      message.success(t('common.updateSuccess'))
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
      tenantId: undefined,
      vendorCodes: [],
      gameCodes: []
    }
    formRef.value?.resetFields()
  }
</script>
