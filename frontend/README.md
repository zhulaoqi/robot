# 🎨 Robot AI Assistant - 前端管理界面

## 📖 简介

这是一个基于 **Vue 3** + **Vite** 构建的轻量级前端管理界面，用于测试和演示 Robot AI Assistant 的所有功能。

### ✨ 特性

- 🎯 **简洁美观** - 现代化 UI 设计，渐变色主题
- 🚀 **快速开发** - Vite 构建，热更新，开发体验极佳
- 📱 **响应式布局** - 适配桌面和移动端
- 🔌 **完整功能** - 覆盖所有后端 API 接口
- 🎨 **无需依赖** - 纯 Vue 3 + 原生 CSS，无额外 UI 框架

---

## 🚀 快速开始

### 方式一：开发模式（推荐用于开发调试）

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

前端将在 `http://localhost:3000` 启动，自动代理后端 API 到 `http://localhost:8080`。

**优势**：
- ✅ 热更新，修改代码立即生效
- ✅ 独立运行，不影响后端
- ✅ 开发工具支持完善

---

### 方式二：生产模式（集成到 Spring Boot）

#### Windows 用户

```bash
# 进入前端目录
cd frontend

# 双击运行构建脚本
build.bat

# 或者命令行运行
.\build.bat
```

#### Mac/Linux 用户

```bash
# 进入前端目录
cd frontend

# 运行构建脚本
./build.sh
```

构建完成后，前端文件会自动输出到 `src/main/resources/static` 目录。

**启动 Spring Boot 应用**：

```bash
# 回到项目根目录
cd ..

# 启动后端
mvn spring-boot:run
```

访问 `http://localhost:8080` 即可看到前端界面！

**优势**：
- ✅ 单一端口，无需跨域
- ✅ 部署简单，只需一个 jar 包
- ✅ 适合生产环境

---

## 📁 项目结构

```
frontend/
├── index.html              # 入口 HTML
├── package.json            # 依赖配置
├── vite.config.js          # Vite 配置
├── build.sh                # Mac/Linux 构建脚本
├── build.bat               # Windows 构建脚本
└── src/
    ├── main.js             # 应用入口
    ├── App.vue             # 根组件
    ├── api/
    │   └── index.js        # API 接口封装
    └── views/
        ├── Home.vue        # 首页
        ├── Chat.vue        # 对话页面
        ├── Sql.vue         # SQL 助手
        ├── Knowledge.vue   # 知识库管理
        ├── Agent.vue       # AI Agent
        └── Mcp.vue         # MCP 工具
```

---

## 🎯 功能模块

### 1️⃣ 首页 (Home)

- 📊 系统状态统计
- 🚀 快速测试入口
- 🎨 功能模块导航

### 2️⃣ 智能对话 (Chat)

- 💬 多轮对话记忆
- 📝 会话 ID 管理
- 🕐 对话历史展示

**测试示例**：
```
会话 ID: user-123
消息: 我叫张三，今年25岁
→ AI: 你好，张三！很高兴认识你。

消息: 我刚才说我叫什么？
→ AI: 你说你叫张三，今年25岁。
```

### 3️⃣ SQL 助手 (Sql)

- 🗄️ 自然语言转 SQL
- 🔥 Prompt 热更新测试
- 📝 Prompt 管理

**测试示例**：
```
查询: 查询所有在读学生的姓名和学号
→ SELECT student_no, name FROM students WHERE status = '在读';
```

### 4️⃣ 知识库管理 (Knowledge)

- ➕ 添加知识
- 🔍 向量检索
- 📊 统计信息
- 🗑️ 清空知识库
- 🎯 RAG 查询对比

**测试示例**：
```
添加知识: Langchain4j 是一个用于构建 AI 应用的 Java 框架
检索查询: 什么是 Langchain4j？
→ 相似度: 95.23% - Langchain4j 是一个用于构建 AI 应用的 Java 框架
```

### 5️⃣ AI Agent (Agent)

- 🤖 多种 Agent 模式
  - Plan-and-Execute（计划执行）
  - Reflexion（反思改进）
  - Chain of Thought（思维链）
  - 智能路由
- 📋 任务编排
- 🎮 交互式任务控制
- 📡 实时流式编排

**测试示例**：
```
模式: Plan-and-Execute
任务: 帮我规划一个三天的北京旅游行程
→ AI 会自动规划：
   1. 查询北京天气
   2. 搜索热门景点
   3. 规划路线
   4. 推荐美食
```

