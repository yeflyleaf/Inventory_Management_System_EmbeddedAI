<!--
  模块名称: 销售订单列表页面
  功能描述: 展示销售订单列表，支持搜索、删除和跳转至出库操作。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">🛍️</span>
        销售订单
      </h3>
      <div class="filter-group">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索订单号、客户或状态..." 
          class="search-input"
        />
        <RefreshButton @click="fetchOrders" />
        <button class="btn-primary" @click="$router.push('/sales/add')">
          ➕ 新建销售
        </button>
        <button class="btn-danger" @click="confirmClearAll">
          🗑️ 清空订单
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-id">订单号</th>
            <th class="th-customer">客户</th>
            <th class="th-amount">总金额</th>
            <th class="th-status">状态</th>
            <th class="th-time">创建时间</th>
            <th class="th-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in paginatedOrders" :key="order.id">
            <td class="order-no">
              <span class="code-badge">{{ order.orderNo }}</span>
            </td>
            <td class="customer-name">{{ order.customerName }}</td>
            <td class="amount-cell">¥{{ order.totalAmount }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
            </td>
            <td class="time-cell">{{ formatDate(order.createdAt) }}</td>
            <td class="action-cell">
              <button v-if="order.status === 'CREATED'" class="btn-small btn-outbound" @click="outbound(order.id)" title="出库">
                📤 出库
              </button>
              <button class="btn-small btn-delete" @click="confirmDelete(order)" title="删除">
                🗑️ 删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredOrders.length === 0">
            <td colspan="6" class="empty-row">
              <div class="empty-state">
                <span class="empty-icon">📭</span>
                <span class="empty-text">暂无销售订单</span>
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

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="delete-modal-overlay" @click="closeDeleteModal">
      <div class="delete-modal" @click.stop>
        <div class="delete-modal-header">
          <span class="delete-icon">⚠️</span>
          <h3>确认删除订单</h3>
        </div>
        <div class="delete-modal-body">
          <p class="delete-warning">您确定要删除以下销售订单吗？此操作不可撤销！</p>
          <div class="delete-item-info">
            <div class="info-row">
              <span class="info-label">订单号:</span>
              <span class="info-value">{{ itemToDelete?.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">客户:</span>
              <span class="info-value">{{ itemToDelete?.customerName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">总金额:</span>
              <span class="info-value">¥{{ itemToDelete?.totalAmount }}</span>
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

    <!-- 清空确认弹窗 -->
    <div v-if="showClearModal" class="delete-modal-overlay" @click="closeClearModal">
      <div class="delete-modal" @click.stop>
        <div class="delete-modal-header">
          <span class="delete-icon">🔥</span>
          <h3>确认清空所有订单</h3>
        </div>
        <div class="delete-modal-body">
          <p class="delete-warning">⚠️ 警告：您确定要清空所有销售订单吗？</p>
          <p style="color: #666; margin-bottom: 10px;">此操作将永久删除所有销售订单记录，且<strong>不可恢复</strong>！</p>
        </div>
        <div class="delete-modal-footer">
          <button class="btn-cancel" @click="closeClearModal" :disabled="isClearing">
            取消
          </button>
          <button class="btn-confirm-delete" @click="executeClear" :disabled="isClearing">
            <span v-if="isClearing">清空中...</span>
            <span v-else>确认清空</span>
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
import { useRouter } from 'vue-router'

const router = useRouter()
const orders = ref([])
const searchQuery = ref('')

// 删除/清空相关状态
const showDeleteModal = ref(false)
const showClearModal = ref(false)
const itemToDelete = ref(null)
const isDeleting = ref(false)
const isClearing = ref(false)

// 过滤后的订单数据
const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  const query = searchQuery.value.toLowerCase()
  return orders.value.filter(order => 
    order.orderNo?.toLowerCase().includes(query) ||
    order.customerName?.toLowerCase().includes(query) ||
    getStatusText(order.status).includes(query) ||
    order.status?.toLowerCase().includes(query)
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
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize.value) || 1
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const fetchOrders = async () => {
  try {
    const response = await axios.get('/sale-orders')
    orders.value = response || []
  } catch (error) {
    console.error('Failed to fetch orders', error)
    showError('获取订单列表失败')
  }
}

const getStatusClass = (status) => {
  return status === 'SHIPPED' ? 'status-shipped' : 'status-created'
}

const getStatusText = (status) => {
  const map = {
    'CREATED': '待出库',
    'SHIPPED': '已发货'
  }
  return map[status] || status
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const outbound = (id) => {
  router.push({ name: '销售出库', query: { orderId: id } })
}

// 删除相关逻辑
const confirmDelete = (order) => {
  itemToDelete.value = order
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
    await axios.delete(`/sale-orders/${itemToDelete.value.id}`)
    showSuccess('删除成功')
    await fetchOrders()
    closeDeleteModal()
  } catch (error) {
    console.error('Failed to delete order', error)
    showError('删除失败')
  } finally {
    isDeleting.value = false
  }
}

// 清空相关逻辑
const confirmClearAll = () => {
  showClearModal.value = true
}

const closeClearModal = () => {
  showClearModal.value = false
  isClearing.value = false
}

const executeClear = async () => {
  if (isClearing.value) return
  
  isClearing.value = true
  try {
    await axios.delete('/sale-orders')
    showSuccess('清空成功')
    await fetchOrders()
    closeClearModal()
  } catch (error) {
    console.error('Failed to clear orders', error)
    showError('清空失败')
  } finally {
    isClearing.value = false
  }
}

onMounted(() => {
  fetchOrders()
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
  border-color: #8e44ad;
  box-shadow: 0 0 0 3px rgba(142, 68, 173, 0.15);
}



.btn-primary {
  padding: 10px 20px;
  background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
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
  box-shadow: 0 4px 12px rgba(142, 68, 173, 0.35);
}

.btn-danger {
  padding: 10px 20px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
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

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.35);
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
.code-badge {
  display: inline-block;
  padding: 4px 8px;
  background: #f3e5f5;
  color: #8e44ad;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

.customer-name {
  font-weight: 500;
  color: #2c3e50;
}

.amount-cell {
  font-weight: 600;
  color: #e67e22;
}

.time-cell {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-created {
  background: #fff3e0;
  color: #ef6c00;
}

.status-shipped {
  background: #e8f5e9;
  color: #2e7d32;
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

.btn-outbound {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.btn-outbound:hover {
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

/* 弹窗通用样式 */
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
