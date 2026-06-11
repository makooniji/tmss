<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="150px" v-loading="formLoading">



      <el-form-item :label="t('game.categoryForm.title')" prop="lang">
        <LangJsonForm v-model="formData.lang" field-key="title" :button-text="t('game.categoryForm.langTitleButton')"
          :dialog-title="t('game.categoryForm.langTitleDialog')" :placeholder="t('game.categoryForm.placeholderTitle')"
          @update:model-value="onLangJsonUpdate" />
      </el-form-item>
      <el-form-item :label="t('game.categoryForm.code')" prop="code">
        <el-input v-model="formData.code" :placeholder="t('game.categoryForm.placeholderCode')" />
      </el-form-item>
      <el-form-item :label="t('game.categoryForm.logo')" prop="logo">
        <UploadImg v-model="formData.logo" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-switch v-model="formData.status" :active-value="0" :inactive-value="1" />
      </el-form-item>
      <el-form-item :label="t('game.categoryForm.sort')" prop="sort">
        <el-input v-model="formData.sort" :placeholder="t('game.categoryForm.placeholderSort')" />
      </el-form-item>

      <el-form-item :label="t('game.categorySub.parentCateName')" prop="cateName">
        <el-select v-model="formData.cateId" :placeholder="t('game.categorySub.selectGameCategory')" clearable>
          <el-option v-for="item in GameTypeList" :key="item.id" :label="item.title" :value="item.id" />
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
import { CategorySubApi, CategorySub } from '@/api/game/categorysub'
import LangJsonForm, {
  createLangJsonValidator,
  resolvePrimaryFieldFromLang
} from '@/components/LangJsonForm'

/** 游戏分类 表单 */
defineOptions({ name: 'CategorySubForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
type CategorySubFormModel = {
  id?: number
  title?: string
  code?: string
  logo?: string
  status?: number
  sort?: number
  lang: Record<string, Record<string, string>>
  cateName?: string
  cateId?: number
}
const formData = ref<CategorySubFormModel>({
  id: undefined,
  title: undefined,
  code: undefined,
  logo: undefined,
  status: undefined,
  sort: undefined,
  lang: {},
  cateName: undefined,
  cateId: undefined
})
const formRules = reactive({
  lang: [
    {
      validator: createLangJsonValidator('title', {
        empty: t('game.categoryForm.titleLangEmpty'),
        noneFilled: t('game.categoryForm.titleLangNoneFilled')
      }),
      trigger: ['change', 'blur']
    }
  ]
})
const formRef = ref() // 表单 Ref

function parseLang(raw: unknown): Record<string, Record<string, string>> {
  if (!raw) return {}
  if (typeof raw === 'object' && raw !== null && !Array.isArray(raw)) {
    return raw as Record<string, Record<string, string>>
  }
  if (typeof raw === 'string') {
    try {
      const o = JSON.parse(raw) as unknown
      return typeof o === 'object' && o !== null && !Array.isArray(o)
        ? (o as Record<string, Record<string, string>>)
        : {}
    } catch {
      return {}
    }
  }
  return {}
}

const onLangJsonUpdate = () => {
  formData.value.title = resolvePrimaryFieldFromLang(
    formData.value.lang,
    'title'
  )
  nextTick(() => formRef.value?.validateField('lang'))
}

interface GameType {
  id: number
  title: string
}
const props = defineProps({
  GameTypeList: {
    type: Array as () => GameType[],
    required: true
  }
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
      const row = await CategorySubApi.getCategorySub(id)
      formData.value = {
        ...row,
        lang: parseLang(row.lang)
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  formData.value.title = resolvePrimaryFieldFromLang(
    formData.value.lang,
    'title'
  )
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = {
      ...(formData.value as unknown as CategorySub),
      title: resolvePrimaryFieldFromLang(formData.value.lang, 'title'),
      lang: formData.value.lang || {}
    } as unknown as CategorySub
    if (formType.value === 'create') {
      await CategorySubApi.createCategorySub(data)
      message.success(t('common.createSuccess'))
    } else {
      await CategorySubApi.updateCategorySub(data)
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
    title: undefined,
    code: undefined,
    logo: undefined,
    status: undefined,
    sort: undefined,
    lang: {},
    cateName: undefined,
    cateId: undefined
  }
  formRef.value?.resetFields()
}
</script>
