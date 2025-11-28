import { http, USE_MOCK } from '@/utils/http'
import { mockDb } from '@/mock/data'
import type { Agent } from '@/types/entity'

// 延迟函数，模拟网络延迟
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * 查询所有智能体
 */
export const getAgentList = async (): Promise<Agent[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return [...mockDb.agents]
  }
  return http.get('/v1/agents')
}

/**
 * 根据ID查询智能体
 */
export const getAgentById = async (id: string): Promise<Agent> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const agent = mockDb.agents.find((a) => a.id === id)
    if (!agent) {
      throw new Error('智能体不存在')
    }
    return { ...agent }
  }
  return http.get(`/v1/agents/${id}`)
}

/**
 * 创建智能体
 */
export const createAgent = async (agent: Agent): Promise<Agent> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const newAgent: Agent = {
      ...agent,
      id: `agent-${Date.now()}`,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
    }
    mockDb.agents.push(newAgent)
    return { ...newAgent }
  }
  return http.post('/v1/agents', agent)
}

/**
 * 更新智能体
 */
export const updateAgent = async (id: string, agent: Agent): Promise<Agent> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.agents.findIndex((a) => a.id === id)
    if (index === -1) {
      throw new Error('智能体不存在')
    }
    const updatedAgent: Agent = {
      ...mockDb.agents[index],
      ...agent,
      id,
      updateTime: new Date().toISOString()
    }
    mockDb.agents[index] = updatedAgent
    return { ...updatedAgent }
  }
  return http.put(`/v1/agents/${id}`, agent)
}

/**
 * 删除智能体
 */
export const deleteAgent = async (id: string): Promise<void> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.agents.findIndex((a) => a.id === id)
    if (index === -1) {
      throw new Error('智能体不存在')
    }
    mockDb.agents.splice(index, 1)
    return
  }
  return http.delete(`/v1/agents/${id}`)
}
