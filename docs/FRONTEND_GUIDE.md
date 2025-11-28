# 🎨 前端集成指南

## 📖 概述

本项目提供了一个完整的 Vue 3 前端管理界面，可以一键构建并集成到 Spring Boot 应用中。

---

## 🚀 快速开始（3 步完成）

### Step 1: 构建前端

**Windows 用户**：

```bash
cd frontend
build.bat
```

**Mac/Linux 用户**：

```bash
cd frontend
./build.sh
```

### Step 2: 启动后端

```bash
cd ..
mvn spring-boot:run
```

### Step 3: 访问界面

打开浏览器访问：`http://localhost:8080`

🎉 **完成！** 你现在可以在浏览器中测试所有功能了！

---

## 📸 界面预览

### 首页
- 功能模块导航
- 系统状态统计
- 快速测试入口

### 对话页面
- 多轮对话记忆
- 实时消息展示
- 会话管理

### SQL 助手
- 自然语言转 SQL
- Prompt 热更新
- Prompt 管理

### 知识库管理
- 添加知识
- 向量检索
- RAG 查询对比
- 统计信息

### AI Agent
- 多种 Agent 模式切换
- 任务编排可视化
- 交互式任务控制
- 实时流式输出

### MCP 工具
- MCP 服务器管理
- 工具列表展示
- 手动执行工具

---

## 🎯 功能清单

### ✅ 已实现功能

| 模块 | 功能 | 状态 |
|------|------|------|
| 首页 | 系统状态统计 | ✅ |
| 首页 | 快速测试 | ✅ |
| 对话 | 多轮对话 | ✅ |
| 对话 | 会话记忆 | ✅ |
| SQL | Text-to-SQL | ✅ |
| SQL | Prompt 管理 | ✅ |
| 知识库 | 添加知识 | ✅ |
| 知识库 | 向量检索 | ✅ |
| 知识库 | RAG 对比 | ✅ |
| Agent | Plan-and-Execute | ✅ |
| Agent | Reflexion | ✅ |
| Agent | Chain of Thought | ✅ |
| Agent | 智能路由 | ✅ |
| Agent | 任务编排 | ✅ |
| Agent | 交互式任务 | ✅ |
| Agent | 流式输出 | ✅ |
| MCP | 智能助手 | ✅ |
| MCP | 服务器列表 | ✅ |
| MCP | 工具列表 | ✅ |
| MCP | 手动执行 | ✅ |

---

## 🔧 技术架构

### 前端技术栈

```
Vue 3.4          - 渐进式框架
Vue Router 4.2   - 路由管理
Axios 1.6        - HTTP 客户端
Vite 5.0         - 构建工具
```

### 构建流程

```
源代码 (frontend/src)
  ↓
Vite 构建
  ↓
静态文件 (src/main/resources/static)
  ↓
Spring Boot 打包
  ↓
单一 JAR 文件
```

### 部署架构

```
用户浏览器
  ↓
http://localhost:8080
  ↓
Spring Boot (端口 8080)
  ├─ 静态资源 (/) → 前端页面
  └─ API 接口 (/ai/*) → 后端逻辑
```

---

## 📁 文件结构

```
robot/
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   │   ├── Home.vue        # 首页
│   │   │   ├── Chat.vue        # 对话
│   │   │   ├── Sql.vue         # SQL
│   │   │   ├── Knowledge.vue   # 知识库
│   │   │   ├── Agent.vue       # Agent
│   │   │   └── Mcp.vue         # MCP
│   │   ├── api/
│   │   │   └── index.js        # API 封装
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── index.html              # HTML 模板
│   ├── package.json            # 依赖配置
│   ├── vite.config.js          # Vite 配置
│   ├── build.sh                # 构建脚本 (Mac/Linux)
│   └── build.bat               # 构建脚本 (Windows)
└── src/main/resources/
    ├── static/                 # 构建输出目录
    │   ├── index.html          # 入口页面
    │   └── assets/             # 静态资源
    └── application.yaml        # Spring Boot 配置
```

---

## 🎨 开发模式

### 启动开发服务器

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动。

### 开发模式优势

- ✅ **热更新**: 修改代码立即生效
- ✅ **独立运行**: 不影响后端开发
- ✅ **开发工具**: Vue DevTools 完整支持
- ✅ **自动代理**: API 请求自动转发到后端

### API 代理配置

