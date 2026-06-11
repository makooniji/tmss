<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item :label="t('tenantPackage.packageName')" prop="name">
        <el-input
          v-model="formData.name"
          :placeholder="t('tenantPackage.placeholderPackageName')"
        />
      </el-form-item>
      <el-form-item :label="t('tenantPackage.menuPermission')">
        <el-card class="w-full h-400px !overflow-y-scroll" shadow="never">
          <template #header>
            {{ t('roleAssignMenu.selectAllToggle') }}:
            <el-switch
              v-model="treeNodeAll"
              :active-text="t('roleAssignMenu.switchYes')"
              :inactive-text="t('roleAssignMenu.switchNo')"
              inline-prompt
              @change="handleCheckedTreeNodeAll"
            />
            {{ t('roleAssignMenu.expandAllToggle') }}:
            <el-switch
              v-model="menuExpand"
              :active-text="t('roleAssignMenu.expand')"
              :inactive-text="t('roleAssignMenu.collapse')"
              inline-prompt
              @change="handleCheckedTreeExpand"
            />
          </template>
          <el-tree
            ref="treeRef"
            :data="menuOptions"
            :props="defaultProps"
            :empty-text="t('roleAssignMenu.treeLoading')"
            node-key="id"
            show-checkbox
          />
        </el-card>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('tenantPackage.remark')" prop="remark">
        <el-input
          v-model="formData.remark"
          :placeholder="t('tenantPackage.placeholderRemark')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">{{
        t('common.ok')
      }}</el-button>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { CommonStatusEnum } from '@/utils/constants'
  import { defaultProps, handleTree } from '@/utils/tree'
  import * as TenantPackageApi from '@/api/system/tenantPackage'
  import * as MenuApi from '@/api/system/menu'
  import { ElTree } from 'element-plus'

  defineOptions({ name: 'SystemTenantPackageForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const formLoading = ref(false)
  const formType = ref('')
  const formData = ref({
    id: null,
    name: null,
    remark: null,
    menuIds: [],
    status: CommonStatusEnum.ENABLE
  })
  const formRules = computed(() => ({
    name: [
      {
        required: true,
        message: t('tenantPackage.nameRequired'),
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: t('tenantPackage.statusRequired'),
        trigger: 'blur'
      }
    ],
    menuIds: [
      {
        required: true,
        message: t('tenantPackage.menuIdsRequired'),
        trigger: 'blur'
      }
    ]
  }))
  const formRef = ref()
  const menuOptions = ref<any[]>([])
  const menuExpand = ref(false)
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const treeNodeAll = ref(false)

  const open = async (type: string, id?: number) => {
    dialogVisible.value = true
    dialogTitle.value =
      type === 'create'
        ? t('tenantPackage.dialogCreate')
        : type === 'update'
          ? t('tenantPackage.dialogUpdate')
          : t('action.' + type)
    formType.value = type
    resetForm()
    menuOptions.value = handleTree(await MenuApi.getSimpleMenusList())
    if (id) {
      formLoading.value = true
      try {
        const res = await TenantPackageApi.getTenantPackage(id)
        formData.value = res
        res.menuIds.forEach((menuId: number) => {
          treeRef.value!.setChecked(menuId, true, false)
        })
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])
  const submitForm = async () => {
    if (!formRef) return
    const valid = await formRef.value.validate()
    if (!valid) return
    formLoading.value = true
    try {
      const data = formData.value as unknown as TenantPackageApi.TenantPackageVO
      data.menuIds = [
        ...(treeRef.value!.getCheckedKeys(false) as unknown as Array<number>),
        ...(treeRef.value!.getHalfCheckedKeys() as unknown as Array<number>)
      ]
      if (formType.value === 'create') {
        await TenantPackageApi.createTenantPackage(data)
        message.success(t('common.createSuccess'))
      } else {
        await TenantPackageApi.updateTenantPackage(data)
        message.success(t('common.updateSuccess'))
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }

  const resetForm = () => {
    treeNodeAll.value = false
    menuExpand.value = false
    formData.value = {
      id: null,
      name: null,
      remark: null,
      menuIds: [],
      status: CommonStatusEnum.ENABLE
    }
    treeRef.value?.setCheckedNodes([])
    formRef.value?.resetFields()
  }

  const handleCheckedTreeNodeAll = () => {
    treeRef.value!.setCheckedNodes(treeNodeAll.value ? menuOptions.value : [])
  }

  const handleCheckedTreeExpand = () => {
    const nodes = treeRef.value?.store.nodesMap
    for (const node in nodes) {
      if (nodes[node].expanded === menuExpand.value) {
        continue
      }
      nodes[node].expanded = menuExpand.value
    }
  }
</script>
