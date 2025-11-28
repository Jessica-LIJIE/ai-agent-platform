<template>
  <div class="agent-editor-container">
    <el-row :gutter="20" class="editor-layout">
      <!-- 表单编辑器 -->
      <el-col :span="24">
        <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
            <span class="card-title">{{ isEdit ? '编辑智能体' : '新建智能体' }}</span>
          </div>
          <div class="header-right">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="loading" @click="handleSave">
              {{ isEdit ? '保存' : '创建' }}
            </el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="智能体名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入智能体名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="draft">草稿</el-radio>
                <el-radio label="published">已发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入智能体描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="系统提示词" prop="prompt">
          <el-input
            v-model="formData.prompt"
            type="textarea"
            :rows="6"
            placeholder="请输入系统提示词，定义智能体的角色和行为..."
            maxlength="2000"
            show-word-limit
          />
          <div class="form-tip">
            提示词用于定义智能体的角色、行为准则和回答风格
          </div>
        </el-form-item>

        <el-form-item label="提示词模板" prop="promptTemplate">
          <el-input
            v-model="formData.promptTemplate"
            type="textarea"
            :rows="4"
            placeholder="请输入提示词模板（可选）"
            maxlength="1000"
            show-word-limit
          />
          <div class="form-tip">可以使用模板变量，如 &#123;&#123;variable&#125;&#125;</div>
        </el-form-item>

        <!-- 模型配置 -->
        <el-divider content-position="left">模型配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择模型">
              <el-select
                v-model="modelConfigForm.modelId"
                placeholder="请选择模型"
                clearable
                filterable
                style="width: 100%"
                :loading="loadingModels"
                @visible-change="loadModelsIfNeeded"
              >
                <el-option
                  v-for="model in llmModels"
                  :key="model.id"
                  :label="getModelLabel(model)"
                  :value="model.id"
                  :disabled="!model.isActive"
                >
                  <div class="model-option">
                    <div class="model-option-name">{{ model.displayName || model.name }}</div>
                    <div class="model-option-info">
                      <el-tag v-if="model.provider" size="small" type="info">{{ model.provider }}</el-tag>
                      <el-tag v-if="model.isDefault" size="small" type="success">默认</el-tag>
                      <span v-if="model.description" class="model-desc">{{ model.description }}</span>
                    </div>
                  </div>
                </el-option>
              </el-select>
              <div class="form-tip">选择智能体使用的语言模型</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-slider
                v-model="modelConfigForm.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                show-input
                :show-input-controls="false"
              />
              <div class="form-tip">控制输出的随机性，范围 0-2</div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 关联配置 -->
        <el-divider content-position="left">关联配置</el-divider>

        <el-form-item label="知识库ID">
          <el-input
            v-model="formData.knowledgeBaseId"
            placeholder="请输入知识库ID（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="知识库ID列表">
          <el-input
            v-model="kbIdsString"
            type="textarea"
            :rows="2"
            placeholder="请输入知识库ID列表，多个ID用逗号分隔（可选）"
            @blur="handleKbIdsChange"
          />
        </el-form-item>

        <el-form-item label="绑定插件">
          <el-select
            v-model="selectedPluginIds"
            multiple
            placeholder="请选择插件（可选）"
            clearable
            filterable
            style="width: 100%"
            :loading="loadingPlugins"
            @visible-change="loadPluginsIfNeeded"
            @change="handlePluginsChange"
          >
            <el-option
              v-for="plugin in availablePlugins"
              :key="plugin.id"
              :label="plugin.name"
              :value="plugin.id!"
              :disabled="!plugin.isEnabled && plugin.status !== 'enabled'"
            >
              <div class="plugin-option">
                <div class="plugin-option-name">
                  {{ plugin.name }}
                  <el-tag v-if="plugin.status === 'enabled' || plugin.isEnabled" size="small" type="success">已启用</el-tag>
                  <el-tag v-else size="small" type="info">已禁用</el-tag>
                  <el-tag v-if="!plugin.userId" size="small" type="warning">系统</el-tag>
                </div>
                <div v-if="plugin.description" class="plugin-option-desc">{{ plugin.description }}</div>
                <div class="plugin-option-id">
                  <span class="identifier-text">标识符: {{ plugin.identifier }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
          <div class="form-tip">为智能体选择可用的插件工具</div>
          <div v-if="selectedPluginIds.length > 0" class="selected-plugins">
            <div class="selected-count">已选择 {{ selectedPluginIds.length }} 个插件</div>
          </div>
        </el-form-item>
      </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAgentStore } from '@/stores/useAgentStore'
