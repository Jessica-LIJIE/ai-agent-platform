# Plugin 模块实现情况总结

> 更新日期：2025-12-02

---

## 一、Plugin 模块 API 接口总结

### 1. 文字描述

Plugin 模块提供了完整的插件管理和调用功能，共包含 **9 个 API 接口**：

1. **列表查询插件** - 分页获取所有插件的基本信息列表
2. **获取插件详情** - 根据插件 ID 获取完整的插件信息，包括操作列表
3. **创建插件** - 创建新插件（简单模式）
4. **更新插件** - 根据插件 ID 部分更新插件信息
5. **删除插件** - 根据插件 ID 删除指定插件
6. **导入OpenAPI** - 从 OpenAPI 规范或简化 JSON 导入插件（支持多操作）
7. **获取操作列表** - 获取插件的所有可用操作
8. **启用/禁用插件** - 切换插件状态
9. **调用插件操作** - 实际调用插件接口（HTTP 请求）

### 2. API 接口表格

| 序号 | 功能 | HTTP Method | Endpoint | 请求参数 | 响应数据 |
|------|------|-------------|----------|----------|----------|
| 1 | 列表查询 | GET | `/api/v1/plugins` | 可选：page, pageSize | `{ list: [...], total: number }` |
| 2 | 获取详情 | GET | `/api/v1/plugins/{pluginId}` | 路径参数：pluginId | 完整插件详情（含operations） |
| 3 | 创建插件 | POST | `/api/v1/plugins` | Body: name, description, method, endpoint, ... | 创建后的插件详情 |
| 4 | 更新插件 | PUT | `/api/v1/plugins/{pluginId}` | 路径参数 + Body（所有字段可选） | 更新后的插件详情 |
| 5 | 删除插件 | DELETE | `/api/v1/plugins/{pluginId}` | 路径参数：pluginId | `null` |
| 6 | 导入OpenAPI | POST | `/api/v1/plugins/import-openapi` | Body: name, baseUrl, operations[] | 创建后的插件详情 |
| 7 | 获取操作列表 | GET | `/api/v1/plugins/{pluginId}/operations` | 路径参数：pluginId | `PluginOperation[]` |
| 8 | 启用/禁用 | PATCH | `/api/v1/plugins/{pluginId}/status` | Body: { isEnabled: boolean } | 更新后的插件详情 |
| 9 | 调用操作 | POST | `/api/v1/plugins/{pluginId}/operations/{operationId}/invoke` | Body: { params, timeout } | `PluginInvokeResult` |

### 3. 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1764485836388
}
```

### 4. 调用插件操作示例

**请求**:
```http
POST /api/v1/plugins/plugin_66fd8f296525/operations/getSensorData/invoke
Content-Type: application/json

