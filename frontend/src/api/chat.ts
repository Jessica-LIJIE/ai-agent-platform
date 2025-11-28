import { http, USE_MOCK } from '@/utils/http'
import { mockDb } from '@/mock/data'
import type { AgentConversation } from '@/types/entity'

// 延迟函数，模拟网络延迟
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms))

// 生成 ID
const generateId = () => `mock-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

export interface CreateSessionResponse {
  session_id: string
}

/**
 * 创建会话
 */
export const createSession = async (): Promise<CreateSessionResponse> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const sessionId = generateId()
    mockDb.sessions.set(sessionId, {
      agentId: '',
      createdAt: new Date().toISOString()
    })
    return {
      session_id: sessionId
    }
  }
  return http.post('/v1/chat/session')
}

/**
 * 获取消息历史
 */
export const getMessageHistory = async (sessionId: string): Promise<AgentConversation[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return mockDb.conversations[sessionId] || []
  }
  return http.get(`/v1/chat/history/${sessionId}`)
}

/**
 * 发送消息
 */
export const sendMessage = async (conversation: AgentConversation): Promise<AgentConversation> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const { sessionId, agentId, query, metadata } = conversation

    // 模拟 AI 回答
    const mockAnswers: Record<string, string> = {
      '你好': '你好！我是一个AI助手，很高兴为你服务！',
      '你是谁': '我是一个AI助手，可以帮助你解答问题和完成任务。',
      '天气': '我可以通过温度查询插件为你查询室内温度信息。',
      '灯': '我可以帮你控制智能灯光。你想开启、关闭还是调整颜色？',
      '温度': '当前室内温度是26.5°C，湿度55%。'
    }

    let answer = `关于"${query}"，我已经理解了你的需求。`
    
    // 尝试匹配关键词
    for (const [keyword, mockAnswer] of Object.entries(mockAnswers)) {
      if (query.includes(keyword)) {
        answer = mockAnswer
        break
      }
    }

    const newConversation: AgentConversation = {
      id: generateId(),
      sessionId,
      agentId,
      userId: conversation.userId || 'user-003-tester',
      query,
      answer,
      metadata: {
        llmModelId: metadata?.llmModelId || 'model-001-qwen-turbo',
        promptTokens: Math.floor(Math.random() * 100) + 50,
        completionTokens: Math.floor(Math.random() * 100) + 30,
        totalTokens: Math.floor(Math.random() * 200) + 80
      },
      conversationType: 'chat',
      createTime: new Date().toISOString()
    }

    // 保存到会话历史
    if (!mockDb.conversations[sessionId]) {
      mockDb.conversations[sessionId] = []
    }
    mockDb.conversations[sessionId].push(newConversation)

    // 更新会话信息
    if (mockDb.sessions.has(sessionId)) {
      mockDb.sessions.set(sessionId, {
        agentId,
        createdAt: mockDb.sessions.get(sessionId)?.createdAt || new Date().toISOString()
      })
    }

    return { ...newConversation }
  }
  return http.post('/v1/chat/message', conversation)
}
