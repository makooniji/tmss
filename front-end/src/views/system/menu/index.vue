<template>
  <!-- 菜单管理 -->
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      
    >
      <el-form-item :label="t('system.menu.name')" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          :placeholder="t('system.menu.placeholderName')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          class="!w-240px"
          clearable
          :placeholder="t('system.menu.selectStatus')"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          {{ t('common.search') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          {{ t('common.reset') }}
        </el-button>
        <el-button
          v-hasPermi="['system:menu:create']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          {{ t('action.create') }}
        </el-button>
        <el-button plain type="danger" @click="toggleExpandAll">
          <Icon class="mr-5px" icon="ep:sort" />
          {{ t('system.menu.expandCollapse') }}
        </el-button>
        <el-button plain @click="refreshMenu">
          <Icon class="mr-5px" icon="ep:refresh" />
          {{ t('system.menu.refreshCache') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-auto-resizer>
      <template #default="{ width }">
        <el-table-v2
          v-model:expanded-row-keys="expandedRowKeys"
          :columns="columns"
          :data="list"
          :expand-column-key="columns[0]!.key"
          :height="1000"
          :width="width"
          fixed
          row-key="id"
        />
      </template>
    </el-auto-resizer>
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <MenuForm ref="formRef" @success="getList" />
</template>
<script lang="tsx" setup>
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { handleTree } from '@/utils/tree'
  import * as MenuApi from '@/api/system/menu'
  import { MenuVO } from '@/api/system/menu'
  import MenuForm from './MenuForm.vue'
  import DictTag from '@/components/DictTag/src/DictTag.vue'
  import { Icon } from '@/components/Icon'
  import { ElButton, TableV2FixedDir, ElSwitch } from 'element-plus'
  import { checkPermi } from '@/utils/permission'
  import { CommonStatusEnum } from '@/utils/constants'
  import { CACHE_KEY, useCache } from '@/hooks/web/useCache'

  defineOptions({ name: 'SystemMenu' })

  const { wsCache } = useCache()
  const { t } = useI18n() // 国际化
  const message = useMessage() // 消息弹窗

  const loading = ref(true) // 列表的加载中
  const list = ref<any[]>([]) // 列表的数据
  const queryParams = reactive({
    name: undefined,
    status: undefined
  })
  const queryFormRef = ref() // 搜索的表单
  const isExpandAll = ref(false) // 是否展开，默认全部折叠
  const refreshTable = ref(true) // 重新渲染表格状态

  // 添加展开行控制
  const expandedRowKeys = ref<number[]>([])

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data = await MenuApi.getMenuList(queryParams)
      list.value = handleTree(data)
    } finally {
      loading.value = false
    }
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    queryFormRef.value.resetFields()
    handleQuery()
  }

  /** 添加/修改操作 */
  const formRef = ref()
  const openForm = (type: string, id?: number, parentId?: number) => {
    formRef.value.open(type, id, parentId)
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      // 删除的二次确认
      await message.delConfirm()
      // 发起删除
      await MenuApi.deleteMenu(id)
      message.success(t('common.delSuccess'))
      // 刷新列表
      await getList()
    } catch {}
  }

  /** 开启/关闭菜单的状态 */
  const menuStatusUpdating = ref<Record<number, boolean>>({}) // 菜单状态更新中的 menu 映射
  const handleStatusChanged = async (menu: MenuVO, val: number) => {
    menuStatusUpdating.value[menu.id] = true
    try {
      menu.status = val
      await MenuApi.updateMenu(menu)
    } finally {
      menuStatusUpdating.value[menu.id] = false
    }
  }

  // 虚拟列表表格（列标题随语言切换）
  const columns = computed(() => [
    {
      key: 'name',
      title: t('system.menu.colName'),
      dataKey: 'name',
      width: 250,
      fixed: TableV2FixedDir.LEFT
    },
    {
      key: 'icon',
      title: t('system.menu.colIcon'),
      dataKey: 'icon',
      width: 100,
      align: 'center' as const,
      cellRenderer: ({ cellData: icon }) => <Icon icon={icon} />
    },
    {
      key: 'sort',
      title: t('system.menu.colSort'),
      dataKey: 'sort',
      width: 60
    },
    {
      key: 'permission',
      title: t('system.menu.colPermission'),
      dataKey: 'permission',
      width: 300
    },
    {
      key: 'component',
      title: t('system.menu.colComponent'),
      dataKey: 'component',
      width: 500
    },
    {
      key: 'componentName',
      title: t('system.menu.colComponentName'),
      dataKey: 'componentName',
      width: 200
    },
    {
      key: 'status',
      title: t('system.menu.colStatus'),
      dataKey: 'status',
      width: 60,
      fixed: TableV2FixedDir.RIGHT,
      cellRenderer: ({ rowData }) => {
        if (!checkPermi(['system:menu:update'])) {
          return (
            <DictTag type={DICT_TYPE.COMMON_STATUS} value={rowData.status} />
          )
        }
        return (
          <ElSwitch
            v-model={rowData.status}
            active-value={CommonStatusEnum.ENABLE}
            inactive-value={CommonStatusEnum.DISABLE}
            loading={menuStatusUpdating.value[rowData.id]}
            class="ml-4px"
            onChange={(val) => handleStatusChanged(rowData, val)}
          />
        )
      }
    },
    {
      key: 'operations',
      title: t('system.menu.colAction'),
      align: 'center' as const,
      width: 160,
      fixed: TableV2FixedDir.RIGHT,
      cellRenderer: ({ rowData }) => {
        const buttons: InstanceType<typeof ElButton>[] = []
        if (checkPermi(['system:menu:update'])) {
          buttons.push(
            <ElButton
              key="edit"
              link
              type="primary"
              onClick={() => openForm('update', rowData.id)}
            >
              {t('system.modify')}
            </ElButton>
          )
        }
        if (checkPermi(['system:menu:create'])) {
          buttons.push(
            <ElButton
              key="create"
              link
              type="primary"
              onClick={() => openForm('create', undefined, rowData.id)}
            >
              {t('action.create')}
            </ElButton>
          )
        }
        if (checkPermi(['system:menu:delete'])) {
          buttons.push(
            <ElButton
              key="delete"
              link
              type="danger"
              onClick={() => handleDelete(rowData.id)}
            >
              {t('action.delete')}
            </ElButton>
          )
        }
        if (buttons.length === 0) {
          return null
        }
        return <>{buttons}</>
      }
    }
  ])

  /** 展开/折叠操作 */
  const toggleExpandAll = () => {
    if (!isExpandAll.value) {
      expandedRowKeys.value = list.value.map((item) => item.id)
    } else {
      expandedRowKeys.value = []
    }
    isExpandAll.value = !isExpandAll.value
  }

  /** 刷新菜单缓存按钮操作 */
  const refreshMenu = async () => {
    try {
      await message.confirm(
        t('system.menu.confirmRefresh'),
        t('system.menu.confirmRefreshTitle')
      )
      wsCache.delete(CACHE_KEY.USER)
      wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
      location.reload()
    } catch {}
  }

  /** 初始化 **/
  onMounted(() => {
    getList()
  })
</script>