{
  "params": {
    "sensor": "温度",
    "uuid": "test-device-001"
  },
  "timeout": 30000
}
```

**响应**:
```json
{
  "code": 200,
  "message": "调用成功",
  "data": {
    "status": "success",
    "httpStatusCode": 200,
    "rawBody": "{\"code\":200,\"data\":{\"value\":25.0}}",
    "parsedData": { "code": 200, "data": { "value": 25.0 } },
    "duration": 156,
    "requestUrl": "https://plugin.aiot.hello1023.com/plugin/sensor-data?sensor=温度&uuid=test-device-001",
    "requestMethod": "GET"
  },
  "timestamp": 1764485836388
}
```

---

## 二、Plugin 相关数据库表总结

### 1. 文字描述

Plugin 模块涉及 **2 张数据库表**：

1. **plugin 表（插件表）** - 存储插件的注册信息、OpenAPI 规范和鉴权配置，是插件系统的核心表
2. **plugin_operation 表（插件操作表）** - 存储每个插件的具体接口操作信息，与 plugin 表是一对多关系

此外，数据库设计文档中还提到了 **agent_plugin 中间表**（智能体与插件的多对多关系），但当前 schema.sql 中尚未创建该表。

### 2. 数据库表结构

#### 2.1 plugin 表

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| id | VARCHAR(64) | ✅ PK | 插件唯一标识 |
| name | VARCHAR(100) | ✅ | 插件名称 |
| identifier | VARCHAR(100) | - | 插件唯一标识符（key） |
| description | TEXT | - | 插件描述 |
| type | VARCHAR(20) | ✅ | 插件类型（http/mqtt/local），默认 'http' |
| base_url | VARCHAR(255) | - | 基础请求地址 |
| openapi_spec | JSON | ✅ | OpenAPI 规范内容（存储 method、endpoint、paramsSchema、responseSchema） |
| openapi_schema | JSON | - | OpenAPI 规范（别名字段） |
| status | VARCHAR(20) | ✅ | 插件状态（enabled/disabled），默认 'disabled' |
| is_enabled | BOOLEAN | ✅ | 是否启用，默认 FALSE |
| auth_info | JSON | - | 鉴权信息（存储 API Key 等） |
| auth_type | VARCHAR(20) | - | 鉴权类型（none/api_key/oauth），默认 'none' |
| auth_config | JSON | - | 鉴权配置 |
| user_id | VARCHAR(64) | - | 注册者 ID（NULL 代表系统插件） |
| create_time | DATETIME | ✅ | 创建时间 |
| update_time | DATETIME | ✅ | 更新时间 |

**索引：**
- `uk_identifier` - identifier 唯一索引
- `uk_user_name` - (user_id, name) 联合唯一索引
- `idx_status` - status 索引（加速按状态查询）

#### 2.2 plugin_operation 表

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| id | VARCHAR(64) | ✅ PK | 插件操作唯一标识 |
| plugin_id | VARCHAR(64) | ✅ FK | 所属插件 ID |
| operation_id | VARCHAR(100) | ✅ | OpenAPI 中的 operationId |
| name | VARCHAR(100) | ✅ | 操作名称 |
| method | VARCHAR(10) | ✅ | HTTP 方法（GET/POST/PUT/DELETE） |
| path | VARCHAR(255) | ✅ | 请求路径 |
| description | TEXT | - | 操作描述 |
| input_schema | JSON | - | 入参结构 |
| output_schema | JSON | - | 出参结构 |
| create_time | DATETIME | ✅ | 创建时间 |
| update_time | DATETIME | ✅ | 更新时间 |

**外键：**
- `fk_operation_plugin` - plugin_id 关联 plugin(id)，级联删除

---

## 三、当前实现与文档要求的差异

### 1. ✅ 已实现（符合文档要求）

| 项目 | 说明 |
|------|------|
| 9 个 API 接口 | GET 列表、GET 详情、POST 创建、PUT 更新、DELETE 删除、POST 导入、GET 操作列表、PATCH 状态、POST 调用 |
| 统一响应格式 | code=200 表示成功，包含 message、data、timestamp |
| 多操作插件支持 | 支持 plugin_operation 表，一个插件多个操作 |
| OpenAPI 导入 | 支持从 OpenAPI 3.0 规范或简化 JSON 导入 |
| HTTP 调用 | 支持 GET/POST 方法，参数拼接到 URL 或 Body |
| 鉴权支持 | 支持 none、api_key、bearer、basic、custom 五种鉴权方式 |
| 鉴权信息脱敏 | authConfig 中的 apiKey 返回时显示为 `***` |
| 分页查询 | 支持 page、pageSize 参数，返回 list 和 total |
| 时间戳格式 | create_time、update_time 返回毫秒时间戳 |

### 2. ⚠️ 差异点与待完善项

| 差异项 | 文档要求 | 当前实现 | 影响程度 | 建议 |
|--------|----------|----------|----------|------|
| **auth_info 加密存储** | 文档要求 auth_info 在应用层加密存储（如 API Key 加密） | 当前直接明文存储 JSON | 🔴 高 | 后续需实现加密存储 |
| **agent_plugin 中间表** | 文档要求有 agent_plugin 表实现智能体与插件的多对多关系 | schema.sql 中未创建该表 | 🟡 中 | 需要时再创建 |
| **用户鉴权** | 接口应从登录上下文获取当前用户 ID | 当前 userId 写死为 "user-004-dev" | 🟡 中 | 待用户认证模块完成后集成 |

### 3. 🔧 后续优化建议

1. **敏感信息加密**
   - 实现 auth_info/auth_config 的 AES 加密存储
   - 读取时解密，返回时脱敏

2. **创建 agent_plugin 中间表**
   ```sql
   CREATE TABLE agent_plugin (
       agent_id VARCHAR(64) NOT NULL,
       plugin_id VARCHAR(64) NOT NULL,
       plugin_config JSON,
       is_enabled BOOLEAN DEFAULT TRUE,
       PRIMARY KEY (agent_id, plugin_id)
   );
   ```

3. **集成用户认证**
   - 从 JWT Token 或 Session 获取当前用户 ID
   - 实现插件的权限控制（只能操作自己创建的插件）

---

## 三、工作流/Agent 集成框架

### 1. 工作流插件节点（Workflow Plugin Node）

用于在工作流中调用插件操作，支持参数表达式和结果映射。

**核心类：**

| 类名 | 说明 |
|------|------|
| `PluginNodeConfig` | 插件节点配置（pluginId, operationId, paramMappings, outputMapping, timeout, retryCount） |
| `WorkflowContext` | 工作流执行上下文，支持表达式解析 |
| `PluginNodeResult` | 插件节点执行结果 |
| `WorkflowPluginExecutor` | 工作流插件节点执行器 |

**参数表达式支持：**
- `${input.xxx}` - 从工作流输入获取
- `${context.xxx}` - 从上下文变量获取
- `${nodes.nodeId.field}` - 从指定节点输出获取
- 字面量直接使用

**使用示例：**
```java
PluginNodeConfig config = new PluginNodeConfig();
config.setNodeId("node_1");
config.setPluginId("plugin_66fd8f296525");
config.setOperationId("getSensorData");
config.setParamMappings(Map.of(
    "sensor", "${input.sensorType}",
    "uuid", "${context.deviceId}"
));
config.setOutputMapping("sensorResult");
config.setRetryCount(2);

