<!--
  模块名称: 供应商列表页面
  功能描述: 展示供应商列表，支持搜索、新增、编辑和删除供应商信息。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">🏭</span>
        供应商管理
      </h3>
      <div class="filter-group">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索供应商名称、联系人或电话..." 
          class="search-input"
        />
        <RefreshButton @click="fetchSuppliers" />
        <button class="btn-primary" @click="openModal()">
          ➕ 新增供应商
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-id">编号</th>
            <th class="th-name">供应商名称</th>
            <th class="th-contact">联系人</th>
            <th class="th-phone">电话</th>
            <th class="th-address">地址</th>
            <th class="th-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(supplier, index) in paginatedSuppliers" :key="supplier.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td class="supplier-name" :title="supplier.name">{{ supplier.name }}</td>
            <td>
              <span class="contact-tag">{{ supplier.contactPerson || '-' }}</span>
            </td>
            <td class="phone-cell">
              <div><span class="phone-icon">📞</span> {{ supplier.phone || '-' }}</div>
              <div v-if="supplier.phone2" class="phone-secondary"><span class="phone-icon">📞</span> {{ supplier.phone2 }}</div>
            </td>
            <td class="address-cell" :title="supplier.address">{{ supplier.address || '-' }}</td>
            <td class="action-cell">
              <button class="btn-small btn-edit" @click="openModal(supplier)" title="编辑">
                ✏️ 编辑
              </button>
              <button class="btn-small btn-delete" @click="confirmDelete(supplier)" title="删除">
                🗑️ 删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredSuppliers.length === 0">
            <td colspan="6" class="empty-row">
              <div class="empty-state">
                <span class="empty-icon">📭</span>
                <span class="empty-text">暂无供应商数据</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <Pagination
      :current-page="currentPage"
      :total-pages="totalPages"
      @update:currentPage="handlePageChange"
    />

    <!-- 编辑/新增模态框 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑供应商' : '新增供应商' }}</h3>
          <button class="close-modal-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveSupplier" id="supplierForm">
            <div class="form-group">
              <label>供应商名称 <span class="required">*</span></label>
              <input v-model="form.name" required placeholder="请输入供应商名称" class="form-input" />
            </div>
            <div class="form-group">
              <label>联系人</label>
              <input v-model="form.contactPerson" placeholder="请输入联系人姓名" class="form-input" />
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input v-model="form.phone" placeholder="请输入主要联系电话" class="form-input" />
            </div>
            <div class="form-group">
              <label>备用电话</label>
              <input v-model="form.phone2" placeholder="请输入备用联系电话" class="form-input" />
            </div>
            <div class="form-group">
              <label>地址</label>
              <input v-model="form.address" placeholder="请输入详细地址" class="form-input" />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="closeModal">取消</button>
          <button type="submit" form="supplierForm" class="btn-confirm" :disabled="isSaving">
            <span v-if="isSaving">保存中...</span>
            <span v-else>保存</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="delete-modal-overlay" @click="closeDeleteModal">
      <div class="delete-modal" @click.stop>
        <div class="delete-modal-header">
          <span class="delete-icon">⚠️</span>
          <h3>确认删除供应商</h3>
        </div>
        <div class="delete-modal-body">
          <p class="delete-warning">您确定要删除以下供应商吗？此操作不可撤销！</p>
          <div class="delete-item-info">
            <div class="info-row">
              <span class="info-label">供应商名称:</span>
              <span class="info-value">{{ itemToDelete?.name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">联系人:</span>
              <span class="info-value">{{ itemToDelete?.contactPerson || '-' }}</span>
            </div>
          </div>
        </div>
        <div class="delete-modal-footer">
          <button class="btn-cancel" @click="closeDeleteModal" :disabled="isDeleting">
            取消
          </button>
          <button class="btn-confirm-delete" @click="executeDelete" :disabled="isDeleting">
            <span v-if="isDeleting">删除中...</span>
            <span v-else>确认删除</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from '@/api/request'
import Pagination from '@/components/Pagination.vue'
import RefreshButton from '@/components/RefreshButton.vue'
import { showError, showSuccess } from '@/utils/toast'
import { computed, onMounted, ref, watch } from 'vue'

const suppliers = ref([])
const searchQuery = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const isSaving = ref(false)
const form = ref({
  id: null,
  name: '',
  contactPerson: '',
  phone: '',
  phone2: '',
  address: '',
})

// 删除相关状态
const showDeleteModal = ref(false)
const itemToDelete = ref(null)
const isDeleting = ref(false)

// 过滤后的供应商数据
const filteredSuppliers = computed(() => {
  if (!searchQuery.value) return suppliers.value
  const query = searchQuery.value.toLowerCase()
  return suppliers.value.filter(supplier => 
    supplier.name?.toLowerCase().includes(query) ||
    supplier.contactPerson?.toLowerCase().includes(query) ||
    supplier.phone?.toLowerCase().includes(query)
  )
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10) // 修改此处可以调整每页显示数量

// 监听搜索条件变化，重置页码
watch(searchQuery, () => {
  currentPage.value = 1
})

// 分页后的数据
const paginatedSuppliers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredSuppliers.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredSuppliers.value.length / pageSize.value) || 1
})

