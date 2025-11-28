<template>
  <div class="chat-view-container">
    <el-row :gutter="20" class="chat-layout">
      <!-- 左侧：智能体选择 -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">选择智能体</span>
            </div>
          </template>

          <el-select
            v-model="selectedAgentId"
            placeholder="请选择智能体"
            filterable
            style="width: 100%"
            :loading="loadingAgents"
            @change="handleAgentChange"
          >
            <el-option
              v-for="agent in agents"
              :key="agent.id"
              :label="agent.name"
              :value="agent.id"
            >
              <div class="agent-option">
                <div class="agent-name">{{ agent.name }}</div>
                <div v-if="agent.description" class="agent-desc">{{ agent.description }}</div>
              </div>
            </el-option>
          </el-select>

          <div v-if="currentAgent" class="agent-info">
            <el-divider />
            <div class="info-item">
              <span class="info-label">状态：</span>
              <el-tag :type="currentAgent.status === 'published' ? 'success' : 'info'">
                {{ currentAgent.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </div>
            <div v-if="currentAgent.description" class="info-item">
              <span class="info-label">描述：</span>
              <span class="info-value">{{ currentAgent.description }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">模型：</span>
              <span class="info-value">{{ getModelId() || '未配置' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：对话区域 -->
      <el-col :span="18">
        <ChatPanel
          :agent-id="selectedAgentId"
          :llm-model-id="getModelId()"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAgentList } from '@/api/agent'
import type { Agent } from '@/types/entity'
import ChatPanel from '@/views/agent/components/ChatPanel.vue'

const route = useRoute()

// 智能体列表
const agents = ref<Agent[]>([])
const loadingAgents = ref(false)
const selectedAgentId = ref<string>('')

// 当前选中的智能体
const currentAgent = computed(() => {
  return agents.value.find((agent) => agent.id === selectedAgentId.value)
})

// 获取模型ID
const getModelId = (): string | undefined => {
  if (!currentAgent.value || !currentAgent.value.modelConfig) {
    return undefined
  }
  return (
    currentAgent.value.modelConfig.modelId ||
    currentAgent.value.modelConfig.model
  )
}

// 加载智能体列表
const loadAgents = async () => {
  loadingAgents.value = true
  try {
    agents.value = await getAgentList()
  } catch (error: any) {
    console.error('加载智能体列表失败:', error)
    ElMessage.error(error.message || '加载智能体列表失败')
  } finally {
    loadingAgents.value = false
  }
}

// 处理智能体切换
const handleAgentChange = (agentId: string) => {
  if (agentId && !getModelId()) {
    ElMessage.warning('该智能体未配置模型，无法进行对话')
  }
}

// 初始化
onMounted(async () => {
  await loadAgents()
  // 如果路由中有 agentId 参数，则自动选择该智能体
  const agentIdFromQuery = route.query.agentId as string
  if (agentIdFromQuery && agents.value.some(agent => agent.id === agentIdFromQuery)) {
    selectedAgentId.value = agentIdFromQuery
    handleAgentChange(agentIdFromQuery)
  }
})
</script>

<style scoped>
.chat-view-container {
  padding: 20px;
  height: calc(100vh - 40px);
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.chat-layout {
  height: 100%;
}

.chat-layout :deep(.el-col) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-layout :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.chat-layout :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.agent-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.agent-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.agent-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-info {
  margin-top: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: var(--el-text-color-primary);
  min-width: 50px;
}

.info-value {
  color: var(--el-text-color-secondary);
  flex: 1;
  word-break: break-word;
}
</style>

