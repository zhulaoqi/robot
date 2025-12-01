<template>
  <div class="production-page">
    <!-- 极简顶部栏 -->
    <div class="page-header">
      <div class="header-stats">
        <span class="stat-item">💬 {{ stats.totalMessages }} 条对话</span>
        <span class="stat-item">⚡ {{ stats.avgResponseTime }}ms 平均响应</span>
      </div>
      <div class="header-right">
        <span class="app-name">AI 助手</span>
        <div class="status-badge" :class="{ online: isOnline }">
          <span class="status-dot"></span>
          {{ isOnline ? '在线' : '离线' }}
        </div>
      </div>
    </div>

    <!-- 主要内容区 - 只有对话 -->
    <div class="main-content">
      <!-- 对话区（全屏） -->
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

          <!-- 思考过程（保存在历史消息中） -->
          <div v-if="msg.thinking_steps && msg.thinking_steps.length > 0" class="message-thinking-history">
            <div class="thinking-toggle" @click="msg.showThinking = !msg.showThinking">
              {{ msg.showThinking ? '▼' : '▶' }} 思考过程
            </div>
            <div v-if="msg.showThinking" class="thinking-detail-list">
              <div v-for="(step, idx) in msg.thinking_steps" :key="idx" class="thinking-line">
                {{ step.message }}
                <span v-if="step.result" class="thinking-result">{{ step.result }}</span>
              </div>
            </div>
          </div>

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

          <!-- 流式进度显示 - 极简文本样式 -->
          <div v-if="isStreaming" class="message-wrapper assistant">
            <div class="message-bubble streaming-progress">
              <div class="message-header">
                <span class="message-avatar">🤖</span>
                <span class="message-role">AI 助手</span>
              </div>
              
              <!-- 极简思考过程 - 纯文本流式显示 -->
              <div v-if="streamingResponse.length > 0" class="thinking-process">
                <div v-for="(step, index) in streamingResponse" :key="index" class="process-line">
                  <span class="process-text">{{ step.message }}</span>
                  <span v-if="step.result" class="process-result">{{ step.result }}</span>
                </div>
              </div>
              
              <!-- 流式回答 -->
              <div v-if="streamingAnswer" class="streaming-answer">
                <div class="answer-divider">─────</div>
                <div class="answer-text">{{ streamingAnswer }}<span class="cursor-blink">▌</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="input-area">
          <div class="mode-selector">
            <button 
              @click="chatMode = 'normal'" 
              :class="['mode-btn', { active: chatMode === 'normal' }]"
            >
              📤 普通模式
            </button>
            <button 
              @click="chatMode = 'stream'" 
              :class="['mode-btn', { active: chatMode === 'stream' }]"
            >
              📡 流式模式
            </button>
          </div>
          <div class="input-container">
            <textarea
              v-model="message"
              class="message-input"
              placeholder="输入任何问题，系统会自动处理..."
              @keydown.enter.prevent="handleEnter"
              rows="1"
              ref="inputRef"
            ></textarea>
            <button 
              @click="chatMode === 'stream' ? sendStreamMessage() : sendMessage()" 
              class="send-btn" 
              :disabled="loading || isStreaming || !message.trim()"
            >
              <span v-if="loading || isStreaming">⏳</span>
              <span v-else>{{ chatMode === 'stream' ? '📡' : '📤' }}</span>
            </button>
          </div>
          <div class="input-footer">
            <span class="input-hint">
              {{ chatMode === 'stream' ? '📡 流式模式：实时查看处理过程' : '📤 普通模式：直接返回结果' }} · Enter 发送
            </span>
            <span class="user-id">用户: {{ userId }}</span>
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

const streamingResponse = ref([])
const isStreaming = ref(false)
const chatMode = ref('stream') // 默认使用流式模式
const streamingAnswer = ref('') // 流式答案

