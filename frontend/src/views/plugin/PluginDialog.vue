<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑插件' : '新增插件'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- JSON 文件导入区域 -->
    <div class="import-section">
      <el-upload
        ref="uploadRef"
        class="json-upload"
        :auto-upload="false"
        :show-file-list="false"
        accept=".json"
        :on-change="handleFileChange"
      >
        <template #trigger>
          <el-button type="primary" :icon="Upload">
            导入 OpenAPI JSON 文件
          </el-button>
        </template>
      </el-upload>
      <span class="import-tip">支持 OpenAPI 3.0 规范的 JSON 文件，导入后将自动填充表单</span>
    </div>

    <el-divider />

    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="right"
    >
      <el-form-item label="插件名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入插件名称"
          clearable
        />
      </el-form-item>

      <el-form-item label="标识符" prop="identifier">
        <el-input
          v-model="formData.identifier"
          placeholder="请输入插件唯一标识符（如：weather_plugin）"
          clearable
        />
        <div class="form-tip">标识符用于在系统中唯一标识此插件</div>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入插件描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="鉴权类型" prop="authType">
        <el-select
          v-model="formData.authType"
          placeholder="请选择鉴权类型"
          clearable
          style="width: 100%"
        >
          <el-option label="无鉴权" value="none" />
          <el-option label="API Key" value="api_key" />
          <el-option label="OAuth" value="oauth" />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="formData.authType && formData.authType !== 'none'"
        label="鉴权配置"
        prop="authConfig"
      >
        <el-input
          v-model="authConfigJson"
          type="textarea"
          :rows="4"
          placeholder='请输入JSON格式的鉴权配置，例如：{"apiKey": "your-key"}'
          @blur="validateJson('authConfig', authConfigJson)"
        />
        <div class="form-tip">请使用有效的JSON格式</div>
      </el-form-item>

      <el-form-item label="OpenAPI规范" prop="openapiSpec">
        <el-input
          v-model="openapiSpecJson"
          type="textarea"
          :rows="8"
          placeholder='请输入OpenAPI规范JSON，例如：{"openapi": "3.0.0", "info": {...}}'
          @blur="validateJson('openapiSpec', openapiSpecJson)"
        />
        <div class="form-tip">请使用有效的OpenAPI 3.0 JSON格式</div>
      </el-form-item>

      <el-form-item label="状态" prop="isEnabled">
        <el-radio-group v-model="formData.isEnabled">
          <el-radio :label="true">已启用</el-radio>
          <el-radio :label="false">已禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadInstance, type UploadFile } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { usePluginStore } from '@/stores/usePluginStore'
import type { Plugin } from '@/types/entity'

interface Props {
  modelValue: boolean
  plugin?: Plugin | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  plugin: null
})

const emit = defineEmits<Emits>()

const pluginStore = usePluginStore()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const loading = ref(false)

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 是否编辑模式
const isEdit = computed(() => !!props.plugin?.id)

// 表单数据
const formData = ref<Plugin>({
  name: '',
  identifier: '',
  description: '',
  authType: 'none',
  authConfig: {},
  openapiSpec: {},
  isEnabled: true,
  status: 'enabled'
})

// JSON 字符串（用于编辑）
const authConfigJson = ref('')
const openapiSpecJson = ref('')

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入插件名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  identifier: [
    { required: true, message: '请输入插件标识符', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9_]*$/,
      message: '标识符只能包含小写字母、数字和下划线，且必须以字母开头',
      trigger: 'blur'
    }
  ],
  description: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 验证 JSON 格式
const validateJson = (field: string, jsonStr: string) => {
  if (!jsonStr || jsonStr.trim() === '') {
    return true
  }

  try {
    const parsed = JSON.parse(jsonStr)
    if (field === 'authConfig') {
      formData.value.authConfig = parsed
    } else if (field === 'openapiSpec') {
      formData.value.openapiSpec = parsed
    }
    return true
  } catch (error) {
    ElMessage.warning(`${field} 格式不正确，请输入有效的 JSON`)
    return false
  }
}

// 处理 JSON 文件上传
const handleFileChange = (file: UploadFile) => {
  if (!file.raw) {
    ElMessage.error('文件读取失败')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const content = e.target?.result as string
      const jsonData = JSON.parse(content)
      
      // 解析 OpenAPI 规范并填充表单
      parseAndFillForm(jsonData)
      
      ElMessage.success('JSON 文件导入成功')
    } catch (error) {
      console.error('JSON 解析失败:', error)
      ElMessage.error('JSON 文件格式错误，请检查文件内容')
    }
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  reader.readAsText(file.raw)
}

