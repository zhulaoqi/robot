import axios from 'axios'

const api = axios.create({
  baseURL: '/ai',
  timeout: 60000
})

// ========== 统一对话接口（推荐用于生产） ==========
const unifiedApi = axios.create({
  baseURL: '/api/v1',
  timeout: 60000
})

export const unifiedChat = (message, userId = 'default') => 
  unifiedApi.get('/chat', { params: { message, userId } })

export const unifiedChatOrchestration = (request, userId = 'default') => 
  unifiedApi.get('/chat/orchestration', { params: { request, userId } })

export const unifiedChatStream = (request, userId = 'default') => 
  `/api/v1/chat/stream?request=${encodeURIComponent(request)}&userId=${userId}`

export const healthCheck = () => unifiedApi.get('/health')

// ========== 基础接口（用于学习和调试） ==========
export const testChat = () => api.get('/chat/test')
export const chat = (memoryId, message) => api.get('/chat', { params: { memoryId, userMessage: message } })
export const streamChat = (memoryId, message) => api.get(`/chat/${memoryId}/stream/memory`, { params: { userMessage: message } })

// SQL 助手
export const generateSql = (memoryId, message) => api.get(`/chat/${memoryId}/sql/generate`, { params: { userMessage: message } })
export const generateSqlHotUpdate = (memoryId, message) => api.get(`/chat/${memoryId}/sql/generate/hotUpdate`, { params: { userMessage: message } })

// 知识库
export const addKnowledge = (content) => api.post('/chat/knowledge/add', content, { headers: { 'Content-Type': 'text/plain;charset=UTF-8' } })
export const searchKnowledge = (query) => api.get('/chat/knowledge/search', { params: { query } })
export const clearKnowledge = () => api.delete('/chat/knowledge/clear')
export const getKnowledgeStats = () => api.get('/chat/knowledge/stats')
export const loadStudentDDL = () => api.post('/chat/knowledge/load-student-ddl')

// RAG 功能
export const expandQuery = (query) => api.get('/chat/query/expand', { params: { query } })
export const ragWithTransform = (query) => api.get('/chat/rag/with-query-transform', { params: { query } })
export const compareRag = (query) => api.get('/chat/rag/compare-all', { params: { query } })

// Agent 功能
export const planTrip = (request) => api.get('/chat/agent/plan-trip', { params: { request } })
export const analyzeData = (request) => api.get('/chat/agent/analyze-data', { params: { request } })

// Agent Demo
export const planExecuteDemo = (task) => api.get('/agent-demo/mode/plan-execute', { params: { task } })
export const reflexionDemo = (task) => api.get('/agent-demo/mode/reflexion', { params: { task } })
export const cotDemo = (problem) => api.get('/agent-demo/mode/chain-of-thought', { params: { problem } })
export const routerDemo = (input) => api.get('/agent-demo/router', { params: { input } })
export const orchestrate = (request) => api.get('/agent-demo/orchestration', { params: { request } })

// 交互式任务
export const startTask = (request) => api.post('/agent-demo/interactive/start', null, { params: { request } })
export const pauseTask = (taskId) => api.post(`/agent-demo/interactive/pause/${taskId}`)
export const resumeTask = (taskId) => api.post(`/agent-demo/interactive/resume/${taskId}`)
export const stopTask = (taskId) => api.post(`/agent-demo/interactive/stop/${taskId}`)
export const getTaskStatus = (taskId) => api.get(`/agent-demo/interactive/status/${taskId}`)
export const listTasks = () => api.get('/agent-demo/interactive/list')

// MCP 功能
export const mcpChat = (message) => api.get('/chat/mcp/chat', { params: { message } })
export const listMcpServers = () => api.get('/chat/mcp/servers')
export const listMcpTools = () => api.get('/chat/mcp/tools')
export const executeMcpTool = (data) => api.post('/chat/mcp/execute', data)

// Prompt 管理
export const listPrompts = () => api.get('/chat/prompts/list')
export const getPrompt = (key) => api.get(`/chat/prompts/${key}`)
export const updatePrompt = (key, content, version) => api.put(`/chat/prompts/${key}`, null, { params: { content, version } })

export default api

