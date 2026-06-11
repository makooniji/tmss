<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('system.tenantdata.merchantCode')" prop="code">
        <p>{{ formData.code }}</p>
      </el-form-item>
      <el-form-item :label="t('system.tenantdata.merchantAccount')" prop="name">
        <p>{{ formData.name }}</p>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <dict-tag
          :type="DICT_TYPE.TENANT_STATUS"
          :value="formData.status || 0"
        />
      </el-form-item>
      <el-form-item
        :label="t('system.tenantdata.playerCount')"
        prop="accountCount"
      >
        <p>{{ formData.accountCount }}</p>
      </el-form-item>
      <el-form-item :label="t('system.tenantdata.openTime')" prop="createTime">
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

  /** 租户游戏日报 表单 */
  defineOptions({ name: 'TenantDayStaticForm' })

  const { t } = useI18n() // 国际化

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  // 显式声明 formData 的类型
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
  const formRef = ref() // 表单 Ref

  /** 打开弹窗 */
  const open = async (type: string, row?: Object) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type

    resetForm()

    formData.value = Object.assign({}, row)
  }
  defineExpose({ open }) // 提供 open 方法，用于打开弹窗

  /** 重置表单 */
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
