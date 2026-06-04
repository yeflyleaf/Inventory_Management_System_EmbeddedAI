<!--
  模块名称: 操作日志页面
  功能描述: 查看和搜索系统操作日志，支持按用户、模块和时间筛选。
-->
<template>
  <div class="operation-logs">
    <!-- 搜索筛选 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <label>用户名</label>
          <input v-model="filters.username" type="text" placeholder="搜索用户名">
        </div>
        <div class="filter-item">
          <label>操作模块</label>
          <select v-model="filters.module" @change="searchLogs">
            <option value="">全部模块</option>
            <option v-for="mod in modules" :key="mod" :value="mod">{{ mod }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label>开始时间</label>
          <input v-model="filters.startTime" type="datetime-local">
        </div>
        <div class="filter-item">
          <label>结束时间</label>
          <input v-model="filters.endTime" type="datetime-local">
        </div>
        <div class="filter-actions">
          <button @click="searchLogs" class="search-btn">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            搜索
          </button>
          <button @click="resetFilters" class="reset-btn">重置</button>
        </div>
      </div>
    </div>
    
    <!-- 日志列表 -->
    <div class="logs-card">
      <div class="card-header">
        <h3>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          操作日志
        </h3>
        <button @click="cleanOldLogs" class="clean-btn">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
          清理旧日志
        </button>
      </div>
      
      <div class="logs-list" v-if="logs.length > 0">
        <div v-for="log in logs" :key="log.id" class="log-item">
          <div class="log-icon" :class="getActionClass(log.action)">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
            </svg>
          </div>
          <div class="log-main">
            <div class="log-header">
              <span class="log-action">{{ log.action || '操作' }}</span>
              <span class="log-module" v-if="log.module">{{ log.module }}</span>
            </div>
            <p class="log-description">{{ log.description || '-' }}</p>
            <div class="log-meta">
              <span class="meta-item">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                {{ log.username }}
              </span>

              <span class="meta-item">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                {{ formatTime(log.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
        </svg>
        <span>暂无操作日志</span>
      </div>
      
      <!-- 分页 -->
      <Pagination
        :current-page="page"
        :total-pages="totalPages"
        @update:currentPage="goToPage"
      />
    </div>
    <ConfirmModal
      v-model:visible="showConfirmModal"
      title="清理日志确认"
      message="确定要清理90天前的旧日志吗？此操作不可恢复。"
      @confirm="handleCleanConfirm"
    />
  </div>
</template>

<script setup>
import axios from '@/api/request'
import ConfirmModal from '@/components/ConfirmModal.vue'
import Pagination from '@/components/Pagination.vue'
import { showError, showSuccess } from '@/utils/toast'
import { onMounted, ref } from 'vue'

const logs = ref([])
const showConfirmModal = ref(false)
const modules = ref([])
const page = ref(1)
const size = ref(15)
const total = ref(0)
const totalPages = ref(0)

const filters = ref({
  username: '',
  module: '',
  startTime: '',
  endTime: ''
})

const fetchLogs = async () => {
  try {
    const params = {
      page: page.value,
      size: size.value,
      ...filters.value
    }
    
    // 转换时间格式
    if (params.startTime) {
      params.startTime = new Date(params.startTime).toISOString().replace('T', ' ').substring(0, 19)
    }
    if (params.endTime) {
      params.endTime = new Date(params.endTime).toISOString().replace('T', ' ').substring(0, 19)
    }
    
    const data = await axios.get('/admin/logs', { params })
    logs.value = data?.list || []
    total.value = data?.total || 0
    totalPages.value = data?.totalPages || 0
  } catch (error) {
    console.error('获取日志失败:', error)
  }
}

const fetchModules = async () => {
  try {
    const data = await axios.get('/admin/logs/modules')
    modules.value = data || []
  } catch (error) {
    console.error('获取模块列表失败:', error)
  }
}

const searchLogs = () => {
  page.value = 1
  fetchLogs()
}

const resetFilters = () => {
  filters.value = {
    username: '',
    module: '',
    startTime: '',
    endTime: ''
  }
  page.value = 1
  fetchLogs()
}

// 跳转到指定页码
const goToPage = (p) => {
  if (p >= 1 && p <= totalPages.value) {
    page.value = p
    fetchLogs()
  }
}

const cleanOldLogs = () => {
  showConfirmModal.value = true
}

const handleCleanConfirm = async () => {
  try {
    const data = await axios.delete('/admin/logs/clean', { params: { keepDays: 90 } })
    showSuccess(data?.message || '清理完成')
    fetchLogs()
  } catch (error) {
    showError('清理失败: ' + (error.message || '未知错误'))
  }
}

const getActionClass = (action) => {
  if (!action) return 'default'
  action = action.toLowerCase()
  if (action.includes('login') || action.includes('登录')) return 'login'
  if (action.includes('create') || action.includes('add') || action.includes('创建') || action.includes('新增')) return 'create'
  if (action.includes('delete') || action.includes('删除')) return 'delete'
  if (action.includes('update') || action.includes('edit') || action.includes('更新') || action.includes('编辑')) return 'update'
  return 'default'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchLogs()
  fetchModules()
})
</script>

<style scoped>
.operation-logs {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 筛选卡片 */
.filter-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.filter-item {
  flex: 1;
  min-width: 180px;
}

.filter-item label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 8px;
}

.filter-item input,
.filter-item select {
  width: 100%;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  box-sizing: border-box;
}

.filter-item input:focus,
.filter-item select:focus {
  outline: none;
  border-color: #667eea;
}

.filter-item input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.filter-item select option {
  background: #1a1a2e;
  color: white;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.search-btn svg {
  width: 16px;
  height: 16px;
}

.reset-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

/* 日志卡片 */
.logs-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin: 0;
}

.card-header h3 svg {
  width: 20px;
  height: 20px;
  color: #667eea;
}

.clean-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clean-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

.clean-btn svg {
  width: 16px;
  height: 16px;
}

/* 日志列表 */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.log-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateX(4px);
}

.log-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-icon svg {
  width: 20px;
  height: 20px;
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

.log-main {
  flex: 1;
  min-width: 0;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.log-action {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.log-module {
  font-size: 11px;
  padding: 2px 8px;
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
  border-radius: 4px;
}

.log-description {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 10px 0;
}

.log-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.meta-item svg {
  width: 14px;
  height: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.4);
  gap: 16px;
}

.empty-state svg {
  width: 48px;
  height: 48px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn svg {
  width: 18px;
  height: 18px;
}

.page-info {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}
</style>
