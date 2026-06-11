<template>
  <el-aside width="280px" class="doc-sidebar">
    <div class="doc-sidebar__inner">
      <h2 class="doc-sidebar__brand">API Docs</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索..."
        clearable
        :prefix-icon="Search"
        class="doc-sidebar__search"
      />
      <el-scrollbar class="doc-sidebar__scroll">
        <el-menu
          :default-active="activeKey"
          class="doc-sidebar__menu"
          @select="onSelect"
        >
          <el-menu-item v-for="m in filterMenus" :key="m.key" :index="m.key">
            {{ m.label }}
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </div>
  </el-aside>
</template>

<script lang="ts" setup>
  import { Search } from '@element-plus/icons-vue'
  import { computed, ref } from 'vue'

  export interface MenuItem {
    key: string
    label: string
  }

  const props = defineProps<{
    menus: MenuItem[]
    activeKey: string
  }>()

  const emit = defineEmits<{
    change: [key: string]
  }>()

  function onSelect(index: string) {
    emit('change', index)
  }

  const keyword = ref('')

  const filterMenus = computed(() => {
    const k = keyword.value.trim().toLowerCase()
    if (!k) return props.menus
    return props.menus.filter((m) => m.label.toLowerCase().includes(k))
  })
</script>

<style scoped lang="scss">
  .doc-sidebar {
    background: var(--el-bg-color);
    border-right: 1px solid var(--el-border-color-lighter);
  }

  .doc-sidebar__inner {
    display: flex;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    padding: 16px 12px;
    box-sizing: border-box;
  }

  .doc-sidebar__brand {
    margin: 0 0 12px 4px;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--el-text-color-primary);
  }

  .doc-sidebar__search {
    margin-bottom: 12px;
  }

  .doc-sidebar__scroll {
    flex: 1;
    min-height: 0;
  }

  .doc-sidebar__menu {
    border-right: none;
  }

  .doc-sidebar__menu :deep(.el-menu-item) {
    height: 42px;
    margin-bottom: 2px;
    line-height: 42px;
    border-radius: 6px;
  }

  .doc-sidebar__menu :deep(.el-menu-item.is-active) {
    font-weight: 600;
  }
</style>
