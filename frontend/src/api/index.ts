import { http, USE_MOCK } from '@/utils/http'
import { mockDb } from '@/mock/data'

// 后端统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * Hello API
 */
export const getHello = async (): Promise<string> => {
  if (USE_MOCK) {
    return Promise.resolve('Hello from Mock Server!')
  }
  return http.get<ApiResponse<string>>('/v1/hello') as unknown as Promise<string>
}

// 导出所有 API
export * from './agent'
export * from './plugin'
export * from './chat'
export * from './llm'

// 导出类型定义
export * from '@/types/entity'
