<!--
  模块名称: 个人资料页面
  功能描述: 用户查看和修改个人信息、修改密码的页面。
-->
<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人信息设置</h1>
      <p class="subtitle">管理您的个人资料和账户安全</p>
    </div>

    <div class="content-grid">
      <!-- 左侧：基本信息 -->
      <div class="card profile-card">
        <div class="card-header">
          <h3>基本信息</h3>
        </div>
        <div class="card-body">
          <div class="user-avatar-large">
            <div class="avatar-wrapper" @click="triggerFileInput">
              <div class="avatar-circle" :class="{ 'has-image': avatarPreview || userStore.user?.avatar }">
                <img v-if="avatarPreview || userStore.user?.avatar" :src="avatarPreview || getAvatarUrl(userStore.user?.avatar)" alt="用户头像" class="avatar-image">
                <span v-else class="avatar-text">{{ userStore.user?.username?.charAt(0).toUpperCase() }}</span>
                <div class="avatar-overlay">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
                    <circle cx="12" cy="13" r="4"></circle>
                  </svg>
                  <span>更换头像</span>
                </div>
              </div>
            </div>
            <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileChange">
            <div class="user-role-badge">{{ userStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
          </div>

          <form @submit.prevent="updateProfile" class="form-layout">
            <div class="form-group">
              <label>用户名</label>
              <input type="text" :value="userStore.user?.username" disabled class="form-input disabled" title="用户名不可修改">
            </div>

            <div class="form-group">
              <label>电子邮箱</label>
              <input type="email" v-model="profileForm.email" class="form-input" :placeholder="userStore.user?.email || '请输入电子邮箱'">
            </div>

            <div class="form-group">
              <label>联系电话</label>
              <input type="tel" v-model="profileForm.phone" class="form-input" :placeholder="userStore.user?.phone || '请输入联系电话'">
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="loading">
                <span v-if="loading">保存中...</span>
                <span v-else>保存修改</span>
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 右侧：安全设置 -->
      <div class="card security-card">
        <div class="card-header">
          <h3>安全设置</h3>
        </div>
        <div class="card-body">
          <form @submit.prevent="changePassword" class="form-layout">
            <div class="form-group">
              <label>当前密码</label>
              <input type="password" v-model="passwordForm.oldPassword" class="form-input" placeholder="请输入当前密码" required>
            </div>

            <div class="form-group">
              <label>新密码</label>
              <input type="password" v-model="passwordForm.newPassword" class="form-input" placeholder="请输入新密码" required minlength="6">
            </div>

            <div class="form-group">
              <label>确认新密码</label>
              <input type="password" v-model="passwordForm.confirmPassword" class="form-input" placeholder="请再次输入新密码" required minlength="6">
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-danger" :disabled="passwordLoading">
                <span v-if="passwordLoading">修改中...</span>
                <span v-else>修改密码</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import api from '@/api/request'
import { useUserStore } from '@/stores/user'
import { showError, showSuccess } from '@/utils/toast'
import { onMounted, reactive, ref } from 'vue'

const userStore = useUserStore()
const loading = ref(false)
const passwordLoading = ref(false)

const fileInput = ref(null)
const avatarPreview = ref(null)
const selectedFile = ref(null)

const profileForm = reactive({
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 20 * 1024 * 1024) {
    showError('图片大小不能超过 20MB')
    return
  }

  if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
    showError('请上传 JPG/PNG/GIF 格式的图片')
    return
  }

  selectedFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 处理头像 URL，兼容 Electron 和 Web 环境
// 功能：将相对路径转换为绝对路径，确保在 Electron (file://) 环境下能正确加载图片
const getAvatarUrl = (url) => {
  if (!url) return ''
  // 如果是 base64 数据，直接返回
  if (url.startsWith('data:')) return url
  // 如果是完整 URL (http开头)，直接返回
  if (url.startsWith('http')) return url
  
  // 否则视为相对路径，需要拼接后端地址
  // 在 Electron 模式下，import.meta.env.VITE_API_BASE_URL 会指向后端地址 (如 http://localhost:8080)
  // 在 Web 模式下，通常是空或者 /api，由 Nginx 代理
  let baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  
  // 关键修复：如果 baseUrl 以 /api 结尾，而图片路径不是以 /api 开头（通常是 /uploads），则去掉 /api
  // 这样 http://10.0.2.2:80/api + /uploads/xxx 就会变成 http://10.0.2.2:80/uploads/xxx
  if (baseUrl.endsWith('/api')) {
    baseUrl = baseUrl.slice(0, -4)
  }
  
  return `${baseUrl}${url}`
}

onMounted(() => {
  if (userStore.user) {
    profileForm.email = userStore.user.email || ''
    profileForm.phone = userStore.user.phone || ''
  }
})

const updateProfile = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    // 如果选择了新头像，先上传头像到服务器
    let avatarUrl = userStore.user?.avatar
    if (selectedFile.value) {
      const formData = new FormData()
      formData.append('file', selectedFile.value)
      
      // 调用后端头像上传接口
      const uploadRes = await api.post('/upload/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      avatarUrl = uploadRes.url
    }

    // 更新用户信息到数据库
    const updateData = {
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: avatarUrl
    }

    // 调用后端更新用户资料接口
    await api.put('/users/profile', updateData)
    
    // 更新 store 中的用户信息
    userStore.user = { ...userStore.user, ...updateData }
    localStorage.setItem('user', JSON.stringify(userStore.user))
    
    showSuccess('个人信息更新成功')
    selectedFile.value = null // 重置选择的文件
  } catch (error) {
    showError(error.response?.data?.message || error.message || '更新失败，请重试')
  } finally {
    loading.value = false
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showError('两次输入的密码不一致')
    return
  }

  if (passwordLoading.value) return
  passwordLoading.value = true

  try {
    // 调用修改密码接口
    await api.post('/users/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    showSuccess('密码修改成功，请重新登录')
    
    // 密码修改成功后退出登录
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
    
  } catch (error) {
    showError(error.response?.data?.message || '密码修改失败，请检查当前密码是否正确')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 8px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid #e0e0e0;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.card-body {
  padding: 24px;
}

/* 头像区域 */
.user-avatar-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  margin-bottom: 12px;
}

.avatar-circle {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(52, 152, 219, 0.3);
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
}

.avatar-circle.has-image {
  background: transparent;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 40px;
  color: white;
  font-weight: 600;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
  gap: 4px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.hidden-input {
  display: none;
}

.user-role-badge {
  background-color: #f0f2f5;
  color: #7f8c8d;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 表单样式 */
.form-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #34495e;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.form-input.disabled {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

.form-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2980b9;
}

.btn-danger {
  background-color: #e74c3c;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #c0392b;
}
</style>
