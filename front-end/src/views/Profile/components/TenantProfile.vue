<template>
  <div class="tenant-profile" v-loading="loading">
    <div class="profile-header">
      <Icon class="profile-header__icon" icon="ep:shop" />
      <span class="profile-header__title">{{
        t('system.tenant.profileMerchantSection')
      }}</span>
    </div>

    <el-descriptions :column="2" border class="profile-descriptions">
      <el-descriptions-item
        v-for="item in profileItems"
        :key="item.label"
        :label="item.label"
        :span="item.span ?? 1"
      >
        <div
          class="field-value"
          :class="{ 'field-value--token': item.maskKey && !item.isEmpty }"
        >
          <span
            v-if="item.maskKey && !item.isEmpty"
            class="token-field is-copyable"
            @click="copyText(item.rawValue)"
          >
            <span class="token-field__sizer">{{ item.rawValue }}</span>
            <span class="token-field__content">{{ getDisplayText(item) }}</span>
          </span>
          <span
            v-else
            class="field-value__text"
            :class="{
              'is-empty': item.isEmpty,
              'is-mono': item.copyable,
              'is-copyable': item.copyable && !item.isEmpty
            }"
            @click="item.copyable && !item.isEmpty && copyText(item.rawValue)"
          >
            {{ getDisplayText(item) }}
          </span>
          <span
            v-if="item.maskKey && !item.isEmpty"
            class="field-value__toggle"
            @click.stop="toggleTokenVisible(item.maskKey)"
          >
            <Icon :icon="tokenVisible[item.maskKey] ? 'ep:hide' : 'ep:view'" />
          </span>
        </div>
      </el-descriptions-item>
      <el-descriptions-item :label="whiteListItem.label">
        <span
          class="field-value__text"
          :class="{ 'is-empty': whiteListItem.isEmpty }"
        >
          {{ whiteListItem.value }}
        </span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.tenant.callbackUrl')">
        <div class="callback-form">
          <el-input
            v-model="notifyUrl"
            :placeholder="t('system.tenant.placeholderCallbackUrl')"
            clearable
            class="callback-form__input"
          />
          <el-button
            type="primary"
            :loading="saving"
            @click="handleSaveNotifyUrl"
          >
            {{ t('common.save') }}
          </el-button>
        </div>
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core'
  import * as TenantApi from '@/api/system/tenant'
  import { getTenantId } from '@/utils/auth'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'ProfileTenantProfile' })

  const { t } = useI18n()
  const message = useMessage()
  const route = useRoute()
  const userStore = useUserStore()
  const loading = ref(false)
  const saving = ref(false)
  const notifyUrl = ref('')

  type TenantDetail = TenantApi.TenantVO & {
    code?: string
    tenantKey?: string
    secretKey?: string
    notifyUrl?: string
    currency?: string[] | string
    parentId?: number
    remark?: string
    balance?: number | string
    skin?: string
    loginIp?: string
    connectionType?: string
    gameHistoryApi?: string
    ip_address?: string | string[]
    ipAddress?: string | string[]
    apiToken?: string
    authToken?: string
    tableListApi?: string
    hostnameApi?: string
  }

  type TokenMaskKey = 'apiToken' | 'authToken'

  type DisplayItem = {
    label: string
    value: string
    rawValue: string
    isEmpty: boolean
    span?: number
    copyable?: boolean
    maskKey?: TokenMaskKey
  }

  const tenantData = ref<TenantDetail>({} as TenantDetail)
  const tokenVisible = reactive<Record<TokenMaskKey, boolean>>({
    apiToken: false,
    authToken: false
  })

  const formatDisplayValue = (value: unknown) => {
    if (value == null || value === '') {
      return '-'
    }
    if (Array.isArray(value)) {
      return value.length ? value.join(', ') : '-'
    }
    return String(value)
  }

  const buildDisplayItem = (
    label: string,
    rawValue: unknown,
    options: { span?: number; copyable?: boolean; maskKey?: TokenMaskKey } = {}
  ): DisplayItem => {
    const value = formatDisplayValue(rawValue)
    return {
      label,
      value,
      rawValue: value === '-' ? '' : value,
      isEmpty: value === '-',
      span: options.span,
      copyable: options.copyable,
      maskKey: options.maskKey
    }
  }

  const profileItems = computed(() => [
    buildDisplayItem(
      t('system.tenant.profileApiToken'),
      tenantData.value.apiToken ?? tenantData.value.tenantKey,
      { copyable: true, maskKey: 'apiToken' }
    ),
    buildDisplayItem(
      t('system.tenant.profileAuthToken'),
      tenantData.value.authToken ?? tenantData.value.secretKey,
      { copyable: true, maskKey: 'authToken' }
    ),
    buildDisplayItem(t('system.tenant.currency'), tenantData.value.currency),
    buildDisplayItem(
      t('system.tenant.profileCurrentIp'),
      tenantData.value.loginIp
    )
  ])

  const whiteListItem = computed(() =>
    buildDisplayItem(
      t('system.tenant.profileWhiteListIp'),
      tenantData.value.ip_address ?? tenantData.value.ipAddress
    )
  )

  const TOKEN_MASK_TEXT = '••••••••'

  const getDisplayText = (item: DisplayItem) => {
    if (item.isEmpty || !item.maskKey || tokenVisible[item.maskKey]) {
      return item.value
    }
    return TOKEN_MASK_TEXT
  }

  const toggleTokenVisible = (key: TokenMaskKey) => {
    tokenVisible[key] = !tokenVisible[key]
  }

  const copyText = async (text: string) => {
    if (!text) {
      return
    }
    const { copy, copied, isSupported } = useClipboard({
      legacy: true,
      source: text
    })
    if (!isSupported) {
      message.error(t('common.copyError'))
      return
    }
    await copy(text)
    if (copied.value) {
      message.success(t('common.copySuccess'))
    }
  }

  const resolveTenantId = () => {
    const routeId = Number(route.query.id)
    if (!Number.isNaN(routeId) && routeId > 0) {
      return routeId
    }
    const tenantId = Number(getTenantId())
    return !Number.isNaN(tenantId) && tenantId > 0 ? tenantId : undefined
  }

  const loadTenantProfile = async () => {
    loading.value = true
    try {
      if (!userStore.getIsSetUser) {
        await userStore.setUserInfoAction()
      }
      tenantData.value =
        await TenantApi.getMerchantInformation(resolveTenantId())
      notifyUrl.value = tenantData.value.notifyUrl ?? ''
    } finally {
      loading.value = false
    }
  }

  const handleSaveNotifyUrl = async () => {
    const value = notifyUrl.value.trim()
    if (!value) {
      message.error(t('system.tenant.notifyUrlRequired'))
      return
    }
    saving.value = true
    try {
      const payload: TenantApi.UpdateMerchantInfoReqVO = { notifyUrl: value }
      const tenantId = resolveTenantId()
      if (tenantId) {
        payload.id = tenantId
      }
      await TenantApi.updateMerchantInfo(payload)
      tenantData.value.notifyUrl = value
      message.success(t('common.updateSuccess'))
    } finally {
      saving.value = false
    }
  }

  onMounted(() => {
    loadTenantProfile()
  })
