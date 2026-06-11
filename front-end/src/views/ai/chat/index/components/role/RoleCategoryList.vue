<template>
  <div class="flex flex-row flex-wrap items-center">
    <div
      class="flex flex-row mr-10px"
      v-for="category in categoryList"
      :key="category"
    >
      <el-button
        plain
        round
        size="small"
        :type="category === active ? 'primary' : ''"
        @click="handleCategoryClick(category)"
      >
        {{ formatCategoryLabel(category) }}
      </el-button>
    </div>
  </div>
</template>
<script setup lang="ts">
  import { PropType } from 'vue'

  const { t } = useI18n()
  /** 与接口约定：全部分类占位符 */
  const CATEGORY_ALL = '全部'

  // 定义属性
  defineProps({
    categoryList: {
      type: Array as PropType<string[]>,
      required: true
    },
    active: {
      type: String,
      required: false,
      // 不可引用本地变量，须与 CATEGORY_ALL 字面量一致
      default: '全部'
    }
  })

  const formatCategoryLabel = (category: string) =>
    category === CATEGORY_ALL ? t('aiChat.role.all') : category

  // 定义回调
  const emits = defineEmits(['onCategoryClick'])

  /** 处理分类点击事件 */
  const handleCategoryClick = async (category: string) => {
    emits('onCategoryClick', category)
  }
</script>