```javascript
// vite.config.js
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/ai': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 🏗️ 生产构建

### 自动构建（推荐）

**Windows**:
```bash
cd frontend
build.bat
```

**Mac/Linux**:
```bash
cd frontend
./build.sh
```

### 手动构建

```bash
cd frontend
npm install
npm run build
```

构建输出：`src/main/resources/static/`

### 构建配置

```javascript
// vite.config.js
export default defineConfig({
  build: {
    outDir: '../src/main/resources/static',  // 输出到 Spring Boot 静态资源目录
    emptyOutDir: true                        // 构建前清空目录
  }
})
```

---

## 🔌 Spring Boot 配置

### 静态资源配置

```yaml
# application.yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/
  mvc:
    static-path-pattern: /**
```

### 路由配置

Spring Boot 会自动处理：

- `/` → `static/index.html`（前端页面）
- `/ai/*` → 后端 API 接口
- `/assets/*` → 静态资源（JS、CSS）

---

## 📊 API 接口映射

### 前端 API 调用

```javascript
// src/api/index.js
import axios from 'axios'

const api = axios.create({
  baseURL: '/ai',           // API 基础路径
  timeout: 60000            // 超时时间
})

export const testChat = () => api.get('/chat/test')
export const chat = (memoryId, message) => 
  api.get('/chat', { params: { memoryId, userMessage: message } })
```

### 后端 API 端点

```java
@RestController
@RequestMapping("/ai")
public class AiServiceController {
    
    @GetMapping("/chat/test")
    public String test() { ... }
    
    @GetMapping("/chat")
    public String chat(@RequestParam String memoryId, 
                      @RequestParam String userMessage) { ... }
}
```

---

## 🎯 使用示例

### 示例 1: 测试对话功能

1. 访问 `http://localhost:8080`
2. 点击导航栏 "对话"
3. 输入会话 ID（如 `user-123`）
4. 输入消息："你好，我是张三"
5. 点击 "发送消息"
6. AI 回复后，继续问："我刚才说我叫什么？"
7. AI 会记住上下文回答："你说你叫张三"

### 示例 2: 测试 SQL 生成

1. 点击导航栏 "SQL助手"
2. 输入查询："查询所有在读学生的姓名和学号"
3. 点击 "生成 SQL"
4. 查看生成的 SQL 语句

### 示例 3: 测试 AI Agent

1. 点击导航栏 "AI Agent"
2. 选择模式："Plan-and-Execute"
3. 输入任务："帮我规划一个三天的北京旅游行程"
4. 点击 "执行任务"
5. 查看 AI 的规划过程和结果

---

## 🐛 故障排查

### 问题 1: 构建失败

**错误**: `npm: command not found`

**解决**:
1. 安装 Node.js: https://nodejs.org/
2. 重启终端
3. 验证安装: `node -v` 和 `npm -v`

### 问题 2: 访问 404

**错误**: 访问 `http://localhost:8080` 显示 404

**解决**:
1. 确认前端已构建: 检查 `src/main/resources/static/index.html` 是否存在
2. 确认 Spring Boot 配置正确（见上文）
3. 重启 Spring Boot 应用

### 问题 3: API 调用失败

**错误**: 前端显示 "Network Error"

**解决**:
1. 确认后端已启动: `http://localhost:8080/ai/chat/test`
2. 检查浏览器控制台错误信息
3. 检查后端日志

### 问题 4: 开发模式代理失败

**错误**: 开发模式下 API 请求 404

**解决**:
1. 确认后端在 `http://localhost:8080` 运行
2. 检查 `vite.config.js` 代理配置
3. 重启开发服务器: `npm run dev`

---

## 🚀 部署建议

### 开发环境

```bash
# 前端开发模式
cd frontend && npm run dev

# 后端开发模式（另一个终端）
mvn spring-boot:run
```

### 测试环境

```bash
# 构建前端
cd frontend && ./build.sh

# 启动后端
cd .. && mvn spring-boot:run
```

### 生产环境

```bash
# 构建前端
cd frontend && ./build.sh

# 打包应用
cd .. && mvn clean package

# 运行 JAR
java -jar target/robot-0.0.1-SNAPSHOT.jar
```

---

## 📚 扩展开发

### 添加新页面

1. 创建组件: `frontend/src/views/NewPage.vue`
2. 添加路由: `frontend/src/main.js`
3. 添加导航: `frontend/src/App.vue`
4. 重新构建

### 添加新 API

1. 后端添加接口: `AiServiceController.java`
2. 前端封装 API: `frontend/src/api/index.js`
3. 页面调用 API

### 自定义样式

全局样式在 `frontend/src/App.vue` 的 `<style>` 中。

---

## 🎓 学习资源

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Spring Boot 静态资源](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content)

---

<div align="center">

**前后端一体化 · 开箱即用**

Made with ❤️ by Robot Team

</div>