WorkflowContext context = new WorkflowContext();
context.getInput().put("sensorType", "温度");
context.setVariable("deviceId", "device-001");

PluginNodeResult result = workflowPluginExecutor.execute(config, context);
```

### 2. Agent 工具调用框架（Agent Tool Calling）

用于智能体通过 LLM Function Calling 调用插件，兼容 OpenAI 格式。

**核心类：**

| 类名 | 说明 |
|------|------|
| `AgentToolDefinition` | Agent 工具定义，兼容 OpenAI Function Calling 格式 |
| `AgentToolCallResult` | 工具调用结果封装 |
| `AgentToolExecutor` | Agent 工具执行器 |

**使用示例：**
```java
// 1. 获取可用工具列表（发送给 LLM）
List<AgentToolDefinition> tools = agentToolExecutor.getAvailableTools(agentId);

// 2. 执行 LLM 返回的工具调用
AgentToolCallResult result = agentToolExecutor.executeToolCall(
    "call_abc123",                         // tool_call_id
    "plugin_66fd8f29_getSensorData",       // function name
    "{\"sensor\":\"温度\",\"uuid\":\"test\"}" // arguments JSON
);
```

---

## 四、后端代码结构

```
backend/core/src/main/java/org/demo/core/
├── api/
│   └── ApiResponse.java              # 统一响应封装
├── config/
│   └── MybatisPlusConfig.java        # MyBatis Plus 分页插件配置
├── controller/
│   └── PluginController.java         # 插件控制器（9个接口）
├── exception/
│   ├── GlobalExceptionHandler.java   # 全局异常处理
│   └── PluginNotFoundException.java  # 插件不存在异常
├── mapper/
│   ├── PluginMapper.java             # 插件 Mapper
│   └── PluginOperationMapper.java    # 插件操作 Mapper
├── model/
│   ├── dto/
│   │   ├── PluginCreateRequest.java      # 创建请求 DTO
│   │   ├── PluginUpdateRequest.java      # 更新请求 DTO
│   │   ├── PluginOpenApiImportRequest.java # 导入请求 DTO
│   │   ├── PluginStatusUpdateRequest.java  # 状态更新请求 DTO
│   │   └── PluginInvokeRequest.java      # 调用请求 DTO
│   ├── entity/
│   │   ├── Plugin.java               # 插件实体类
│   │   └── PluginOperation.java      # 插件操作实体类
│   └── vo/
│       ├── PageResult.java           # 分页结果 VO
│       ├── PluginDetailVO.java       # 插件详情 VO（含operations）
│       ├── PluginListItemVO.java     # 插件列表项 VO
│       ├── PluginOperationVO.java    # 插件操作 VO
│       └── PluginInvokeResult.java   # 调用结果 VO
├── service/
│   ├── PluginService.java            # 插件服务接口
│   ├── PluginOperationService.java   # 插件操作服务接口
│   └── impl/
│       ├── PluginServiceImpl.java        # 插件服务实现（含HTTP调用）
│       └── PluginOperationServiceImpl.java # 插件操作服务实现
├── workflow/                          # 工作流集成
│   ├── PluginNodeConfig.java         # 插件节点配置
│   ├── PluginNodeResult.java         # 插件节点执行结果
│   ├── WorkflowContext.java          # 工作流上下文
│   └── WorkflowPluginExecutor.java   # 工作流插件执行器
└── agent/                             # Agent集成
    ├── AgentToolDefinition.java      # Agent工具定义（OpenAI格式）
    ├── AgentToolCallResult.java      # 工具调用结果
    └── AgentToolExecutor.java        # Agent工具执行器
