<template>
  <!-- 地区 & IP -->
  <!-- 操作栏 -->
  <ContentWrap>
    <el-button type="primary" plain @click="openForm()">
      <Icon icon="ep:plus" class="mr-5px" /> {{ t('system.area.ipQuery') }}
    </el-button>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <div style="width: 100%; height: 700px">
      <!-- AutoResizer 自动调节大小 -->
      <el-auto-resizer>
        <template #default="{ height, width }">
          <!-- Virtualized Table 虚拟化表格：高性能，解决表格在大数据量下的卡顿问题 -->
          <el-table-v2
            v-loading="loading"
            :columns="columns"
            :data="list"
            :width="width"
            :height="height"
            expand-column-key="id"
          />
        </template>
      </el-auto-resizer>
    </div>
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <AreaForm ref="formRef" />
</template>
<script setup lang="tsx">
  import { Column } from 'element-plus'
  import AreaForm from './AreaForm.vue'
  import * as AreaApi from '@/api/system/area'

  defineOptions({ name: 'SystemArea' })

  const { t } = useI18n()

  // 表格的 column 字段
  const columns = computed<Column[]>(() => [
    {
      dataKey: 'id',
      title: t('system.area.id'),
      width: 400,
      fixed: true,
      key: 'id'
    },
    {
      dataKey: 'name',
      title: t('system.area.placeName'),
      width: 200
    }
  ])
  const loading = ref(true) // 列表的加载中
  const list = ref([]) // 表格的数据

  /** 获得数据列表 */
  const getList = async () => {
    loading.value = true
    try {
      list.value = await AreaApi.getAreaTree()
    } finally {
      loading.value = false
    }
  }

  /** 添加/修改操作 */
  const formRef = ref()
  const openForm = () => {
    formRef.value.open()
  }

  /** 初始化 **/
  onMounted(() => {
    getList()
  })
</script>