const sendStreamMessage = async () => {
  if (!message.value.trim() || isStreaming.value) return

  const userMsg = message.value
  message.value = ''
  isStreaming.value = true
  streamingResponse.value = []
  streamingAnswer.value = '' // 重置流式答案

  // 添加用户消息
  chatHistory.value.push({
    role: 'user',
    content: userMsg,
    time: new Date().toLocaleTimeString()
  })

  // 创建 EventSource 连接
  const url = `/api/smart/chat/stream?userId=${userId.value}&message=${encodeURIComponent(userMsg)}`
  const eventSource = new EventSource(url)

  let finalAnswer = ''
  let intentInfo = null
  let performanceInfo = null

  eventSource.onmessage = (event) => {
    try {
      console.log('收到 SSE 数据:', event.data)
      
      // 处理 SSE 数据格式：去掉可能的 "data: " 前缀
      let jsonStr = event.data
      if (jsonStr.startsWith('data: ')) {
        jsonStr = jsonStr.substring(6) // 去掉 "data: " 前缀
      }
      
      const data = JSON.parse(jsonStr)
      streamingResponse.value.push(data)

      // 根据事件类型处理 - 构建结果文本
      let stepWithResult = {
        message: data.message,
        event: data.event,
        result: null
      }
      
      switch (data.event) {
        case 'intent_start':
          console.log('🔍 开始识别意图')
          break
        case 'intent_result':
          console.log('✅ 意图识别:', data.data.intent_type)
          intentInfo = {
            type: data.data.intent_type,
            confidence: data.data.confidence
          }
          // 添加识别结果
          stepWithResult.result = `${intentLabels[data.data.intent_type]} (置信度: ${(data.data.confidence * 100).toFixed(0)}%)`
          break
        case 'capability_prepare':
          console.log('⚙️ 准备能力:', data.data)
          // 显示启用的能力
          const caps = []
          if (data.data.knowledge) caps.push('知识库')
          if (data.data.tools) caps.push('工具')
          if (data.data.memory) caps.push('记忆')
          if (caps.length > 0) {
            stepWithResult.result = `[${caps.join(', ')}]`
          }
          break
        case 'execution_start':
          console.log('🚀 开始执行')
          if (data.data.mode) {
            stepWithResult.result = `模式: ${data.data.mode}`
          }
          break
        case 'execution_step':
          console.log('▶️ 执行步骤:', data.message)
          // 如果有SQL或其他结果，显示出来
          if (data.data.sql) {
            stepWithResult.result = `\nSQL: ${data.data.sql}`
          }
          if (data.data.ddl) {
            stepWithResult.result = `\n表结构: ${data.data.ddl}`
          }
          if (data.data.result) {
            stepWithResult.result = `\n${data.data.result}`
          }
          break
        case 'final_result':
          finalAnswer = data.data.answer
          performanceInfo = data.data.performance
          console.log('✅ 最终结果:', finalAnswer)
          
          // 逐字显示效果
          streamingAnswer.value = ''
          let charIndex = 0
          const typeInterval = setInterval(() => {
            if (charIndex < finalAnswer.length) {
              streamingAnswer.value += finalAnswer[charIndex]
              charIndex++
              scrollToBottom()
            } else {
              clearInterval(typeInterval)
              
              // 显示完成后添加到历史记录
              chatHistory.value.push({
                role: 'assistant',
                content: finalAnswer,
                time: new Date().toLocaleTimeString(),
                streaming: true,
                intent: intentInfo,
                performance: performanceInfo,
                thinking_steps: [...streamingResponse.value], // 保留完整的思考过程
                showThinking: false // 默认折叠思考过程
              })
              
              // 更新最近意图
              if (intentInfo) {
                recentIntents.value.unshift(intentInfo)
                if (recentIntents.value.length > 5) {
                  recentIntents.value.pop()
                }
              }
              
              // 关闭 EventSource
              eventSource.close()
              isStreaming.value = false
              
              // 延迟清空，确保已经添加到 chatHistory
              setTimeout(() => {
                streamingResponse.value = []
                streamingAnswer.value = ''
              }, 100)
            }
          }, 30) // 30ms 一个字，速度适中
          
          scrollToBottom()
          break
        case 'error':
          console.error('❌ 错误:', data.data.error)
          chatHistory.value.push({
            role: 'assistant',
            content: '❌ 错误: ' + data.data.error,
            time: new Date().toLocaleTimeString()
          })
          scrollToBottom()
          break
      }
      
      scrollToBottom()
    } catch (error) {
      console.error('解析事件失败:', error, '原始数据:', event.data)
    }
  }

  eventSource.onerror = (error) => {
    console.log('SSE 连接关闭或出错')
    if (eventSource.readyState !== EventSource.CLOSED) {
      eventSource.close()
    }
    // 如果还在流式中但出错，显示错误
    if (isStreaming.value && !streamingAnswer.value) {
      isStreaming.value = false
      setTimeout(() => {
        streamingResponse.value = []
        streamingAnswer.value = ''
      }, 100)
    }
    scrollToBottom()
  }

  // 设置超时关闭
  setTimeout(() => {
    if (eventSource.readyState !== EventSource.CLOSED) {
      eventSource.close()
      isStreaming.value = false
      setTimeout(() => {
        streamingResponse.value = []
        streamingAnswer.value = ''
      }, 100)
    }
  }, 60000)
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
  if (chatMode.value === 'stream') {
    sendStreamMessage()
  } else {
    sendMessage()
  }
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

/* 极简页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.header-stats {
  display: flex;
  gap: 1.5rem;
  font-size: 0.8rem;
  color: #666;
}

.header-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.app-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #333;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.75rem;
  border-radius: 16px;
  background: #f8f9fa;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-badge.online {
  background: #d4edda;
  color: #155724;
}

.status-dot {
  width: 6px;
  height: 6px;
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

/* 主要内容区 - 全屏对话 */
.main-content {
  flex: 1;
  min-height: 0;
  display: flex;
}

/* 对话容器 - 全屏 */
.chat-container {
  flex: 1;
  background: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
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

.mode-selector {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.mode-btn {
  flex: 1;
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.875rem;
  font-weight: 500;
}

.mode-btn:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
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


/* 流式进度样式 - 极简 */
.streaming-progress {
  border: none !important;
  background: white !important;
  max-width: 100% !important;
}

/* 极简思考过程 - 纯文本 */
.thinking-process {
  margin-top: 1rem;
  font-size: 0.85rem;
  line-height: 1.8;
  color: #666;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
}

.process-line {
  margin: 0.5rem 0;
  padding-left: 1rem;
  border-left: 2px solid #e9ecef;
  animation: fadeInLeft 0.3s ease-out;
}

.process-text {
  color: #555;
  display: block;
}

.process-result {
  color: #667eea;
  display: block;
  margin-top: 0.25rem;
  padding-left: 1rem;
  white-space: pre-wrap;
  word-break: break-all;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}


/* 流式答案样式 - 极简 */
.streaming-answer {
  margin-top: 1.5rem;
}

.answer-divider {
  color: #ddd;
  margin-bottom: 1rem;
  font-size: 0.8rem;
}

.answer-text {
  line-height: 1.7;
  white-space: pre-wrap;
  color: #333;
  font-size: 1rem;
}

.cursor-blink {
  display: inline-block;
  color: #667eea;
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 49% { opacity: 1; }
  50%, 100% { opacity: 0; }
}

/* 历史消息中的思考过程 - 可折叠 */
.message-thinking-history {
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px dashed #e9ecef;
  font-size: 0.85rem;
}

.thinking-toggle {
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
  user-select: none;
  margin-bottom: 0.5rem;
}

.thinking-toggle:hover {
  color: #5568d3;
}

.thinking-detail-list {
  margin-top: 0.5rem;
  padding-left: 1.5rem;
  color: #666;
  line-height: 1.7;
}

.thinking-line {
  margin: 0.4rem 0;
}

.thinking-result {
  display: block;
  color: #667eea;
  padding-left: 1rem;
  margin-top: 0.2rem;
  white-space: pre-wrap;
  word-break: break-all;
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
