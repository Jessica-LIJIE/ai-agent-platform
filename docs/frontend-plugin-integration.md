# 插件模块前端集成指南

## 1. 概述

本文档为前端团队提供插件模块的集成指南，包括API接口说明、数据格式、页面设计建议等。

---

## 2. API 接口清单

### 2.1 基础信息

- **Base URL**: `/api` (通过 Vite 代理转发到后端)
- **响应格式**: 统一响应格式

```typescript
interface ApiResponse<T> {
  code: number;      // 200=成功, 其他=失败
  message: string;   // 响应消息
  data: T;           // 响应数据
  timestamp: number; // 时间戳
}
```

### 2.2 插件管理接口

#### 2.2.1 获取插件列表

```http
GET /api/plugins?pageNo=1&pageSize=10
```

**响应数据**:
```typescript
interface PageResult<T> {
  list: T[];
  total: number;
}

interface PluginListItem {
  id: string;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
}
```

#### 2.2.2 获取插件详情

```http
GET /api/plugins/{pluginId}
```

**响应数据**:
```typescript
interface PluginDetail {
  id: string;
  name: string;
  description: string;
  type: string;           // 插件类型: rest, graphql, etc.
  baseUrl: string;        // 插件基础URL
  status: string;         // enabled | disabled
  isEnabled: boolean;
  authType: string;       // none, api_key, bearer, basic, custom
  authConfig: object;     // 鉴权配置（敏感信息已脱敏）
  operations: PluginOperation[];  // 操作列表
  createTime: number;
  updateTime: number;
  
  // 兼容字段（单操作插件）
  method?: string;
  endpoint?: string;
  paramsSchema?: object;
  responseSchema?: object;
}

interface PluginOperation {
  id: string;
  operationId: string;    // 操作标识符
  name: string;
  method: string;         // GET, POST, PUT, DELETE
  path: string;
  description: string;
  inputSchema: object;    // 输入参数Schema (JSON Schema格式)
  outputSchema: object;   // 输出结果Schema
}
```

#### 2.2.3 创建插件（简单模式）

```http
POST /api/plugins
Content-Type: application/json

{
  "name": "插件名称",
  "description": "插件描述",
  "method": "GET",
  "endpoint": "/api/xxx",
  "paramsSchema": {},
  "responseSchema": {},
  "authType": "none",
  "authConfig": {}
}
```

#### 2.2.4 导入OpenAPI规范（推荐）

```http
POST /api/plugins/import-openapi
Content-Type: application/json

{
  "name": "AIOT智能设备控制",
  "description": "智能家居设备控制插件",
  "type": "rest",
  "baseUrl": "https://api.example.com",
  "authType": "api_key",
  "authConfig": {
    "apiKey": "your-api-key",
    "headerName": "X-API-Key"
  },
  "operations": [
    {
      "operationId": "getSensorData",
      "name": "获取传感器数据",
      "method": "GET",
      "path": "/plugin/sensor-data",
      "description": "获取指定传感器的实时数据",
      "inputSchema": {
        "type": "object",
        "properties": {
          "sensor": { "type": "string", "description": "传感器类型" },
          "uuid": { "type": "string", "description": "设备唯一标识" }
        },
        "required": ["sensor", "uuid"]
      }
    }
  ]
}
```

#### 2.2.5 更新插件

```http
PUT /api/plugins/{pluginId}
Content-Type: application/json

{
  "name": "更新后的名称",
  "description": "更新后的描述"
}
```

#### 2.2.6 删除插件

```http
DELETE /api/plugins/{pluginId}
```

#### 2.2.7 启用/禁用插件

```http
PATCH /api/plugins/{pluginId}/status
Content-Type: application/json

{
  "isEnabled": true
}
```

#### 2.2.8 获取插件操作列表

```http
GET /api/plugins/{pluginId}/operations
```

**响应数据**: `PluginOperation[]`

#### 2.2.9 调用插件操作

