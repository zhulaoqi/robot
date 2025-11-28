<template>
  <div class="production-page">
    <!-- 顶部标题区 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <span class="icon">🚀</span>
          智能对话系统
        </h1>
        <p class="page-subtitle">完全自动化 · 智能路由 · 黑盒执行 · 生产就绪</p>
      </div>
      <div class="status-badge" :class="{ online: isOnline }">
        <span class="status-dot"></span>
        {{ isOnline ? '在线' : '离线' }}
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 对话区 -->
      <div class="chat-container">
        <!-- 消息列表 -->
        <div class="messages-area" ref="messagesArea">
          <div v-if="chatHistory.length === 0" class="empty-state">
            <div class="empty-icon">💬</div>
            <h3>开始对话</h3>
            <p>输入任何问题，系统会自动识别意图并智能处理</p>
            <div class="example-questions">
              <div class="example-label">试试这些：</div>
              <button @click="setExample('查询所有在读学生的姓名和学号')" class="example-btn">
                查询所有在读学生的姓名和学号
              </button>
              <button @click="setExample('深圳今天天气怎么样')" class="example-btn">
                深圳今天天气怎么样
              </button>
              <button @click="setExample('LangChain4j 是什么')" class="example-btn">
                LangChain4j 是什么
              </button>
            </div>
          </div>

          <div v-for="(msg, index) in chatHistory" :key="index" :class="['message-wrapper', msg.role]">
            <div class="message-bubble">
              <div class="message-header">
                <span class="message-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</span>
                <span class="message-role">{{ msg.role === 'user' ? '你' : 'AI 助手' }}</span>
                <span class="message-time">{{ msg.time }}</span>
              </div>
              <div class="message-content">{{ msg.content }}</div>
              
              <!-- 意图标签 -->
              <div v-if="msg.intent" class="message-intent">
                <span class="intent-badge" :class="msg.intent.type.toLowerCase()">
                  {{ intentLabels[msg.intent.type] || msg.intent.type }}
                </span>
                <span class="intent-confidence">{{ (msg.intent.confidence * 100).toFixed(0) }}%</span>
              </div>
              
              <!-- 能力使用情况 -->
              <div v-if="msg.capabilities" class="message-capabilities">
                <span v-if="msg.capabilities.knowledge" class="capability-tag">📚 知识库</span>
                <span v-if="msg.capabilities.tools" class="capability-tag">🔧 工具</span>
                <span v-if="msg.capabilities.memory" class="capability-tag">💾 记忆</span>
              </div>
              
              <!-- 性能指标 -->
              <div v-if="msg.performance" class="message-performance">
                <span class="perf-item">识别: {{ msg.performance.intent_recognition_ms }}ms</span>
                <span class="perf-item">执行: {{ msg.performance.execution_ms }}ms</span>
                <span class="perf-item">总计: {{ msg.performance.total_ms }}ms</span>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="message-wrapper assistant">
            <div class="message-bubble loading">
              <div class="message-header">
                <span class="message-avatar">🤖</span>
                <span class="message-role">AI 助手</span>
              </div>
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <div class="input-container">
            <textarea 
              v-model="message" 
              class="message-input" 
              placeholder="输入任何问题，系统会自动处理..."
              @keydown.enter.prevent="handleEnter"
              rows="1"
              ref="inputRef"
            ></textarea>
            <button @click="sendMessage" class="send-btn" :disabled="loading || !message.trim()">
              <span v-if="loading">⏳</span>
              <span v-else>✈️</span>
            </button>
          </div>
          <div class="input-footer">
            <span class="input-hint">Enter 发送 · Shift+Enter 换行</span>
            <span class="user-id">用户: {{ userId }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="info-panel">
        <!-- 系统状态 -->
        <div class="info-card">
          <h3 class="info-title">📊 系统状态</h3>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalMessages }}</div>
              <div class="stat-label">消息数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.avgResponseTime }}ms</div>
              <div class="stat-label">平均响应</div>
            </div>
          </div>
        </div>

        <!-- 能力说明 -->
        <div class="info-card">
          <h3 class="info-title">✨ 自动化能力</h3>
          <div class="capability-list">
            <div class="capability-item">
              <span class="cap-icon">🔍</span>
              <div class="cap-content">
                <div class="cap-title">智能意图识别</div>
                <div class="cap-desc">自动分析问题类型</div>
              </div>
            </div>
            <div class="capability-item">
              <span class="cap-icon">🎯</span>
              <div class="cap-content">
                <div class="cap-title">动态能力选择</div>
                <div class="cap-desc">自动启用所需功能</div>
              </div>
            </div>
            <div class="capability-item">
              <span class="cap-icon">⚡</span>
              <div class="cap-content">
                <div class="cap-title">自动任务执行</div>
                <div class="cap-desc">检索、生成、调用</div>
              </div>
            </div>
            <div class="capability-item">
              <span class="cap-icon">📈</span>
              <div class="cap-content">
                <div class="cap-title">性能监控</div>
                <div class="cap-desc">实时性能指标</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近意图 -->
        <div v-if="recentIntents.length > 0" class="info-card">
          <h3 class="info-title">🎯 最近识别</h3>
          <div class="intent-list">
            <div v-for="(intent, index) in recentIntents" :key="index" class="intent-item">
              <span class="intent-badge" :class="intent.type.toLowerCase()">
                {{ intentLabels[intent.type] }}
              </span>
              <span class="intent-confidence">{{ (intent.confidence * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import axios from 'axios'

const smartApi = axios.create({
  baseURL: '/api/smart',
  timeout: 60000
})

const userId = ref('user123')
const message = ref('')
const loading = ref(false)
const chatHistory = ref([])
const recentIntents = ref([])
const isOnline = ref(true)
const messagesArea = ref(null)
const inputRef = ref(null)

const intentLabels = {
  SQL_QUERY: 'SQL查询',
  KNOWLEDGE_QA: '知识问答',
  TOOL_CALL: '工具调用',
  PURE_CHAT: '纯对话'
}

const stats = computed(() => {
  const total = chatHistory.value.length
  const responseTimes = chatHistory.value
    .filter(m => m.performance)
    .map(m => m.performance.total_ms)
  
  const avgTime = responseTimes.length > 0
    ? Math.round(responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length)
    : 0
  
  return {
    totalMessages: total,
    avgResponseTime: avgTime
  }
})

const sendMessage = async () => {
  if (!message.value.trim() || loading.value) return
  
  const userMsg = message.value
  message.value = ''
  loading.value = true
  
  // 添加用户消息
  chatHistory.value.push({
    role: 'user',
    content: userMsg,
    time: new Date().toLocaleTimeString()
  })
  
  scrollToBottom()
  
  try {
    const res = await smartApi.get('/chat', {
      params: {
        userId: userId.value,
        message: userMsg
      }
    })
    
    // 添加 AI 回复
    chatHistory.value.push({
      role: 'assistant',
      content: res.data.message,
      time: new Date().toLocaleTimeString(),
      intent: res.data.intent,
      capabilities: res.data.capabilities_used,
      performance: res.data.performance
    })
    
    // 更新最近意图
    if (res.data.intent) {
      recentIntents.value.unshift({
        type: res.data.intent.type,
        confidence: res.data.intent.confidence
      })
      if (recentIntents.value.length > 5) {
        recentIntents.value.pop()
      }
    }
    
    scrollToBottom()
    
  } catch (error) {
    chatHistory.value.push({
      role: 'assistant',
      content: '❌ 错误: ' + (error.response?.data?.message || error.message),
      time: new Date().toLocaleTimeString()
    })
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

const handleEnter = (e) => {
  if (e.shiftKey) {
    // Shift+Enter 换行
    return
  }
  // Enter 发送
  e.preventDefault()
  sendMessage()
}

const setExample = (text) => {
  message.value = text
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus()
    }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesArea.value) {
      messagesArea.value.scrollTop = messagesArea.value.scrollHeight
    }
  })
}

// 检查健康状态
onMounted(async () => {
  try {
    await smartApi.get('/health')
    isOnline.value = true
  } catch (error) {
    isOnline.value = false
  }
})
</script>

<style scoped>
.production-page {
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e9ecef;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-title .icon {
  font-size: 3rem;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(15deg); }
}

.page-subtitle {
  color: #666;
  margin: 0.5rem 0 0 0;
  font-size: 1rem;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  background: #f8f9fa;
  font-size: 0.875rem;
  font-weight: 600;
}

.status-badge.online {
  background: #d4edda;
  color: #155724;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
}

.status-badge.online .status-dot {
  background: #28a745;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 主要内容区 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 2rem;
  flex: 1;
  min-height: 0;
}

/* 对话容器 */
.chat-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
  scroll-behavior: smooth;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.empty-state p {
  color: #666;
  margin-bottom: 2rem;
}

.example-questions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 500px;
  margin: 0 auto;
}

