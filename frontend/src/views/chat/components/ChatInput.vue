<template>
  <div class="chat-input-container">
    <el-input
      v-model="inputText"
      type="textarea"
      :rows="3"
      :placeholder="placeholder"
      :disabled="disabled"
      @keydown.ctrl.enter="handleSend"
      @keydown.meta.enter="handleSend"
    />
    <div class="input-footer">
      <div class="input-tip">
        按 Ctrl+Enter 或 Cmd+Enter 发送
      </div>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="disabled || !inputText.trim()"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  placeholder?: string
  disabled?: boolean
  loading?: boolean
}

interface Emits {
  (e: 'send', message: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '输入消息...',
  disabled: false,
  loading: false
})

const emit = defineEmits<Emits>()

const inputText = ref('')

// 监听 loading 状态变化（用于重置状态）
watch(() => props.loading, (newVal, oldVal) => {
  // 如果从发送中变为完成，但输入框已有内容，说明可能是失败重试
  // 这里不做自动清空，让用户决定是否重发
})

const handleSend = () => {
  const text = inputText.value.trim()
  if (text && !props.disabled && !props.loading) {
    emit('send', text)
    // 立即清空输入框
    inputText.value = ''
  }
}

// 暴露方法，允许外部清空输入
defineExpose({
  clear: () => {
    inputText.value = ''
  }
})
</script>

<style scoped>
.chat-input-container {
  border-top: 1px solid var(--el-border-color-light);
  padding: 12px;
  background-color: var(--el-bg-color);
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.input-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>