```http
POST /api/plugins/{pluginId}/operations/{operationId}/invoke
Content-Type: application/json

{
  "params": {
    "sensor": "温度",
    "uuid": "device-001"
  },
  "timeout": 30000
}
```

**响应数据**:
```typescript
interface PluginInvokeResult {
  success: boolean;
  httpStatusCode: number;
  rawBody: string;
  parsedData: any;
  errorMessage: string;
  duration: number;      // 执行耗时(ms)
  timeout: boolean;
  requestUrl: string;
  requestMethod: string;
}
```

---

## 3. TypeScript 类型定义

建议在 `src/types/plugin.ts` 中添加以下类型定义：

```typescript
// src/types/plugin.ts

export interface PluginListItem {
  id: string;
  name: string;
  description: string;
  createTime: number;
  updateTime: number;
}

export interface PluginOperation {
  id: string;
  operationId: string;
  name: string;
  method: string;
  path: string;
  description: string;
  inputSchema: Record<string, any>;
  outputSchema: Record<string, any>;
}

export interface PluginDetail extends PluginListItem {
  type: string;
  baseUrl: string;
  status: 'enabled' | 'disabled';
  isEnabled: boolean;
  authType: 'none' | 'api_key' | 'bearer' | 'basic' | 'custom';
  authConfig: Record<string, any>;
  operations: PluginOperation[];
  
  // 兼容字段
  method?: string;
  endpoint?: string;
  paramsSchema?: Record<string, any>;
  responseSchema?: Record<string, any>;
}

export interface PluginCreateRequest {
  name: string;
  description?: string;
  method?: string;
  endpoint?: string;
  paramsSchema?: Record<string, any>;
  responseSchema?: Record<string, any>;
  authType?: string;
  authConfig?: Record<string, any>;
}

export interface PluginImportRequest {
  name: string;
  description?: string;
  type?: string;
  baseUrl: string;
  authType?: string;
  authConfig?: Record<string, any>;
  operations?: OperationDefinition[];
  openapiSpec?: Record<string, any>;
}

export interface OperationDefinition {
  operationId: string;
  name?: string;
  method: string;
  path: string;
  description?: string;
  inputSchema?: Record<string, any>;
  outputSchema?: Record<string, any>;
}

export interface PluginInvokeRequest {
  params: Record<string, any>;
  timeout?: number;
}

export interface PluginInvokeResult {
  success: boolean;
  httpStatusCode: number;
  rawBody: string;
  parsedData: any;
  errorMessage: string;
  duration: number;
  timeout: boolean;
  requestUrl: string;
  requestMethod: string;
}
```

---

## 4. API 封装示例

建议在 `src/api/plugin.ts` 中封装API调用：

```typescript
// src/api/plugin.ts
import http from '@/utils/http';
import type {
  PluginListItem,
  PluginDetail,
  PluginCreateRequest,
  PluginImportRequest,
  PluginInvokeRequest,
  PluginInvokeResult,
  PluginOperation,
} from '@/types/plugin';
import type { PageResult, ApiResponse } from '@/types/common';

const BASE_URL = '/plugins';

/**
 * 获取插件列表
 */
export const getPluginList = (pageNo = 1, pageSize = 10) => {
  return http.get<PageResult<PluginListItem>>(`${BASE_URL}`, {
    params: { pageNo, pageSize }
  });
};

/**
 * 获取插件详情
 */
export const getPluginDetail = (pluginId: string) => {
  return http.get<PluginDetail>(`${BASE_URL}/${pluginId}`);
};

/**
 * 创建插件（简单模式）
 */
export const createPlugin = (data: PluginCreateRequest) => {
  return http.post<PluginDetail>(`${BASE_URL}`, data);
};

/**
 * 导入OpenAPI规范
 */
export const importOpenApi = (data: PluginImportRequest) => {
  return http.post<PluginDetail>(`${BASE_URL}/import-openapi`, data);
};

/**
 * 更新插件
 */
export const updatePlugin = (pluginId: string, data: Partial<PluginCreateRequest>) => {
  return http.put<PluginDetail>(`${BASE_URL}/${pluginId}`, data);
};

/**
 * 删除插件
 */
export const deletePlugin = (pluginId: string) => {
  return http.delete(`${BASE_URL}/${pluginId}`);
};

/**
 * 启用/禁用插件
 */
export const updatePluginStatus = (pluginId: string, isEnabled: boolean) => {
  return http.patch<PluginDetail>(`${BASE_URL}/${pluginId}/status`, { isEnabled });
};

/**
 * 获取插件操作列表
 */
export const getPluginOperations = (pluginId: string) => {
  return http.get<PluginOperation[]>(`${BASE_URL}/${pluginId}/operations`);
};

/**
 * 调用插件操作
 */
export const invokeOperation = (
  pluginId: string,
  operationId: string,
  data: PluginInvokeRequest
) => {
  return http.post<PluginInvokeResult>(
    `${BASE_URL}/${pluginId}/operations/${operationId}/invoke`,
    data
  );
};
```

