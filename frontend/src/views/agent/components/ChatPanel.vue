<template>
  <div class="chat-panel">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <span class="chat-title">对话测试</span>
          <el-button
            v-if="sessionId"
            type="text"
            size="small"
            @click="handleNewSession"
          >
            新对话
          </el-button>
        </div>
      </template>

      <!-- 消息列表 -->
      <div ref="messagesContainerRef" class="messages-container">
        <div v-if="!sessionId" class="empty-state">
          <el-empty description="点击'开始对话'开始测试智能体" />
        </div>
        <div v-else-if="messages.length === 0" class="empty-state">
          <el-empty description="还没有消息，开始对话吧！" />
        </div>
        <div v-else class="messages-list">
          <template v-for="msg in messages" :key="msg.id">
            <!-- 用户消息 -->
            <ChatMessage
              v-if="msg.query"
              :message="{ ...msg, answer: undefined, id: msg.id + '-query' }"
              :is-user="true"
            />
            <!-- AI回答 -->
            <ChatMessage
              v-if="msg.answer"
              :message="{ ...msg, query: undefined, id: msg.id + '-answer' }"
              :is-user="false"
            />
          </template>
        </div>
        <div v-if="sending" class="sending-indicator">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI正在思考...</span>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input-wrapper">
        <div v-if="!sessionId" class="start-chat">
          <el-button
            type="primary"
            :disabled="!canStartChat"
            @click="handleStartChat"
          >
            开始对话
          </el-button>
          <div v-if="!canStartChat" class="disabled-tip">
            请先配置模型ID才能开始对话
          </div>
        </div>
        <ChatInput
          v-else
          :disabled="inputDisabled"
          :loading="sending"
          placeholder="输入消息..."
          @send="handleSendMessage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { createSession, getMessageHistory, sendMessage } from '@/api/chat'
import type { AgentConversation } from '@/types/entity'
import ChatMessage from '@/views/chat/components/ChatMessage.vue'
import ChatInput from '@/views/chat/components/ChatInput.vue'

interface Props {
  agentId?: string
  llmModelId?: string
}

const props = defineProps<Props>()

// 会话ID
const sessionId = ref<string>('')
// 消息列表
const messages = ref<AgentConversation[]>([])
// 发送中状态
const sending = ref(false)
// 输入框禁用状态
const inputDisabled = ref(false)
// 消息容器引用（用于滚动）
const messagesContainerRef = ref<HTMLElement>()

// 是否可以开始对话
const canStartChat = computed(() => {
  return !!props.agentId && !!props.llmModelId
})

// 开始对话
const handleStartChat = async () => {
  if (!canStartChat.value) {
    ElMessage.warning('请先配置智能体的模型ID')
    return
  }

  try {
    const response = await createSession()
    sessionId.value = response.session_id
    messages.value = []
    ElMessage.success('对话已开始')
    
    // 加载历史消息
    await loadHistory()
  } catch (error: any) {
    console.error('创建会话失败:', error)
    ElMessage.error(error.message || '创建会话失败')
  }
}

// 新对话
const handleNewSession = async () => {
  try {
    const response = await createSession()
    sessionId.value = response.session_id
    messages.value = []
    ElMessage.success('已开始新对话')
  } catch (error: any) {
    console.error('创建新会话失败:', error)
    ElMessage.error(error.message || '创建新会话失败')
  }
}

// 加载历史消息
const loadHistory = async () => {
  if (!sessionId.value) return

  try {
    const history = await getMessageHistory(sessionId.value)
    messages.value = history
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    console.error('加载历史消息失败:', error)
    ElMessage.error(error.message || '加载历史消息失败')
  }
}

// 发送消息
const handleSendMessage = async (query: string) => {
  if (!sessionId.value || !props.agentId || !props.llmModelId) {
    ElMessage.warning('会话未初始化或缺少必要信息')
    return
  }

  if (!query.trim()) {
    return
  }

  // 创建用户消息（先显示）
  const userMessage: AgentConversation = {
    sessionId: sessionId.value,
    agentId: props.agentId,
    query: query,
    answer: undefined,
    metadata: {
      llmModelId: props.llmModelId
    }
  }

  // 添加到消息列表
  messages.value.push(userMessage)
  sending.value = true
  inputDisabled.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 发送消息到后端
    const response = await sendMessage({
      sessionId: sessionId.value,
      agentId: props.agentId,
      query: query,
      metadata: {
        llmModelId: props.llmModelId
      }
    })

    // 更新消息（包含AI回答）
    const index = messages.value.findIndex((msg) => 
      msg.query === query && !msg.answer
    )
    if (index !== -1) {
      messages.value[index] = response
    } else {
      messages.value.push(response)
    }

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error: any) {
    console.error('发送消息失败:', error)
    ElMessage.error(error.message || '发送消息失败')
    
    // 移除失败的消息
    const index = messages.value.findIndex((msg) => 
      msg.query === query && !msg.answer
    )
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } finally {
    sending.value = false
    inputDisabled.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
  }
}

// 监听 agentId 或 modelId 变化，重置会话
watch(
  () => [props.agentId, props.llmModelId],
  () => {
    sessionId.value = ''
    messages.value = []
  }
)

// 组件卸载时清理
onUnmounted(() => {
  sessionId.value = ''
  messages.value = []
})
</script>

<style scoped>
.chat-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.messages-list {
  display: flex;
  flex-direction: column;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
}

.sending-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.chat-input-wrapper {
  border-top: 1px solid var(--el-border-color-light);
}

.start-chat {
  padding: 20px;
  text-align: center;
}

.disabled-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>

