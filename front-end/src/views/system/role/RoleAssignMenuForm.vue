<template>
  <Dialog v-model="dialogVisible" :title="t('roleAssignMenu.title')">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      label-width="100px"
    >
      <el-form-item :label="t('roleAssignMenu.roleName')">
        <el-tag>{{ formData.name }}</el-tag>
      </el-form-item>
      <el-form-item :label="t('roleAssignMenu.roleCode')">
        <el-tag>{{ formData.code }}</el-tag>
      </el-form-item>
      <el-form-item :label="t('roleAssignMenu.menuPermission')">
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
  import { defaultProps, handleTree } from '@/utils/tree'
  import * as RoleApi from '@/api/system/role'
  import * as MenuApi from '@/api/system/menu'
  import * as PermissionApi from '@/api/system/permission'

  defineOptions({ name: 'SystemRoleAssignMenuForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const formLoading = ref(false)
  const formData = reactive({
    id: undefined as number | undefined,
    name: '',
    code: '',
    menuIds: [] as number[]
  })
  const formRef = ref()
  const menuOptions = ref<any[]>([])
  const menuExpand = ref(false)
  const treeRef = ref()
  const treeNodeAll = ref(false)

  const open = async (row: RoleApi.RoleVO) => {
    dialogVisible.value = true
    resetForm()
    menuOptions.value = handleTree(await MenuApi.getSimpleMenusList())
    formData.id = row.id
    formData.name = row.name
    formData.code = row.code
    formLoading.value = true
    try {
      formData.menuIds = await PermissionApi.getRoleMenuList(row.id)
      formData.menuIds.forEach((menuId: number) => {
        treeRef.value.setChecked(menuId, true, false)
      })
    } finally {
      formLoading.value = false
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
      if (formData.id === undefined) return
      const data = {
        roleId: formData.id,
        menuIds: [
          ...(treeRef.value.getCheckedKeys(false) as unknown as Array<number>),
          ...(treeRef.value.getHalfCheckedKeys() as unknown as Array<number>)
        ]
      }
      await PermissionApi.assignRoleMenu(data)
      message.success(t('common.updateSuccess'))
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }

  const resetForm = () => {
    treeNodeAll.value = false
    menuExpand.value = false
    formData.id = undefined
    formData.name = ''
    formData.code = ''
    formData.menuIds = []
    treeRef.value?.setCheckedNodes([])
    formRef.value?.resetFields()
  }

  const handleCheckedTreeNodeAll = () => {
    treeRef.value.setCheckedNodes(treeNodeAll.value ? menuOptions.value : [])
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