import { getLlmModelList } from '@/api/llm'
import { getPluginList } from '@/api'
import type { Agent, LlmModel, Plugin } from '@/types/entity'

const route = useRoute()
const router = useRouter()
const agentStore = useAgentStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

// 是否为编辑模式
const agentId = computed(() => route.params.id as string)
const isEdit = computed(() => agentId.value && agentId.value !== 'new')

// 表单数据
const formData = ref<Agent>({
  name: '',
  description: '',
  prompt: '',
  promptTemplate: '',
  status: 'draft',
  knowledgeBaseId: '',
  kbIds: [],
  toolsConfig: []
})

// 模型配置表单（从 modelConfig 中提取）
const modelConfigForm = ref({
  modelId: '',
  temperature: 0.7
})

// 知识库ID字符串（用于输入）
const kbIdsString = ref('')
// 选中的插件ID列表（用于多选下拉框）
const selectedPluginIds = ref<string[]>([])

// LLM模型列表
const llmModels = ref<LlmModel[]>([])
const loadingModels = ref(false)
const modelsLoaded = ref(false)

// 插件列表
const availablePlugins = ref<Plugin[]>([])
const loadingPlugins = ref(false)
const pluginsLoaded = ref(false)

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入智能体名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' }
  ],
  prompt: [
    { max: 2000, message: '提示词不能超过 2000 个字符', trigger: 'blur' }
  ]
}

// 处理知识库ID列表变化
const handleKbIdsChange = () => {
  if (kbIdsString.value.trim()) {
    formData.value.kbIds = kbIdsString.value
      .split(',')
      .map((id) => id.trim())
      .filter((id) => id.length > 0)
  } else {
    formData.value.kbIds = []
  }
}

// 处理插件ID列表变化
const handlePluginsChange = (value: string[]) => {
  formData.value.toolsConfig = value || []
}

// 获取模型标签
const getModelLabel = (model: LlmModel) => {
  return model.displayName || model.name
}

// 加载模型列表
const loadModels = async () => {
  if (modelsLoaded.value) return
  
  loadingModels.value = true
  try {
    llmModels.value = await getLlmModelList()
    // 只显示激活的模型
    llmModels.value = llmModels.value.filter((model) => model.isActive !== false)
    modelsLoaded.value = true
  } catch (error: any) {
    console.error('加载模型列表失败:', error)
    ElMessage.error(error.message || '加载模型列表失败')
  } finally {
    loadingModels.value = false
  }
}

// 需要时加载模型列表
const loadModelsIfNeeded = (visible: boolean) => {
  if (visible && !modelsLoaded.value) {
    loadModels()
  }
}

// 加载插件列表
const loadPlugins = async () => {
  if (pluginsLoaded.value) return
  
  loadingPlugins.value = true
  try {
    availablePlugins.value = await getPluginList()
    pluginsLoaded.value = true
  } catch (error: any) {
    console.error('加载插件列表失败:', error)
    ElMessage.error(error.message || '加载插件列表失败')
  } finally {
    loadingPlugins.value = false
  }
}

// 需要时加载插件列表
const loadPluginsIfNeeded = (visible: boolean) => {
  if (visible && !pluginsLoaded.value) {
    loadPlugins()
  }
}

