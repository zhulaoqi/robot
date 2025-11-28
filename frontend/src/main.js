import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'
import UnifiedChat from './views/UnifiedChat.vue'
import Chat from './views/Chat.vue'
import Sql from './views/Sql.vue'
import Knowledge from './views/Knowledge.vue'
import Agent from './views/Agent.vue'
import Mcp from './views/Mcp.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/unified', component: UnifiedChat },
  { path: '/chat', component: Chat },
  { path: '/sql', component: Sql },
  { path: '/knowledge', component: Knowledge },
  { path: '/agent', component: Agent },
  { path: '/mcp', component: Mcp }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(router)
app.mount('#app')

