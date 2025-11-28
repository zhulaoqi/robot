# 🚀 前端快速启动指南

## 📖 3 分钟启动前端界面

### Windows 用户

```bash
# 1. 进入前端目录
cd frontend

# 2. 双击运行（或命令行运行）
build.bat

# 3. 回到项目根目录，启动后端
cd ..
mvn spring-boot:run

# 4. 打开浏览器访问
http://localhost:8080
```

### Mac/Linux 用户

```bash
# 1. 进入前端目录
cd frontend

# 2. 运行构建脚本
./build.sh

# 3. 回到项目根目录，启动后端
cd ..
mvn spring-boot:run

# 4. 打开浏览器访问
http://localhost:8080
```

---

## 🎯 功能导航

访问 `http://localhost:8080` 后，你会看到以下模块：

### 1️⃣ 首页
- 查看系统状态
- 快速测试基础对话

### 2️⃣ 对话
- 测试多轮对话记忆
- 查看对话历史

### 3️⃣ SQL助手
- 自然语言转 SQL
- Prompt 热更新测试

### 4️⃣ 知识库
- 添加知识到向量库
- 测试 RAG 检索
- 查询改写对比

### 5️⃣ AI Agent
- Plan-and-Execute 模式
- Reflexion 模式
- Chain of Thought 模式
- 任务编排
- 交互式任务控制

### 6️⃣ MCP工具
- MCP 智能助手
- 查看可用工具
- 手动执行工具

---

## 💡 快速测试

### 测试 1: 基础对话

1. 点击首页的 "🚀 测试基础对话" 按钮
2. 查看 AI 回复

### 测试 2: 多轮对话

1. 进入 "对话" 页面
2. 输入："我叫张三，今年25岁"
3. 再输入："我刚才说我叫什么？"
4. AI 会记住上下文回答

### 测试 3: SQL 生成

1. 进入 "SQL助手" 页面
2. 输入："查询所有在读学生"
3. 点击 "生成 SQL"
4. 查看生成的 SQL 语句

### 测试 4: AI Agent

1. 进入 "AI Agent" 页面
2. 选择 "Plan-and-Execute" 模式
3. 输入："帮我规划一个三天的北京旅游行程"
4. 点击 "执行任务"
5. 查看 AI 的规划过程

---

## ❓ 常见问题

### Q: 构建失败怎么办？

**A**: 确保已安装 Node.js (https://nodejs.org/)

### Q: 访问 localhost:8080 显示 404？

**A**: 
1. 确认前端已构建（检查 `src/main/resources/static/index.html`）
2. 确认后端已启动
3. 重启后端应用

### Q: 如何开发调试？

**A**: 使用开发模式：

```bash
cd frontend
npm install
npm run dev
```

前端会在 `http://localhost:3000` 启动，自动代理后端 API。

---

## 📚 详细文档

- [前端 README](../frontend/README.md)
- [前端集成指南](FRONTEND_GUIDE.md)
- [API 文档](API.md)
- [Agent Demo 文档](AGENT_DEMO.md)

---

<div align="center">

**简单 · 快速 · 好用**

🎉 享受你的 AI 应用开发之旅！

</div>

