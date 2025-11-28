<template>
  <div class="home">
    <div class="hero">
      <h1 class="hero-title">🤖 Robot AI Assistant</h1>
      <p class="hero-subtitle">基于 Langchain4j 的企业级 AI 对话系统</p>
      
      <!-- 生产级入口 -->
      <div class="production-entry">
        <router-link to="/production" class="production-btn">
          <div class="production-icon">🚀</div>
          <div class="production-content">
            <h3>生产级对话系统</h3>
            <p>动态配置 · 统一入口 · 能力组合</p>
          </div>
          <div class="production-arrow">→</div>
        </router-link>
      </div>
    </div>

    <div class="features grid grid-3">
      <div class="feature-card">
        <div class="feature-icon">💬</div>
        <h3>智能对话</h3>
        <p>支持多轮对话记忆，流式输出，上下文理解</p>
        <router-link to="/chat" class="feature-link">立即体验 →</router-link>
      </div>

      <div class="feature-card">
        <div class="feature-icon">🗄️</div>
        <h3>Text-to-SQL</h3>
        <p>自然语言转 SQL，智能数据库查询</p>
        <router-link to="/sql" class="feature-link">立即体验 →</router-link>
      </div>

      <div class="feature-card">
        <div class="feature-icon">📚</div>
        <h3>RAG 知识库</h3>
        <p>向量检索增强生成，专业领域问答</p>
        <router-link to="/knowledge" class="feature-link">立即体验 →</router-link>
      </div>

      <div class="feature-card">
        <div class="feature-icon">🤖</div>
        <h3>AI Agent</h3>
        <p>任务编排，多步骤执行，智能路由</p>
        <router-link to="/agent" class="feature-link">立即体验 →</router-link>
      </div>

      <div class="feature-card">
        <div class="feature-icon">🔌</div>
        <h3>MCP 工具</h3>
        <p>跨语言工具调用，Python + Java 协作</p>
        <router-link to="/mcp" class="feature-link">立即体验 →</router-link>
      </div>

      <div class="feature-card">
        <div class="feature-icon">⚡</div>
        <h3>实时流式</h3>
        <p>SSE 推送，任务进度可视化</p>
        <router-link to="/agent" class="feature-link">立即体验 →</router-link>
      </div>
    </div>

    <div class="stats card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
        <h2 class="card-title" style="margin: 0;">系统状态</h2>
        <button @click="loadStats" class="btn btn-secondary btn-sm">🔄 刷新</button>
      </div>
      <div class="stats-grid grid grid-3">
        <div class="stat-item">
          <div class="stat-value">{{ stats.knowledge }}</div>
          <div class="stat-label">知识库条目</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.tasks }}</div>
          <div class="stat-label">运行中任务</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.tools }}</div>
          <div class="stat-label">可用工具</div>
        </div>
      </div>
    </div>

    <div class="quick-test card">
      <h2 class="card-title">快速测试</h2>
      <button @click="quickTest" class="btn btn-primary" :disabled="testing">
        {{ testing ? '测试中...' : '🚀 测试基础对话' }}
      </button>
      <div v-if="testResult" class="result-box">{{ testResult }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { testChat, getKnowledgeStats, listTasks, listMcpTools } from '../api'

const stats = ref({
  knowledge: 0,
  tasks: 0,
  tools: 0
})

const testing = ref(false)
const testResult = ref('')

const loadStats = async () => {
  // 知识库统计
  try {
    const knowledgeRes = await getKnowledgeStats()
    stats.value.knowledge = knowledgeRes.data.total_vectors || 0
    console.log('知识库条目:', stats.value.knowledge)
  } catch (error) {
    console.error('加载知识库统计失败:', error)
    stats.value.knowledge = 0
  }
  
  // 任务统计
  try {
    const tasksRes = await listTasks()
    stats.value.tasks = tasksRes.data.total || 0
    console.log('运行中任务:', stats.value.tasks)
  } catch (error) {
    console.error('加载任务统计失败:', error)
    stats.value.tasks = 0
  }
  
  // 工具统计
  try {
    const toolsRes = await listMcpTools()
    console.log('工具列表响应:', toolsRes.data)
    
    let toolCount = 0
    if (toolsRes.data) {
      // 遍历所有服务器的工具
      for (const [serverName, tools] of Object.entries(toolsRes.data)) {
        if (Array.isArray(tools)) {
          toolCount += tools.length
          console.log(`${serverName}: ${tools.length} 个工具`)
        }
      }
    }
    stats.value.tools = toolCount
    console.log('可用工具总数:', toolCount)
  } catch (error) {
    console.error('加载工具列表失败:', error)
    stats.value.tools = 0
  }
}

const quickTest = async () => {
  testing.value = true
  testResult.value = ''
  try {
    const res = await testChat()
    testResult.value = res.data
  } catch (error) {
    testResult.value = '测试失败: ' + error.message
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 3rem 0;
  margin-bottom: 3rem;
}

.hero-title {
  font-size: 3rem;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 1rem;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
}

.features {
  margin-bottom: 3rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.feature-card p {
  color: #666;
  margin-bottom: 1rem;
}

.feature-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.stats-grid {
  margin-top: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  color: #666;
  margin-top: 0.5rem;
}

.quick-test {
  text-align: center;
}

.quick-test .btn {
  margin-top: 1rem;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.production-entry {
  margin-top: 2rem;
}

.production-btn {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  text-decoration: none;
  color: white;
  transition: all 0.3s;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.production-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.4);
}

.production-icon {
  font-size: 3rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.production-content h3 {
  font-size: 1.5rem;
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.production-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.875rem;
}

.production-arrow {
  font-size: 2rem;
  margin-left: auto;
  transition: transform 0.3s;
}

.production-btn:hover .production-arrow {
  transform: translateX(10px);
}
</style>

