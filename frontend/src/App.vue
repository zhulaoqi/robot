<template>
  <div class="app">
    <nav class="navbar">
      <div class="nav-brand">
        <span class="logo">🤖</span>
        <span class="title">Robot AI Assistant</span>
      </div>
      <div class="nav-menu">
        <router-link to="/" class="nav-item">🏠 首页</router-link>
        <router-link to="/production" class="nav-item production-nav">🚀 生产级</router-link>
        <router-link to="/orchestration" class="nav-item orchestration-nav">⚡ 任务编排</router-link>
        <div class="nav-divider"></div>
        <router-link to="/unified" class="nav-item">🎯 统一对话</router-link>
        <router-link to="/chat" class="nav-item">💬 对话</router-link>
        <router-link to="/sql" class="nav-item">🗄️ SQL</router-link>
        <router-link to="/knowledge" class="nav-item">📚 知识库</router-link>
        <router-link to="/agent" class="nav-item">🤖 Agent</router-link>
        <router-link to="/mcp" class="nav-item">🔌 MCP</router-link>
      </div>
    </nav>
    <main class="main-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
</script>

<style>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo {
  font-size: 2rem;
}

.title {
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-menu {
  display: flex;
  gap: 1rem;
}

.nav-item {
  padding: 0.5rem 1rem;
  text-decoration: none;
  color: #333;
  border-radius: 8px;
  transition: all 0.3s;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.nav-divider {
  width: 1px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 0.5rem;
}

.production-nav {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.3);
}

.production-nav:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.4);
}

.orchestration-nav {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%) !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(250, 112, 154, 0.3);
}

.orchestration-nav:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 112, 154, 0.4);
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
}

.card-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #333;
}

.input-group {
  margin-bottom: 1rem;
}

.input-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #555;
}

.input-field {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.input-field:focus {
  outline: none;
  border-color: #667eea;
}

.textarea-field {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.textarea-field:focus {
  outline: none;
  border-color: #667eea;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-danger {
  background: #ff4757;
  color: white;
}

.btn-danger:hover {
  background: #ff3838;
}

.result-box {
  background: #f8f9fa;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  white-space: pre-wrap;
  font-family: 'Courier New', monospace;
  max-height: 400px;
  overflow-y: auto;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #667eea;
}

.error {
  background: #fff5f5;
  border: 2px solid #ff4757;
  color: #ff4757;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.success {
  background: #f0fff4;
  border: 2px solid #48bb78;
  color: #48bb78;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.grid {
  display: grid;
  gap: 1.5rem;
}

.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

@media (max-width: 768px) {
  .grid-2, .grid-3 {
    grid-template-columns: 1fr;
  }
  
  .navbar {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>

