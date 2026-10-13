<template>
  <!-- TODO @芋艿：可优化，对标 vben 版本 -->
  <div class="flex">
    <el-card class="user w-1/3" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ t('profile.user.title') }}</span>
        </div>
      </template>
      <ProfileUser ref="profileUserRef" />
    </el-card>
    <el-card class="user ml-3 w-2/3" shadow="hover">
      <div>
        <el-tabs v-model="activeName" class="profile-tabs" tab-position="top">
          <el-tab-pane :label="t('profile.info.basicInfo')" name="basicInfo">
            <BasicInfo @success="handleBasicInfoSuccess" />
          </el-tab-pane>
          <el-tab-pane
            :label="t('profile.info.tenantProfile')"
            name="tenantProfile"
          >
            <TenantProfile />
          </el-tab-pane>
          <el-tab-pane :label="t('profile.info.resetPwd')" name="resetPwd">
            <ResetPwd />
          </el-tab-pane>
          <el-tab-pane :label="t('profile.info.userSocial')" name="userSocial">
            <UserSocial v-model:activeName="activeName" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>
<script lang="ts" setup>
  import {
    BasicInfo,
    ProfileUser,
    ResetPwd,
    TenantProfile,
    UserSocial
  } from './components'

  const { t } = useI18n()
  const route = useRoute()
  defineOptions({ name: 'Profile' })
  const activeName = ref((route.query.tab as string) || 'basicInfo')

  watch(
    () => route.query.tab,
    (tab) => {
      if (typeof tab === 'string' && tab) {
        activeName.value = tab
      }
    }
  )
  const profileUserRef = ref()

  // 处理基本信息更新成功
  const handleBasicInfoSuccess = async () => {
    await profileUserRef.value?.refresh()
  }
</script>
<style scoped>
  .user {
    max-height: 960px;
    padding: 15px 20px 20px;
  }

  .card-header {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  :deep(.el-card .el-card__header, .el-card .el-card__body) {
    padding: 15px !important;
  }

  .profile-tabs {
    min-height: 520px;
  }

  .profile-tabs > .el-tabs__content {
    padding: 24px 8px;
    font-weight: 600;
    color: #6b778c;
  }

  .el-tabs--left .el-tabs__content {
    height: 100%;
  }
</style>
