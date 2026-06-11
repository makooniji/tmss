<!-- 多语言配置表单 -->
<script lang="ts" setup>
  import { InfoFilled } from '@element-plus/icons-vue'
  import { propTypes } from '@/utils/propTypes'
  import { getSystemLangDictOptions } from '../langUtils'

  defineOptions({ name: 'LangJsonForm' })

  const { t } = useI18n()

  const props = defineProps({
    /** 形如 { "zh-CN": { "name": "首页" }, "en-US": { "name": "home" } } */
    modelValue: propTypes.object.def(() => ({})),
    /** 各语言对象内的字段名，如菜单名称用 name */
    fieldKey: propTypes.string.def('name'),
    buttonText: propTypes.string.def(''),
    dialogTitle: propTypes.string.def(''),
    placeholder: propTypes.string.def(''),
    /** 内层弹窗 z-index，避免与外层 Dialog 重叠冲突 */
    dialogZIndex: propTypes.number.def(4000)
  })

  const emit = defineEmits<{
    'update:modelValue': [value: Record<string, Record<string, string>>]
  }>()

  const innerVisible = ref(false)
  /** 字典 value（语言标识） -> 输入框文本 */
  const localValues = ref<Record<string, string>>({})

  const langOptions = computed(() => getSystemLangDictOptions())

  const displayButtonText = computed(
    () => props.buttonText || t('langJson.configureDefault')
  )
  const displayDialogTitle = computed(
    () => props.dialogTitle || t('langJson.dialogTitleDefault')
  )
  const displayPlaceholder = computed(
    () => props.placeholder || t('langJson.inputPlaceholder')
  )

  /** 已配置语言摘要（用于气泡提示，每行一种语言） */
  const summaryText = computed(() => {
    const v = props.modelValue as Record<string, Record<string, string>>
    if (!v || typeof v !== 'object') return ''
    const parts: string[] = []
    for (const dict of langOptions.value) {
      const text = v[dict.value]?.[props.fieldKey]?.trim()
      if (text) {
        parts.push(
          t('langJson.summaryLine', {
            label: dict.label,
            code: dict.value,
            text
          })
        )
      }
    }
    return parts.length ? parts.join('\n') : ''
  })

  function openDialog() {
    const src = (props.modelValue || {}) as Record<
      string,
      Record<string, string>
    >
    const next: Record<string, string> = {}
    for (const dict of langOptions.value) {
      next[dict.value] = src[dict.value]?.[props.fieldKey] ?? ''
    }
    localValues.value = next
    innerVisible.value = true
  }

  function confirm() {
    const out: Record<string, Record<string, string>> = {}
    for (const dict of langOptions.value) {
      const raw = (localValues.value[dict.value] ?? '').trim()
      if (raw) {
        out[dict.value] = { [props.fieldKey]: raw }
      }
    }
    emit('update:modelValue', out)
    innerVisible.value = false
  }
</script>

<template>
  <div class="lang-json-form inline-flex flex-wrap items-center gap-8px">
    <el-button type="primary" plain @click="openDialog">{{
      displayButtonText
    }}</el-button>

    <!-- 显示多语言配置的摘要 -->
    <el-tooltip
      v-if="summaryText"
      placement="top"
      :show-after="300"
      popper-class="lang-json-form-summary-tooltip"
    >
      <template #content>
        <div class="max-w-320px text-13px leading-relaxed whitespace-pre-wrap">
          {{ summaryText }}
        </div>
      </template>
      <el-icon
        class="cursor-help align-middle text-[var(--el-color-info)]"
        :size="20"
      >
        <InfoFilled />
      </el-icon>
    </el-tooltip>

    <!-- 多语言配置弹窗 -->
    <el-dialog
      v-model="innerVisible"
      :title="displayDialogTitle"
      width="520px"
      append-to-body
      destroy-on-close
      :z-index="dialogZIndex"
      align-center
      class="lang-json-form-dialog"
    >
      <el-form label-width="100px">
        <el-form-item
          v-for="dict in langOptions"
          :key="dict.value"
          :label="dict.label"
        >
          <el-input
            v-model="localValues[dict.value]"
            clearable
            :placeholder="displayPlaceholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="innerVisible = false">{{
          t('common.cancel')
        }}</el-button>
        <el-button type="primary" @click="confirm">{{
          t('common.ok')
        }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
