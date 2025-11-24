import { http } from '@/utils/http'

// 后端统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// API 服务函数（响应拦截器已解包，直接返回 data）
export const getHello = (): Promise<string> => 
  http.get<ApiResponse<string>>('/v1/hello') as unknown as Promise<string>