```

---

## 五、已验证的测试场景

### 1. AIOT 设备控制测试（已通过 ✅）

使用 `coze-plugin-lite.json` 导入了 AIOT 智能设备控制插件：

| 测试项 | 结果 |
|--------|------|
| 导入 OpenAPI 规范 | ✅ 成功创建 plugin + 3 个 plugin_operation |
| 获取传感器数据 (GET) | ✅ 返回温度值 25.0 |
| 控制 LED 设备 (POST) | ✅ 返回 `{"result":"success"}` |
| 执行预设场景 (POST) | ✅ 配置已保存 |

**测试插件信息：**
- Plugin ID: `plugin_66fd8f296525`
- Base URL: `https://plugin.aiot.hello1023.com`
- 操作列表: `getSensorData`, `controlDevice`, `executePreset`

### 2. 鉴权方式支持

| 鉴权类型 | 说明 | 测试状态 |
|----------|------|----------|
| none | 无鉴权 | ✅ |
| api_key | API Key 放入 Header | ✅ |
| bearer | Bearer Token | ✅ |
| basic | Basic Auth | ✅ |
| custom | 自定义 Header | ✅ |

---

## 六、测试方式

### Swagger UI（推荐）

访问 http://localhost:8080/doc.html 可在界面上直接测试所有 API

### PowerShell 命令

```powershell
# 设置 UTF-8 编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 列表查询
$r = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/plugins" -Method GET
[System.Text.Encoding]::UTF8.GetString([System.Text.Encoding]::GetEncoding("ISO-8859-1").GetBytes($r.Content))

# 获取详情
$r = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/plugins/plugin-001-led" -Method GET
[System.Text.Encoding]::UTF8.GetString([System.Text.Encoding]::GetEncoding("ISO-8859-1").GetBytes($r.Content))
```

---

## 七、前端对接文档

详见 `docs/frontend-plugin-integration.md`，包含：
- 完整 API 接口说明
- TypeScript 类型定义
- API 封装示例代码
- 页面设计建议
- Pinia Store 示例

