<template>
  <div class="plugin-list-container">
    <div class="page-header">
      <h2>插件管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-row :gutter="16">
        <el-col :span="10">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索插件名称、标识符或描述"
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
            <el-option label="已启用" value="enabled" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </el-col>
        <el-col :span="8" style="text-align: right;">
          <el-button @click="resetFilters">重置筛选</el-button>
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            新增插件
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 插件列表 - 卡片形式 -->
    <div v-loading="pluginStore.loading" class="plugins-grid">
      <el-empty v-if="!pluginStore.loading && filteredPluginList.length === 0" description="暂无插件数据">
        <el-button type="primary" @click="handleCreate">创建第一个插件</el-button>
      </el-empty>
      
      <el-card
        v-for="plugin in filteredPluginList"
        :key="plugin.id"
        class="plugin-card"
        shadow="hover"
        :body-style="{ padding: '0' }"
      >
        <div class="card-header">
          <div class="header-top">
            <div class="plugin-icon">
              <el-icon size="24"><Connection /></el-icon>
            </div>
            <div class="plugin-info">
              <h3 class="plugin-name">{{ plugin.name }}</h3>
              <div class="plugin-badges">
                <el-tag :type="plugin.isEnabled || plugin.status === 'enabled' ? 'success' : 'info'" size="small">
                  {{ plugin.isEnabled || plugin.status === 'enabled' ? '已启用' : '已禁用' }}
                </el-tag>
                <el-tag v-if="!plugin.userId" type="warning" size="small">系统插件</el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <p class="plugin-description">{{ plugin.description || '暂无描述' }}</p>
          
          <div class="plugin-stats">
            <div class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ plugin.identifier || 'N/A' }}</span>
            </div>
            <div class="stat-item" v-if="plugin.openapiSpec">
              <el-icon><Link /></el-icon>
              <span>{{ Object.keys(plugin.openapiSpec?.paths || {}).length }} 个API</span>
            </div>
            <div class="stat-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(plugin.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <el-button type="primary" size="small" @click="handleEdit(plugin)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(plugin)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 插件编辑/创建弹窗 -->
    <PluginDialog
      v-model="dialogVisible"
      :plugin="currentPlugin"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Connection, Document, Link, Clock } from '@element-plus/icons-vue'
import { usePluginStore } from '@/stores/usePluginStore'
import type { Plugin } from '@/types/entity'
import PluginDialog from './PluginDialog.vue'

const pluginStore = usePluginStore()

// 搜索关键词
const searchKeyword = ref('')
// 状态筛选
const filterStatus = ref<string>('')
// 弹窗显示状态
const dialogVisible = ref(false)
// 当前编辑的插件
const currentPlugin = ref<Plugin | null>(null)

// 过滤后的插件列表
const filteredPluginList = computed(() => {
  let list = pluginStore.pluginList

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(
      (plugin) =>
        plugin.name?.toLowerCase().includes(keyword) ||
        plugin.identifier?.toLowerCase().includes(keyword) ||
        plugin.description?.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (filterStatus.value) {
    list = list.filter((plugin) => {
      if (filterStatus.value === 'enabled') {
        return plugin.isEnabled || plugin.status === 'enabled'
      } else {
        return !plugin.isEnabled && plugin.status !== 'enabled'
      }
    })
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

// 创建插件
const handleCreate = () => {
  currentPlugin.value = null
  dialogVisible.value = true
}

// 编辑插件
const handleEdit = (plugin: Plugin) => {
  currentPlugin.value = { ...plugin }
  dialogVisible.value = true
}

// 删除插件
const handleDelete = async (plugin: Plugin) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除插件 "${plugin.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await pluginStore.removePlugin(plugin.id!)
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除插件失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 弹窗成功回调
const handleDialogSuccess = () => {
  dialogVisible.value = false
  currentPlugin.value = null
}

// 初始化加载数据
onMounted(async () => {
  try {
    await pluginStore.fetchPluginList()
  } catch (error: any) {
    ElMessage.error(error.message || '加载插件列表失败')
  }
})
</script>

<style scoped>
.plugin-list-container {
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
.plugins-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

/* 插件卡片 */
.plugin-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid var(--border-light);
  background: #ffffff;
}

.plugin-card:hover {
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

.plugin-icon {
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

.plugin-info {
  flex: 1;
  min-width: 0;
}

.plugin-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.plugin-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 卡片主体 */
.card-body {
  padding: 20px;
  background: #ffffff;
}

.plugin-description {
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

.plugin-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  .plugins-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .plugins-grid {
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
