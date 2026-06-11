<template>
  <TesterMapPage :load-map="getGameTesterMap" />
</template>

<script lang="ts" setup>
  import { getGameTesterMap } from '@/api/tester/map'
  import TesterMapPage from '../tester/TesterMapPage.vue'

  defineOptions({ name: 'ToolsGameTest' })
</script>
