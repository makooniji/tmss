<template>
  <Dialog
    v-model="dialogVisible"
    :title="t('role.dataPermissionTitle')"
    width="800"
  >
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      label-width="100px"
    >
      <el-form-item :label="t('role.roleName')">
        <el-tag>{{ formData.name }}</el-tag>
      </el-form-item>
      <el-form-item :label="t('role.roleCode')">
        <el-tag>{{ formData.code }}</el-tag>
      </el-form-item>
      <el-form-item :label="t('role.dataScope')">
        <el-select v-model="formData.dataScope">
          <el-option
            v-for="item in getIntDictOptions(DICT_TYPE.SYSTEM_DATA_SCOPE)"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <el-form-item
      v-if="formData.dataScope === SystemDataScopeEnum.DEPT_CUSTOM"
      :label="t('role.deptScope')"
      label-width="100px"
    >
      <el-card class="w-full h-400px !overflow-y-scroll" shadow="never">
        <template #header>
          {{ t('roleAssignMenu.selectAllToggle') }}:
          <el-switch
            v-model="treeNodeAll"
            :active-text="t('roleAssignMenu.switchYes')"
            :inactive-text="t('roleAssignMenu.switchNo')"
            inline-prompt
            @change="handleCheckedTreeNodeAll()"
          />
          {{ t('roleAssignMenu.expandAllToggle') }}:
          <el-switch
            v-model="deptExpand"
            :active-text="t('roleAssignMenu.expand')"
            :inactive-text="t('roleAssignMenu.collapse')"
            inline-prompt
            @change="handleCheckedTreeExpand"
          />
          {{ t('role.parentChildLink') }}:
          <el-switch
            v-model="checkStrictly"
            :active-text="t('roleAssignMenu.switchYes')"
            :inactive-text="t('roleAssignMenu.switchNo')"
            inline-prompt
          />
        </template>
        <el-tree
          ref="treeRef"
          :check-strictly="!checkStrictly"
          :data="deptOptions"
          :props="defaultProps"
          default-expand-all
          :empty-text="t('role.treeLoading')"
          node-key="id"
          show-checkbox
        />
      </el-card>
    </el-form-item>
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
  import { defaultProps, handleTree } from '@/utils/tree'
  import { SystemDataScopeEnum } from '@/utils/constants'
  import * as RoleApi from '@/api/system/role'
  import * as DeptApi from '@/api/system/dept'
  import * as PermissionApi from '@/api/system/permission'

  defineOptions({ name: 'SystemRoleDataPermissionForm' })

  const { t } = useI18n()
  const message = useMessage()

  const dialogVisible = ref(false)
  const formLoading = ref(false)
  const formData = reactive({
    id: undefined as number | undefined,
    name: '',
    code: '',
    dataScope: undefined as number | undefined,
    dataScopeDeptIds: [] as number[]
  })
  const formRef = ref()
  const deptOptions = ref<any[]>([])
  const deptExpand = ref(true)
  const treeRef = ref()
  const treeNodeAll = ref(false)
  const checkStrictly = ref(true)

  const open = async (row: RoleApi.RoleVO) => {
    dialogVisible.value = true
    resetForm()
    deptOptions.value = handleTree(await DeptApi.getSimpleDeptList())
    formData.id = row.id
    formData.name = row.name
    formData.code = row.code
    formData.dataScope = row.dataScope
    await nextTick()
    row.dataScopeDeptIds?.forEach((deptId: number): void => {
      treeRef.value.setChecked(deptId, true, false)
    })
  }
  defineExpose({ open })

  const emit = defineEmits(['success'])
  const submitForm = async () => {
    if (formData.id === undefined) return
    formLoading.value = true
    try {
      const data = {
        roleId: formData.id,
        dataScope: formData.dataScope,
        dataScopeDeptIds:
          formData.dataScope !== SystemDataScopeEnum.DEPT_CUSTOM
            ? []
            : (treeRef.value.getCheckedKeys(false) as number[])
      }
      await PermissionApi.assignRoleDataScope(data)
      message.success(t('common.updateSuccess'))
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }

  const resetForm = () => {
    treeNodeAll.value = false
    deptExpand.value = true
    checkStrictly.value = true
    formData.id = undefined
    formData.name = ''
    formData.code = ''
    formData.dataScope = undefined
    formData.dataScopeDeptIds = []
    treeRef.value?.setCheckedNodes([])
    formRef.value?.resetFields()
  }

  const handleCheckedTreeNodeAll = () => {
    treeRef.value.setCheckedNodes(treeNodeAll.value ? deptOptions.value : [])
  }

  const handleCheckedTreeExpand = () => {
    const nodes = treeRef.value?.store.nodesMap
    for (const node in nodes) {
      if (nodes[node].expanded === deptExpand.value) {
        continue
      }
      nodes[node].expanded = deptExpand.value
    }
  }
</script>
