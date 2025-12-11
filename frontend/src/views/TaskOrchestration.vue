<template>
  <div class="task-orchestration">
    <div class="header">
      <h1>🤖 任务编排对话机器人</h1>
      <p class="subtitle">基于DAG的智能任务分解与并行执行</p>
    </div>

    <div class="main-content">
      <!-- 左侧：对话区 -->
      <div class="chat-panel">
        <div class="input-area">
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="4"
            placeholder="输入你的请求，AI会自动分解为任务并执行...&#10;&#10;示例：&#10;- 查询学生成绩数据，分析分布情况，生成报告&#10;- 查询深圳天气，推荐周末活动&#10;- 分析数据库中的学生信息并给出建议"
            :disabled="isExecuting"
            @keydown.ctrl.enter="submitTask"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              :loading="isExecuting"
              @click="submitTask"
              :disabled="!userInput.trim()"
            >
              <i class="el-icon-s-promotion"></i>
              {{ isExecuting ? '执行中...' : '提交任务' }}
            </el-button>
            <el-button 
              v-if="currentDagId && isExecuting"
              type="danger"
              @click="cancelTask"
            >
              <i class="el-icon-close"></i>
              取消
            </el-button>
          </div>
        </div>

        <!-- 历史记录 -->
        <div class="history-section">
          <h3>历史任务</h3>
          <div class="history-list">
            <div 
              v-for="dag in dagHistory" 
              :key="dag.dag_id"
              class="history-item"
              :class="{ active: currentDagId === dag.dag_id }"
              @click="viewDAG(dag.dag_id)"
            >
              <div class="history-header">
                <el-tag :type="getStateType(dag.state)" size="small">
                  {{ getStateText(dag.state) }}
                </el-tag>
                <span class="history-time">{{ formatTime(dag.create_time) }}</span>
              </div>
              <div class="history-content">{{ dag.user_request }}</div>
              <el-progress 
                :percentage="dag.progress" 
                :status="dag.progress === 100 ? 'success' : null"
                :show-text="false"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：DAG可视化 + 状态 -->
      <div class="dag-panel">
        <div v-if="!currentDagId" class="empty-state">
          <i class="el-icon-document-add" style="font-size: 64px; color: #ccc;"></i>
          <p>提交任务后，这里会显示任务执行的DAG图</p>
        </div>

        <div v-else class="dag-content">
          <!-- DAG信息 -->
          <div class="dag-info">
            <h3>
              {{ currentDAG?.user_request }}
              <el-tag :type="getStateType(currentDAG?.state)" size="small" style="margin-left: 10px;">
                {{ getStateText(currentDAG?.state) }}
              </el-tag>
            </h3>
            
            <!-- ✅ 最终答案（DAG完成后显示） -->
            <div v-if="currentDAG?.final_answer" class="final-answer">
              <h4>📋 最终结果</h4>
              <div class="answer-content">{{ currentDAG.final_answer }}</div>
            </div>
            
            <!-- 统计信息 -->
            <div class="statistics" v-if="currentDAG?.statistics">
              <el-descriptions :column="3" size="small" border>
                <el-descriptions-item label="总任务">
                  {{ currentDAG.statistics.total }}
                </el-descriptions-item>
                <el-descriptions-item label="已完成">
                  <span style="color: #67c23a;">{{ currentDAG.statistics.success }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="失败">
                  <span style="color: #f56c6c;">{{ currentDAG.statistics.failed }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="执行中">
                  <span style="color: #409eff;">{{ currentDAG.statistics.running }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="等待中">
                  <span style="color: #909399;">{{ currentDAG.statistics.pending }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="进度">
                  <el-progress 
                    :percentage="currentDAG.statistics.progress" 
                    :status="currentDAG.statistics.progress === 100 ? 'success' : null"
                  />
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <!-- DAG图 -->
          <div class="dag-graph">
            <h4>任务依赖图 (DAG)</h4>
            <div ref="dagGraphContainer" class="graph-container"></div>
          </div>

          <!-- 任务列表 -->
          <div class="task-list">
            <h4>任务详情</h4>
            <el-timeline>
              <el-timeline-item
                v-for="task in currentDAG?.tasks"
                :key="task.task_id"
                :type="getTaskTimelineType(task.state)"
                :icon="getTaskIcon(task.state)"
              >
                <div class="task-item">
                  <div class="task-header">
                    <span class="task-id">{{ task.task_id }}</span>
                    <el-tag :type="getStateType(task.state)" size="mini">
                      {{ getStateText(task.state) }}
                    </el-tag>
                    <el-tag type="info" size="mini" style="margin-left: 5px;">
                      {{ task.type }}
                    </el-tag>
                    <span v-if="task.duration_ms" class="task-duration">
                      耗时: {{ task.duration_ms }}ms
                    </span>
                  </div>
                  <div class="task-description">{{ task.description }}</div>
                  
                  <!-- 依赖关系 -->
                  <div v-if="task.dependencies.length > 0" class="task-dependencies">
                    <i class="el-icon-connection"></i>
                    依赖: {{ task.dependencies.join(', ') }}
                  </div>

                  <!-- 执行结果 -->
                  <div v-if="task.result" class="task-result">
                    <el-collapse>
                      <el-collapse-item title="查看结果" name="1">
                        <pre>{{ task.result }}</pre>
                      </el-collapse-item>
                    </el-collapse>
                  </div>

                  <!-- 错误信息 -->
                  <div v-if="task.error" class="task-error">
                    <el-alert type="error" :closable="false">
                      {{ task.error }}
                    </el-alert>
                  </div>

                  <!-- 重试信息 -->
                  <div v-if="task.retry_count > 0" class="task-retry">
                    <el-tag type="warning" size="mini">
                      已重试 {{ task.retry_count }} 次
                    </el-tag>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';

export default {
  name: 'TaskOrchestration',
  data() {
    return {
      userInput: '',
      currentDagId: null,
      currentDAG: null,
      dagHistory: [],
      isExecuting: false,
      eventSource: null,
      chart: null,
    };
  },
  mounted() {
    this.loadHistory();
  },
  beforeUnmount() {
    this.closeEventSource();
    if (this.chart) {
      this.chart.dispose();
    }
  },
  methods: {
    // 提交任务
    async submitTask() {
      if (!this.userInput.trim()) {
        return;
      }

      this.isExecuting = true;

      try {
        const response = await axios.post('/ai/orchestration/submit', null, {
          params: { request: this.userInput }
        });

        if (response.data.success) {
          this.currentDagId = response.data.dag_id;
          this.$message.success('任务已提交');
          
          // 开始实时监听
          this.streamDAGStatus(this.currentDagId);
          
          // 清空输入
          this.userInput = '';
        } else {
          this.$message.error(response.data.error || '任务提交失败');
          this.isExecuting = false;
        }
      } catch (error) {
        console.error('提交任务失败:', error);
        this.$message.error('任务提交失败: ' + error.message);
        this.isExecuting = false;
      }
    },

    // 流式接收DAG状态
    streamDAGStatus(dagId) {
      this.closeEventSource();

      const url = `http://localhost:8080/ai/orchestration/status/${dagId}/stream`;
      this.eventSource = new EventSource(url);

      this.eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          // ✅ 强制触发Vue响应式更新（创建新对象）
          this.currentDAG = Object.assign({}, data, {
            tasks: [...(data.tasks || [])],
            statistics: {...(data.statistics || {})}
          });
          
          // ✅ 每次都重新渲染（SSE每0.5秒推送一次，频率不高）
          this.$nextTick(() => {
            this.renderDAGGraph();
          });
          
          // ✅ 强制Vue更新（确保模板重新渲染）
          this.$forceUpdate();
          
        } catch (error) {
          console.error('解析SSE数据失败:', error);
        }
      };

      this.eventSource.onerror = () => {
        console.log('SSE连接关闭');
        this.closeEventSource();
        this.isExecuting = false;
        
        // ✅ 最后再渲染一次并强制更新
        this.$nextTick(() => {
          this.renderDAGGraph();
          this.$forceUpdate();
        });
        
        this.loadHistory();
      };
    },

    // 关闭SSE连接
    closeEventSource() {
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
    },

    // 取消任务
    async cancelTask() {
      if (!this.currentDagId) {
        return;
      }

      try {
        await axios.post(`/ai/orchestration/cancel/${this.currentDagId}`);
        this.$message.info('任务已取消');
        this.closeEventSource();
        this.isExecuting = false;
        this.loadHistory();
      } catch (error) {
        console.error('取消任务失败:', error);
        this.$message.error('取消失败');
      }
    },

    // 查看历史DAG
    async viewDAG(dagId) {
      this.currentDagId = dagId;
      this.closeEventSource();
      this.isExecuting = false;

      try {
        const response = await axios.get(`/ai/orchestration/status/${dagId}`);
        this.currentDAG = response.data;
        
        this.$nextTick(() => {
          this.renderDAGGraph();
        });
      } catch (error) {
        console.error('查询DAG失败:', error);
      }
    },

    // 加载历史
    async loadHistory() {
      try {
        const response = await axios.get('/ai/orchestration/list');
        if (response.data.success) {
          this.dagHistory = response.data.dags;
        }
      } catch (error) {
        console.error('加载历史失败:', error);
      }
    },

    // 渲染DAG图
    renderDAGGraph() {
      if (!this.$refs.dagGraphContainer || !this.currentDAG) {
        return;
      }

      if (!this.chart) {
        this.chart = echarts.init(this.$refs.dagGraphContainer);
      }

      const tasks = this.currentDAG.tasks || [];
      
      // 构建节点
      const nodes = tasks.map(task => ({
        id: task.task_id,
        name: task.description.substring(0, 20) + (task.description.length > 20 ? '...' : ''),
        category: this.getStateCategory(task.state),
        symbolSize: 60,
        label: {
          show: true
        },
        itemStyle: {
          color: this.getStateColor(task.state)
        }
      }));

      // 构建边
      const links = [];
      tasks.forEach(task => {
        task.dependencies.forEach(dep => {
          links.push({
            source: dep,
            target: task.task_id
          });
        });
      });

      const option = {
        tooltip: {
          formatter: (params) => {
            if (params.dataType === 'node') {
              const task = tasks.find(t => t.task_id === params.data.id);
              return `
                <strong>${task.task_id}</strong><br/>
                类型: ${task.type}<br/>
                状态: ${this.getStateText(task.state)}<br/>
                ${task.duration_ms ? `耗时: ${task.duration_ms}ms` : ''}
              `;
            }
            return '';
          }
        },
        series: [{
          type: 'graph',
          layout: 'force',
          data: nodes,
          links: links,
          roam: true,
          label: {
            show: true,
            position: 'bottom',
            fontSize: 10
          },
          force: {
            repulsion: 200,
            edgeLength: 100
          },
          emphasis: {
            focus: 'adjacency',
            lineStyle: {
              width: 3
            }
          }
        }]
      };

      // ✅ 使用 notMerge: false 和 lazyUpdate: true 优化渲染性能
      this.chart.setOption(option, {
        notMerge: false,  // 增量更新而不是完全重绘
        lazyUpdate: true  // 延迟更新
      });
    },

    // 工具方法
    getStateType(state) {
      const types = {
        'PENDING': 'info',
        'RUNNING': 'primary',
        'SUCCESS': 'success',
        'FAILED': 'danger',
        'CANCELLED': 'warning',
        'COMPLETED': 'success',
        'PARTIAL_FAILED': 'warning'
      };
      return types[state] || 'info';
    },

    getStateText(state) {
      const texts = {
        'PENDING': '等待中',
        'RUNNING': '执行中',
        'SUCCESS': '成功',
        'FAILED': '失败',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成',
        'PARTIAL_FAILED': '部分失败',
        'SKIPPED': '已跳过'
      };
      return texts[state] || state;
    },

    getStateColor(state) {
      const colors = {
        'PENDING': '#909399',
        'RUNNING': '#409eff',
        'SUCCESS': '#67c23a',
        'FAILED': '#f56c6c',
        'CANCELLED': '#e6a23c'
      };
      return colors[state] || '#909399';
    },

    getStateCategory(state) {
      return ['PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED'].indexOf(state);
    },

    getTaskTimelineType(state) {
      const types = {
        'SUCCESS': 'success',
        'FAILED': 'danger',
        'RUNNING': 'primary',
        'PENDING': 'info'
      };
      return types[state] || 'info';
    },

    getTaskIcon(state) {
      const icons = {
        'SUCCESS': 'el-icon-check',
        'FAILED': 'el-icon-close',
        'RUNNING': 'el-icon-loading',
        'PENDING': 'el-icon-time'
      };
      return icons[state] || 'el-icon-more';
    },

    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.task-orchestration {
  padding: 20px;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.subtitle {
  margin: 5px 0 0;
  color: #909399;
  font-size: 14px;
}

.main-content {
  display: flex;
  gap: 20px;
  flex: 1;
  overflow: hidden;
}

.chat-panel {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-area {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.input-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.history-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.history-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.history-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.history-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-time {
  font-size: 12px;
  color: #909399;
}

.history-content {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dag-panel {
  flex: 1;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  overflow-y: auto;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.dag-content h3, .dag-content h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.statistics {
  margin: 15px 0;
}

.dag-graph {
  margin: 20px 0;
}

.graph-container {
  width: 100%;
  height: 400px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.task-list {
  margin-top: 20px;
}

.task-item {
  padding: 10px;
  background: #fafafa;
  border-radius: 4px;
}

.task-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.task-id {
  font-weight: bold;
  color: #606266;
}

.task-duration {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.task-description {
  color: #303133;
  margin-bottom: 8px;
}

.task-dependencies {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.task-result {
  margin-top: 10px;
}

.task-result pre {
  max-height: 200px;
  overflow-y: auto;
  background: white;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
}

.task-error {
  margin-top: 10px;
}

.task-retry {
  margin-top: 10px;
}

.final-answer {
  margin: 20px 0;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.final-answer h4 {
  margin: 0 0 15px 0;
  font-size: 18px;
  font-weight: bold;
}

.answer-content {
  background: rgba(255, 255, 255, 0.1);
  padding: 15px;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 15px;
}
</style>

