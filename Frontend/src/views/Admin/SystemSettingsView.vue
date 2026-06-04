<!--
  模块名称: 系统设置页面
  功能描述: 管理系统全局配置，如公司信息、库存预警阈值、单据前缀等。
-->
<template>
  <div class="system-settings">
    <!-- 公司信息设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="header-icon company">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 21h18"/>
            <path d="M5 21V7l8-4v18"/>
            <path d="M19 21V11l-6-4"/>
            <path d="M9 9v.01"/>
            <path d="M9 12v.01"/>
            <path d="M9 15v.01"/>
            <path d="M9 18v.01"/>
          </svg>
        </div>
        <div class="header-content">
          <h3>公司信息</h3>
          <p>设置公司基本信息，用于单据显示</p>
        </div>
      </div>
      
      <div class="settings-form">
        <div class="form-row">
          <div class="form-group">
            <label>公司名称</label>
            <input v-model="settings.company_name" type="text" placeholder="请输入公司名称">
          </div>
          <div class="form-group">
            <label>联系电话</label>
            <input v-model="settings.company_phone" type="tel" placeholder="请输入联系电话">
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>公司邮箱</label>
            <input v-model="settings.company_email" type="email" placeholder="请输入公司邮箱">
          </div>
          <div class="form-group">
            <label>公司地址</label>
            <input v-model="settings.company_address" type="text" placeholder="请输入公司地址">
          </div>
        </div>
      </div>
    </div>
    
    <!-- 库存设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="header-icon inventory">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <div class="header-content">
          <h3>库存设置</h3>
          <p>配置库存相关的系统参数</p>
        </div>
      </div>
      
      <div class="settings-form">
        <div class="form-row">
          <div class="form-group">
            <label>低库存预警阈值</label>
            <div class="input-with-suffix">
              <input v-model="settings.low_stock_threshold" type="number" min="0" placeholder="10">
              <span class="suffix">件</span>
            </div>
            <span class="form-help">当商品库存低于此值时触发预警</span>
          </div>
          <div class="form-group">
            <label>库存积压判定天数</label>
            <div class="input-with-suffix">
              <input v-model="settings.inventory_backlog_days" type="number" min="0" placeholder="60">
              <span class="suffix">天</span>
            </div>
            <span class="form-help">无动销超过此天数视为积压</span>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>允许负库存</label>
            <div class="toggle-wrapper">
              <label class="toggle">
                <input type="checkbox" v-model="allowNegativeStock">
                <span class="slider"></span>
              </label>
              <span class="toggle-label">{{ allowNegativeStock ? '允许' : '不允许' }}</span>
            </div>
            <span class="form-help">开启后可出库超过当前库存数量</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 单据设置 -->
    <div class="settings-card">
      <div class="card-header">
        <div class="header-icon orders">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
        </div>
        <div class="header-content">
          <h3>单据设置</h3>
          <p>配置订单编号前缀等参数</p>
        </div>
      </div>
      
      <div class="settings-form">
        <div class="form-row">
          <div class="form-group">
            <label>采购单编号前缀</label>
            <input v-model="settings.order_prefix_purchase" type="text" placeholder="PO" maxlength="5">
            <span class="form-help">示例: {{ settings.order_prefix_purchase || 'PO' }}202412090001</span>
          </div>
          <div class="form-group">
            <label>销售单编号前缀</label>
            <input v-model="settings.order_prefix_sales" type="text" placeholder="SO" maxlength="5">
            <span class="form-help">示例: {{ settings.order_prefix_sales || 'SO' }}202412090001</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 保存按钮 -->
    <div class="actions-bar">
      <button @click="resetSettings" class="reset-btn" :disabled="saving">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
          <path d="M3 3v5h5"/>
        </svg>
        重置
      </button>
      <button @click="saveSettings" class="save-btn" :disabled="saving">
        <svg v-if="!saving" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
          <polyline points="17 21 17 13 7 13 7 21"/>
          <polyline points="7 3 7 8 15 8"/>
        </svg>
        <span v-if="saving" class="spinner"></span>
        {{ saving ? '保存中...' : '保存设置' }}
      </button>
    </div>
    

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/request'
import { showSuccess, showError } from '@/utils/toast'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const settings = ref({
  company_name: '',
  company_address: '',
  company_phone: '',
  company_email: '',
  low_stock_threshold: '10',
  inventory_backlog_days: '60',
  order_prefix_purchase: 'PO',
  order_prefix_sales: 'SO',
  allow_negative_stock: 'false'
})

const originalSettings = ref({})
const saving = ref(false)

const allowNegativeStock = computed({
  get: () => settings.value.allow_negative_stock === 'true',
  set: (val) => { settings.value.allow_negative_stock = val ? 'true' : 'false' }
})

const fetchSettings = async () => {
  try {
    const data = await axios.get('/admin/settings/map')
    if (data) {
      settings.value = { ...settings.value, ...data }
      originalSettings.value = { ...settings.value }
    }
  } catch (error) {
    console.error('获取设置失败:', error)
  }
}

const saveSettings = async () => {
  saving.value = true
  try {
    await axios.put('/admin/settings', settings.value)
    originalSettings.value = { ...settings.value }
    settingsStore.updateSettings(settings.value)
    showSuccess('设置保存成功！')
  } catch (error) {
    console.error('保存设置失败:', error)
    showError('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  settings.value = { ...originalSettings.value }
}

onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.system-settings {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 900px;
}

/* 设置卡片 */
.settings-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.header-icon.company {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header-icon.inventory {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.header-icon.orders {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.header-content h3 {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin: 0 0 4px 0;
}

.header-content p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

/* 表单样式 */
.settings-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.form-group input {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: white;
  font-size: 14px;
  transition: all 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.form-help {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

/* 带后缀的输入框 */
.input-with-suffix {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-suffix input {
  flex: 1;
  padding-right: 50px;
}

.input-with-suffix .suffix {
  position: absolute;
  right: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
}

/* Toggle 开关 */
.toggle-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 26px;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 26px;
  transition: 0.3s;
}

.slider::before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

.toggle input:checked + .slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.toggle input:checked + .slider::before {
  transform: translateX(22px);
}

.toggle-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

/* 操作栏 */
.actions-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 8px;
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

.reset-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.reset-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reset-btn svg {
  width: 18px;
  height: 18px;
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.save-btn svg {
  width: 18px;
  height: 18px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Toast 提示 */
.toast {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  z-index: 1000;
}

.toast.success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #0f3460;
}

.toast svg {
  width: 20px;
  height: 20px;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
