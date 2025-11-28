<template>
  <div class="mcp-page">
    <div class="card">
      <h2 class="card-title">🔌 MCP 智能助手</h2>
      
      <div class="input-group">
        <label class="input-label">消息内容</label>
        <textarea 
          v-model="message" 
          class="textarea-field" 
          placeholder="例如：帮我计算 sqrt(16) + pow(2, 3)，然后查询深圳天气"
        ></textarea>
      </div>

      <button @click="sendMessage" class="btn btn-primary" :disabled="loading">
        {{ loading ? '处理中...' : '发送消息' }}
      </button>

      <div v-if="result" class="result-box">{{ result }}</div>
    </div>

    <div class="card">
      <h2 class="card-title">🌐 MCP 服务器</h2>
      
      <button @click="loadServers" class="btn btn-secondary">刷新列表</button>

      <div v-if="servers.length > 0" class="servers-list">
        <div v-for="server in servers" :key="server.name" class="server-item">
          <h4>{{ server.name }}</h4>
          <p>{{ server.description }}</p>
          <div class="server-info">
            <span>版本: {{ server.version }}</span>
            <span>工具数: {{ server.tools_count }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">🔧 可用工具</h2>
      
      <button @click="loadTools" class="btn btn-secondary">刷新列表</button>

      <div v-if="tools.java_tools || tools.mcp_tools" class="tools-grid">
        <div class="tools-section">
          <h3>Java 工具 ({{ tools.java_tools?.length || 0 }})</h3>
          <div v-for="tool in tools.java_tools" :key="tool.name" class="tool-item">
            <div class="tool-name">{{ tool.name }}</div>
            <div class="tool-desc">{{ tool.description }}</div>
          </div>
        </div>

        <div class="tools-section">
          <h3>MCP 工具 ({{ tools.mcp_tools?.length || 0 }})</h3>
          <div v-for="tool in tools.mcp_tools" :key="tool.name" class="tool-item">
            <div class="tool-name">{{ tool.name }}</div>
            <div class="tool-desc">{{ tool.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">⚡ 手动执行工具</h2>
      
      <div class="input-group">
        <label class="input-label">工具名称</label>
        <input v-model="toolName" class="input-field" placeholder="例如：calculator" />
      </div>

      <div class="input-group">
        <label class="input-label">参数 (JSON)</label>
        <textarea 
          v-model="toolArgs" 
          class="textarea-field" 
          placeholder='例如：{"operation": "sqrt", "x": 16}'
        ></textarea>
      </div>

      <button @click="executeTool" class="btn btn-primary" :disabled="executing">
        {{ executing ? '执行中...' : '执行工具' }}
      </button>

      <div v-if="toolResult" class="result-box">{{ toolResult }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { mcpChat, listMcpServers, listMcpTools, executeMcpTool } from '../api'

const message = ref('')
const loading = ref(false)
const result = ref('')

const servers = ref([])
const tools = ref({})

const toolName = ref('')
const toolArgs = ref('')
const executing = ref(false)
const toolResult = ref('')

const sendMessage = async () => {
  if (!message.value.trim()) return
  
  loading.value = true
  result.value = ''
  
  try {
    const res = await mcpChat(message.value)
    result.value = res.data
  } catch (error) {
    result.value = '错误: ' + error.message
  } finally {
    loading.value = false
  }
}

const loadServers = async () => {
  try {
    const res = await listMcpServers()
    servers.value = res.data
  } catch (error) {
    alert('加载失败: ' + error.message)
  }
}

const loadTools = async () => {
  try {
    const res = await listMcpTools()
    tools.value = res.data
  } catch (error) {
    alert('加载失败: ' + error.message)
  }
}

const executeTool = async () => {
  if (!toolName.value.trim()) return
  
  executing.value = true
  toolResult.value = ''
  
  try {
    const args = toolArgs.value ? JSON.parse(toolArgs.value) : {}
    const res = await executeMcpTool({
      tool_name: toolName.value,
      arguments: args
    })
    toolResult.value = JSON.stringify(res.data, null, 2)
  } catch (error) {
    toolResult.value = '错误: ' + error.message
  } finally {
    executing.value = false
  }
}
</script>

<style scoped>
.servers-list {
  margin-top: 1.5rem;
  display: grid;
  gap: 1rem;
}

.server-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px solid #e0e0e0;
}

.server-item h4 {
  margin-bottom: 0.5rem;
  color: #333;
}

.server-item p {
  color: #666;
  margin-bottom: 1rem;
}

.server-info {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #999;
}

.tools-grid {
  margin-top: 1.5rem;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
}

.tools-section h3 {
  margin-bottom: 1rem;
  color: #333;
}

.tool-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 0.75rem;
  border-left: 4px solid #667eea;
}

.tool-name {
  font-weight: bold;
  color: #667eea;
  margin-bottom: 0.5rem;
}

.tool-desc {
  font-size: 0.875rem;
  color: #666;
}

@media (max-width: 768px) {
  .tools-grid {
    grid-template-columns: 1fr;
  }
}
</style>