.example-label {
  font-size: 0.875rem;
  color: #999;
  margin-bottom: 0.5rem;
}

.example-btn {
  padding: 1rem;
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
  font-size: 0.875rem;
}

.example-btn:hover {
  border-color: #667eea;
  background: #f0f4ff;
  transform: translateX(5px);
}

/* 消息气泡 */
.message-wrapper {
  margin-bottom: 1.5rem;
  display: flex;
  animation: slideIn 0.3s;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 1rem 1.5rem;
  border-radius: 16px;
  background: #f8f9fa;
}

.message-wrapper.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-wrapper.assistant .message-bubble {
  background: white;
  border: 2px solid #e9ecef;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
}

.message-avatar {
  font-size: 1.25rem;
}

.message-role {
  font-weight: 600;
}

.message-time {
  margin-left: auto;
  opacity: 0.6;
  font-size: 0.75rem;
}

.message-content {
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-intent {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.message-wrapper.user .message-intent {
  border-top-color: rgba(255, 255, 255, 0.2);
}

.intent-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.intent-badge.sql_query {
  background: #d4edda;
  color: #155724;
}

.intent-badge.knowledge_qa {
  background: #d1ecf1;
  color: #0c5460;
}

.intent-badge.tool_call {
  background: #fff3cd;
  color: #856404;
}

.intent-badge.pure_chat {
  background: #f8d7da;
  color: #721c24;
}

.intent-confidence {
  font-size: 0.75rem;
  opacity: 0.6;
}

.message-capabilities {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.capability-tag {
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  font-size: 0.7rem;
  background: rgba(255, 255, 255, 0.2);
}

.message-wrapper.assistant .capability-tag {
  background: #f8f9fa;
}

.message-performance {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
  font-size: 0.7rem;
  opacity: 0.6;
}

/* 加载动画 */
.typing-indicator {
  display: flex;
  gap: 0.25rem;
  padding: 0.5rem 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 输入区 */
.input-area {
  padding: 1.5rem;
  background: #f8f9fa;
  border-top: 2px solid #e9ecef;
}

.input-container {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 16px;
  font-size: 1rem;
  resize: none;
  font-family: inherit;
  transition: all 0.3s;
  max-height: 120px;
}

.message-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  font-size: 0.75rem;
  color: #999;
}

.user-id {
  font-weight: 600;
}

/* 信息面板 */
.info-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  overflow-y: auto;
}

.info-card {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.info-title {
  font-size: 1rem;
  margin: 0 0 1rem 0;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 0.75rem;
  color: #666;
  margin-top: 0.25rem;
}

.capability-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.capability-item {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.cap-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 12px;
}

.cap-title {
  font-weight: 600;
  font-size: 0.875rem;
  color: #333;
}

.cap-desc {
  font-size: 0.75rem;
  color: #999;
}

.intent-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.intent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
