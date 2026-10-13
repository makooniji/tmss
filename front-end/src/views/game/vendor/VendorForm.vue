<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
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
          :placeholder="t('game.vendor.selectCurrency')"
          multiple
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_CURRENCY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.vendor.categoryCode')" prop="categoryCode">
        <el-select
          v-model="formData.categoryCode"
          :placeholder="t('game.vendor.selectCategoryCode')"
          multiple
          clearable
          class="w-full"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.code"
            :label="item.title"
            :value="item.code"
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

      <el-divider content-position="left">子场馆配置</el-divider>

      <div
        v-for="(sub, index) in formData.subVendors"
        :key="index"
        class="sub-vendor-card"
      >
        <div class="sub-vendor-header">
          <span>子场馆 #{{ index + 1 }}</span>
          <el-button
            type="danger"
            link
            icon="Delete"
            @click="removeSubVendor(index)"
          >
            删除
          </el-button>
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="API URL"
              :prop="'subVendors.' + index + '.apiUrl'"
              :rules="{
                required: true,
                message: 'API URL不能为空',
                trigger: 'blur'
              }"
            >
              <el-input v-model="sub.apiUrl" placeholder="请输入 API URL" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="代理/商户号"
              :prop="'subVendors.' + index + '.vendorAgent'"
              :rules="{
                required: true,
                message: '代理号不能为空',
                trigger: 'blur'
              }"
            >
              <el-input
                v-model="sub.vendorAgent"
                placeholder="请输入代理/商户号"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="密钥 (Key)"
              :prop="'subVendors.' + index + '.vendorKey'"
              :rules="{
                required: true,
                message: '密钥不能为空',
                trigger: 'blur'
              }"
            >
              <el-input v-model="sub.vendorKey" placeholder="请输入密钥" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="账号"
              :prop="'subVendors.' + index + '.account'"
              :rules="{
                required: true,
                message: '账号不能为空',
                trigger: 'blur'
              }"
            >
              <el-input v-model="sub.account" placeholder="请输入账号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="币种"
              :prop="'subVendors.' + index + '.currency'"
              :rules="{
                required: true,
                message: '请选择币种',
                trigger: 'change'
              }"
            >
              <el-select v-model="sub.currency" placeholder="请选择币种">
                <el-option
                  v-for="dict in getStrDictOptions(
                    DICT_TYPE.SYSTEM_CURRENCY_TYPE
                  )"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <el-form-item label-width="0">
        <el-button
          type="primary"
          plain
          icon="Plus"
          class="w-full"
          @click="addSubVendor"
        >
          添加子场馆
        </el-button>
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
  import { CategoryApi, Category } from '@/api/game/category'

  /** 游戏厂商 表单 */
  defineOptions({ name: 'VendorForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中
  const formType = ref('') // 表单的类型

  // 1. 初始化 formData 中包含 subVendors 数组
  const formData = ref({
    id: undefined,
    vendorCode: undefined,
    vendorName: undefined,
    logo: undefined,
    currency: undefined,
    status: undefined,
    remark: undefined,
    categoryCode: [] as string[],
    subVendors: [] as any[] // 子场馆列表
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

  const categoryOptions = ref<Category[]>([])

  const parseCommaCodes = (value?: string | string[]) => {
    if (Array.isArray(value)) {
      return value.filter(Boolean)
    }
    return (value || '')
      .replace(/\[|\]/g, '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }

  const loadCategoryOptions = async () => {
    const data = await CategoryApi.getCategoryPage({
      pageNo: 1,
      pageSize: 200
    })
    categoryOptions.value = data?.list || []
  }

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
        const data = await VendorApi.getVendor(id)
        // 2. 确保回显时 subVendors 至少是一个空数组，防止报错
        formData.value = {
          ...data,
          currency: parseCommaCodes(
            data.currency as string | string[] | undefined
          ),
          categoryCode: parseCommaCodes(data.categoryCode),
          subVendors: data.subVendors || []
        }
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open }) // 提供 open 方法

  /** 添加子场馆 */
  const addSubVendor = () => {
    formData.value.subVendors.push({
      apiUrl: '',
      vendorAgent: '',
      vendorKey: '',
      account: '',
      currency: ''
    })
  }

  /** 删除子场馆 */
  const removeSubVendor = (index: number) => {
    formData.value.subVendors.splice(index, 1)
  }

  /** 提交表单 */
  const emit = defineEmits(['success'])
  const submitForm = async () => {
    // 校验表单（动态增加的子表单规则也会在此被触发检验）
    await formRef.value.validate()
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
      remark: undefined,
      subVendors: [] // 3. 重置时清空子场馆
    }
    formRef.value?.resetFields()
  }
  // 2. 在组件挂载时调用
  onMounted(() => {
    loadCategoryOptions()
  })
</script>

<style scoped>
  /* 增加的轻量样式，使子场馆配置块看起来更清晰 */
  .sub-vendor-card {
    padding: 15px 15px 0;
    margin-bottom: 15px;
    background-color: var(--el-fill-color-blank);
    border: 1px solid var(--el-border-color-light);
    border-radius: 4px;
  }

  .sub-vendor-header {
    display: flex;
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: bold;
    color: var(--el-text-color-secondary);
    justify-content: space-between;
    align-items: center;
  }

  .w-full {
    width: 100%;
  }
</style>
