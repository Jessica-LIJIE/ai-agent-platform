import { http, USE_MOCK } from '@/utils/http'
import { mockDb } from '@/mock/data'
import type { LlmModel, LlmProvider } from '@/types/entity'

// 延迟函数，模拟网络延迟
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * 查询所有 LLM 模型
 */
export const getLlmModelList = async (): Promise<LlmModel[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return [...mockDb.llmModels]
  }
  return http.get('/v1/llm/models')
}

/**
 * 根据ID查询 LLM 模型
 */
export const getLlmModelById = async (id: string): Promise<LlmModel> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const model = mockDb.llmModels.find((m) => m.id === id)
    if (!model) {
      throw new Error('模型不存在')
    }
    return { ...model }
  }
  return http.get(`/v1/llm/models/${id}`)
}

/**
 * 创建 LLM 模型
 */
export const createLlmModel = async (model: LlmModel): Promise<LlmModel> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const newModel: LlmModel = {
      ...model,
      id: `model-${Date.now()}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    mockDb.llmModels.push(newModel)
    return { ...newModel }
  }
  return http.post('/v1/llm/models', model)
}

/**
 * 更新 LLM 模型
 */
export const updateLlmModel = async (id: string, model: LlmModel): Promise<LlmModel> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.llmModels.findIndex((m) => m.id === id)
    if (index === -1) {
      throw new Error('模型不存在')
    }
    const updatedModel: LlmModel = {
      ...mockDb.llmModels[index],
      ...model,
      id,
      updatedAt: new Date().toISOString()
    }
    mockDb.llmModels[index] = updatedModel
    return { ...updatedModel }
  }
  return http.put(`/v1/llm/models/${id}`, model)
}

/**
 * 删除 LLM 模型
 */
export const deleteLlmModel = async (id: string): Promise<void> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.llmModels.findIndex((m) => m.id === id)
    if (index === -1) {
      throw new Error('模型不存在')
    }
    mockDb.llmModels.splice(index, 1)
    return
  }
  return http.delete(`/v1/llm/models/${id}`)
}

/**
 * 查询所有 LLM 提供商
 */
export const getLlmProviderList = async (): Promise<LlmProvider[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return [...mockDb.llmProviders]
  }
  return http.get('/v1/llm/providers')
}
