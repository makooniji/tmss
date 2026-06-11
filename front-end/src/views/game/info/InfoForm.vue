<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('game.info.vendorCode')" prop="vendorCode">
        <el-select
          v-model="formData.vendorCode"
          :placeholder="t('game.info.selectVendorCode')"
        >
          <el-option
            v-for="item in vendorCodeList"
            :key="item.id"
            :label="item.vendorCode"
            :value="item.vendorCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.gameName')" prop="lang">
        <LangJsonForm
          v-model="formData.lang"
          field-key="gameName"
          :button-text="t('game.info.langGameNameButton')"
          :dialog-title="t('game.info.gameNameDialogTitle')"
          :placeholder="t('game.info.placeholderGameName')"
          @update:model-value="onLangJsonUpdate"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.gameCode')" prop="gameCode">
        <el-input
          v-model="formData.gameCode"
          :placeholder="t('game.info.placeholderGameCode')"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.category')" prop="cateId">
        <el-select
          v-model="formData.cateId"
          :placeholder="t('game.info.selectCategory')"
          @change="handleCateIdChange"
        >
          <el-option
            v-for="item in GameTypeList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.subCategory')" prop="subCateId">
        <el-select
          v-model="formData.subCateId"
          :placeholder="t('game.info.selectSubCategory')"
          :disabled="!formData.cateId"
        >
          <el-option
            v-for="item in categorySubList"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.currency')" prop="supportCurrency">
        <el-select
          v-model="formData.supportCurrency"
          :placeholder="t('game.info.selectCurrency')"
          multiple
          clearable
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_CURRENCY_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.lang')" prop="supportLang">
        <el-select
          v-model="formData.supportLang"
          :placeholder="t('game.info.selectLang')"
          multiple
          clearable
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.SYSTEM_LANG)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('game.info.cover')" prop="logo">
        <UploadImg v-model="formData.logo" />
      </el-form-item>
      <el-form-item :label="t('game.info.sort')" prop="sort">
        <el-input
          v-model="formData.sort"
          :placeholder="t('game.info.placeholderSort')"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.maintain')" prop="maintain">
        <el-switch
          v-model="formData.maintain"
          :active-value="1"
          :inactive-value="0"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.status')" prop="status">
        <el-switch
          v-model="formData.status"
          :active-value="0"
          :inactive-value="1"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.onlineCount')" prop="onlineCount">
        <el-input
          v-model="formData.onlineCount"
          :placeholder="t('game.info.placeholderOnlineCount')"
        />
      </el-form-item>
      <el-form-item :label="t('game.info.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('game.info.placeholderRemark')"
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
  import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
  import { InfoApi, Info } from '@/api/game/info'
  import { CategorySubApi, CategorySub } from '@/api/game/categorysub'
  import LangJsonForm, {
    createLangJsonValidator,
    resolvePrimaryFieldFromLang
  } from '@/components/LangJsonForm'

  /** 游戏信息 表单 */
  defineOptions({ name: 'InfoForm' })

  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  const formData = ref({
    id: undefined,
    vendorCode: undefined,
    gameName: undefined as string | undefined,
    lang: {} as Record<string, Record<string, string>>,
    gameCode: undefined,
    cateId: undefined,
    subCateId: undefined,
    supportCurrency: undefined,
    supportLang: undefined,
    logo: undefined,
    sort: undefined,
    maintain: undefined,
    onlineCount: undefined,
    remark: undefined
  })
  const formRules = reactive({
    maintain: [
      {
        required: true,
        message: t('game.common.maintainRequired'),
        trigger: 'blur'
      }
    ],
    lang: [
      {
        validator: createLangJsonValidator('gameName', {
          empty: t('game.common.gameNameLangEmpty'),
          noneFilled: t('game.common.gameNameLangNoneFilled')
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
    formData.value.gameName = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'gameName'
    )
    nextTick(() => formRef.value?.validateField('lang'))
  }

  const handleCateIdChange = async (cateId: number) => {
    if (!cateId) {
      categorySubList.value = []
      return
    }
    try {
      const data = await CategorySubApi.getCategorySubPage({
        pageNo: 1,
        pageSize: 200,
        cateId: cateId
      })
      categorySubList.value = data.list
    } catch (error) {
      categorySubList.value = []
    }
  }

  interface GameType {
    id: number
    title: string
  }

  interface CodeList {
    id: number
    vendorCode: string
  }

  const props = defineProps({
    GameTypeList: {
      type: Array as () => GameType[],
      required: true
    },
    vendorCodeList: {
      type: Array as () => CodeList[],
      required: true
    }
  })

  const categorySubList = ref<CategorySub[]>([])

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
        const row = await InfoApi.getInfo(id)
        formData.value = {
          ...row,
          lang: parseLang(row.lang)
        }
        if (row.cateId) {
          await handleCateIdChange(Number(row.cateId))
        }
        // 如果有小类ID，获取小类信息并添加到列表中用于回显
        if (row.subCateId) {
          const subCate = await CategorySubApi.getCategorySub(
            Number(row.subCateId)
          )
          if (
            subCate &&
            !categorySubList.value.find((item) => item.id === subCate.id)
          ) {
            categorySubList.value.push(subCate)
          }
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
    formData.value.gameName = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'gameName'
    )
    // 校验表单
    await formRef.value.validate()
    // 提交请求
    formLoading.value = true
    try {
      const data = {
        ...(formData.value as unknown as Info),
        gameName: resolvePrimaryFieldFromLang(formData.value.lang, 'gameName'),
        lang: formData.value.lang || {}
      } as Info
      if (formType.value === 'create') {
        await InfoApi.createInfo(data)
        message.success(t('common.createSuccess'))
      } else {
        await InfoApi.updateInfo(data)
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
      gameName: undefined as string | undefined,
      lang: {},
      gameCode: undefined,
      cateId: undefined,
      subCateId: undefined,
      supportCurrency: undefined,
      supportLang: undefined,
      logo: undefined,
      sort: undefined,
      maintain: undefined,
      onlineCount: undefined,
      remark: undefined
    }
    categorySubList.value = []
    formRef.value?.resetFields()
  }
</script>
