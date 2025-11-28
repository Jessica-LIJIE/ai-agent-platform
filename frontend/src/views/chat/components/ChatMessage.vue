<template>
  <div class="chat-message" :class="messageClass">
    <div class="message-content">
      <div class="message-avatar">
        <el-avatar :size="32">
          {{ isUser ? '我' : 'AI' }}
        </el-avatar>
      </div>
      <div class="message-bubble">
        <div class="message-text">
          <div v-if="message.query" class="query-text">{{ message.query }}</div>
          <div v-if="message.answer" class="answer-text">{{ message.answer }}</div>
          <div v-if="!message.query && !message.answer" class="empty-text">加载中...</div>
        </div>
        <div class="message-time">
          {{ formatTime(message.createTime) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AgentConversation } from '@/types/entity'

interface Props {
  message: AgentConversation
  isUser?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isUser: false
})

// 消息样式类
const messageClass = computed(() => ({
  'message-user': props.isUser,
  'message-assistant': !props.isUser
}))

// 格式化时间
const formatTime = (dateTime?: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.chat-message {
  margin-bottom: 16px;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-user .message-content {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  min-width: 100px;
}

.message-user .message-bubble {
  background-color: var(--el-color-primary);
  color: white;
  border-radius: 12px 12px 4px 12px;
  padding: 10px 14px;
}

.message-assistant .message-bubble {
  background-color: var(--el-bg-color-page);
  color: var(--el-text-color-primary);
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px 12px 12px 4px;
  padding: 10px 14px;
}

.message-text {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.5;
}

.query-text {
  margin-bottom: 8px;
}

.answer-text {
  color: inherit;
}

.empty-text {
  color: var(--el-text-color-placeholder);
  font-style: italic;
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  margin-top: 6px;
  text-align: right;
}

.message-user .message-time {
  color: rgba(255, 255, 255, 0.8);
}
</style>

