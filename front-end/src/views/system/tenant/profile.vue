<template>
  <ContentWrap>
    <TenantProfile />
  </ContentWrap>
</template>

<script setup lang="ts">
  import TenantProfile from '@/views/Profile/components/TenantProfile.vue'

  defineOptions({ name: 'SystemTenantProfile' })
</script>