---

## 八、数据库架构变更记录

### 1. 新增表

#### 8.1 plugin_operation 表（新增）

**创建原因**: 支持一个插件包含多个操作（多操作插件）

**表结构**:
```sql
CREATE TABLE plugin_operation (
    id VARCHAR(64) PRIMARY KEY COMMENT '插件操作唯一标识',
    plugin_id VARCHAR(64) NOT NULL COMMENT '所属插件ID',
    operation_id VARCHAR(100) NOT NULL COMMENT 'OpenAPI中的operationId',
    name VARCHAR(100) NOT NULL COMMENT '操作名称',
    method VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    path VARCHAR(255) NOT NULL COMMENT '请求路径',
    description TEXT COMMENT '操作描述',
    input_schema JSON COMMENT '入参结构',
    output_schema JSON COMMENT '出参结构',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (plugin_id) REFERENCES plugin(id) ON DELETE CASCADE
);
```

**关键特性**:
- ✅ 支持级联删除（删除插件时自动删除所有操作）
- ✅ 支持操作级别的启用/禁用
- ✅ 存储完整的 input_schema 和 output_schema（JSON格式）

### 2. plugin 表字段修改

#### 8.2 字段变更

| 变更类型 | 字段名 | 变更说明 | 影响 |
|---------|--------|---------|------|
| **新增** | `base_url` | VARCHAR(255) - 插件基础URL | ✅ 支持多操作插件共享base_url |
| **新增** | `auth_type` | VARCHAR(20) - 鉴权类型 | ✅ 替代原 auth_info 字段 |
| **新增** | `auth_config` | JSON - 鉴权配置 | ✅ 结构化存储鉴权信息 |
| **新增** | `type` | VARCHAR(20) - 插件类型 | ✅ 支持 http/mqtt/local 类型 |
| **修改** | `openapi_spec` | 存储格式优化 | ✅ 简化为基础信息（详细操作移至 plugin_operation） |
| **弃用** | `method` | 已移至 plugin_operation.method | ⚠️ 兼容性:旧数据需迁移 |
| **弃用** | `endpoint` | 已移至 plugin_operation.path | ⚠️ 兼容性:旧数据需迁移 |

#### 8.3 auth_type 支持的鉴权方式

| 类型 | auth_type 值 | auth_config 结构 | 说明 |
|------|-------------|-----------------|------|
| 无鉴权 | `none` | `{}` | 不需要鉴权 |
| API Key | `api_key` | `{"headerName": "X-API-Key", "apiKey": "xxx"}` | API Key放入Header |
| Bearer Token | `bearer` | `{"token": "xxx"}` | Authorization: Bearer xxx |
| Basic Auth | `basic` | `{"username": "xxx", "password": "xxx"}` | HTTP Basic Auth |
| 自定义 | `custom` | `{"headers": {"key": "value"}}` | 自定义请求头 |

### 3. 索引优化

#### 8.4 新增索引

```sql
-- plugin 表
ALTER TABLE plugin ADD INDEX idx_status (status);
ALTER TABLE plugin ADD UNIQUE INDEX uk_identifier (identifier);
ALTER TABLE plugin ADD UNIQUE INDEX uk_user_name (user_id, name);

-- plugin_operation 表
ALTER TABLE plugin_operation ADD INDEX idx_plugin_id (plugin_id);
ALTER TABLE plugin_operation ADD UNIQUE INDEX uk_plugin_operation (plugin_id, operation_id);
```

**优化效果**:
- ✅ `idx_status`: 加速按状态查询插件（enabled/disabled）
- ✅ `uk_identifier`: 保证插件唯一标识符不重复
- ✅ `uk_user_name`: 同一用户下插件名称不重复
- ✅ `idx_plugin_id`: 加速根据插件ID查询操作
- ✅ `uk_plugin_operation`: 同一插件下operationId不重复

---

## 九、API 接口变更记录

