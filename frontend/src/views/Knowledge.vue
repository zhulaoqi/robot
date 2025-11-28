<template>
  <div class="knowledge-page">
    <div class="card">
      <h2 class="card-title">📚 知识库管理</h2>
      
      <div class="stats-row">
        <div class="stat-box">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总条目</div>
        </div>
        <div class="stat-box">
          <div class="stat-value">{{ stats.dimension }}</div>
          <div class="stat-label">向量维度</div>
        </div>
      </div>

      <div class="button-group">
        <button @click="loadStats" class="btn btn-secondary">刷新统计</button>
        <button @click="loadDDL" class="btn btn-primary">加载学生DDL</button>
        <button @click="clearKnowledge" class="btn btn-danger">清空知识库</button>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">➕ 添加知识</h2>
      
      <div class="input-group">
        <label class="input-label">知识内容</label>
        <textarea 
          v-model="newKnowledge" 
          class="textarea-field" 
          placeholder="输入要添加到知识库的内容..."
        ></textarea>
      </div>

      <button @click="addKnowledge" class="btn btn-primary" :disabled="adding">
        {{ adding ? '添加中...' : '添加到知识库' }}
      </button>

      <div v-if="addResult" :class="addResult.success ? 'success' : 'error'">
        {{ addResult.message }}
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">🔍 知识检索</h2>
      
      <div class="input-group">
        <label class="input-label">检索查询</label>
        <input 
          v-model="searchQuery" 
          class="input-field" 
          placeholder="输入要检索的内容..."
          @keydown.enter="searchKnowledge"
        />
      </div>

      <button @click="searchKnowledge" class="btn btn-primary" :disabled="searching">
        {{ searching ? '检索中...' : '开始检索' }}
      </button>

      <div v-if="searchResults.length > 0" class="search-results">
        <h3>检索结果 ({{ searchResults.length }} 条)</h3>
        <div v-for="(item, index) in searchResults" :key="index" class="result-item">
          <div class="result-score">相似度: {{ (item.score * 100).toFixed(2) }}%</div>
          <div class="result-content">{{ item.content }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">🎯 RAG 查询对比</h2>
      
      <div class="input-group">
        <label class="input-label">查询内容</label>
        <input 
          v-model="ragQuery" 
          class="input-field" 
          placeholder="输入查询内容..."
        />
      </div>

      <div class="button-group">
        <button @click="expandQuery" class="btn btn-secondary">查询扩展</button>
        <button @click="ragWithTransform" class="btn btn-secondary">查询改写</button>
        <button @click="compareRag" class="btn btn-primary">全方法对比</button>
      </div>

      <div v-if="ragResult" class="result-box">{{ ragResult }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  getKnowledgeStats, 
  addKnowledge as addKnowledgeApi, 
  searchKnowledge as searchKnowledgeApi,
  clearKnowledge as clearKnowledgeApi,
  loadStudentDDL,
  expandQuery as expandQueryApi,
  ragWithTransform as ragWithTransformApi,
  compareRag as compareRagApi
} from '../api'

const stats = ref({ total: 0, dimension: 1536 })
const newKnowledge = ref('')
const adding = ref(false)
const addResult = ref(null)

const searchQuery = ref('')
const searching = ref(false)
const searchResults = ref([])

const ragQuery = ref('')
const ragResult = ref('')

const loadStats = async () => {
  try {
    const res = await getKnowledgeStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const addKnowledge = async () => {
  if (!newKnowledge.value.trim()) return
  
  adding.value = true
  addResult.value = null
  
  try {
    await addKnowledgeApi(newKnowledge.value)
    addResult.value = { success: true, message: '添加成功！' }
    newKnowledge.value = ''
    loadStats()
  } catch (error) {
    addResult.value = { success: false, message: '添加失败: ' + error.message }
  } finally {
    adding.value = false
  }
}

const searchKnowledge = async () => {
  if (!searchQuery.value.trim()) return
  
  searching.value = true
  searchResults.value = []
  
  try {
    const res = await searchKnowledgeApi(searchQuery.value)
    searchResults.value = res.data
  } catch (error) {
    alert('检索失败: ' + error.message)
  } finally {
    searching.value = false
  }
}

const clearKnowledge = async () => {
  if (!confirm('确定要清空知识库吗？此操作不可恢复！')) return
  
  try {
    await clearKnowledgeApi()
    alert('清空成功！')
    loadStats()
  } catch (error) {
    alert('清空失败: ' + error.message)
  }
}

const loadDDL = async () => {
  try {
    await loadStudentDDL()
    alert('加载成功！')
    loadStats()
  } catch (error) {
    alert('加载失败: ' + error.message)
  }
}

const expandQuery = async () => {
  if (!ragQuery.value.trim()) return
  try {
    const res = await expandQueryApi(ragQuery.value)
    ragResult.value = res.data
  } catch (error) {
    ragResult.value = '错误: ' + error.message
  }
}

const ragWithTransform = async () => {
  if (!ragQuery.value.trim()) return
  try {
    const res = await ragWithTransformApi(ragQuery.value)
    ragResult.value = res.data
  } catch (error) {
    ragResult.value = '错误: ' + error.message
  }
}

const compareRag = async () => {
  if (!ragQuery.value.trim()) return
  try {
    const res = await compareRagApi(ragQuery.value)
    ragResult.value = JSON.stringify(res.data, null, 2)
  } catch (error) {
    ragResult.value = '错误: ' + error.message
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stats-row {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-box {
  flex: 1;
  text-align: center;
  padding: 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 1rem;
  opacity: 0.9;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-results {
  margin-top: 2rem;
}

.search-results h3 {
  margin-bottom: 1rem;
  color: #333;
}

.result-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 4px solid #667eea;
}

.result-score {
  font-weight: bold;
  color: #667eea;
  margin-bottom: 0.5rem;
}

.result-content {
  line-height: 1.6;
}
</style>

