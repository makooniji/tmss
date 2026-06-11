<template>
  <TesterMapPage :load-map="getSportsTesterMap" />
</template>

<script lang="ts" setup>
  import { getSportsTesterMap } from '@/api/tester/map'
  import TesterMapPage from '../tester/TesterMapPage.vue'

  defineOptions({ name: 'ToolsSportsTest' })
</script>