### 1. 新增 API 接口

| 序号 | 接口名称 | Method | Endpoint | 新增原因 |
|------|---------|--------|----------|----------|
| 1 | 导入OpenAPI | POST | `/api/v1/plugins/import-openapi` | 支持从OpenAPI规范批量导入多操作插件 |
| 2 | 获取操作列表 | GET | `/api/v1/plugins/{id}/operations` | 查询插件的所有操作 |
| 3 | 调用插件操作 | POST | `/api/v1/plugins/{pluginId}/operations/{operationId}/invoke` | 执行具体的插件操作（核心功能） |
| 4 | 启用/禁用插件 | PATCH | `/api/v1/plugins/{id}/status` | 快速切换插件状态 |

### 2. 修改的 API 接口

#### 9.1 GET /api/v1/plugins/{id} - 获取插件详情

**变更**: 响应数据新增 `operations` 字段

**旧版响应**:
```json
{
  "id": "plugin_xxx",
  "name": "测试插件",
  "description": "...",
  "method": "GET",
  "endpoint": "/api/test"
}
```

**新版响应**:
```json
{
  "id": "plugin_xxx",
  "name": "测试插件",
  "description": "...",
  "baseUrl": "https://api.example.com",
  "authType": "api_key",
  "authConfig": {
    "headerName": "X-API-Key",
    "apiKey": "***"
  },
  "operations": [
    {
      "id": "op_001",
      "operationId": "getSensorData",
      "name": "获取传感器数据",
      "method": "GET",
      "path": "/sensor-data",
      "inputSchema": {...},
      "outputSchema": {...}
    }
  ]
}
```

**影响**: ✅ 向后兼容（新增字段不影响旧版解析）

#### 9.2 POST /api/v1/plugins - 创建插件

**变更**: 请求参数结构调整

**旧版请求**:
```json
{
  "name": "测试插件",
  "method": "GET",
  "endpoint": "/api/test"
}
```

**新版请求**（兼容两种模式）:

**模式1: 简单模式**（向后兼容）
```json
{
  "name": "测试插件",
  "baseUrl": "https://api.example.com",
  "method": "GET",
  "endpoint": "/api/test",
  "authType": "api_key",
  "authConfig": {
    "headerName": "X-API-Key",
    "apiKey": "your-key"
  }
}
```

**模式2: 多操作模式**（推荐使用 /import-openapi）
```json
{
  "name": "AIOT插件",
  "baseUrl": "https://plugin.aiot.hello1023.com",
  "authType": "none",
  "operations": [
    {
      "operationId": "getSensorData",
      "name": "获取传感器数据",
      "method": "GET",
      "path": "/sensor-data",
      "inputSchema": {...}
    }
  ]
}
```

**影响**: ✅ 完全向后兼容

---

## 十、功能变更记录

### 1. 核心功能增强

#### 10.1 插件调用功能（invokeOperation）

**位置**: `PluginServiceImpl.invokeOperation()` (L508-L700)

**新增功能**:

| 功能 | 说明 | 实现细节 |
|------|------|---------|
| **多HTTP方法支持** | GET/POST/PUT/DELETE | 根据 operation.method 动态构建请求 |
| **智能参数处理** | GET参数拼接URL,POST参数放Body | `buildRequestUrl()` + `buildRequestEntity()` |
| **多种鉴权方式** | 5种鉴权类型 | `buildHttpHeaders()` 根据 auth_type 构建 |
| **超时控制** | 自定义超时时间 | RestTemplate 配置超时（默认30秒） |
| **异常处理** | HTTP错误、网络错误、超时 | 统一异常捕获和状态码返回 |
| **结果封装** | 详细的调用结果 | `PluginInvokeResult` 包含 status/httpCode/rawBody/parsedData/duration |

