<!--
  模块名称: 管理员仪表盘
  功能描述: 管理员专用的仪表盘，展示系统概况、统计数据和快捷管理入口。
-->
<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card users">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">用户总数</span>
          <span class="stat-value">{{ dashboard.totalUsers }}</span>
          <span class="stat-sub">活跃用户: {{ dashboard.activeUsers }}</span>
        </div>
      </div>
      
      <div class="stat-card operations">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">今日操作</span>
          <span class="stat-value">{{ dashboard.totalOperationsToday }}</span>
          <span class="stat-sub">系统运行正常</span>
        </div>
      </div>
      
      <div class="stat-card products">
        <div class="stat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">商品总数</span>
          <span class="stat-value">{{ dashboard.totalProducts || 0 }}</span>
          <span class="stat-sub">低库存: {{ dashboard.lowStockProducts || 0 }} | 缺货0库存: {{ dashboard.outOfStockProducts || 0 }}</span>
        </div>
      </div>
      
    </div>
    
    <!-- 快捷操作和最近活动 -->
    <div class="dashboard-grid">
      <!-- 快捷操作 -->
      <div class="dashboard-card quick-actions">
        <h3 class="card-title">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
          快捷操作
        </h3>
        <div class="actions-grid">
          <router-link to="/admin/users" class="action-item">
            <div class="action-icon users">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="8.5" cy="7" r="4"/>
                <line x1="20" y1="8" x2="20" y2="14"/>
                <line x1="23" y1="11" x2="17" y2="11"/>
              </svg>
            </div>
            <span>添加用户</span>
          </router-link>
          
          <router-link to="/admin/logs" class="action-item">
            <div class="action-icon logs">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </div>
            <span>查看日志</span>
          </router-link>
          
          <router-link to="/admin/settings" class="action-item">
            <div class="action-icon settings">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
              </svg>
            </div>
            <span>系统设置</span>
          </router-link>
          
          <router-link to="/dashboard" class="action-item">
            <div class="action-icon back">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="9"/>
                <rect x="14" y="3" width="7" height="5"/>
                <rect x="14" y="12" width="7" height="9"/>
                <rect x="3" y="16" width="7" height="5"/>
              </svg>
            </div>
            <span>用户仪表盘</span>
          </router-link>
        </div>
      </div>
      
      <!-- 最近操作日志 -->
      <div class="dashboard-card recent-logs">
        <h3 class="card-title">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          最近操作
        </h3>
        <div class="logs-list" v-if="recentLogs.length > 0">
          <div v-for="log in recentLogs" :key="log.id" class="log-item">
            <div class="log-icon" :class="getLogIconClass(log.action)">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
              </svg>
            </div>
            <div class="log-content">
              <div class="log-title">{{ log.description || log.action }}</div>
              <div class="log-meta">
                <span class="log-user">{{ log.username }}</span>
                <span class="log-time">{{ formatTime(log.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-logs">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <span>暂无操作记录</span>
        </div>
      </div>
    </div>
    

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/request'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const dashboard = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalOperationsToday: 0,
  totalProducts: 0,
  lowStockProducts: 0,
  outOfStockProducts: 0,
  systemVersion: '2.0.0'
})

const recentLogs = ref([])

const fetchDashboard = async () => {
  try {
    const data = await axios.get('/admin/dashboard')
    dashboard.value = { ...dashboard.value, ...data }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

const fetchRecentLogs = async () => {
  try {
    const data = await axios.get('/admin/logs/recent', { params: { limit: 5 } })
    recentLogs.value = data || []
  } catch (error) {
    console.error('获取最近日志失败:', error)
  }
}

const getLogIconClass = (action) => {
  if (!action) return 'default'
  if (action.includes('登录') || action.includes('LOGIN')) return 'login'
  if (action.includes('创建') || action.includes('CREATE') || action.includes('ADD')) return 'create'
  if (action.includes('删除') || action.includes('DELETE')) return 'delete'
  if (action.includes('更新') || action.includes('UPDATE')) return 'update'
  return 'default'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  settingsStore.fetchSettings()
  fetchDashboard()
  fetchRecentLogs()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
  color: white;
}

.stat-card.users .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.operations .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.products .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.version .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: white;
  line-height: 1.2;
}

.stat-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4px;
}

/* Dashboard Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

/* 通用卡片样式 */
.dashboard-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin: 0 0 20px 0;
}

.card-title svg {
  width: 20px;
  height: 20px;
  color: #667eea;
}

/* 快捷操作 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.action-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.action-icon.logs {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.action-icon.settings {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.action-icon.back {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

/* 最近日志 */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
}

.log-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-icon svg {
  width: 18px;
  height: 18px;
}

.log-icon.default {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

.log-icon.login {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.log-icon.create {
  background: rgba(67, 233, 123, 0.2);
  color: #43e97b;
}

.log-icon.update {
  background: rgba(79, 172, 254, 0.2);
  color: #4facfe;
}

.log-icon.delete {
  background: rgba(245, 87, 108, 0.2);
  color: #f5576c;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-title {
  font-size: 14px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.empty-logs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.4);
  gap: 12px;
}

.empty-logs svg {
  width: 40px;
  height: 40px;
}

/* 系统信息 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
}

.info-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.info-value {
  font-size: 14px;
  color: white;
  font-weight: 600;
}
</style>
