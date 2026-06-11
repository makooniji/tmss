<template>
  <el-header class="doc-page-header" height="52px">
    <span class="doc-page-header__title">{{ title }}</span>
  </el-header>
</template>

<script lang="ts" setup>
  defineProps<{
    title?: string
  }>()
</script>

<style scoped lang="scss">
  .doc-page-header {
    display: flex;
    padding: 0 24px;
    background: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-lighter);
    align-items: center;
  }

  .doc-page-header__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
</style>