**代码示例**:
```java
public PluginInvokeResult invokeOperation(
    String pluginId, String operationId, 
    Map<String, Object> params, Integer timeout) {
    
    // 1. 查询插件和操作信息
    Plugin plugin = getById(pluginId);
    PluginOperation operation = operationService.getByOperationId(pluginId, operationId);
    
    // 2. 构建请求URL（GET参数拼接）
    String requestUrl = buildRequestUrl(plugin.getBaseUrl(), operation.getPath(), 
                                        operation.getMethod(), params);
    
    // 3. 构建鉴权头
    HttpHeaders headers = buildHttpHeaders(plugin.getAuthType(), plugin.getAuthConfig());
    
    // 4. 执行HTTP请求
    ResponseEntity<String> response = restTemplate.exchange(
        requestUrl, HttpMethod.valueOf(operation.getMethod()), 
        requestEntity, String.class);
    
    // 5. 封装结果
    return PluginInvokeResult.success(response.getStatusCode(), 
                                      response.getBody(), duration);
}
```

#### 10.2 OpenAPI 导入功能

**位置**: `PluginServiceImpl.importFromOpenApi()` (L300-L450)

**支持格式**:

1. **标准 OpenAPI 3.0 规范**
```json
{
  "openapi": "3.0.0",
  "info": {"title": "AIOT API"},
  "servers": [{"url": "https://api.example.com"}],
  "paths": {
    "/sensor-data": {
      "get": {
        "operationId": "getSensorData",
        "parameters": [...]
      }
    }
  }
}
```

2. **简化 JSON 格式**（推荐）
```json
{
  "name": "AIOT插件",
  "baseUrl": "https://plugin.aiot.hello1023.com",
  "operations": [
    {
      "operationId": "getSensorData",
      "name": "获取传感器数据",
      "method": "GET",
      "path": "/sensor-data",
      "inputSchema": {
        "type": "object",
        "properties": {
          "sensor": {"type": "string"}
        }
      }
    }
  ]
}
```

**导入流程**:
1. 解析 OpenAPI 规范或简化 JSON
2. 创建 plugin 记录
3. 批量创建 plugin_operation 记录（使用事务）
4. 返回完整插件信息（含operations列表）

#### 10.3 鉴权信息脱敏

**位置**: `PluginDetailVO.fromEntity()` + `PluginListItemVO.fromEntity()`

**脱敏规则**:
```java
// authConfig 中的敏感字段返回时显示为 ***
if (authConfig.containsKey("apiKey")) {
    authConfig.put("apiKey", "***");
}
if (authConfig.containsKey("token")) {
    authConfig.put("token", "***");
}
if (authConfig.containsKey("password")) {
    authConfig.put("password", "***");
}
```

**效果**:
- ✅ 列表查询: authConfig 显示为 `{"headerName": "X-API-Key", "apiKey": "***"}`
- ✅ 详情查询: 同上
- ✅ 数据库存储: 明文存储（后续需加密）

---

## 十一、集成框架功能

### 1. Workflow 集成（工作流插件节点）

**位置**: `workflow/WorkflowPluginExecutor.java`

**核心功能**:
- ✅ 支持参数表达式解析 (`${input.xxx}`, `${context.xxx}`, `${nodes.xxx}`)
- ✅ 支持输出映射到上下文变量
- ✅ 支持重试机制（retryCount）
- ✅ 支持超时配置

**使用示例**:
```java
PluginNodeConfig config = new PluginNodeConfig();
config.setPluginId("plugin_xxx");
config.setOperationId("getSensorData");
config.setParamMappings(Map.of(
    "sensor", "${input.sensorType}",  // 从工作流输入获取
    "uuid", "${context.deviceId}"     // 从上下文获取
));
config.setOutputMapping("sensorResult"); // 结果存入上下文
config.setRetryCount(2);                 // 失败重试2次

PluginNodeResult result = workflowPluginExecutor.execute(config, context);
```

### 2. Agent 工具调用框架（LLM Function Calling）

**位置**: `agent/AgentToolExecutor.java`

