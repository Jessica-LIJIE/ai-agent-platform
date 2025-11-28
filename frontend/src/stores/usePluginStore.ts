import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Plugin } from '@/types/entity'
import {
  getPluginList,
  getPluginById,
  createPlugin,
  updatePlugin,
  deletePlugin
} from '@/api'

/**
 * 插件 Store
 * 管理插件的状态和 CRUD 操作
 */
export const usePluginStore = defineStore('plugin', () => {
  // 状态
  const pluginList = ref<Plugin[]>([])
  const loading = ref(false)
  const currentPlugin = ref<Plugin | null>(null)

  // 获取插件列表
  const fetchPluginList = async () => {
    loading.value = true
    try {
      pluginList.value = await getPluginList()
      return pluginList.value
    } catch (error) {
      console.error('获取插件列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 根据ID获取插件详情
  const fetchPluginById = async (id: string) => {
    loading.value = true
    try {
      currentPlugin.value = await getPluginById(id)
      return currentPlugin.value
    } catch (error) {
      console.error('获取插件详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建插件
  const addPlugin = async (plugin: Plugin) => {
    loading.value = true
    try {
      const created = await createPlugin(plugin)
      // 刷新列表
      await fetchPluginList()
      return created
    } catch (error) {
      console.error('创建插件失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新插件
  const editPlugin = async (id: string, plugin: Plugin) => {
    loading.value = true
    try {
      const updated = await updatePlugin(id, plugin)
      // 刷新列表
      await fetchPluginList()
      return updated
    } catch (error) {
      console.error('更新插件失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除插件
  const removePlugin = async (id: string) => {
    loading.value = true
    try {
      await deletePlugin(id)
      // 刷新列表
      await fetchPluginList()
    } catch (error) {
      console.error('删除插件失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 重置当前插件
  const resetCurrentPlugin = () => {
    currentPlugin.value = null
  }

  return {
    // 状态
    pluginList,
    loading,
    currentPlugin,
    // 方法
    fetchPluginList,
    fetchPluginById,
    addPlugin,
    editPlugin,
    removePlugin,
    resetCurrentPlugin
  }
})