---

## 5. 页面设计建议

### 5.1 插件列表页面 (`PluginList.vue`)

**功能点**:
1. 分页展示插件列表
2. 搜索/筛选功能
3. 每个插件显示：名称、描述、状态开关、操作按钮
4. 操作按钮：查看详情、编辑、删除
5. "导入插件"按钮（打开导入对话框）

**UI 建议**:
```
┌─────────────────────────────────────────────────────────┐
│  插件管理                              [+ 导入插件]     │
├─────────────────────────────────────────────────────────┤
│  🔍 搜索插件...                                         │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🔌 AIOT智能设备控制                    [启用] ✓  │   │
│  │    智能家居设备控制插件 | 3个操作               │   │
│  │    📅 2024-01-15      [详情] [编辑] [删除]     │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🔌 天气查询插件                        [禁用] ✗  │   │
│  │    获取实时天气信息 | 1个操作                   │   │
│  │    📅 2024-01-10      [详情] [编辑] [删除]     │   │
│  └─────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│                    < 1 2 3 ... 10 >                     │
└─────────────────────────────────────────────────────────┘
```

### 5.2 插件导入对话框 (`PluginImportDialog.vue`)

**功能点**:
1. 表单输入：名称、描述、baseUrl
2. 鉴权配置：选择类型，输入对应参数
3. 操作定义：手动添加或粘贴JSON/OpenAPI规范
4. 预览导入结果

**表单字段**:
- 插件名称 (必填)
- 插件描述
- 基础URL (必填)
- 鉴权类型 (下拉选择)
- 鉴权配置 (根据类型动态显示)
- 操作定义方式 (手动/JSON导入)

### 5.3 插件详情页面 (`PluginDetail.vue`)

**功能点**:
1. 显示插件基本信息
2. 操作列表展示
3. 操作测试面板（可选）
4. 编辑/删除按钮

**UI 建议**:
```
┌─────────────────────────────────────────────────────────┐
│  ← 返回                                                 │
├─────────────────────────────────────────────────────────┤
│  🔌 AIOT智能设备控制                                    │
│  ────────────────────────────────────────────────────   │
│  描述：智能家居设备控制插件                             │
│  类型：REST API                                         │
│  基础URL：https://plugin.aiot.hello1023.com            │
│  状态：✅ 已启用                                        │
│  鉴权：API Key (X-API-Key: ***)                        │
├─────────────────────────────────────────────────────────┤
│  📋 操作列表 (3)                                        │
│  ┌───────────────────────────────────────────────────┐ │
│  │ GET  getSensorData - 获取传感器数据               │ │
│  │      /plugin/sensor-data                 [测试]   │ │
│  └───────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────┐ │
│  │ POST controlDevice - 控制设备                     │ │
│  │      /plugin/control                     [测试]   │ │
│  └───────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────┐ │
│  │ POST executePreset - 执行预设场景                 │ │
│  │      /plugin/preset                      [测试]   │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 5.4 操作测试面板 (`OperationTestPanel.vue`)

**功能点**:
1. 根据 inputSchema 动态生成表单
2. 发送测试请求
3. 显示响应结果（JSON格式化）
4. 显示执行耗时

---

## 6. 状态管理 (Pinia Store)

建议更新 `src/stores/usePluginStore.ts`：

```typescript
// src/stores/usePluginStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { PluginListItem, PluginDetail } from '@/types/plugin';
import * as pluginApi from '@/api/plugin';

