<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      
    >
      <el-form-item :label="t('common.userId')" prop="userId">
        <el-select
          v-model="queryParams.userId"
          clearable
          :placeholder="t('common.placeholderUserId')"
          class="!w-240px"
        >
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('aiChat.manager.chatNo')" prop="title">
        <el-input
          v-model="queryParams.title"
          :placeholder="t('aiChat.manager.placeholderChatNo')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          :start-placeholder="t('common.startDate')"
          :end-placeholder="t('common.endDate')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"
          ><Icon icon="ep:search" class="mr-5px" />
          {{ t('common.search') }}</el-button
        >
        <el-button @click="resetQuery"
          ><Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}</el-button
        >
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column
        :label="t('aiChat.manager.conversationId')"
        align="center"
        prop="id"
        width="180"
        fixed="left"
      />
      <el-table-column
        :label="t('aiChat.manager.convTitle')"
        align="center"
        prop="title"
        width="180"
        fixed="left"
      />
      <el-table-column
        :label="t('aiChat.manager.user')"
        align="center"
        prop="userId"
        width="180"
      >
        <template #default="scope">
          <span>{{
            userList.find((item) => item.id === scope.row.userId)?.nickname
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('aiChat.manager.role')"
        align="center"
        prop="roleName"
        width="180"
      />
      <el-table-column
        :label="t('aiChat.manager.model')"
        align="center"
        prop="model"
        width="180"
      />
      <el-table-column
        :label="t('aiChat.manager.msgCount')"
        align="center"
        prop="messageCount"
      />
      <el-table-column
        :label="t('common.createTime')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        :label="t('aiChat.manager.temperature')"
        align="center"
        prop="temperature"
      />
      <el-table-column
        :label="t('aiChat.manager.replyTokens')"
        align="center"
        prop="maxTokens"
        width="120"
      />
      <el-table-column
        :label="t('aiChat.manager.maxContexts')"
        align="center"
        prop="maxContexts"
        width="120"
      />
      <el-table-column
        :label="t('table.action')"
        align="center"
        width="180"
        fixed="right"
      >
        <template #default="scope">
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['ai:chat-conversation:delete']"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
  import { dateFormatter } from '@/utils/formatTime'
  import {
    ChatConversationApi,
    ChatConversationVO
  } from '@/api/ai/chat/conversation'
  import * as UserApi from '@/api/system/user'

  const message = useMessage() // 消息弹窗
  const { t } = useI18n() // 国际化

  const loading = ref(true) // 列表的加载中
  const list = ref<ChatConversationVO[]>([]) // 列表的数据
  const total = ref(0) // 列表的总页数
  const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    userId: undefined,
    title: undefined,
    createTime: []
  })
  const queryFormRef = ref() // 搜索的表单
  const userList = ref<UserApi.UserVO[]>([]) // 用户列表

  /** 查询列表 */
  const getList = async () => {
    loading.value = true
    try {
      const data =
        await ChatConversationApi.getChatConversationPage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  /** 搜索按钮操作 */
  const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
  }

  /** 重置按钮操作 */
  const resetQuery = () => {
    queryFormRef.value.resetFields()
    handleQuery()
  }

  /** 删除按钮操作 */
  const handleDelete = async (id: number) => {
    try {
      await message.delConfirm()
      await ChatConversationApi.deleteChatConversationByAdmin(id)
      message.success(t('common.delSuccess'))
      await getList()
    } catch {}
  }

  /** 初始化 **/
  onMounted(async () => {
    getList()
    userList.value = await UserApi.getSimpleUserList()
  })
</script>
