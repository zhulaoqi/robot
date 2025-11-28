<template>
  <div class="unified-chat-page">
    <div class="card">
      <h2 class="card-title">🎯 统一对话接口（生产级）</h2>
      <p class="subtitle">一个接口搞定所有场景，系统自动识别意图并路由到合适的处理方式</p>
      
      <div class="input-group">
        <label class="input-label">用户 ID</label>
        <input v-model="userId" class="input-field" placeholder="输入用户ID（可选）" />
      </div>

      <div class="input-group">
        <label class="input-label">消息内容</label>
        <textarea 
          v-model="message" 
          class="textarea-field" 
          placeholder="输入任何问题，系统会自动识别意图：&#10;- SQL查询：查询所有在读学生&#10;- 知识问答：什么是Langchain4j&#10;- 工具调用：深圳今天天气怎么样&#10;- 任务规划：帮我规划一个北京三日游"
          @keydown.ctrl.enter="sendMessage"
        ></textarea>
      </div>

      <div class="button-group">
        <button @click="sendMessage" class="btn btn-primary" :disabled="loading">
          {{ loading ? '处理中...' : '🚀 发送消息 (Ctrl+Enter)' }}
        </button>
        <button @click="sendWithOrchestration" class="btn btn-secondary" :disabled="loading">
          📋 任务编排模式
        </button>
        <button @click="clearHistory" class="btn btn-secondary">清空历史</button>
      </div>

      <div v-if="chatHistory.length > 0" class="chat-history">
        <h3>对话历史</h3>
        <div v-for="(item, index) in chatHistory" :key="index" class="chat-message" :class="item.role">
          <div class="message-header">
            <span class="message-role">{{ item.role === 'user' ? '👤 用户' : '🤖 AI' }}</span>
            <span class="message-mode" v-if="item.mode">{{ item.mode }}</span>
            <span class="message-time">{{ item.time }}</span>
          </div>
          <div class="message-content">{{ item.content }}</div>
          <div v-if="item.duration" class="message-meta">
            耗时: {{ item.duration }}ms
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">📊 功能说明</h2>
      
      <div class="features-grid">
        <div class="feature-box">
          <div class="feature-icon">🗄️</div>
          <h4>SQL 查询</h4>
          <p>自动检索 DDL + 生成 SQL</p>
          <code>查询所有在读学生</code>
        </div>

        <div class="feature-box">
          <div class="feature-icon">📚</div>
          <h4>知识问答</h4>
          <p>向量检索 + RAG 增强</p>
          <code>什么是 Langchain4j</code>
        </div>

        <div class="feature-box">
          <div class="feature-icon">🔧</div>
          <h4>工具调用</h4>
          <p>天气、地点、时间等</p>
          <code>深圳今天天气怎么样</code>
        </div>

        <div class="feature-box">
          <div class="feature-icon">📋</div>
          <h4>任务编排</h4>
          <p>复杂多步骤任务</p>
          <code>帮我规划北京三日游</code>
        </div>

        <div class="feature-box">
          <div class="feature-icon">🧮</div>
          <h4>数学计算</h4>
          <p>复杂数学运算</p>
          <code>计算 sqrt(16) + pow(2, 3)</code>
        </div>

        <div class="feature-box">
          <div class="feature-icon">💻</div>
          <h4>代码生成</h4>
          <p>自动生成并检查代码</p>
          <code>写一个快速排序函数</code>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">🎯 统一接口 vs 基础接口</h2>
      
      <div class="comparison-table">
        <table>
          <thead>
            <tr>
              <th>特性</th>
              <th>统一接口 (/api/v1/chat)</th>
              <th>基础接口 (/ai/chat/*)</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>使用场景</td>
              <td>✅ 生产环境、对外服务</td>
              <td>🔧 学习、调试、测试</td>
            </tr>
            <tr>
              <td>接口数量</td>
              <td>✅ 1个接口搞定</td>
              <td>📚 多个功能接口</td>
            </tr>
            <tr>
              <td>意图识别</td>
              <td>✅ 自动识别</td>
              <td>❌ 手动指定</td>
            </tr>
            <tr>
              <td>响应格式</td>
              <td>✅ 统一格式</td>
              <td>📝 各有不同</td>
            </tr>
            <tr>
              <td>集成难度</td>
              <td>✅ 简单</td>
              <td>🔧 需要了解细节</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { unifiedChat, unifiedChatOrchestration } from '../api'

const userId = ref('user-' + Date.now())
const message = ref('')
const loading = ref(false)
const chatHistory = ref([])

const sendMessage = async () => {
  if (!message.value.trim()) return
  
  const userMessage = message.value
  chatHistory.value.push({
    role: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })
  
  message.value = ''
  loading.value = true
  
  try {
    const res = await unifiedChat(userMessage, userId.value)
    const data = res.data
    
    chatHistory.value.push({
      role: 'assistant',
      content: JSON.stringify(data.result, null, 2),
      mode: data.result?.mode || '智能路由',
      duration: data.duration_ms,
      time: new Date().toLocaleTimeString()
    })
  } catch (error) {
    chatHistory.value.push({
      role: 'assistant',
      content: '错误: ' + error.message,
      time: new Date().toLocaleTimeString()
    })
  } finally {
    loading.value = false
  }
}

const sendWithOrchestration = async () => {
  if (!message.value.trim()) return
  
  const userMessage = message.value
  chatHistory.value.push({
    role: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })
  
  message.value = ''
  loading.value = true
  
  try {
    const res = await unifiedChatOrchestration(userMessage, userId.value)
    const data = res.data
    
    chatHistory.value.push({
      role: 'assistant',
      content: JSON.stringify(data.orchestration, null, 2),
      mode: '任务编排',
      duration: data.duration_ms,
      time: new Date().toLocaleTimeString()
    })
  } catch (error) {
    chatHistory.value.push({
      role: 'assistant',
      content: '错误: ' + error.message,
      time: new Date().toLocaleTimeString()
    })
  } finally {
    loading.value = false
  }
}

const clearHistory = () => {
  chatHistory.value = []
  userId.value = 'user-' + Date.now()
}
</script>

<style scoped>
.subtitle {
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.chat-history {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e0e0e0;
}

.chat-history h3 {
  margin-bottom: 1rem;
  color: #333;
}

.chat-message {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border-radius: 8px;
}

.chat-message.user {
  background: #e3f2fd;
  margin-left: 10%;
}

.chat-message.assistant {
  background: #f5f5f5;
  margin-right: 10%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.message-role {
  font-weight: bold;
  color: #667eea;
}

.message-mode {
  padding: 0.25rem 0.5rem;
  background: #667eea;
  color: white;
  border-radius: 4px;
  font-size: 0.75rem;
}

.message-time {
  font-size: 0.875rem;
  color: #999;
  margin-left: auto;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.6;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.message-meta {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #999;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1rem;
}

.feature-box {
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
  border: 2px solid #e0e0e0;
  transition: all 0.3s;
}

.feature-box:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.feature-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.feature-box h4 {
  margin-bottom: 0.5rem;
  color: #333;
}

.feature-box p {
  color: #666;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.feature-box code {
  display: block;
  background: white;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  color: #667eea;
  margin-top: 0.5rem;
}

.comparison-table {
  overflow-x: auto;
  margin-top: 1rem;
}

.comparison-table table {
  width: 100%;
  border-collapse: collapse;
}

.comparison-table th,
.comparison-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.comparison-table th {
  background: #f8f9fa;
  font-weight: bold;
  color: #333;
}

.comparison-table td {
  color: #666;
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .chat-message.user {
    margin-left: 0;
  }
  
  .chat-message.assistant {
    margin-right: 0;
  }
}
</style>

