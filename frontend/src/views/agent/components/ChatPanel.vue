<template>
  <div class="chat-panel">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <span class="chat-title">对话测试</span>
          <div class="chat-header-actions">
            <el-button
              v-if="sessionId && boundPlugins.length > 0"
              :type="showPluginPanel ? 'primary' : 'default'"
              size="small"
              @click="showPluginPanel = !showPluginPanel"
            >
              <el-icon><Connection /></el-icon>
              插件 ({{ boundPlugins.length }})
            </el-button>
            <el-button
              v-if="sessionId"
              type="text"
              size="small"
              @click="handleNewSession"
            >
              新对话
            </el-button>
          </div>
        </div>
      </template>

      <!-- 插件选择面板 -->
      <div v-if="showPluginPanel && boundPlugins.length > 0" class="plugin-panel">
        <div class="plugin-panel-header">
          <span>选择要调用的插件</span>
          <el-switch
            v-model="enablePluginCall"
            active-text="启用"
            inactive-text="禁用"
            size="small"
          />
        </div>
        <div v-if="enablePluginCall" class="plugin-select-area">
          <!-- 设备UUID（全局参数） -->
          <div class="param-row">
            <span class="param-label">设备UUID<span class="required">*</span>:</span>
            <el-input
              v-model="deviceUuid"
              placeholder="输入设备UUID"
              size="small"
              style="width: 280px"
            />
          </div>
          
          <!-- 插件选择 -->
          <div class="param-row">
            <span class="param-label">插件:</span>
            <el-select
              v-model="selectedPluginId"
              placeholder="选择插件"
              size="small"
              style="width: 200px"
              @change="handlePluginChange"
            >
              <el-option
                v-for="plugin in boundPlugins"
                :key="plugin.id"
                :label="plugin.name"
                :value="plugin.id"
              />
            </el-select>
          </div>

          <!-- 操作选择 -->
          <div v-if="selectedPluginId && pluginOperations.length > 0" class="param-row">
            <span class="param-label">操作:</span>
            <el-select
              v-model="selectedOperationId"
              placeholder="选择操作"
              size="small"
              style="width: 200px"
              @change="handleOperationChange"
            >
              <el-option
                v-for="op in pluginOperations"
                :key="op.operationId"
                :label="op.name || op.operationId"
                :value="op.operationId"
              />
            </el-select>
          </div>
        </div>

        <!-- 根据操作类型显示智能参数选择 -->
        <div v-if="enablePluginCall && selectedOperationId" class="smart-params">
          <!-- getSensorData: 传感器查询 -->
          <template v-if="selectedOperationId === 'getSensorData'">
            <div class="param-row">
              <span class="param-label">传感器类型:</span>
              <el-select v-model="sensorType" placeholder="选择传感器" size="small" style="width: 200px">
                <el-option label="温度 (DHT11)" value="温度" />
                <el-option label="湿度 (DHT11)" value="湿度" />
                <el-option label="DS18B20温度" value="DS18B20温度" />
                <el-option label="雨水" value="雨水" />
                <el-option label="雨水级别" value="雨水级别" />
              </el-select>
            </div>
          </template>

          <!-- controlDevice: 设备控制 -->
          <template v-else-if="selectedOperationId === 'controlDevice'">
            <div class="param-row">
              <span class="param-label">设备类型:</span>
              <el-select v-model="portType" placeholder="选择设备" size="small" style="width: 150px">
                <el-option label="LED灯" value="led" />
                <el-option label="继电器" value="relay" />
                <el-option label="舵机" value="servo" />
                <el-option label="PWM" value="pwm" />
              </el-select>
            </div>
            <div class="param-row">
              <span class="param-label">端口号:</span>
              <el-select v-model="portId" placeholder="端口" size="small" style="width: 100px">
                <el-option v-for="i in 4" :key="i" :label="'端口 ' + i" :value="i" />
              </el-select>
            </div>
            <div class="param-row">
              <span class="param-label">动作:</span>
              <el-select v-model="deviceAction" placeholder="动作" size="small" style="width: 120px">
                <el-option label="打开" value="on" />
                <el-option label="关闭" value="off" />
                <el-option label="设置值" value="set" />
              </el-select>
            </div>
            <div v-if="deviceAction === 'set'" class="param-row">
              <span class="param-label">设置值:</span>
              <el-input-number
                v-model="deviceValue"
                :min="0"
                :max="portType === 'servo' ? 180 : 100"
                size="small"
                style="width: 150px"
              />
              <span class="param-hint">{{ portType === 'servo' ? '(舵机角度 0-180)' : '(PWM占空比 0-100)' }}</span>
            </div>
          </template>

          <!-- executePreset: 执行预设 -->
          <template v-else-if="selectedOperationId === 'executePreset'">
            <div class="param-row">
              <span class="param-label">预设名称:</span>
              <el-input
                v-model="presetName"
                placeholder="输入预设标识，如 led_blink_xxx"
                size="small"
                style="width: 280px"
              />
            </div>
          </template>
        </div>
      </div>

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
import { Loading, Connection } from '@element-plus/icons-vue'
import { createSession, getMessageHistory, sendMessage } from '@/api/chat'
import { getPluginDetail, getPluginOperations } from '@/api/plugin'
import { getAgentDetail } from '@/api/agent'
import type { AgentConversation, Plugin } from '@/types/entity'
import ChatMessage from '@/views/chat/components/ChatMessage.vue'
import ChatInput from '@/views/chat/components/ChatInput.vue'

