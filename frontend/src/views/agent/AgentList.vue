<template>
  <div class="agent-list-container">
    <div class="page-header">
      <h2>智能体管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-row :gutter="16">
        <el-col :span="10">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索智能体名称、描述或提示词"
            clearable
            :prefix-icon="Search"
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="filterStatus"
            placeholder="筛选状态"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-col>
        <el-col :span="8" style="text-align: right;">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            新增智能体
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 智能体列表 - 卡片形式 -->
    <div v-loading="agentStore.loading" class="agents-grid">
      <el-empty v-if="!agentStore.loading && filteredAgentList.length === 0" description="暂无智能体数据">
        <el-button type="primary" @click="handleCreate">创建第一个智能体</el-button>
      </el-empty>
      
      <el-card
        v-for="agent in filteredAgentList"
        :key="agent.id"
        class="agent-card"
        shadow="hover"
        :body-style="{ padding: '0' }"
      >
        <div class="card-header">
          <div class="header-top">
            <div class="agent-icon">
              <el-icon size="24"><ChatDotRound /></el-icon>
            </div>
            <div class="agent-info">
              <h3 class="agent-name">{{ agent.name }}</h3>
              <div class="agent-badges">
                <el-tag :type="agent.status === 'published' ? 'success' : 'info'" size="small">
                  {{ agent.status === 'published' ? '已发布' : '草稿' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <p class="agent-description">{{ agent.description || '暂无描述' }}</p>
          
          <div class="agent-stats">
            <div class="stat-item">
              <el-icon><Connection /></el-icon>
              <span>{{ agent.toolsConfig?.length || 0 }} 个插件</span>
            </div>
            <div class="stat-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(agent.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <el-button type="success" size="small" @click="handleChat(agent)">
            <el-icon><ChatDotSquare /></el-icon>
            对话
          </el-button>
          <el-button type="primary" size="small" @click="handleEdit(agent)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(agent)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, ChatDotRound, ChatDotSquare, Connection, Clock } from '@element-plus/icons-vue'
import { useAgentStore } from '@/stores/useAgentStore'
import type { Agent } from '@/types/entity'

const router = useRouter()
const agentStore = useAgentStore()

// 搜索关键词
const searchKeyword = ref('')
// 状态筛选
const filterStatus = ref<string>('')

// 过滤后的智能体列表
const filteredAgentList = computed(() => {
  let list = agentStore.agentList

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(
      (agent) =>
        agent.name?.toLowerCase().includes(keyword) ||
        agent.description?.toLowerCase().includes(keyword) ||
        agent.prompt?.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (filterStatus.value) {
    list = list.filter((agent) => agent.status === filterStatus.value)
  }

  return list
})

// 格式化日期时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
}

// 创建智能体
const handleCreate = () => {
  router.push({ name: 'agent-editor', params: { id: 'new' } })
}

// 对话智能体
const handleChat = (agent: Agent) => {
  router.push({ name: 'chat', query: { agentId: agent.id } })
}

// 编辑智能体
const handleEdit = (agent: Agent) => {
  router.push({ name: 'agent-editor', params: { id: agent.id! } })
}

// 删除智能体
const handleDelete = async (agent: Agent) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体 "${agent.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await agentStore.removeAgent(agent.id!)
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除智能体失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 初始化加载数据
onMounted(async () => {
  try {
    await agentStore.fetchAgentList()
  } catch (error: any) {
    ElMessage.error(error.message || '加载智能体列表失败')
  }
})
</script>

<style scoped>
.agent-list-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.filter-section {
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 卡片网格布局 */
.agents-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

/* 智能体卡片 */
.agent-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid var(--border-light);
  background: #ffffff;
}

.agent-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.agent-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
}

.agent-info {
  flex: 1;
  min-width: 0;
}

.agent-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 卡片主体 */
.card-body {
  padding: 20px;
  background: #ffffff;
}

.agent-description {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-item .el-icon {
  font-size: 16px;
}

/* 卡片底部 */
.card-footer {
  padding: 12px 20px;
  background: #f5f7fa;
  display: flex;
  gap: 8px;
  border-top: 1px solid var(--border-light);
}

.card-footer .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .agents-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .agents-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    padding: 12px;
  }
  
  .filter-section .el-row .el-col {
    margin-bottom: 12px;
  }
  
  .filter-section .el-col[style*="text-align: right"] {
    text-align: left !important;
  }
}
</style>