// 初始化表单数据
const initFormData = async () => {
  if (isEdit.value) {
    // 编辑模式：加载现有数据
    try {
      await agentStore.fetchAgentById(agentId.value)
      const agent = agentStore.currentAgent
      if (agent) {
        formData.value = {
          ...agent,
          status: agent.status || 'draft'
        }

        // 处理模型配置
        if (agent.modelConfig) {
          modelConfigForm.value.modelId = agent.modelConfig.modelId || agent.modelConfig.model || ''
          modelConfigForm.value.temperature = agent.modelConfig.temperature || 0.7
        }

        // 处理知识库ID列表
        if (agent.kbIds && agent.kbIds.length > 0) {
          kbIdsString.value = agent.kbIds.join(', ')
        } else {
          kbIdsString.value = ''
        }

        // 处理插件ID列表
        if (agent.toolsConfig && agent.toolsConfig.length > 0) {
          selectedPluginIds.value = agent.toolsConfig
        } else {
          selectedPluginIds.value = []
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '加载智能体信息失败')
      router.push({ name: 'agent-list' })
    }
  } else {
    // 新建模式：重置表单
    formData.value = {
      name: '',
      description: '',
      prompt: '',
      promptTemplate: '',
      status: 'draft',
      knowledgeBaseId: '',
      kbIds: [],
      toolsConfig: []
    }
    modelConfigForm.value = {
      modelId: '',
      temperature: 0.7
    }
    kbIdsString.value = ''
    selectedPluginIds.value = []
  }
}

// 保存表单
const handleSave = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 构建模型配置
    const modelConfig: Record<string, any> = {}
    if (modelConfigForm.value.modelId) {
      modelConfig.modelId = modelConfigForm.value.modelId
      modelConfig.model = modelConfigForm.value.modelId
    }
    if (modelConfigForm.value.temperature !== undefined) {
      modelConfig.temperature = modelConfigForm.value.temperature
    }

    // 构建提交数据
    const submitData: Agent = {
      ...formData.value,
      modelConfig: Object.keys(modelConfig).length > 0 ? modelConfig : undefined
    }

    // 确保数组字段正确
    if (!submitData.kbIds) submitData.kbIds = []
    if (!submitData.toolsConfig) submitData.toolsConfig = []

    loading.value = true

    if (isEdit.value) {
      // 更新智能体
      await agentStore.editAgent(agentId.value, submitData)
      ElMessage.success('保存成功')
      // 刷新数据
      await initFormData()
    } else {
      // 创建智能体
      const created = await agentStore.addAgent(submitData)
      ElMessage.success('创建成功')
      // 跳转到编辑页面
      router.replace({ name: 'agent-editor', params: { id: created.id! } })
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    if (error !== false) {
      // error !== false 表示不是表单验证失败
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push({ name: 'agent-list' })
}

// 返回列表
const handleBack = () => {
  router.push({ name: 'agent-list' })
}

// 监听路由变化，重新加载数据
watch(
  () => route.params.id,
  () => {
    initFormData()
  },
  { immediate: false }
)

// 初始化
onMounted(() => {
  initFormData()
  // 预加载模型和插件列表
  loadModels()
  loadPlugins()
})
</script>

<style scoped>
.agent-editor-container {
  padding: 20px;
  height: calc(100vh - 40px);
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: calc(100vh - 40px);
}

.editor-layout {
  height: 100%;
}

.editor-layout :deep(.el-col) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-layout :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.editor-layout :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

:deep(.el-divider__text) {
  font-size: 14px;
  font-weight: 600;
}

/* 模型选项样式 */
.model-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-option-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.model-option-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.model-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

/* 插件选项样式 */
.plugin-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.plugin-option-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.plugin-option-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.plugin-option-id {
  display: flex;
  align-items: center;
}

.identifier-text {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  font-family: monospace;
}

/* 已选择插件信息 */
.selected-plugins {
  margin-top: 8px;
}

.selected-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>