interface PluginOperation {
  operationId: string
  method: string
  path: string
  summary?: string
  description?: string
  parameters?: Array<{
    name: string
    in: string
    required: boolean
    description?: string
    schema?: any
  }>
}

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

// ========== 插件调用相关状态 ==========
// 是否显示插件面板
const showPluginPanel = ref(false)
// 是否启用插件调用
const enablePluginCall = ref(false)
// 智能体绑定的插件列表
const boundPlugins = ref<Plugin[]>([])
// 选中的插件ID
const selectedPluginId = ref<string>('')
// 选中插件的操作列表
const pluginOperations = ref<PluginOperation[]>([])
// 选中的操作ID
const selectedOperationId = ref<string>('')

// ========== 智能参数（根据操作类型自动填充） ==========
// 全局：设备UUID
const deviceUuid = ref<string>('ab3b34d1-fae0-489b-80e8-19a8a6c7543d')
// getSensorData: 传感器类型
const sensorType = ref<string>('温度')
// controlDevice: 设备类型、端口、动作、值
const portType = ref<string>('led')
const portId = ref<number>(1)
const deviceAction = ref<string>('on')
const deviceValue = ref<number>(90)
// executePreset: 预设名称
const presetName = ref<string>('')

// 构建插件调用参数
const buildPluginParams = (): Record<string, any> => {
  switch (selectedOperationId.value) {
    case 'getSensorData':
      return {
        uuid: deviceUuid.value,
        sensor: sensorType.value
      }
    case 'controlDevice':
      const params: Record<string, any> = {
        device_uuid: deviceUuid.value,
        port_type: portType.value,
        port_id: portId.value,
        action: deviceAction.value
      }
      if (deviceAction.value === 'set') {
        params.value = deviceValue.value
      }
      return params
    case 'executePreset':
      return {
        device_uuid: deviceUuid.value,
        preset_name: presetName.value,
        parameters: {}
      }
    default:
      return {}
  }
}

// 验证参数是否完整
const validatePluginParams = (): string | null => {
  if (!deviceUuid.value) {
    return '请输入设备UUID'
  }
  switch (selectedOperationId.value) {
    case 'getSensorData':
      if (!sensorType.value) return '请选择传感器类型'
      break
    case 'controlDevice':
      if (!portType.value) return '请选择设备类型'
      if (!portId.value) return '请选择端口号'
      if (!deviceAction.value) return '请选择动作'
      break
    case 'executePreset':
      if (!presetName.value) return '请输入预设名称'
      break
  }
  return null
}

// 是否可以开始对话
const canStartChat = computed(() => {
  return !!props.agentId && !!props.llmModelId
})

// 加载智能体绑定的插件
const loadBoundPlugins = async () => {
  if (!props.agentId) {
    boundPlugins.value = []
    return
  }

  try {
    // 获取智能体详情
    const agent = await getAgentDetail(props.agentId)
    const toolsConfig = agent.toolsConfig || []
    
    if (toolsConfig.length === 0) {
      boundPlugins.value = []
      return
    }

    // 获取每个插件的详情
    const plugins: Plugin[] = []
    for (const pluginId of toolsConfig) {
      try {
        const plugin = await getPluginDetail(pluginId)
        if (plugin && plugin.isEnabled) {
          plugins.push(plugin)
        }
      } catch (e) {
        console.warn(`加载插件 ${pluginId} 失败:`, e)
      }
    }
    boundPlugins.value = plugins
  } catch (error) {
    console.error('加载智能体绑定的插件失败:', error)
    boundPlugins.value = []
  }
}

// 插件选择变化时，加载操作列表
const handlePluginChange = async (pluginId: string) => {
  selectedOperationId.value = ''
  pluginOperations.value = []

  if (!pluginId) return

  try {
    const operations = await getPluginOperations(pluginId)
    pluginOperations.value = operations
  } catch (error) {
    console.error('加载插件操作失败:', error)
    ElMessage.error('加载插件操作失败')
  }
}

// 操作变化时，重置相关参数
const handleOperationChange = () => {
  // 重置为默认值
  sensorType.value = '温度'
  portType.value = 'led'
  portId.value = 1
  deviceAction.value = 'on'
  deviceValue.value = 90
  presetName.value = ''
}

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

  // 构建插件调用参数
  let pluginCall: { pluginId: string; operationId: string; params: Record<string, any> } | undefined
  if (enablePluginCall.value && selectedPluginId.value && selectedOperationId.value) {
    // 验证参数
    const validationError = validatePluginParams()
    if (validationError) {
      ElMessage.warning(validationError)
      return
    }

    pluginCall = {
      pluginId: selectedPluginId.value,
      operationId: selectedOperationId.value,
      params: buildPluginParams()
    }
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
    // 发送消息到后端（包含插件调用信息）
    const response = await sendMessage({
      sessionId: sessionId.value,
      agentId: props.agentId,
      query: query,
      metadata: {
        llmModelId: props.llmModelId
      },
      pluginCall
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
    // 重新加载绑定的插件
    loadBoundPlugins()
  }
)

// 组件挂载时加载插件
onMounted(() => {
  loadBoundPlugins()
})

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

.chat-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
}

/* 插件面板样式 */
.plugin-panel {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.plugin-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
}

.plugin-select-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.smart-params {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.param-label {
  font-size: 13px;
  color: #606266;
  min-width: 80px;
  text-align: right;
}

.param-label .required {
  color: #f56c6c;
  margin-left: 2px;
}

.param-hint {
  font-size: 12px;
  color: #909399;
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

