<!--
  模块名称: 主布局组件
  功能描述: 系统的主布局结构，包含侧边栏导航和顶部栏。
-->
<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo">
        <span class="logo-text">{{ settingsStore.companyName }}</span>
        <div class="company-popup">
          <div class="popup-header">{{ settingsStore.companyName }}</div>
          <div class="popup-content">
            <div class="info-item">
              <span class="label">电话:</span>
              <span class="value">{{ settingsStore.settings.company_phone || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ settingsStore.settings.company_email || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">地址:</span>
              <span class="value">{{ settingsStore.settings.company_address || '暂无' }}</span>
            </div>
          </div>
        </div>
      </div>
      <nav>
        <router-link to="/dashboard" class="nav-item">仪表盘</router-link>

        <div class="nav-group">商品与库存</div>
        <router-link to="/items" class="nav-item">商品列表</router-link>
        <router-link to="/items/add" class="nav-item">新增商品</router-link>
        <router-link to="/stock" class="nav-item">库存列表</router-link>
        <router-link to="/stock/flow" class="nav-item">库存流水</router-link>
        <router-link to="/stock/adjust" class="nav-item">库存调整</router-link>

        <div class="nav-group">采购管理</div>
        <router-link to="/purchase" class="nav-item">采购订单</router-link>
        <router-link to="/purchase/add" class="nav-item">新建采购</router-link>
        <router-link to="/purchase/inbound" class="nav-item">采购入库</router-link>

        <div class="nav-group">销售管理</div>
        <router-link to="/sales" class="nav-item">销售订单</router-link>
        <router-link to="/sales/add" class="nav-item">新建销售</router-link>
        <router-link to="/sales/outbound" class="nav-item">销售出库</router-link>

        <div class="nav-group">基础信息</div>
        <router-link to="/customers" class="nav-item">客户管理</router-link>
        <router-link to="/suppliers" class="nav-item">供应商管理</router-link>
        
        <!-- 管理员入口 -->
        <template v-if="isAdmin">
          <div class="nav-group admin-group">管理员</div>
          <router-link to="/admin" class="nav-item admin-link">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="admin-icon">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            管理控制台
          </router-link>
        </template>
      </nav>
      <div class="sidebar-footer">
        <div class="user-badge" @click="router.push('/profile')" title="点击编辑个人信息">
          <div class="user-avatar">
            <img v-if="userStore.user?.avatar" :src="getAvatarUrl(userStore.user.avatar)" alt="用户头像">
            <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <div class="user-details">
            <span class="user-name">{{ userStore.user?.username || '用户' }}</span>
            <span class="user-role">{{ userStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
          </div>
        </div>
        <button @click="logout" class="logout-btn">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span>退出登录</span>
        </button>
      </div>
    </aside>
    <main class="main-content">
      <header class="top-bar">
        <h2>{{ currentRouteName }}</h2>
      </header>
      <div class="content-area">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

onMounted(() => {
  settingsStore.fetchSettings()
})

const currentRouteName = computed(() => route.name || '库存管理系统')

// 检查是否为管理员
const isAdmin = computed(() => {
  const user = userStore.user
  return user && user.role === 'ADMIN'
})

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('data:')) return url
  if (url.startsWith('http')) return url
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseUrl}${url}`
}
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  background-color: #f5f7fa;
}

.sidebar {
  width: 240px;
  background-color: #2c3e50;
  color: white;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  padding: 20px;
  font-size: 1.2rem;
  font-weight: bold;
  border-bottom: 1px solid #34495e;
  text-align: center;
  position: relative;
  cursor: pointer;
  -webkit-app-region: drag; /* 允许拖拽窗口 */
}

.logo-text {
  display: inline-block;
  transition: color 0.3s;
}

.logo:hover .logo-text {
  color: #3498db;
}

.company-popup {
  position: absolute;
  top: 100%;
  left: 10px;
  width: 280px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  padding: 0;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: left;
  color: #333;
  pointer-events: none;
  overflow: hidden;
  -webkit-app-region: no-drag; /* 弹窗内容不可拖拽，防止交互问题 */
}

.logo:hover .company-popup {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
  pointer-events: auto;
}

.popup-header {
  background: #3498db;
  color: white;
  padding: 12px 15px;
  font-size: 1rem;
  font-weight: 600;
}

.popup-content {
  padding: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 0.9rem;
  line-height: 1.4;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #7f8c8d;
  width: 40px;
  flex-shrink: 0;
  font-weight: 500;
}

.info-item .value {
  color: #2c3e50;
  word-break: break-all;
}

nav {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.nav-group {
  padding: 15px 20px 5px;
  font-size: 0.75rem;
  text-transform: uppercase;
  color: #7f8c8d;
  font-weight: bold;
}

.nav-item {
  display: block;
  padding: 10px 20px;
  color: #ecf0f1;
  text-decoration: none;
  transition: background-color 0.2s;
}

.nav-item:hover,
.nav-item.router-link-active {
  background-color: #34495e;
  border-left: 4px solid #3498db;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #34495e;
  background-color: #2c3e50;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-badge:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar svg {
  width: 20px;
  height: 20px;
  color: white;
}

.user-details {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-name {
  color: white;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: #bdc3c7;
  font-size: 12px;
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background-color: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: rgba(231, 76, 60, 0.2);
}

.logout-btn svg {
  width: 16px;
  height: 16px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  -webkit-app-region: drag; /* 允许拖拽窗口 */
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 管理员入口样式 */
.admin-group {
  color: #f1c40f;
  margin-top: 10px;
  border-top: 1px solid #34495e;
  padding-top: 15px;
}

.admin-link {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border-left: 4px solid #9b59b6 !important;
}

.admin-link:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
}

.admin-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
</style>