export const usePluginStore = defineStore('plugin', () => {
  // State
  const pluginList = ref<PluginListItem[]>([]);
  const currentPlugin = ref<PluginDetail | null>(null);
  const loading = ref(false);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(10);

  // Getters
  const enabledPlugins = computed(() => 
    pluginList.value.filter(p => (p as any).isEnabled)
  );

  // Actions
  const fetchPluginList = async () => {
    loading.value = true;
    try {
      const res = await pluginApi.getPluginList(pageNo.value, pageSize.value);
      pluginList.value = res.data.list;
      total.value = res.data.total;
    } finally {
      loading.value = false;
    }
  };

  const fetchPluginDetail = async (pluginId: string) => {
    loading.value = true;
    try {
      const res = await pluginApi.getPluginDetail(pluginId);
      currentPlugin.value = res.data;
    } finally {
      loading.value = false;
    }
  };

  const togglePluginStatus = async (pluginId: string, isEnabled: boolean) => {
    await pluginApi.updatePluginStatus(pluginId, isEnabled);
    await fetchPluginList();
  };

  const removePlugin = async (pluginId: string) => {
    await pluginApi.deletePlugin(pluginId);
    await fetchPluginList();
  };

  return {
    // State
    pluginList,
    currentPlugin,
    loading,
    total,
    pageNo,
    pageSize,
    // Getters
    enabledPlugins,
    // Actions
    fetchPluginList,
    fetchPluginDetail,
    togglePluginStatus,
    removePlugin,
  };
});
```

---

## 7. 路由配置建议

```typescript
// src/router/index.ts
{
  path: '/plugins',
  name: 'PluginList',
  component: () => import('@/views/plugin/PluginList.vue'),
  meta: { title: '插件管理' }
},
{
  path: '/plugins/:id',
  name: 'PluginDetail',
  component: () => import('@/views/plugin/PluginDetail.vue'),
  meta: { title: '插件详情' }
}
```

---

## 8. 注意事项

### 8.1 鉴权配置安全
- 后端返回的 `authConfig` 中敏感字段（如 apiKey）已脱敏显示为 `***`
- 更新插件时，如果不修改鉴权配置，不要传递 `authConfig` 字段

### 8.2 操作调用
- `invokeOperation` 可能耗时较长，建议设置 loading 状态
- 默认超时时间为 30 秒，可通过 `timeout` 参数调整
- 响应中的 `parsedData` 为解析后的 JSON 对象，`rawBody` 为原始字符串

### 8.3 inputSchema 表单生成
- inputSchema 使用 JSON Schema 格式
- 建议使用库如 `@vjsf/core` 或手动解析生成表单
- 必填字段在 `required` 数组中

### 8.4 错误处理
```typescript
try {
  const res = await invokeOperation(pluginId, operationId, { params });
  if (res.data.success) {
    // 处理成功
    console.log(res.data.parsedData);
  } else {
    // 调用失败（如HTTP错误、超时）
    ElMessage.error(res.data.errorMessage);
  }
} catch (error) {
  // 网络错误等
  ElMessage.error('请求失败');
}
```

---

## 9. 开发计划建议

| 阶段 | 任务 | 预计工时 |
|------|------|----------|
| P0 | 插件列表页面 | 4h |
| P0 | 插件导入对话框 | 4h |
| P1 | 插件详情页面 | 3h |
| P1 | 启用/禁用功能 | 1h |
| P2 | 操作测试面板 | 4h |
| P2 | inputSchema动态表单 | 6h |

---

## 10. 联系方式

如有问题，请联系后端开发人员。
