# AI Agent Platform Frontend

AI 智能体平台前端应用，基于 Vue 3 + TypeScript + Vite 构建的现代化 Web 应用。

## 📋 项目简介

这是一个功能完整的 AI 智能体管理平台前端项目，提供智能体管理、插件管理、对话测试等核心功能。项目采用现代化的前端技术栈，支持 Mock 数据模式和真实 API 模式切换，便于开发和测试。

## ✨ 主要功能

- **智能体管理** - 创建、编辑、删除和查看智能体配置
- **插件管理** - 管理 AI 智能体可用的插件工具
- **对话测试** - 与智能体进行实时对话，测试智能体功能
- **系统测试** - 系统功能测试页面

## 🛠️ 技术栈

- **框架**: Vue 3.5+ (Composition API)
- **语言**: TypeScript 5.9+
- **构建工具**: Vite 7.2+
- **UI 框架**: Element Plus 2.11+
- **状态管理**: Pinia 3.0+
- **路由**: Vue Router 4.6+
- **HTTP 客户端**: Axios 1.13+
- **样式**: Tailwind CSS 4.1+
- **图标**: Element Plus Icons

## 📁 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口定义
│   │   ├── agent.ts       # 智能体相关 API
│   │   ├── chat.ts        # 对话相关 API
│   │   ├── plugin.ts      # 插件相关 API
│   │   ├── llm.ts         # LLM 模型相关 API
│   │   └── index.ts       # API 统一导出
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   │   ├── MainLayout.vue # 主布局组件
│   │   └── HelloWorld.vue
│   ├── mock/              # Mock 数据
│   │   └── data.ts        # Mock 数据定义
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # Pinia 状态管理
│   │   ├── index.ts
│   │   ├── useAgentStore.ts    # 智能体状态管理
│   │   └── usePluginStore.ts   # 插件状态管理
│   ├── styles/            # 全局样式
│   │   ├── index.css
│   │   └── tailwind.css
│   ├── types/             # TypeScript 类型定义
│   │   └── entity.ts      # 实体类型定义
│   ├── utils/             # 工具函数
│   │   └── http.ts        # HTTP 请求封装
│   ├── views/             # 页面视图
│   │   ├── agent/         # 智能体相关页面
│   │   │   ├── AgentList.vue
│   │   │   ├── AgentEditor.vue
│   │   │   └── components/
│   │   ├── chat/          # 对话相关页面
│   │   │   ├── ChatView.vue
│   │   │   └── components/
│   │   ├── plugin/        # 插件相关页面
│   │   │   ├── PluginList.vue
│   │   │   └── PluginDialog.vue
│   │   └── HomeView.vue   # 首页
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── Dockerfile             # Docker 构建文件
├── nginx.conf             # Nginx 配置
├── package.json           # 项目依赖
├── tsconfig.json          # TypeScript 配置
├── vite.config.ts         # Vite 配置
└── tailwind.config.ts     # Tailwind CSS 配置
```

## 🚀 快速开始

### 环境要求

- Node.js >= 20.x
- npm >= 9.x 或 yarn >= 1.22.x

### 安装依赖

```bash
npm install
```

### 开发模式

启动开发服务器（默认端口 5173）：

```bash
npm run dev
```

开发服务器会自动启动，支持热模块替换（HMR）。

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

### 预览生产构建

```bash
npm run preview
```

## ⚙️ 配置说明

### 环境变量

项目支持通过环境变量进行配置，`.env.development`：

```env
# API 基础路径（默认: /api）
VITE_BASE_API=/api

# 是否启用 Mock 模式（默认: true）
# 设置为 false 时，将使用真实的后端 API
VITE_USE_MOCK=true
```

### API 代理配置

开发模式下，Vite 会将 `/api` 请求代理到后端服务器。在 `vite.config.ts` 中配置：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true,
    }
  }
}
```

### Mock 模式

项目内置了完整的 Mock 数据支持，默认启用 Mock 模式。Mock 数据定义在 `src/mock/data.ts` 中。

- **启用 Mock**: 设置 `VITE_USE_MOCK=true` 或不设置（默认启用）
- **禁用 Mock**: 设置 `VITE_USE_MOCK=false`，将使用真实后端 API

## 🐳 Docker 部署

### 构建 Docker 镜像

```bash
docker build -t ai-agent-platform-frontend .
```

### 运行容器

```bash
docker run -d -p 80:80 ai-agent-platform-frontend
```

### Docker Compose

如果使用 Docker Compose，确保后端服务名为 `backend`，端口为 `8080`。Nginx 配置会自动代理 `/api/` 请求到后端服务。

## 📡 API 接口

### 智能体 API

- `GET /api/v1/agents` - 获取智能体列表
- `GET /api/v1/agents/:id` - 获取智能体详情
- `POST /api/v1/agents` - 创建智能体
- `PUT /api/v1/agents/:id` - 更新智能体
- `DELETE /api/v1/agents/:id` - 删除智能体

### 插件 API

- `GET /api/v1/plugins` - 获取插件列表
- `GET /api/v1/plugins/:id` - 获取插件详情
- `POST /api/v1/plugins` - 创建插件
- `PUT /api/v1/plugins/:id` - 更新插件
- `DELETE /api/v1/plugins/:id` - 删除插件

### 对话 API

- `POST /api/v1/chat/session` - 创建会话
- `GET /api/v1/chat/history/:sessionId` - 获取消息历史
- `POST /api/v1/chat/message` - 发送消息

### 响应格式

后端统一响应格式：

```typescript
{
  code: number,        // 状态码，0 表示成功
  message: string,     // 消息
  data: any,          // 数据
  timestamp: number    // 时间戳
}
```

## 🎨 开发规范

### 代码风格

- 使用 TypeScript 进行类型检查
- 遵循 Vue 3 Composition API 最佳实践
- 使用 ESLint 和 Prettier 保持代码风格一致（如已配置）

### 组件开发

- 使用 `<script setup>` 语法
- 组件命名使用 PascalCase
- Props 和 Emits 需要定义类型

### 状态管理

- 使用 Pinia 进行全局状态管理
- Store 文件命名格式：`useXxxStore.ts`
- 每个功能模块对应一个 Store

### API 调用

- 所有 API 调用统一通过 `src/api/` 目录下的文件
- 使用 `src/utils/http.ts` 封装的 axios 实例
- 支持 Mock 模式和真实 API 模式自动切换

## 🔧 常见问题

### 1. 端口被占用

修改 `vite.config.ts` 中的 `server.port` 配置：

```typescript
server: {
  port: 3000,  // 修改为其他端口
  // ...
}
```

### 2. API 请求失败

- 检查后端服务是否正常运行
- 确认 `VITE_BASE_API` 配置正确
- 检查浏览器控制台的网络请求错误信息
- 确认 CORS 配置正确

### 3. Mock 数据不生效

- 确认 `VITE_USE_MOCK` 环境变量设置为 `true`
- 检查 `src/mock/data.ts` 文件是否存在
- 重启开发服务器

## 📝 更新日志

### v0.0.0

- 初始版本
- 实现智能体管理功能
- 实现插件管理功能
- 实现对话测试功能
- 支持 Mock 数据模式
- 支持 Docker 部署

## 📄 许可证

本项目为私有项目。

## 👥 贡献

欢迎提交 Issue 和 Pull Request。

## 📮 联系方式

如有问题或建议，请联系项目维护者。

