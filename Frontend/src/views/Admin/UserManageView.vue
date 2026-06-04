<!--
  模块名称: 用户管理页面
  功能描述: 管理系统用户，包括添加、编辑、删除用户以及重置密码等功能。
-->
<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>用户管理</h2>
        <p>管理系统用户账号和权限</p>
      </div>
      <button @click="showAddModal = true" class="add-btn">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        添加用户
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-group">
        <input 
          v-model="searchForm.name" 
          type="text" 
          placeholder="搜索用户名或昵称..." 
          @keyup.enter="handleSearch"
        >
        <select v-model="searchForm.role" @change="handleSearch">
          <option value="">所有角色</option>
          <option value="ADMIN">管理员</option>
          <option value="USER">普通用户</option>
        </select>
        <button @click="handleSearch" class="search-btn">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button>
        <button @click="handleReset" class="reset-btn">
          重置
        </button>
      </div>
    </div>
    
    <!-- 用户列表 -->
    <div class="users-card">
      <div class="table-wrapper">
        <table class="users-table">
          <thead>
            <tr>
              <th>用户信息</th>
              <th>角色</th>
              <th>状态</th>
              <th>联系方式</th>
              <th>最后登录</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in paginatedUsers" :key="user.id" class="user-row">
              <td>
                <div class="user-info">
                  <div class="user-avatar">
                    {{ user.username?.charAt(0).toUpperCase() }}
                  </div>
                  <div class="user-details">
                    <span class="user-name">{{ user.nickname || user.username }}</span>
                    <span class="user-username">@{{ user.username }}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="role-badge" :class="user.role?.toLowerCase()">
                  {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="user.status === 1 ? 'active' : 'inactive'">
                  {{ user.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="contact-info">
                  <div v-if="user.phone" class="contact-item phone">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                    </svg>
                    {{ user.phone }}
                  </div>
                  <div v-if="user.email" class="contact-item email">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                      <polyline points="22,6 12,13 2,6"/>
                    </svg>
                    {{ user.email }}
                  </div>
                  <span v-if="!user.phone && !user.email" class="no-data">-</span>
                </div>
              </td>
              <td>
                <span class="last-login">{{ formatDate(user.lastLoginAt) }}</span>
              </td>
              <td>
                <div class="action-btns">
                  <button @click="editUser(user)" class="action-btn edit" title="编辑">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button @click="toggleStatus(user)" class="action-btn" :class="user.status === 1 ? 'disable' : 'enable'" :title="user.status === 1 ? '禁用' : '启用'">
                    <svg v-if="user.status === 1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                    </svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                      <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
                  </button>
                  <button @click="deleteUser(user)" class="action-btn delete" title="删除">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      <line x1="10" y1="11" x2="10" y2="17"/>
                      <line x1="14" y1="11" x2="14" y2="17"/>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页 -->
      <Pagination
        :current-page="page"
        :total-pages="totalPages"
        @update:currentPage="goToPage"
      />
      
      <div v-if="users.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <span>暂无用户数据</span>
      </div>
    </div>
    
    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑用户' : '添加用户' }}</h3>
          <button @click="closeModal" class="close-btn">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="submitUser" class="user-form">
          <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input v-model="form.username" type="text" placeholder="请输入用户名" required :disabled="showEditModal">
          </div>
          
          <div class="form-group" v-if="!showEditModal">
            <label>密码 <span class="required">*</span></label>
            <input v-model="form.password" type="password" placeholder="请输入密码" required>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="form.email" type="email" placeholder="请输入邮箱">
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input v-model="form.phone" type="tel" placeholder="请输入手机号">
            </div>
          </div>
          
          <div class="form-group">
            <label>角色</label>
            <select v-model="form.role">
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <ConfirmModal
      v-model:visible="showConfirmModal"
      title="删除确认"
      :message="confirmMessage"
      @confirm="handleDeleteConfirm"
    />
  </div>
</template>

<script setup>
import axios from '@/api/request'
import ConfirmModal from '@/components/ConfirmModal.vue'
import Pagination from '@/components/Pagination.vue'
import { useSettingsStore } from '@/stores/settings'
import { showError, showSuccess } from '@/utils/toast'
import { computed, onMounted, ref } from 'vue'

const settingsStore = useSettingsStore()

const users = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const submitting = ref(false)
const showConfirmModal = ref(false)
const confirmMessage = ref('')
const userToDelete = ref(null)

// 分页状态
const page = ref(1)
const pageSize = ref(15)

const totalPages = computed(() => {
  return Math.ceil(users.value.length / pageSize.value) || 1
})

const paginatedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return users.value.slice(start, end)
})

// 跳转到指定页码
const goToPage = (p) => {
  if (p >= 1 && p <= totalPages.value) {
    page.value = p
  }
}

const searchForm = ref({
  name: '',
  role: ''
})

const form = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'USER'
})

