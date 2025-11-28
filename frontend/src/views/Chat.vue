<template>
  <div class="chat-page">
    <div class="card">
      <h2 class="card-title">💬 智能对话</h2>
      
      <div class="input-group">
        <label class="input-label">会话 ID</label>
        <input v-model="memoryId" class="input-field" placeholder="输入会话ID（用于记忆上下文）" />
      </div>

      <div class="input-group">
        <label class="input-label">消息内容</label>
        <textarea v-model="message" class="textarea-field" placeholder="输入你的问题..." @keydown.ctrl.enter="sendMessage"></textarea>
      </div>

      <div class="button-group">
        <button @click="sendMessage" class="btn btn-primary" :disabled="loading">
          {{ loading ? '发送中...' : '发送消息 (Ctrl+Enter)' }}
        </button>
        <button @click="clearHistory" class="btn btn-secondary">清空对话</button>
      </div>

      <div v-if="chatHistory.length > 0" class="chat-history">
        <h3>对话历史</h3>
        <div v-for="(item, index) in chatHistory" :key="index" class="chat-message" :class="item.role">
          <div class="message-role">{{ item.role === 'user' ? '👤 用户' : '🤖 AI' }}</div>
          <div class="message-content">{{ item.content }}</div>
          <div class="message-time">{{ item.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { chat } from '../api'

const memoryId = ref('user-' + Date.now())
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
    const res = await chat(memoryId.value, userMessage)
    chatHistory.value.push({
      role: 'assistant',
      content: res.data,
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
  memoryId.value = 'user-' + Date.now()
}
</script>

<style scoped>
.button-group {
  display: flex;
  gap: 1rem;
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
  margin-left: 20%;
}

.chat-message.assistant {
  background: #f5f5f5;
  margin-right: 20%;
}

.message-role {
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #667eea;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.message-time {
  font-size: 0.875rem;
  color: #999;
  margin-top: 0.5rem;
}
</style>

