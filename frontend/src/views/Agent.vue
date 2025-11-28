<template>
  <div class="agent-page">
    <div class="card">
      <h2 class="card-title">🤖 AI Agent 模式</h2>
      
      <div class="mode-selector">
        <button 
          v-for="mode in modes" 
          :key="mode.value"
          @click="selectedMode = mode.value"
          class="mode-btn"
          :class="{ active: selectedMode === mode.value }"
        >
          {{ mode.icon }} {{ mode.label }}
        </button>
      </div>

      <div class="input-group">
        <label class="input-label">任务描述</label>
        <textarea 
          v-model="task" 
          class="textarea-field" 
          :placeholder="getPlaceholder()"
        ></textarea>
      </div>

      <button @click="executeTask" class="btn btn-primary" :disabled="executing">
        {{ executing ? '执行中...' : '执行任务' }}
      </button>

      <div v-if="result" class="result-box">{{ result }}</div>
    </div>

    <div class="card">
      <h2 class="card-title">📋 任务编排</h2>
      
      <div class="input-group">
        <label class="input-label">用户请求</label>
        <textarea 
          v-model="orchestrationRequest" 
          class="textarea-field" 
          placeholder="例如：帮我分析一下学生成绩，并生成报告"
        ></textarea>
      </div>

      <button @click="doOrchestrate" class="btn btn-primary" :disabled="orchestrating">
        {{ orchestrating ? '编排中...' : '开始编排' }}
      </button>

      <div v-if="orchestrationResult" class="orchestration-result">
        <div v-for="(phase, index) in orchestrationResult.phases" :key="index" class="phase-item">
          <h4>{{ phase.name }}</h4>
          <p>耗时: {{ phase.duration_ms }}ms</p>
          <pre>{{ JSON.stringify(phase, null, 2) }}</pre>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">🎮 交互式任务</h2>
      
      <div class="input-group">
        <label class="input-label">任务请求</label>
        <input 
          v-model="interactiveRequest" 
          class="input-field" 
          placeholder="输入任务请求..."
        />
      </div>

      <div class="button-group">
        <button @click="startTask" class="btn btn-primary">启动任务</button>
        <button @click="loadTasks" class="btn btn-secondary">刷新列表</button>
      </div>

      <div v-if="tasks.length > 0" class="tasks-list">
        <h3>运行中的任务</h3>
        <div v-for="task in tasks" :key="task.task_id" class="task-item">
          <div class="task-header">
            <span class="task-id">{{ task.task_id }}</span>
            <span class="task-status" :class="task.status">{{ task.status }}</span>
          </div>
          <div class="task-request">{{ task.user_request }}</div>
          <div class="task-controls">
            <button @click="pauseTask(task.task_id)" class="btn btn-secondary" v-if="task.status === 'RUNNING'">
              暂停
            </button>
            <button @click="resumeTask(task.task_id)" class="btn btn-secondary" v-if="task.status === 'PAUSED'">
              继续
            </button>
            <button @click="stopTask(task.task_id)" class="btn btn-danger">
              停止
            </button>
            <button @click="viewTaskStatus(task.task_id)" class="btn btn-secondary">
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 class="card-title">📡 实时流式编排</h2>
      
      <div class="input-group">
        <label class="input-label">请求内容</label>
        <input 
          v-model="streamRequest" 
          class="input-field" 
          placeholder="输入请求内容..."
        />
      </div>

      <button @click="startStreaming" class="btn btn-primary" :disabled="streaming">
        {{ streaming ? '执行中...' : '开始流式执行' }}
      </button>

      <div v-if="streamEvents.length > 0" class="stream-events">
        <h3>执行过程</h3>
        <div v-for="(event, index) in streamEvents" :key="index" class="event-item" :class="event.event">
          <span class="event-type">{{ event.event }}</span>
          <span class="event-data">{{ event.data }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  planExecuteDemo, 
  reflexionDemo, 
  cotDemo, 
  routerDemo,
  orchestrate as orchestrateApi,
  startTask as startTaskApi,
  pauseTask as pauseTaskApi,
  resumeTask as resumeTaskApi,
  stopTask as stopTaskApi,
  getTaskStatus,
  listTasks
} from '../api'

const modes = [
  { value: 'plan-execute', label: 'Plan-and-Execute', icon: '📋' },
  { value: 'reflexion', label: 'Reflexion', icon: '🔄' },
  { value: 'cot', label: 'Chain of Thought', icon: '🧠' },
  { value: 'router', label: '智能路由', icon: '🧭' }
]

const selectedMode = ref('plan-execute')
const task = ref('')
const executing = ref(false)
const result = ref('')

const orchestrationRequest = ref('')
const orchestrating = ref(false)
const orchestrationResult = ref(null)

