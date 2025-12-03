/**
 * 实体类型定义
 * 对应后端 Java Entity 类
 */

/**
 * 智能体实体
 */
export interface Agent {
  id?: string
  name: string
  description?: string
  prompt?: string
  promptTemplate?: string
  modelConfig?: Record<string, any>
  status?: string // draft/published
  userId?: string
  workflowId?: string
  knowledgeBaseId?: string
  kbIds?: string[]
  toolsConfig?: string[] // 插件ID列表
  createTime?: string
  updateTime?: string
}

/**
 * 插件实体
 */
export interface Plugin {
  id?: string
  name: string
  identifier: string
  description?: string
  openapiSpec?: any // OpenAPI规范内容
  openapiSchema?: any // OpenAPI规范（别名字段）
  status?: string // enabled/disabled
  isEnabled?: boolean
  authInfo?: any // 鉴权信息
  authType?: string // none/api_key/oauth等
  authConfig?: any // 鉴权配置
  userId?: string
  createTime?: string
  updateTime?: string
}

/**
 * 对话元数据
 */
export interface ConversationMetadata {
  llmModelId?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  references?: string[]
  remarks?: string
}

/**
 * 插件调用信息
 */
export interface PluginCall {
  pluginId: string
  operationId: string
  params: Record<string, any>
}

/**
 * 智能体对话历史实体
 */
export interface AgentConversation {
  id?: string
  sessionId: string
  agentId: string
  userId?: string
  query: string
  answer?: string
  metadata?: ConversationMetadata
  conversationType?: string // chat/debug
  createTime?: string
  pluginCall?: PluginCall  // 插件调用信息
}

/**
 * LLM模型实体
 */
export interface LlmModel {
  id?: string
  name: string
  displayName?: string
  provider?: string
  modelType?: string
  apiBase?: string
  apiKey?: string
  apiVersion?: string
  maxTokens?: number
  temperature?: number
  topP?: number
  enableDeepThinking?: boolean
  frequencyPenalty?: number
  presencePenalty?: number
  config?: string
  description?: string
  isActive?: boolean
  isDefault?: boolean
  isSystem?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

/**
 * LLM提供商实体
 */
export interface LlmProvider {
  id?: string
  code: string
  name: string
  title?: string
  description?: string
  applyUrl?: string
  docUrl?: string
  defaultApiBase?: string
  hasFreeQuota?: boolean
  icon?: string
  tagType?: string
  country?: string
  sortOrder?: number
  isActive?: boolean
  createdAt?: string
  updatedAt?: string
}