const fetchUsers = async () => {
  try {
    const params = {}
    if (searchForm.value.name) params.name = searchForm.value.name
    if (searchForm.value.role) params.role = searchForm.value.role
    
    const data = await axios.get('/admin/users', { params })
    users.value = data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const editUser = (user) => {
  form.value = {
    id: user.id,
    username: user.username,
    password: '',
    nickname: user.nickname || '',
    email: user.email || '',
    phone: user.phone || '',
    role: user.role || 'USER'
  }
  showEditModal.value = true
}

const toggleStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await axios.put(`/admin/users/${user.id}/status`, {
      status: newStatus
    })
    user.status = newStatus
    showSuccess('用户状态更新成功')
  } catch (error) {
    console.error('更新用户状态失败:', error)
    showError('更新用户状态失败')
  }
}

const deleteUser = (user) => {
  userToDelete.value = user
  confirmMessage.value = `确定要删除用户 "${user.nickname || user.username}" 吗？此操作不可恢复！`
  showConfirmModal.value = true
}

const handleDeleteConfirm = async () => {
  if (!userToDelete.value) return
  
  try {
    await axios.delete(`/admin/users/${userToDelete.value.id}`)
    showSuccess('用户删除成功')
    fetchUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
    showError(error.message || '删除失败')
  } finally {
    userToDelete.value = null
  }
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const handleReset = () => {
  searchForm.value = {
    name: '',
    role: ''
  }
  page.value = 1
  fetchUsers()
}

const submitUser = async () => {
  submitting.value = true
  try {
    if (showEditModal.value) {
      await axios.put(`/admin/users/${form.value.id}`, form.value)
      showSuccess('用户更新成功')
    } else {
      await axios.post('/admin/users', form.value)
      showSuccess('用户添加成功')
    }
    closeModal()
    fetchUsers()
  } catch (error) {
    console.error('保存用户失败:', error)
    showError(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  form.value = {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    role: 'USER'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '从未登录'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric',
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  settingsStore.fetchSettings()
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.page-header p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin: 4px 0 0 0;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.add-btn svg {
  width: 18px;
  height: 18px;
}

/* 搜索栏 */
.search-bar {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  padding: 16px;
}

.search-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-group input {
  flex: 1;
  max-width: 300px;
  padding: 10px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: white;
  font-size: 14px;
  box-sizing: border-box;
}

.search-group select {
  padding: 10px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  box-sizing: border-box;
}

.search-group select option {
  background: #1a1a2e;
}

.search-btn, .reset-btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
}

.search-btn {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-btn:hover {
  background: rgba(102, 126, 234, 0.3);
}

.search-btn svg {
  width: 16px;
  height: 16px;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

/* 用户表格卡片 */
.users-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  overflow: hidden;
}

.table-wrapper {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  text-align: left;
  padding: 16px 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.users-table td {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.user-row:hover {
  background: rgba(255, 255, 255, 0.03);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-username {
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
}

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.role-badge.admin {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.role-badge.user {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: rgba(67, 233, 123, 0.2);
  color: #43e97b;
}

.status-badge.inactive {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.contact-item svg {
  width: 14px;
  height: 14px;
  opacity: 0.7;
}

.contact-item.phone {
  color: rgba(255, 255, 255, 0.9);
}

.contact-item.email {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.no-data {
  color: rgba(255, 255, 255, 0.3);
}

.last-login {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.action-btn.edit {
  background: rgba(79, 172, 254, 0.2);
  color: #4facfe;
}

.action-btn.edit:hover {
  background: rgba(79, 172, 254, 0.3);
}

.action-btn.disable {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.action-btn.disable:hover {
  background: rgba(239, 68, 68, 0.3);
}

.action-btn.enable {
  background: rgba(67, 233, 123, 0.2);
  color: #43e97b;
}

.action-btn.enable:hover {
  background: rgba(67, 233, 123, 0.3);
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.action-btn.delete:hover {
  background: rgba(239, 68, 68, 0.3);
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

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 480px;
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.close-btn svg {
  width: 18px;
  height: 18px;
}

/* 表单样式 */
.user-form {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: white;
  font-size: 14px;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 0;
  padding: 20px;
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

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-group select {
  cursor: pointer;
}

.form-group select option {
  background: #1a1a2e;
  color: white;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.cancel-btn {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.submit-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
