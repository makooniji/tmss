<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item :label="t('dictData.dictType')" prop="type">
        <el-input
          v-model="formData.dictType"
          :disabled="typeof formData.id !== 'undefined'"
          :placeholder="t('dictData.dictTypePlaceholder')"
        />
      </el-form-item>
      <el-form-item :label="t('dictData.dataLabel')" prop="lang">
        <LangJsonForm
          v-model="formData.lang"
          field-key="label"
          :button-text="t('dictData.langButton')"
          :dialog-title="t('dictData.dataLabelDialogTitle')"
          :placeholder="t('dictData.dataLabelPlaceholder')"
          @update:model-value="onLangJsonUpdate"
        />
      </el-form-item>
      <el-form-item :label="t('dictData.dataValue')" prop="value">
        <el-input
          v-model="formData.value"
          :placeholder="t('dictData.dataValuePlaceholder')"
        />
      </el-form-item>
      <el-form-item :label="t('dictData.displaySort')" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
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
      <el-form-item :label="t('dictData.colorType')" prop="colorType">
        <el-select v-model="formData.colorType">
          <el-option
            v-for="item in colorTypeOptions"
            :key="item.value"
            :label="item.label + '(' + item.value + ')'"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('dictData.cssClass')" prop="cssClass">
        <el-input
          v-model="formData.cssClass"
          :placeholder="t('dictData.cssClassPlaceholder')"
        />
      </el-form-item>
      <el-form-item :label="t('form.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('dictData.remarkPlaceholder')"
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
  import * as DictDataApi from '@/api/system/dict/dict.data'
  import { CommonStatusEnum } from '@/utils/constants'
  import LangJsonForm, {
    createLangJsonValidator,
    resolvePrimaryFieldFromLang
  } from '@/components/LangJsonForm'

  defineOptions({ name: 'SystemDictDataForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  const formData = ref({
    id: undefined,
    sort: undefined,
    label: '',
    lang: {} as Record<string, Record<string, string>>,
    value: '',
    dictType: '',
    status: CommonStatusEnum.ENABLE,
    colorType: '',
    cssClass: '',
    remark: ''
  })
  const formRules = reactive({
    lang: [
      {
        validator: createLangJsonValidator('label', {
          empty: t('dictData.langEmpty'),
          noneFilled: t('dictData.langNoneFilled')
        }),
        trigger: ['change', 'blur']
      }
    ],
    value: [
      { required: true, message: t('dictData.valueRequired'), trigger: 'blur' }
    ],
    sort: [
      { required: true, message: t('dictData.sortRequired'), trigger: 'blur' }
    ],
    status: [
      {
        required: true,
        message: t('dictData.statusRequired'),
        trigger: 'change'
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
    formData.value.label = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'label'
    )
    nextTick(() => formRef.value?.validateField('lang'))
  }

  const colorTypeOptions = computed(() => [
    { value: 'default', label: t('dictData.colorDefault') },
    { value: 'primary', label: t('dictData.colorPrimary') },
    { value: 'success', label: t('dictData.colorSuccess') },
    { value: 'info', label: t('dictData.colorInfo') },
    { value: 'warning', label: t('dictData.colorWarning') },
    { value: 'danger', label: t('dictData.colorDanger') }
  ])

  /** 打开弹窗 */
  const open = async (type: string, id?: number, dictType?: string) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    resetForm()
    if (dictType) {
      formData.value.dictType = dictType
    }
    // 修改时，设置数据
    if (id) {
      formLoading.value = true
      try {
        const row = await DictDataApi.getDictData(id)
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
    // 校验表单
    if (!formRef) return
    formData.value.label = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'label'
    )
    const valid = await formRef.value.validate()
    if (!valid) return
    // 提交请求
    formLoading.value = true
    try {
      const data = {
        ...(formData.value as unknown as DictDataApi.DictDataVO),
        label: resolvePrimaryFieldFromLang(formData.value.lang, 'label'),
        lang: formData.value.lang || {}
      } as DictDataApi.DictDataVO
      if (formType.value === 'create') {
        await DictDataApi.createDictData(data)
        message.success(t('common.createSuccess'))
      } else {
        await DictDataApi.updateDictData(data)
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
      sort: undefined,
      label: '',
      lang: {},
      value: '',
      dictType: '',
      status: CommonStatusEnum.ENABLE,
      colorType: '',
      cssClass: '',
      remark: ''
    }
    formRef.value?.resetFields()
  }
</script>
