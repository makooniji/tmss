<template>
  <Dialog :title="t('aiChat.conversationForm.title')" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="130px"
      v-loading="formLoading"
    >
      <el-form-item
        :label="t('aiChat.conversationForm.systemMessage')"
        prop="systemMessage"
      >
        <el-input
          type="textarea"
          v-model="formData.systemMessage"
          :rows="4"
          :placeholder="t('aiChat.conversationForm.placeholderSystem')"
        />
      </el-form-item>
      <el-form-item :label="t('aiChat.conversationForm.model')" prop="modelId">
        <el-select
          v-model="formData.modelId"
          :placeholder="t('aiChat.conversationForm.selectModel')"
        >
          <el-option
            v-for="model in models"
            :key="model.id"
            :label="model.name"
            :value="model.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="t('aiChat.conversationForm.temperature')"
        prop="temperature"
      >
        <el-input-number
          v-model="formData.temperature"
          :placeholder="t('aiChat.conversationForm.placeholderTemperature')"
          :min="0"
          :max="2"
          :precision="2"
          class="!w-1/1"
        />
      </el-form-item>
      <el-form-item
        :label="t('aiChat.conversationForm.maxTokens')"
        prop="maxTokens"
      >
        <el-input-number
          v-model="formData.maxTokens"
          :placeholder="t('aiChat.conversationForm.placeholderMaxTokens')"
          :min="0"
          :max="8192"
          class="!w-1/1"
        />
      </el-form-item>
      <el-form-item
        :label="t('aiChat.conversationForm.maxContexts')"
        prop="maxContexts"
      >
        <el-input-number
          v-model="formData.maxContexts"
          :placeholder="t('aiChat.conversationForm.placeholderMaxContexts')"
          :min="0"
          :max="20"
          class="!w-1/1"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">{{
        t('common.ok')
      }}</el-button>
      <el-button @click="dialogVisible = false">{{
        t('common.cancel')
      }}</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
  import { ModelApi, ModelVO } from '@/api/ai/model/model'
  import {
    ChatConversationApi,
    ChatConversationVO
  } from '@/api/ai/chat/conversation'
  import { AiModelTypeEnum } from '@/views/ai/utils/constants'

  /** AI 聊天对话的更新表单 */
  defineOptions({ name: 'ChatConversationUpdateForm' })

  const message = useMessage() // 消息弹窗
  const { t } = useI18n()

  const dialogVisible = ref(false) // 弹窗的是否展示
  const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
  const formData = ref({
    id: undefined,
    systemMessage: undefined,
    modelId: undefined,
    temperature: undefined,
    maxTokens: undefined,
    maxContexts: undefined
  })
  const formRules = computed(() => ({
    modelId: [
      {
        required: true,
        message: t('aiChat.conversationForm.modelRequired'),
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: t('aiChat.conversationForm.statusRequired'),
        trigger: 'blur'
      }
    ],
    temperature: [
      {
        required: true,
        message: t('aiChat.conversationForm.temperatureRequired'),
        trigger: 'blur'
      }
    ],
    maxTokens: [
      {
        required: true,
        message: t('aiChat.conversationForm.maxTokensRequired'),
        trigger: 'blur'
      }
    ],
    maxContexts: [
      {
        required: true,
        message: t('aiChat.conversationForm.maxContextsRequired'),
        trigger: 'blur'
      }
    ]
  }))
  const formRef = ref() // 表单 Ref
  const models = ref([] as ModelVO[]) // 聊天模型列表

  /** 打开弹窗 */
  const open = async (id: number) => {
    dialogVisible.value = true
    resetForm()
    // 修改时，设置数据
    if (id) {
      formLoading.value = true
      try {
        const data = await ChatConversationApi.getChatConversationMy(id)
        formData.value = Object.keys(formData.value).reduce((obj, key) => {
          if (data.hasOwnProperty(key)) {
            obj[key] = data[key]
          }
          return obj
        }, {})
      } finally {
        formLoading.value = false
      }
    }
    // 获得下拉数据
    models.value = await ModelApi.getModelSimpleList(AiModelTypeEnum.CHAT)
  }
  defineExpose({ open }) // 提供 open 方法，用于打开弹窗

  /** 提交表单 */
  const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
  const submitForm = async () => {
    // 校验表单
    await formRef.value.validate()
    // 提交请求
    formLoading.value = true
    try {
      const data = formData.value as unknown as ChatConversationVO
      await ChatConversationApi.updateChatConversationMy(data)
      message.success(t('aiChat.conversationForm.updateOk'))
      dialogVisible.value = false
      // 发送操作成功的事件
      emit('success')
    } finally {
      formLoading.value = false
    }
  }

  /** 重置表单 */
  const resetForm = () => {
    formData.value = {
      id: undefined,
      systemMessage: undefined,
      modelId: undefined,
      temperature: undefined,
      maxTokens: undefined,
      maxContexts: undefined
    }
    formRef.value?.resetFields()
  }
</script>
