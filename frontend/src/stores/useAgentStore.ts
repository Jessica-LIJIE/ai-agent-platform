import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Agent } from '@/types/entity'
import {
  getAgentList,
  getAgentById,
  createAgent,
  updateAgent,
  deleteAgent
} from '@/api'

/**
 * 智能体 Store
 * 管理智能体的状态和 CRUD 操作
 */
export const useAgentStore = defineStore('agent', () => {
  // 状态
  const agentList = ref<Agent[]>([])
  const loading = ref(false)
  const currentAgent = ref<Agent | null>(null)

  // 获取智能体列表
  const fetchAgentList = async () => {
    loading.value = true
    try {
      agentList.value = await getAgentList()
      return agentList.value
    } catch (error) {
      console.error('获取智能体列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 根据ID获取智能体详情
  const fetchAgentById = async (id: string) => {
    loading.value = true
    try {
      currentAgent.value = await getAgentById(id)
      return currentAgent.value
    } catch (error) {
      console.error('获取智能体详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建智能体
  const addAgent = async (agent: Agent) => {
    loading.value = true
    try {
      const created = await createAgent(agent)
      // 刷新列表
      await fetchAgentList()
      return created
    } catch (error) {
      console.error('创建智能体失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新智能体
  const editAgent = async (id: string, agent: Agent) => {
    loading.value = true
    try {
      const updated = await updateAgent(id, agent)
      // 刷新列表
      await fetchAgentList()
      return updated
    } catch (error) {
      console.error('更新智能体失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除智能体
  const removeAgent = async (id: string) => {
    loading.value = true
    try {
      await deleteAgent(id)
      // 刷新列表
      await fetchAgentList()
    } catch (error) {
      console.error('删除智能体失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 重置当前智能体
  const resetCurrentAgent = () => {
    currentAgent.value = null
  }

  return {
    // 状态
    agentList,
    loading,
    currentAgent,
    // 方法
    fetchAgentList,
    fetchAgentById,
    addAgent,
    editAgent,
    removeAgent,
    resetCurrentAgent
  }
})

