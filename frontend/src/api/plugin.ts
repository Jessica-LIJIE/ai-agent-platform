import { http, USE_MOCK } from '@/utils/http'
import { mockDb } from '@/mock/data'
import type { Plugin } from '@/types/entity'

// 延迟函数，模拟网络延迟
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * 查询所有插件
 */
export const getPluginList = async (): Promise<Plugin[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return [...mockDb.plugins]
  }
  return http.get('/v1/plugins')
}

/**
 * 根据ID查询插件
 */
export const getPluginById = async (id: string): Promise<Plugin> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const plugin = mockDb.plugins.find((p) => p.id === id)
    if (!plugin) {
      throw new Error('插件不存在')
    }
    return { ...plugin }
  }
  return http.get(`/v1/plugins/${id}`)
}

/**
 * 创建插件
 */
export const createPlugin = async (plugin: Plugin): Promise<Plugin> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const newPlugin: Plugin = {
      ...plugin,
      id: `plugin-${Date.now()}`,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
    }
    mockDb.plugins.push(newPlugin)
    return { ...newPlugin }
  }
  return http.post('/v1/plugins', plugin)
}

/**
 * 更新插件
 */
export const updatePlugin = async (id: string, plugin: Plugin): Promise<Plugin> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.plugins.findIndex((p) => p.id === id)
    if (index === -1) {
      throw new Error('插件不存在')
    }
    const updatedPlugin: Plugin = {
      ...mockDb.plugins[index],
      ...plugin,
      id,
      updateTime: new Date().toISOString()
    }
    mockDb.plugins[index] = updatedPlugin
    return { ...updatedPlugin }
  }
  return http.put(`/v1/plugins/${id}`, plugin)
}

/**
 * 删除插件
 */
export const deletePlugin = async (id: string): Promise<void> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const index = mockDb.plugins.findIndex((p) => p.id === id)
    if (index === -1) {
      throw new Error('插件不存在')
    }
    mockDb.plugins.splice(index, 1)
    return
  }
  return http.delete(`/v1/plugins/${id}`)
}