### 6️⃣ MCP 工具 (Mcp)

- 🔌 MCP 智能助手
- 🌐 MCP 服务器列表
- 🔧 可用工具展示
- ⚡ 手动执行工具

**测试示例**：
```
消息: 帮我计算 sqrt(16) + pow(2, 3)，然后查询深圳天气
→ [调用 Python MCP calculator] 计算结果：12.0
   [调用 Java getWeather] 深圳今天晴天，温度25-32℃
```

---

## 🎨 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vue Router 4** - 官方路由管理器
- **Axios** - HTTP 客户端
- **Vite 5** - 下一代前端构建工具
- **原生 CSS** - 无额外 UI 框架，轻量高效

---

## 🔧 开发指南

### 添加新页面

1. 在 `src/views/` 创建新组件：

```vue
<!-- src/views/NewPage.vue -->
<template>
  <div class="new-page">
    <div class="card">
      <h2 class="card-title">新页面</h2>
      <!-- 你的内容 -->
    </div>
  </div>
</template>

<script setup>
// 你的逻辑
</script>

<style scoped>
/* 你的样式 */
</style>
```

2. 在 `src/main.js` 添加路由：

```javascript
import NewPage from './views/NewPage.vue'

const routes = [
  // ... 现有路由
  { path: '/new', component: NewPage }
]
```

3. 在导航栏添加链接（`src/App.vue`）：

```vue
<router-link to="/new" class="nav-item">新页面</router-link>
```

### 添加新 API

在 `src/api/index.js` 添加：

```javascript
export const newApi = (params) => api.get('/new-endpoint', { params })
```

---

## 🎯 样式规范

项目使用统一的样式类：

### 布局类

- `.card` - 卡片容器
- `.grid` - 网格布局
- `.grid-2` - 两列网格
- `.grid-3` - 三列网格

### 表单类

- `.input-group` - 输入组
- `.input-label` - 标签
- `.input-field` - 输入框
- `.textarea-field` - 文本域

### 按钮类

- `.btn` - 基础按钮
- `.btn-primary` - 主要按钮（紫色渐变）
- `.btn-secondary` - 次要按钮（灰色）
- `.btn-danger` - 危险按钮（红色）

### 结果展示类

- `.result-box` - 结果框
- `.success` - 成功提示
- `.error` - 错误提示
- `.loading` - 加载状态

---

## 📊 API 代理配置

开发模式下，Vite 会自动代理 API 请求：

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/ai': {
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true
      }
    }
  }
})
```

**说明**：
- 前端请求 `/ai/chat/test`
- 实际请求 `http://localhost:8080/ai/chat/test`
- 无需担心跨域问题

---

## 🚀 构建优化

### 开发模式

```bash
npm run dev
```

- ✅ 热更新
- ✅ Source Map
- ✅ 开发工具支持

### 生产构建

```bash
npm run build
```

- ✅ 代码压缩
- ✅ Tree Shaking
- ✅ 资源优化
- ✅ 输出到 `../src/main/resources/static`

---

## ❓ 常见问题

### Q: 前端启动后无法访问后端 API？

**A**: 确保后端已启动在 `http://localhost:8080`，并检查 `vite.config.js` 的代理配置。

### Q: 构建后访问 404？

**A**: 确保：
1. 构建成功（检查 `src/main/resources/static` 目录）
2. Spring Boot 已配置静态资源（`application.yaml`）
3. 访问 `http://localhost:8080`（不是 3000）

### Q: 如何修改后端地址？

**A**: 修改 `vite.config.js` 中的 `proxy.target`：

```javascript
proxy: {
  '/ai': {
    target: 'http://your-backend:8080',  // 修改这里
    changeOrigin: true
  }
}
```

### Q: 如何添加新的依赖？

**A**: 使用 npm 安装：

```bash
npm install package-name
```

---

## 📚 相关文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Vue Router 文档](https://router.vuejs.org/zh/)
- [Axios 文档](https://axios-http.com/zh/)
- [后端 API 文档](../docs/API.md)

---

## 🤝 贡献指南

欢迎提交 PR 改进前端界面！

**改进方向**：
- 🎨 UI/UX 优化
- 🚀 性能优化
- 📱 移动端适配
- 🌐 国际化支持
- ♿ 无障碍支持

---

<div align="center">

**简洁 · 高效 · 易用**

Made with ❤️ by Robot Team

</div>