// 处理页码变更
const handlePageChange = (page) => {
  currentPage.value = page
}

const fetchSuppliers = async () => {
  try {
    const data = await axios.get('/suppliers')
    suppliers.value = data || []
  } catch (error) {
    console.error('Failed to fetch suppliers', error)
    showError('获取供应商列表失败')
  }
}

const openModal = (supplier = null) => {
  if (supplier) {
    isEdit.value = true
    form.value = { ...supplier }
  } else {
    isEdit.value = false
    form.value = { id: null, name: '', contactPerson: '', phone: '', phone2: '', address: '' }
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  isSaving.value = false
}

const saveSupplier = async () => {
  if (isSaving.value) return
  isSaving.value = true
  try {
    if (isEdit.value) {
      await axios.put('/suppliers', form.value)
      showSuccess('供应商信息更新成功')
    } else {
      await axios.post('/suppliers', form.value)
      showSuccess('新增供应商成功')
    }
    closeModal()
    fetchSuppliers()
  } catch (error) {
    console.error('Failed to save supplier', error)
    showError(error.message || '保存失败')
  } finally {
    isSaving.value = false
  }
}

// 删除相关逻辑
const confirmDelete = (supplier) => {
  itemToDelete.value = supplier
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  itemToDelete.value = null
  isDeleting.value = false
}

const executeDelete = async () => {
  if (!itemToDelete.value || isDeleting.value) return
  
  isDeleting.value = true
  try {
    await axios.delete(`/suppliers/${itemToDelete.value.id}`)
    showSuccess('删除成功')
    // 从列表中移除
    suppliers.value = suppliers.value.filter(s => s.id !== itemToDelete.value.id)
    closeDeleteModal()
  } catch (error) {
    console.error('Failed to delete supplier', error)
    showError('删除失败')
  } finally {
    isDeleting.value = false
  }
}

onMounted(() => {
  fetchSuppliers()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
}

.section-title .icon {
  font-size: 1.4rem;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  padding: 10px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  min-width: 260px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
}



.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.35);
}

.table-container {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.data-table th {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.data-table tbody tr {
  transition: all 0.2s ease;
}

.data-table tbody tr:hover {
  background-color: #f8f9fa;
}

/* 单元格样式 */
.supplier-name {
  font-weight: 500;
  color: #2c3e50;
}

.contact-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 4px;
  font-size: 0.9rem;
}

.phone-cell {
  font-family: 'Courier New', monospace;
  color: #555;
}

.phone-icon {
  margin-right: 4px;
  font-size: 0.9em;
}

.phone-secondary {
  font-size: 0.85rem;
  color: #888;
  margin-top: 2px;
}

.address-cell {
  color: #7f8c8d;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-cell {
  white-space: nowrap;
}

/* 按钮样式 */
.btn-small {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  margin-right: 6px;
  transition: all 0.3s ease;
  color: white;
}

.btn-small:hover {
  transform: translateY(-2px);
}

.btn-edit {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.btn-edit:hover {
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.4);
}

.btn-delete {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

.btn-delete:hover {
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

/* 空状态 */
.empty-row {
  text-align: center;
  padding: 40px !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #adb5bd;
}

.empty-icon {
  font-size: 3rem;
  opacity: 0.5;
}

.empty-text {
  font-size: 1rem;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
}

.close-modal-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.close-modal-btn:hover {
  opacity: 1;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
  font-size: 0.9rem;
}

.required {
  color: #e74c3c;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.modal-footer {
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-confirm {
  padding: 10px 24px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.35);
}

.btn-confirm:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 删除确认弹窗样式 */
.delete-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  animation: fadeIn 0.3s ease;
}

.delete-modal {
  background: white;
  border-radius: 16px;
  width: 420px;
  max-width: 90%;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

.delete-modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
}

.delete-icon {
  font-size: 28px;
}

.delete-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.delete-modal-body {
  padding: 24px;
}

.delete-warning {
  color: #e74c3c;
  font-weight: 500;
  margin: 0 0 20px 0;
  padding: 12px 16px;
  background: #fdf2f2;
  border-radius: 8px;
  border-left: 4px solid #e74c3c;
}

.delete-item-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.info-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed #e9ecef;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 80px;
  color: #6c757d;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

.delete-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.btn-cancel {
  padding: 10px 24px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-cancel:hover:not(:disabled) {
  background: #7f8c8d;
}

.btn-cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-confirm-delete {
  padding: 10px 24px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-confirm-delete:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

.btn-confirm-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 0;
  gap: 16px;
}

.pagination span {
  font-size: 14px;
  color: #6c757d;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #495057;
  transition: all 0.2s ease;
}

.pagination button:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #c1c9d0;
  color: #2c3e50;
}

.pagination button:disabled {
  background: #f8f9fa;
  color: #adb5bd;
  cursor: not-allowed;
  border-color: #e9ecef;
}
</style>
