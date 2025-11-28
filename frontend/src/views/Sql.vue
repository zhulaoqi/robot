<template>
  <div class="sql-page">
    <div class="card">
      <h2 class="card-title">🗄️ Text-to-SQL 助手</h2>
      
      <div class="input-group">
        <label class="input-label">会话 ID</label>
        <input v-model="memoryId" class="input-field" placeholder="输入会话ID" />
      </div>

      <div class="input-group">
        <label class="input-label">自然语言查询</label>
        <textarea 
          v-model="query" 
          class="textarea-field" 
          placeholder="例如：查询所有在读学生的姓名和学号"
        ></textarea>
      </div>

      <div class="button-group">
        <button @click="generateSql(false)" class="btn btn-primary" :disabled="loading">
          {{ loading ? '生成中...' : '生成 SQL' }}
        </button>
        <button @click="generateSql(true)" class="btn btn-secondary" :disabled="loading">
          热更新 Prompt
        </button>
      </div>

      <div v-if="result" class="result-box">{{ result }}</div>
    </div>

    <div class="card">
      <h2 class="card-title">📝 Prompt 管理</h2>
      
      <div class="button-group">
        <button @click="loadPrompts" class="btn btn-secondary">加载 Prompts</button>
      </div>

      <div v-if="prompts.length > 0" class="prompts-list">
        <div v-for="prompt in prompts" :key="prompt.key" class="prompt-item">
          <h4>{{ prompt.name }}</h4>
          <p>版本: {{ prompt.version }}</p>
          <button @click="viewPrompt(prompt.key)" class="btn btn-secondary">查看详情</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { generateSql as generateSqlApi, generateSqlHotUpdate, listPrompts, getPrompt } from '../api'

const memoryId = ref('sql-' + Date.now())
const query = ref('')
const loading = ref(false)
const result = ref('')
const prompts = ref([])

const generateSql = async (useHotUpdate) => {
  if (!query.value.trim()) return
  
  loading.value = true
  result.value = ''
  
  try {
    const api = useHotUpdate ? generateSqlHotUpdate : generateSqlApi
    const res = await api(memoryId.value, query.value)
    result.value = res.data
  } catch (error) {
    result.value = '错误: ' + error.message
  } finally {
    loading.value = false
  }
}

const loadPrompts = async () => {
  try {
    const res = await listPrompts()
    prompts.value = Object.entries(res.data).map(([key, value]) => ({
      key,
      ...value
    }))
  } catch (error) {
    alert('加载失败: ' + error.message)
  }
}

const viewPrompt = async (key) => {
  try {
    const res = await getPrompt(key)
    alert(`Prompt: ${key}\n\n${res.data}`)
  } catch (error) {
    alert('加载失败: ' + error.message)
  }
}
</script>

<style scoped>
.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.prompts-list {
  margin-top: 1.5rem;
  display: grid;
  gap: 1rem;
}

.prompt-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
}

.prompt-item h4 {
  margin-bottom: 0.5rem;
  color: #333;
}

.prompt-item p {
  color: #666;
  margin-bottom: 1rem;
}
</style>