</script>

<style lang="scss" scoped>
  .tenant-profile {
    min-height: 420px;
    padding: 4px 4px 8px;
  }

  .profile-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;

    &__icon {
      font-size: 18px;
      color: var(--el-color-primary);
    }

    &__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .profile-descriptions {
    margin-bottom: 20px;

    :deep(.el-descriptions__label) {
      width: 120px;
      font-weight: 500;
    }

    :deep(.el-descriptions__content) {
      word-break: break-all;
    }
  }

  .field-value {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    max-width: 100%;

    &--token {
      display: flex;
      width: 100%;
    }

    &__text {
      min-width: 0;
      word-break: break-all;

      &.is-mono {
        font-family: Consolas, Monaco, 'Courier New', monospace;
        font-size: 13px;
      }

      &.is-copyable {
        cursor: pointer;
        transition: color 0.2s;

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }

    &__toggle {
      flex-shrink: 0;
      width: 16px;
      font-size: 14px;
      line-height: 20px;
      color: var(--el-text-color-secondary);
      text-align: center;
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .token-field {
    position: relative;
    flex: 1;
    min-width: 0;
    font-family: Consolas, Monaco, 'Courier New', monospace;
    font-size: 13px;
    line-height: 20px;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: var(--el-color-primary);
    }

    &__sizer {
      display: block;
      max-width: 100%;
      overflow: hidden;
      color: transparent;
      text-overflow: ellipsis;
      white-space: nowrap;
      user-select: none;
    }

    &__content {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .callback-form {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    width: 100%;

    &__input {
      flex: 1;
      min-width: 140px;
    }
  }

  .is-empty {
    color: var(--el-text-color-placeholder);
  }
</style>
