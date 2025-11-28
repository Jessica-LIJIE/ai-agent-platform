/**
 * Mock 数据
 * 基于后端数据库的真实数据
 */
import type { Agent, Plugin, LlmModel, LlmProvider, AgentConversation } from '@/types/entity'

// Mock 插件数据（基于 data.sql）
export const mockPlugins: Plugin[] = [
  {
    id: 'plugin-001-led',
    name: '智能灯光控制',
    identifier: 'led_controller',
    description: '用于开启、关闭和调整智能LED灯的亮度或颜色。',
    status: 'enabled',
    isEnabled: true,
    authType: 'api_key',
    authConfig: {
      apiKey: 'demo-key'
    },
    openapiSpec: {
      openapi: '3.0.0',
      info: {
        title: 'LED Control API'
      },
      paths: {
        '/light/on': {},
        '/light/off': {}
      }
    },
    userId: 'user-002-home',
    createTime: '2025-11-15T10:00:00',
    updateTime: '2025-11-15T10:00:00'
  },
  {
    id: 'plugin-002-temp',
    name: '室内温度查询',
    identifier: 'temperature_sensor',
    description: '获取当前房间的实时温度和湿度数据。',
    status: 'enabled',
    isEnabled: true,
    authType: 'none',
    authConfig: {},
    openapiSpec: {
      openapi: '3.0.0',
      info: {
        title: 'Temperature API'
      },
      paths: {
        '/sensor/current_temp': {}
      }
    },
    userId: 'user-002-home',
    createTime: '2025-11-15T11:30:00',
    updateTime: '2025-11-15T11:30:00'
  },
  {
    id: 'plugin-003-calendar',
    name: '家庭日程提醒',
    identifier: 'family_calendar',
    description: '用于查询和添加家庭共享日历事件。',
    status: 'disabled',
    isEnabled: false,
    authType: 'oauth',
    authConfig: {},
    openapiSpec: {
      openapi: '3.0.0',
      info: {
        title: 'Calendar API'
      },
      paths: {
        '/events': {}
      }
    },
    userId: 'user-004-dev',
    createTime: '2025-11-18T14:00:00',
    updateTime: '2025-11-18T14:00:00'
  }
]

// Mock 智能体数据（基于 data.sql）
export const mockAgents: Agent[] = [
  {
    id: 'agent-001-smarthome',
    name: '智能家居助理',
    description: '你可以控制家里的LED灯，查询室内温度，并能回答关于设备文档的问题。',
    prompt: '你是一个友好的智能家居助理，请优先使用插件执行操作，如果用户提问关于设备的问题，请参考知识库。',
    promptTemplate: '你是一个友好的智能家居助理，请优先使用插件执行操作，如果用户提问关于设备的问题，请参考知识库。',
    status: 'published',
    modelConfig: {
      model: 'gpt-4-turbo',
      temperature: 0.2
    },
    userId: 'user-002-home',
    workflowId: 'wf-001-home-ctrl',
    kbIds: ['kb-001-dev', 'kb-002-faq'],
    toolsConfig: ['plugin-001-led', 'plugin-002-temp'],
    createTime: '2025-11-22T10:00:00',
    updateTime: '2025-11-22T10:00:00'
  },
  {
    id: 'agent-002-scheduler',
    name: '日程管理Agent',
    description: '专门用于处理家庭日程、提醒和日历查询。',
    prompt: '你是一个日程管理专家，请利用日历插件帮助用户安排生活。',
    promptTemplate: '你是一个日程管理专家，请利用日历插件帮助用户安排生活。',
    status: 'draft',
    modelConfig: {
      model: 'gpt-3.5-turbo',
      temperature: 0.5
    },
    userId: 'user-002-home',
    kbIds: [],
    toolsConfig: ['plugin-003-calendar'],
    createTime: '2025-11-23T09:30:00',
    updateTime: '2025-11-23T09:30:00'
  }
]

