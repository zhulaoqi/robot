import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus' 
import 'element-plus/dist/index.css' 
import App from './App.vue'
import Home from './views/Home.vue'
import Production from './views/Production.vue'
import UnifiedChat from './views/UnifiedChat.vue'
import Chat from './views/Chat.vue'
import Sql from './views/Sql.vue'
import Knowledge from './views/Knowledge.vue'
import Agent from './views/Agent.vue'
import Mcp from './views/Mcp.vue'
import TaskOrchestration from './views/TaskOrchestration.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/production', component: Production },
  { path: '/unified', component: UnifiedChat },
  { path: '/chat', component: Chat },
  { path: '/sql', component: Sql },
  { path: '/knowledge', component: Knowledge },
  { path: '/agent', component: Agent },
  { path: '/mcp', component: Mcp },
  { path: '/orchestration', component: TaskOrchestration }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(router)
app.use(ElementPlus) 
app.mount('#app')

