# AI Agent Platform - Frontend

AI Agent 平台前端项目，基于 Vue 3 + TypeScript + Vite 构建。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **UI 组件库**: Element Plus
- **HTTP 客户端**: Axios
- **样式框架**: Tailwind CSS

## 快速开始

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

启动后，访问 `http://localhost:5173`（或终端显示的地址）

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist/` 目录

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── api/          # API 接口定义
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── router/       # 路由配置
│   ├── stores/       # Pinia 状态管理
│   ├── styles/       # 全局样式
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   ├── App.vue       # 根组件
│   └── main.ts       # 入口文件
├── public/           # 公共静态资源
├── index.html        # HTML 模板
└── vite.config.ts    # Vite 配置
```

## 环境配置

### 开发环境

项目使用 `.env.development` 文件配置开发环境变量：

```env
VITE_BASE_API=/api
```

### API 代理

开发环境下，Vite 会将 `/api` 请求代理到后端服务器（默认：`http://localhost:8080`）

代理配置在 `vite.config.ts` 中：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```

## 开发说明

### 路径别名

项目配置了 `@` 别名指向 `src/` 目录，使用示例：

```typescript
import { http } from '@/utils/http'
import { getHello } from '@/api'
```

### API 调用

所有 API 请求通过 `src/utils/http.ts` 封装的 axios 实例，已配置：

- 请求/响应拦截器
- 统一错误处理
- 自动解包后端响应格式 `{code, message, data, timestamp}`

使用示例：

```typescript
import { getHello } from '@/api'

const message = await getHello()
```

## 脚本命令

- `npm run dev` - 启动开发服务器
- `npm run build` - 构建生产版本
- `npm run preview` - 预览生产构建