// 解析 OpenAPI JSON 并填充表单
const parseAndFillForm = (jsonData: any) => {
  // 检查是否是 OpenAPI 规范
  if (jsonData.openapi || jsonData.swagger) {
    // 标准 OpenAPI 格式
    const info = jsonData.info || {}
    
    // 填充基本信息
    if (info.title && !formData.value.name) {
      formData.value.name = info.title
    }
    if (info.description && !formData.value.description) {
      formData.value.description = info.description
    }
    
    // 从 title 生成 identifier（如果没有填写）
    if (info.title && !formData.value.identifier) {
      formData.value.identifier = info.title
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '_')
        .replace(/^_+|_+$/g, '')
        .substring(0, 50)
    }
  } else if (jsonData.name) {
    // 简化格式（直接的插件定义）
    if (jsonData.name && !formData.value.name) {
      formData.value.name = jsonData.name
    }
    if (jsonData.description && !formData.value.description) {
      formData.value.description = jsonData.description
    }
    if (jsonData.identifier && !formData.value.identifier) {
      formData.value.identifier = jsonData.identifier
    }
  }
  
  // 设置完整的 openapiSpec
  formData.value.openapiSpec = jsonData
  openapiSpecJson.value = JSON.stringify(jsonData, null, 2)
  
  // 解析鉴权配置
  if (jsonData.components?.securitySchemes) {
    const schemes = jsonData.components.securitySchemes
    const schemeKeys = Object.keys(schemes)
    if (schemeKeys.length > 0) {
      const firstScheme = schemes[schemeKeys[0]]
      if (firstScheme.type === 'apiKey') {
        formData.value.authType = 'api_key'
      } else if (firstScheme.type === 'oauth2') {
        formData.value.authType = 'oauth'
      } else if (firstScheme.scheme === 'bearer') {
        formData.value.authType = 'api_key'
      }
    }
  }
}

// 初始化表单数据
const initFormData = () => {
  if (props.plugin) {
    // 编辑模式：填充现有数据
    formData.value = {
      ...props.plugin,
      isEnabled: props.plugin.isEnabled ?? props.plugin.status === 'enabled'
    }

    // 将 JSON 对象转换为字符串用于编辑
    if (formData.value.authConfig) {
      try {
        authConfigJson.value =
          typeof formData.value.authConfig === 'string'
            ? formData.value.authConfig
            : JSON.stringify(formData.value.authConfig, null, 2)
      } catch (error) {
        authConfigJson.value = ''
      }
    } else {
      authConfigJson.value = ''
    }

    if (formData.value.openapiSpec) {
      try {
        openapiSpecJson.value =
          typeof formData.value.openapiSpec === 'string'
            ? formData.value.openapiSpec
            : JSON.stringify(formData.value.openapiSpec, null, 2)
      } catch (error) {
        openapiSpecJson.value = ''
      }
    } else {
      openapiSpecJson.value = ''
    }
  } else {
    // 新建模式：重置表单
    formData.value = {
      name: '',
      identifier: '',
      description: '',
      authType: 'none',
      authConfig: {},
      openapiSpec: {},
      isEnabled: true,
      status: 'enabled'
    }
    authConfigJson.value = ''
    openapiSpecJson.value = ''
  }
}

// 监听弹窗打开，初始化表单
watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      initFormData()
      // 重置表单验证
      setTimeout(() => {
        formRef.value?.clearValidate()
      }, 0)
    }
  }
)

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 验证 JSON 字段
    if (authConfigJson.value && !validateJson('authConfig', authConfigJson.value)) {
      return
    }
    if (openapiSpecJson.value && !validateJson('openapiSpec', openapiSpecJson.value)) {
      return
    }

    // 构建提交数据
    const submitData: Plugin = {
      ...formData.value,
      status: formData.value.isEnabled ? 'enabled' : 'disabled'
    }

    // 如果没有填写 JSON，清空对象
    if (!authConfigJson.value || authConfigJson.value.trim() === '') {
      submitData.authConfig = {}
    }
    if (!openapiSpecJson.value || openapiSpecJson.value.trim() === '') {
      submitData.openapiSpec = {}
    }

    loading.value = true

    if (isEdit.value && props.plugin?.id) {
      // 更新插件
      await pluginStore.editPlugin(props.plugin.id, submitData)
      ElMessage.success('更新成功')
    } else {
      // 创建插件
      await pluginStore.addPlugin(submitData)
      ElMessage.success('创建成功')
    }

    emit('success')
  } catch (error: any) {
    console.error('提交失败:', error)
    if (error !== false) {
      // error !== false 表示不是表单验证失败
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}
</script>

<style scoped>
.import-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 8px;
  margin-bottom: 8px;
}

.json-upload {
  flex-shrink: 0;
}

.import-tip {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