**核心功能**:
- ✅ 生成 OpenAI 兼容的工具定义（`getAvailableTools()`）
- ✅ 执行工具调用（`executeToolCall()`）
- ✅ 批量执行工具（`executeToolCalls()`）
- ✅ 工具映射缓存（提高性能）

**工具定义格式**:
```json
{
  "type": "function",
  "function": {
    "name": "plugin_xxx_getSensorData",
    "description": "获取传感器数据",
    "parameters": {
      "type": "object",
      "properties": {
        "sensor": {"type": "string", "description": "传感器类型"},
        "uuid": {"type": "string", "description": "设备UUID"}
      },
      "required": ["sensor", "uuid"]
    }
  }
}
```

**使用示例**:
```java
// 1. 获取Agent可用工具
List<AgentToolDefinition> tools = agentToolExecutor.getAvailableTools("agent_002");

// 2. 执行LLM返回的工具调用
AgentToolCallResult result = agentToolExecutor.executeToolCall(
    "call_abc123",
    "plugin_xxx_getSensorData",
    "{\"sensor\":\"温度\",\"uuid\":\"device-001\"}"
);
```

### 3. Chat 集成（对话中调用插件）

**位置**: `controller/ChatController.sendMessage()` (L90-L180)

**实现方式**:

**模式1: 手动指定插件调用（兼容旧方式）**
```java
// 前端请求格式
{
  "agentId": "agent_002",
  "query": "查询lorem设备温度",
  "pluginCall": {
    "pluginId": "plugin_xxx",
    "operationId": "getSensorData",
    "params": {"sensor": "温度", "uuid": "xxx"}
  }
}

// 后端处理流程
if (conversation.getPluginCall() != null) {
    // 1. 执行插件调用
    PluginInvokeResult result = pluginService.invokeOperation(...);
    
    // 2. 构造工具结果摘要
    String toolSummary = "插件调用成功，结果: " + result.getParsedData();
    
    // 3. 将结果作为上下文给LLM
    String llmInput = toolSummary + "\n原始问题: " + query;
    
    // 4. LLM生成自然语言回答
    String answer = llmService.chat(agentId, llmModelId, llmInput);
}
```

**模式2: 自动 Function Calling（推荐，新方式）**
```java
// 前端请求格式（无需指定 pluginCall）
{
  "sessionId": "xxx-xxx-xxx",
  "agentId": "agent_002",
  "query": "设备 1fcb3c12-63eb-4a67-9f85-293e24bf367c 温度多少",
  "metadata": {"llmModelId": "model-001-qwen-turbo"}
}

// 后端处理流程
else {
    // 1. 获取对话历史（用于提取之前使用过的设备 UUID）
    List<Map<String, String>> conversationHistory = getConversationHistory(sessionId);
    
    // 2. 使用 Function Calling 服务
    answer = functionCallingService.chatWithFunctions(
        agentId, llmModelId, query, sessionId, conversationHistory);
}
```

**特点**:
- ✅ 模式1: 用户/前端显式指定要调用的插件（精准控制）
- ✅ 模式2: 基于关键词自动匹配工具并执行（智能识别）
- ✅ 模式2 支持从用户问题中提取设备 UUID
- ✅ 模式2 支持会话级别的 UUID 缓存（后续对话无需重复提供 UUID）

---

## 十二、技术文档索引

| 文档名称 | 路径 | 说明 |
|---------|------|------|
| Plugin API文档 | `docs/PLUGIN_change.md` | 本文档 |
| 前端对接文档 | `docs/frontend-plugin-integration.md` | API封装、类型定义、Store示例 |
| 系统完整性报告 | `docs/PLUGIN_SYSTEM_COMPLETION_REPORT.md` | P0/P1任务完成情况 |
| 快速参考手册 | `docs/PLUGIN_SYSTEM_QUICK_REFERENCE.md` | API速查、测试用例 |
| 数据库设计 | `docs/database.md` | 表结构、字段说明 |
| 需求文档 | `docs/requirements/requirement.md` | 原始需求 |