// Mock LLM 提供商数据（基于 data.sql）
export const mockLlmProviders: LlmProvider[] = [
  {
    id: 'provider-001-qwen',
    code: 'qwen',
    name: '通义千问',
    title: '通义千问',
    description: '阿里云通义千问（模型服务平台百炼）',
    applyUrl: 'https://dashscope.console.aliyun.com/',
    docUrl: 'https://help.aliyun.com/zh/model-studio/qwen-api-reference',
    defaultApiBase: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    hasFreeQuota: true,
    tagType: 'primary',
    country: 'cn',
    sortOrder: 1,
    isActive: true,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'provider-002-doubao',
    code: 'doubao',
    name: '豆包',
    title: '豆包',
    description: '火山引擎豆包（字节跳动）',
    applyUrl: 'https://console.volcengine.com/ark',
    docUrl: 'https://www.volcengine.com/docs/82379/1330310',
    defaultApiBase: 'https://ark.cn-beijing.volces.com/api/v3',
    hasFreeQuota: true,
    tagType: 'success',
    country: 'cn',
    sortOrder: 10,
    isActive: true,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'provider-003-openai',
    code: 'openai',
    name: 'OpenAI',
    title: 'OpenAI GPT系列',
    description: 'OpenAI 提供的 GPT 系列大语言模型，包括 GPT-3.5、GPT-4 等，业界领先的对话和生成能力。',
    applyUrl: 'https://platform.openai.com/',
    docUrl: 'https://platform.openai.com/docs/api-reference',
    defaultApiBase: 'https://api.openai.com/v1',
    hasFreeQuota: false,
    tagType: 'info',
    country: 'us',
    sortOrder: 20,
    isActive: true,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  }
]

// Mock LLM 模型数据（基于 data.sql）
export const mockLlmModels: LlmModel[] = [
  {
    id: 'model-001-qwen-turbo',
    name: 'qwen-turbo',
    displayName: '通义千问-Turbo',
    provider: 'qwen',
    modelType: 'chat',
    apiBase: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 8192,
    temperature: 0.70,
    topP: 0.90,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: '阿里云通义千问大语言模型，性能强劲，响应快速，适合对话场景',
    isActive: true,
    isDefault: true,
    isSystem: true,
    sortOrder: 1,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'model-002-qwen-plus',
    name: 'qwen-plus',
    displayName: '通义千问-Plus',
    provider: 'qwen',
    modelType: 'chat',
    apiBase: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 32768,
    temperature: 0.70,
    topP: 0.90,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: '阿里云通义千问Plus版本，更强大的理解和生成能力',
    isActive: true,
    isDefault: false,
    isSystem: true,
    sortOrder: 2,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'model-003-qwen-max',
    name: 'qwen-max',
    displayName: '通义千问-Max',
    provider: 'qwen',
    modelType: 'chat',
    apiBase: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 8192,
    temperature: 0.70,
    topP: 0.90,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: '阿里云通义千问Max版本，最强理解能力，适合复杂任务',
    isActive: true,
    isDefault: false,
    isSystem: true,
    sortOrder: 3,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'model-004-doubao-pro',
    name: 'doubao-pro-32k',
    displayName: '豆包-Pro-32k',
    provider: 'doubao',
    modelType: 'chat',
    apiBase: 'https://ark.cn-beijing.volces.com/api/v3',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 32768,
    temperature: 0.70,
    topP: 0.90,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: '字节跳动豆包Pro版本，支持32k上下文，适合长文本处理',
    isActive: true,
    isDefault: false,
    isSystem: true,
    sortOrder: 10,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'model-005-gpt35-turbo',
    name: 'gpt-3.5-turbo',
    displayName: 'GPT-3.5 Turbo',
    provider: 'openai',
    modelType: 'chat',
    apiBase: 'https://api.openai.com/v1',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 4096,
    temperature: 0.70,
    topP: 1.00,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: 'OpenAI GPT-3.5 Turbo 模型，快速高效，性价比高',
    isActive: true,
    isDefault: false,
    isSystem: true,
    sortOrder: 20,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  },
  {
    id: 'model-006-gpt4',
    name: 'gpt-4',
    displayName: 'GPT-4',
    provider: 'openai',
    modelType: 'chat',
    apiBase: 'https://api.openai.com/v1',
    apiKey: 'YOUR_API_KEY_HERE',
    maxTokens: 8192,
    temperature: 0.70,
    topP: 1.00,
    enableDeepThinking: false,
    frequencyPenalty: 0.00,
    presencePenalty: 0.00,
    description: 'OpenAI GPT-4 模型，更强大的推理和理解能力',
    isActive: true,
    isDefault: false,
    isSystem: true,
    sortOrder: 21,
    createdAt: '2025-11-24T00:00:00',
    updatedAt: '2025-11-24T00:00:00'
  }
]

