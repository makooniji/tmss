<!-- 菜单表单  新增/修改 -->
<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item :label="t('menuForm.parentMenu')">
        <el-tree-select
          v-model="formData.parentId"
          :data="menuTree"
          :default-expanded-keys="[0]"
          :props="defaultProps"
          check-strictly
          node-key="id"
        />
      </el-form-item>
      <el-form-item :label="t('menuForm.menuNameLabel')" prop="lang">
        <LangJsonForm
          v-model="formData.lang"
          fieldKey="name"
          :button-text="t('menuForm.configureMenuName')"
          :dialog-title="t('menuForm.menuNameTitle')"
          :placeholder="t('menuForm.menuNamePlaceholder')"
          @update:model-value="onLangJsonUpdate"
        />
      </el-form-item>
      <el-form-item :label="t('menuForm.menuType')" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio-button
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_MENU_TYPE)"
            :key="dict.label"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.type !== 3" :label="t('menuForm.menuIcon')">
        <IconSelect v-model="formData.icon" clearable />
      </el-form-item>
      <el-form-item
        v-if="formData.type !== 3"
        :label="t('menuForm.routePath')"
        prop="path"
      >
        <template #label>
          <Tooltip
            :message="t('menuForm.routePathTooltip')"
            :title="t('menuForm.routePath')"
          />
        </template>
        <el-input
          v-model="formData.path"
          clearable
          :placeholder="t('menuForm.placeholderRoutePath')"
        />
      </el-form-item>
      <el-form-item
        v-if="formData.type === 2"
        :label="t('menuForm.componentPath')"
        prop="component"
      >
        <el-input
          v-model="formData.component"
          clearable
          :placeholder="t('menuForm.placeholderComponentPath')"
        />
      </el-form-item>
      <el-form-item
        v-if="formData.type === 2"
        :label="t('menuForm.componentNameLabel')"
        prop="componentName"
      >
        <el-input
          v-model="formData.componentName"
          clearable
          :placeholder="t('menuForm.placeholderComponentName')"
        />
      </el-form-item>
      <el-form-item
        v-if="formData.type !== 1"
        :label="t('menuForm.permission')"
        prop="permission"
      >
        <template #label>
          <Tooltip
            :message="t('menuForm.permissionTooltip')"
            :title="t('menuForm.permission')"
          />
        </template>
        <el-input
          v-model="formData.permission"
          clearable
          :placeholder="t('menuForm.placeholderPermission')"
        />
      </el-form-item>
      <el-form-item :label="t('menuForm.displaySort')" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          clearable
          controls-position="right"
        />
      </el-form-item>
      <el-form-item :label="t('menuForm.menuStatus')" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.label"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="formData.type !== 3"
        :label="t('menuForm.visibleStatus')"
        prop="visible"
      >
        <template #label>
          <Tooltip
            :message="t('menuForm.visibleTooltip')"
            :title="t('menuForm.visibleStatus')"
          />
        </template>
        <el-radio-group v-model="formData.visible">
          <el-radio key="true" :value="true" border>{{
            t('menuForm.show')
          }}</el-radio>
          <el-radio key="false" :value="false" border>{{
            t('menuForm.hide')
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="formData.type !== 3"
        :label="t('menuForm.alwaysShow')"
        prop="alwaysShow"
      >
        <template #label>
          <Tooltip
            :message="t('menuForm.alwaysShowTooltip')"
            :title="t('menuForm.alwaysShow')"
          />
        </template>
        <el-radio-group v-model="formData.alwaysShow">
          <el-radio key="true" :value="true" border>{{
            t('menuForm.always')
          }}</el-radio>
          <el-radio key="false" :value="false" border>{{
            t('menuForm.notAlways')
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="formData.type === 2"
        :label="t('menuForm.cacheStatus')"
        prop="keepAlive"
      >
        <template #label>
          <Tooltip
            :message="t('menuForm.cacheTooltip')"
            :title="t('menuForm.cacheStatus')"
          />
        </template>
        <el-radio-group v-model="formData.keepAlive">
          <el-radio key="true" :value="true" border>{{
            t('menuForm.cache')
          }}</el-radio>
          <el-radio key="false" :value="false" border>{{
            t('menuForm.noCache')
          }}</el-radio>
        </el-radio-group>
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
  import * as MenuApi from '@/api/system/menu'
  import LangJsonForm, {
    createLangJsonValidator,
    resolvePrimaryFieldFromLang
  } from '@/components/LangJsonForm'
  import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
  import { CommonStatusEnum, SystemMenuTypeEnum } from '@/utils/constants'
  import { defaultProps, handleTree } from '@/utils/tree'

  defineOptions({ name: 'SystemMenuForm' })

  const { wsCache } = useCache()
  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const dialogVisible = ref(false) // 弹窗的是否展示
  const dialogTitle = ref('') // 弹窗的标题
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formType = ref('') // 表单的类型：create - 新增；update - 修改
  /** 多语言结构：{ "zh-CN": { "name": "..." }, "en-US": { "name": "..." } } */
  const formData = ref({
    id: undefined,
    name: '',
    lang: {} as Record<string, Record<string, string>>,
    permission: '',
    type: SystemMenuTypeEnum.DIR,
    sort: Number(undefined),
    parentId: 0,
    path: '',
    icon: '',
    component: '',
    componentName: '',
    status: CommonStatusEnum.ENABLE,
    visible: true,
    keepAlive: true,
    alwaysShow: true
  })
  const formRules = computed(() => ({
    lang: [
      {
        validator: createLangJsonValidator('name', {
          empty: t('menuForm.langEmpty'),
          noneFilled: t('menuForm.langNoneFilled')
        }),
        trigger: ['change', 'blur']
      }
    ],
    type: [
      { required: true, message: t('menuForm.typeRequired'), trigger: 'blur' }
    ],
    sort: [
      { required: true, message: t('menuForm.sortRequired'), trigger: 'blur' }
    ],
    path: [
      { required: true, message: t('menuForm.pathRequired'), trigger: 'blur' }
    ],
    status: [
      { required: true, message: t('dictData.statusRequired'), trigger: 'blur' }
    ]
  }))
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
    formData.value.name = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'name'
    )
    nextTick(() => formRef.value?.validateField('lang'))
  }

  /** 打开弹窗 */
  const open = async (type: string, id?: number, parentId?: number) => {
    dialogVisible.value = true
    dialogTitle.value = t('action.' + type)
    formType.value = type
    resetForm()
    if (parentId) {
      formData.value.parentId = parentId
    }
    // 修改时，设置数据
    if (id) {
      formLoading.value = true
      try {
        const row = await MenuApi.getMenu(id)
        formData.value = {
          ...row,
          lang: parseLang(row.lang)
        }
      } finally {
        formLoading.value = false
      }
    }
    // 获得菜单列表
    await getTree()
  }
  defineExpose({ open }) // 提供 open 方法，用于打开弹窗

  /** 提交表单 */
  const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
  const submitForm = async () => {
    // 校验表单
    if (!formRef) return
    formData.value.name = resolvePrimaryFieldFromLang(
      formData.value.lang,
      'name'
    )
    const valid = await formRef.value.validate()
    if (!valid) return
    // 提交请求
    formLoading.value = true
    try {
      if (
        formData.value.type === SystemMenuTypeEnum.DIR ||
        formData.value.type === SystemMenuTypeEnum.MENU
      ) {
        if (!isExternal(formData.value.path)) {
          if (
            formData.value.parentId === 0 &&
            formData.value.path.charAt(0) !== '/'
          ) {
            message.error(t('menuForm.pathMustStartWithSlash'))
            return
          } else if (
            formData.value.parentId !== 0 &&
            formData.value.path.charAt(0) === '/'
          ) {
            message.error(t('menuForm.pathMustNotStartWithSlash'))
            return
          }
        }
      }
      const data = {
        ...(formData.value as unknown as MenuApi.MenuVO),
        name: resolvePrimaryFieldFromLang(formData.value.lang, 'name'),
        lang: formData.value.lang || {}
      } as MenuApi.MenuVO
      if (formType.value === 'create') {
        await MenuApi.createMenu(data)
        message.success(t('common.createSuccess'))
      } else {
        await MenuApi.updateMenu(data)
        message.success(t('common.updateSuccess'))
      }
      dialogVisible.value = false
      // 发送操作成功的事件
      emit('success')
    } finally {
      formLoading.value = false
      // 清空，从而触发刷新
      wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
    }
  }

  /** 获取下拉框[上级菜单]的数据  */
  const menuTree = ref<Tree[]>([]) // 树形结构
  const getTree = async () => {
    menuTree.value = []
    const res = await MenuApi.getSimpleMenusList()
    let menu: Tree = { id: 0, name: t('menuForm.rootCategory'), children: [] }
    menu.children = handleTree(res)
    menuTree.value.push(menu)
  }

  /** 重置表单 */
  const resetForm = () => {
    formData.value = {
      id: undefined,
      name: '',
      lang: {},
      permission: '',
      type: SystemMenuTypeEnum.DIR,
      sort: Number(undefined),
      parentId: 0,
      path: '',
      icon: '',
      component: '',
      componentName: '',
      status: CommonStatusEnum.ENABLE,
      visible: true,
      keepAlive: true,
      alwaysShow: true
    }
    formRef.value?.resetFields()
  }

  /** 判断 path 是不是外部的 HTTP 等链接 */
  const isExternal = (path: string) => {
    return /^(https?:|mailto:|tel:)/.test(path)
  }
</script>