const interactiveRequest = ref('')
const tasks = ref([])

const streamRequest = ref('')
const streaming = ref(false)
const streamEvents = ref([])

const getPlaceholder = () => {
  const placeholders = {
    'plan-execute': '例如：帮我规划一个三天的北京旅游行程',
    'reflexion': '例如：写一个快速排序函数',
    'cot': '例如：计算 (23 + 47) * 3 - 15',
    'router': '例如：深圳今天天气怎么样？'
  }
  return placeholders[selectedMode.value]
}

const executeTask = async () => {
  if (!task.value.trim()) return
  
  executing.value = true
  result.value = ''
  
  try {
    let res
    switch (selectedMode.value) {
      case 'plan-execute':
        res = await planExecuteDemo(task.value)
        break
      case 'reflexion':
        res = await reflexionDemo(task.value)
        break
      case 'cot':
        res = await cotDemo(task.value)
        break
      case 'router':
        res = await routerDemo(task.value)
        break
    }
    result.value = JSON.stringify(res.data, null, 2)
  } catch (error) {
    result.value = '错误: ' + (error.response?.data || error.message)
  } finally {
    executing.value = false
  }
}

const orchestrate = async () => {
  if (!orchestrationRequest.value.trim()) return
  
  orchestrating.value = true
  orchestrationResult.value = null
  
  try {
    const res = await orchestrateApi(orchestrationRequest.value)
    orchestrationResult.value = res.data
  } catch (error) {
    alert('编排失败: ' + error.message)
  } finally {
    orchestrating.value = false
  }
}

const startTask = async () => {
  if (!interactiveRequest.value.trim()) return
  
  try {
    const res = await startTaskApi(interactiveRequest.value)
    alert('任务已启动: ' + res.data)
    loadTasks()
  } catch (error) {
    alert('启动失败: ' + error.message)
  }
}

const loadTasks = async () => {
  try {
    const res = await listTasks()
    tasks.value = res.data
  } catch (error) {
    console.error('加载任务失败:', error)
  }
}

const pauseTask = async (taskId) => {
  try {
    await pauseTaskApi(taskId)
    loadTasks()
  } catch (error) {
    alert('暂停失败: ' + error.message)
  }
}

const resumeTask = async (taskId) => {
  try {
    await resumeTaskApi(taskId)
    loadTasks()
  } catch (error) {
    alert('继续失败: ' + error.message)
  }
}

const stopTask = async (taskId) => {
  try {
    await stopTaskApi(taskId)
    loadTasks()
  } catch (error) {
    alert('停止失败: ' + error.message)
  }
}

const viewTaskStatus = async (taskId) => {
  try {
    const res = await getTaskStatus(taskId)
    alert(JSON.stringify(res.data, null, 2))
  } catch (error) {
    alert('查询失败: ' + error.message)
  }
}

const startStreaming = () => {
  if (!streamRequest.value.trim()) return
  
  streaming.value = true
  streamEvents.value = []
  
  const eventSource = new EventSource(`/ai/agent-demo/orchestration/streaming?request=${encodeURIComponent(streamRequest.value)}`)
  
  eventSource.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      streamEvents.value.push({
        event: data.event || 'message',
        data: JSON.stringify(data.data, null, 2)
      })
    } catch (error) {
      console.error('解析事件失败:', error)
    }
  }
  
  eventSource.onerror = () => {
    eventSource.close()
    streaming.value = false
  }
}
</script>

<style scoped>
.mode-selector {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.mode-btn {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.mode-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.orchestration-result {
  margin-top: 2rem;
}

.phase-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 4px solid #667eea;
}

.phase-item h4 {
  margin-bottom: 0.5rem;
  color: #333;
}

.phase-item pre {
  background: white;
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.875rem;
}

.tasks-list {
  margin-top: 2rem;
}

.tasks-list h3 {
  margin-bottom: 1rem;
  color: #333;
}

.task-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.task-id {
  font-weight: bold;
  color: #667eea;
}

.task-status {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.task-status.RUNNING {
  background: #48bb78;
  color: white;
}

.task-status.PAUSED {
  background: #ed8936;
  color: white;
}

.task-status.COMPLETED {
  background: #667eea;
  color: white;
}

.task-request {
  margin-bottom: 1rem;
  color: #666;
}

.task-controls {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.task-controls .btn {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.stream-events {
  margin-top: 2rem;
}

.stream-events h3 {
  margin-bottom: 1rem;
  color: #333;
}

.event-item {
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  display: flex;
  gap: 1rem;
}

.event-type {
  font-weight: bold;
  color: #667eea;
  min-width: 150px;
}

.event-data {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.button-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}
</style>