// Mock 对话历史数据（基于 data.sql）
const mockConversations: Record<string, AgentConversation[]> = {
  'sess-home-001': [
    {
      id: 'conv-001',
      sessionId: 'sess-home-001',
      agentId: 'agent-001-smarthome',
      userId: 'user-003-tester',
      query: '帮我把客厅的灯打开。',
      answer: '好的，已调用 [智能灯光控制] 插件，客厅灯已开启。',
      metadata: {
        llmModelId: 'model-001-qwen-turbo',
        promptTokens: 50,
        completionTokens: 30,
        totalTokens: 80
      },
      conversationType: 'chat',
      createTime: '2025-11-23T11:00:00'
    },
    {
      id: 'conv-002',
      sessionId: 'sess-home-001',
      agentId: 'agent-001-smarthome',
      userId: 'user-003-tester',
      query: '现在房间里温度是多少？',
      answer: '正在查询... [室内温度查询] 插件返回：当前室内温度是26.5°C，湿度55%。',
      metadata: {
        llmModelId: 'model-001-qwen-turbo',
        promptTokens: 65,
        completionTokens: 35,
        totalTokens: 100
      },
      conversationType: 'chat',
      createTime: '2025-11-23T11:01:30'
    }
  ],
  'sess-home-002': [
    {
      id: 'conv-003',
      sessionId: 'sess-home-002',
      agentId: 'agent-001-smarthome',
      userId: 'user-003-tester',
      query: 'LED灯的手册在哪里？',
      answer: '根据知识库[智能设备开发文档]，LED灯的手册是 "LED_Manual.pdf"，请查阅。',
      metadata: {
        llmModelId: 'model-001-qwen-turbo',
        promptTokens: 80,
        completionTokens: 40,
        totalTokens: 120
      },
      conversationType: 'chat',
      createTime: '2025-11-24T10:00:00'
    }
  ],
  'sess-home-003': [
    {
      id: 'conv-004',
      sessionId: 'sess-home-003',
      agentId: 'agent-001-smarthome',
      userId: 'user-003-tester',
      query: '怎么给设备排除故障？',
      answer: '请参考知识库 [常见问题解答] 中的 "Troubleshooting.md"，里面提供了详细的故障排除步骤。',
      metadata: {
        llmModelId: 'model-001-qwen-turbo',
        promptTokens: 90,
        completionTokens: 45,
        totalTokens: 135
      },
      conversationType: 'chat',
      createTime: '2025-11-24T11:00:00'
    },
    {
      id: 'conv-005',
      sessionId: 'sess-home-003',
      agentId: 'agent-001-smarthome',
      userId: 'user-003-tester',
      query: '把卧室灯调成红色。',
      answer: '已调用 [智能灯光控制] 插件，卧室灯颜色已调整为红色。',
      metadata: {
        llmModelId: 'model-001-qwen-turbo',
        promptTokens: 70,
        completionTokens: 35,
        totalTokens: 105
      },
      conversationType: 'chat',
      createTime: '2025-11-24T11:01:30'
    }
  ]
}

// 导出 mock 数据存储（模拟数据库）
export const mockDb = {
  plugins: [...mockPlugins],
  agents: [...mockAgents],
  llmModels: [...mockLlmModels],
  llmProviders: [...mockLlmProviders],
  conversations: { ...mockConversations },
  sessions: new Map<string, { agentId: string; createdAt: string }>()
}
