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
  // 后端返回分页格式 { list: [...], total: n }，这里提取 list
  const response: any = await http.get('/v1/plugins')
  const list = response?.list || response || []
  
  // 后端返回 is_enabled (下划线)，前端使用 isEnabled (驼峰)，需要映射
  return list.map((item: any) => ({
    ...item,
    isEnabled: item.is_enabled ?? item.isEnabled ?? false,
    createTime: item.create_time || item.createTime,
    updateTime: item.update_time || item.updateTime
  }))
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
 * 如果 openapiSpec 包含 paths，自动使用 OpenAPI 导入接口
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
  
  // 检查是否有 OpenAPI 规范（包含 paths）
  const spec = plugin.openapiSpec
  console.log('[createPlugin] openapiSpec:', spec)
  console.log('[createPlugin] typeof spec:', typeof spec)
  console.log('[createPlugin] has paths:', spec && typeof spec === 'object' && 'paths' in spec)
  
  if (spec && typeof spec === 'object' && 'paths' in spec && Object.keys((spec as any).paths || {}).length > 0) {
    console.log('[createPlugin] 使用 OpenAPI 导入接口')
    // 使用 OpenAPI 导入接口
    return importFromOpenApi(plugin)
  }
  
  console.log('[createPlugin] 使用普通创建接口')
  return http.post('/v1/plugins', plugin)
}

/**
 * 通过 OpenAPI 规范导入插件
 * 会自动解析 paths 创建 operations
 */
export const importFromOpenApi = async (plugin: Plugin): Promise<Plugin> => {
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
  
  const spec = plugin.openapiSpec as any
  
  // 从 OpenAPI 规范中提取 baseUrl
  let baseUrl = ''
  if (spec?.servers && Array.isArray(spec.servers) && spec.servers.length > 0) {
    baseUrl = spec.servers[0].url || ''
  }
  
  // 构建导入请求
  const importRequest = {
    name: plugin.name,
    description: plugin.description || spec?.info?.description || '',
    type: 'openapi',
    baseUrl: baseUrl,
    openapiSpec: spec,
    authType: plugin.authType || 'none',
    authConfig: plugin.authConfig || {}
  }
  
  return http.post('/v1/plugins/import-openapi', importRequest)
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

/**
 * 更新插件状态（启用/禁用）
 */
export const updatePluginStatus = async (id: string, isEnabled: boolean): Promise<Plugin> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    const plugin = mockDb.plugins.find((p) => p.id === id)
    if (!plugin) {
      throw new Error('插件不存在')
    }
    plugin.isEnabled = isEnabled
    plugin.status = isEnabled ? 'enabled' : 'disabled'
    return { ...plugin }
  }
  // 后端使用 PATCH 方法
  return http.patch(`/v1/plugins/${id}/status`, { isEnabled })
}

/**
 * 获取插件的操作列表
 */
export const getPluginOperations = async (pluginId: string): Promise<any[]> => {
  if (USE_MOCK) {
    await delay(200 + Math.random() * 300)
    return []
  }
  const response: any = await http.get(`/v1/plugins/${pluginId}`)
  return response?.operations || []
}

/**
 * 调用插件操作
 */
export const invokePluginOperation = async (
  pluginId: string,
  operationId: string,
  params: Record<string, any>,
  timeout?: number
): Promise<any> => {
  if (USE_MOCK) {
    await delay(500 + Math.random() * 500)
    return {
      status: 'success',
      httpStatusCode: 200,
      rawBody: '{"code":200,"data":{"value":25.5,"unit":"°C"}}',
      parsedData: { code: 200, data: { value: 25.5, unit: '°C' } },
      duration: 150
    }
  }
  return http.post(`/v1/plugins/${pluginId}/operations/${operationId}/invoke`, {
    params,
    timeout: timeout || 30000
  })
}

/**
 * 获取插件详情（getPluginById的别名）
 */
export const getPluginDetail = getPluginById
