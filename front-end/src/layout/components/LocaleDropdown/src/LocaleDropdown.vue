<!-- 多语言切换 -->
<script lang="ts" setup>
  import { useLocaleStore } from '@/store/modules/locale'
  import { useLocale } from '@/hooks/web/useLocale'
  import { propTypes } from '@/utils/propTypes'
  import { useDesign } from '@/hooks/web/useDesign'

  defineOptions({ name: 'LocaleDropdown' })

  const { getPrefixCls } = useDesign()

  const prefixCls = getPrefixCls('locale-dropdown')

  defineProps({
    color: propTypes.string.def('')
  })

  // 语言存储
  const localeStore = useLocaleStore()

  // 语言列表
  const langMap = computed(() => localeStore.getLocaleMap)

  // 当前语言
  const currentLang = computed(() => localeStore.getCurrentLocale)

  const setLang = async (lang: LocaleType) => {
    if (lang === unref(currentLang).lang) return
    const { changeLocale } = useLocale()
    await changeLocale(lang)
    window.location.reload()
  }
</script>

<template>
  <ElDropdown :class="prefixCls" trigger="click" @command="setLang">
    <Icon
      :class="$attrs.class"
      :color="color"
      :size="18"
      class="cursor-pointer !p-0"
      icon="ion:language-sharp"
    />
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem
          v-for="item in langMap"
          :key="item.lang"
          :command="item.lang"
        >
          {{ item.name }}
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>
