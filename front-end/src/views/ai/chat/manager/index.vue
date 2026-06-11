<template>
  <ContentWrap>
    <el-tabs>
      <el-tab-pane :label="t('aiChat.manager.tabConversations')">
        <ChatConversationList />
      </el-tab-pane>
      <el-tab-pane :label="t('aiChat.manager.tabMessages')">
        <ChatMessageList />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script setup lang="ts">
  import ChatConversationList from './ChatConversationList.vue'
  import ChatMessageList from './ChatMessageList.vue'

  defineOptions({ name: 'AiChatManager' })

  const { t } = useI18n()
</script>
