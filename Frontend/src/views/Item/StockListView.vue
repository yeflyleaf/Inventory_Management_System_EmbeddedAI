<!--
  模块名称: 库存列表页面
  功能描述: 展示商品库存状态，支持按分类、库存状态（低库存/零库存/积压）筛选，提供库存统计概览。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">📦</span>
        库存列表
      </h3>
      <div class="filter-group">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索商品名称或编码..."
          class="search-input"
        />
        <select v-model="categoryFilter" class="filter-select">
          <option value="">全部分类</option>
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
        <select v-model="stockFilter" class="filter-select">
          <option value="">全部库存状态</option>
          <option value="low">低库存 (&lt; {{ LOW_STOCK_THRESHOLD }})</option>
          <option value="zero">零库存</option>
          <option value="normal">正常库存</option>
          <option value="backlog">库存积压 (> {{ inventoryBacklogDays }} 天)</option>
        </select>
        <RefreshButton @click="fetchStock" :loading="loading" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card total">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalSku }}</span>
          <span class="stat-label">SKU 总数</span>
        </div>
      </div>
      <div class="stat-card stock">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalStock }}</span>
          <span class="stat-label">库存总量</span>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">⚠️</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.lowStock }}</span>
          <span class="stat-label">低库存警告</span>
        </div>
      </div>
      <div class="stat-card danger">
        <div class="stat-icon">❌</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.zeroStock }}</span>
          <span class="stat-label">缺货0库存</span>
        </div>
      </div>
    </div>

    <!-- 库存表格 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-id">编号</th>
            <th class="th-sku">商品编码</th>
            <th class="th-name">
              <button
                class="sort-btn"
                @click="sortStock('itemName')"
                :class="{ active: sortBy === 'itemName' }"
              >
                商品名称
                <span class="sort-icon" v-if="sortBy === 'itemName'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-category">
              <button
                class="sort-btn"
                @click="sortStock('category')"
                :class="{ active: sortBy === 'category' }"
              >
                分类
                <span class="sort-icon" v-if="sortBy === 'category'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-warehouse">仓库</th>
            <th class="th-stock">
              <button
                class="sort-btn"
                @click="sortStock('currentStock')"
                :class="{ active: sortBy === 'currentStock' }"
              >
                当前库存
                <span class="sort-icon" v-if="sortBy === 'currentStock'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-status">库存状态</th>
            <th class="th-action">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="loading-row">
              <div class="loading-state">
                <div class="spinner"></div>
                <span class="loading-text">加载中...</span>
              </div>
            </td>
          </tr>
          <template v-else>
            <tr v-for="(item, index) in paginatedStock" :key="item.itemId" :class="getRowClass(item)">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td :title="'点击复制: ' + item.itemSku">
                <span class="sku-badge copyable" @click="copyToClipboard(item.itemSku, '商品编码')" v-throttle>
                  {{ item.itemSku }}
                  <span class="copy-icon">📋</span>
                </span>
              </td>
              <td :title="'点击复制: ' + item.itemName">
                <span class="item-name copyable" @click="copyToClipboard(item.itemName, '商品名称')" v-throttle>
                  {{ item.itemName }}
                  <span class="copy-icon">📋</span>
                </span>
              </td>
              <td :title="item.category || '未分类'">
                <span class="category-tag">{{ item.category || '未分类' }}</span>
              </td>
              <td>{{ item.warehouseName || '默认仓库' }}</td>
              <td class="stock-cell">
                <span class="stock-number" :class="getStockClass(item.currentStock)">
                  {{ item.currentStock }}
                </span>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(item.currentStock)">
                  {{ getStatusText(item.currentStock) }}
                </span>
              </td>
              <td class="action-cell">
                <button class="btn-small btn-flow" @click="viewFlow(item)" title="查看流水" v-throttle>
                  📋 流水
                </button>
                <button class="btn-small btn-adjust" @click="adjustStock(item)" title="调整库存" v-throttle>
                  ⚙️ 调整
                </button>
                <button class="btn-small btn-delete" @click="confirmDelete(item)" title="删除商品" v-throttle>
                  🗑️ 删除
                </button>
              </td>
            </tr>
            <tr v-if="filteredStock.length === 0">
              <td colspan="8" class="empty-row">
                <div class="empty-state">
                  <span class="empty-icon">📭</span>
                  <span class="empty-text">暂无库存数据</span>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <Pagination
      :current-page="currentPage"
      :total-pages="totalPages"
      @update:currentPage="handlePageChange"
    />

    <!-- 删除库存记录确认对话框 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal-container">
        <div class="modal-header">
          <span class="modal-icon">⚠️</span>
          <h3>确认删除库存记录</h3>
        </div>
        <div class="modal-body">
          <p>您确定要删除以下商品的库存记录吗？</p>
          <div class="delete-item-info">
            <div class="info-row">
              <span class="info-label">商品编码：</span>
              <span class="info-value sku">{{ itemToDelete?.itemSku }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">商品名称：</span>
              <span class="info-value name">{{ itemToDelete?.itemName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">当前库存：</span>
              <span class="info-value stock">{{ itemToDelete?.currentStock }}</span>
            </div>
          </div>
          <p class="warning-text">
            ⚠️
            删除后该商品将从库存列表中移除，所有库存流水记录也会被清除。商品本身仍保留在商品列表中。
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="cancelDelete">取消</button>
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

import { useSettingsStore } from '@/stores/settings'
import { storeToRefs } from 'pinia'

const settingsStore = useSettingsStore()
// ⚙️ 低库存阈值设置 - 从系统设置获取
const { lowStockThreshold: LOW_STOCK_THRESHOLD, inventoryBacklogDays } = storeToRefs(settingsStore)

const stockList = ref([])
const searchQuery = ref('')
const categoryFilter = ref('')
const stockFilter = ref('')
const loading = ref(false)

// 排序相关状态
const sortBy = ref('')
const sortOrder = ref('asc') // 'asc' 或 'desc'

// 计算分类列表
const categories = computed(() => {
  const cats = new Set()
  stockList.value.forEach((item) => {
    if (item.category) cats.add(item.category)
  })
  return Array.from(cats)
})

// 过滤后的库存数据
const filteredStock = computed(() => {
  return stockList.value.filter((item) => {
    // 搜索过滤
    const matchSearch =
      !searchQuery.value ||
      item.itemName?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.itemSku?.toLowerCase().includes(searchQuery.value.toLowerCase())

    // 分类过滤
    const matchCategory = !categoryFilter.value || item.category === categoryFilter.value

    // 库存状态过滤
    let matchStock = true
    if (stockFilter.value === 'low') {
      matchStock = item.currentStock > 0 && item.currentStock < LOW_STOCK_THRESHOLD.value
    } else if (stockFilter.value === 'zero') {
      matchStock = item.currentStock === 0
    } else if (stockFilter.value === 'normal') {
      matchStock = item.currentStock >= LOW_STOCK_THRESHOLD.value
    } else if (stockFilter.value === 'backlog') {
      // 积压判定: 库存 > 0 且 (无最后变动时间 或 最后变动时间超过设定天数)
      if (item.currentStock > 0) {
        if (!item.lastMovementDate) {
          matchStock = true
        } else {
          const lastDate = new Date(item.lastMovementDate)
          const diffTime = Math.abs(new Date() - lastDate)
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) 
          matchStock = diffDays > inventoryBacklogDays.value
        }
      } else {
        matchStock = false
      }
    }

    return matchSearch && matchCategory && matchStock
  })
})

// 排序后的库存数据
const sortedStock = computed(() => {
  if (!sortBy.value) return filteredStock.value

  return [...filteredStock.value].sort((a, b) => {
    let aValue, bValue

    switch (sortBy.value) {
      case 'itemName':
        aValue = a.itemName || ''
        bValue = b.itemName || ''
        break
      case 'category':
        aValue = a.category || ''
        bValue = b.category || ''
        break
      case 'currentStock':
        aValue = parseFloat(a.currentStock) || 0
        bValue = parseFloat(b.currentStock) || 0
        break
      default:
        return 0
    }

    if (sortBy.value === 'currentStock') {
      // 数值排序
      const result = aValue - bValue
      return sortOrder.value === 'asc' ? result : -result
    } else {
      // 字符串排序
      const result = aValue.localeCompare(bValue, 'zh-CN')
      return sortOrder.value === 'asc' ? result : -result
    }
  })
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(15) // 修改此处可以调整每页显示数量

// 监听搜索条件变化，重置页码
watch([searchQuery, categoryFilter, stockFilter], () => {
  currentPage.value = 1
})

// 监听排序条件变化，重置页码
watch(sortBy, () => {
  currentPage.value = 1
})

// 分页后的数据
const paginatedStock = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedStock.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(sortedStock.value.length / pageSize.value) || 1
})

const handlePageChange = (page) => {
  currentPage.value = page
}

/**
 * 排序函数
 */
const sortStock = (field) => {
  if (sortBy.value === field) {
    // 如果点击同一个字段，切换排序顺序
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    // 如果点击不同字段，设置为升序
    sortBy.value = field
    sortOrder.value = 'asc'
  }
  // 排序后重置到第一页
  currentPage.value = 1
}

// 统计数据
const stats = computed(() => {
  const total = stockList.value
  return {
    totalSku: total.length,
    totalStock: total.reduce((sum, item) => sum + (item.currentStock || 0), 0),
    lowStock: total.filter(
      (item) => item.currentStock > 0 && item.currentStock < LOW_STOCK_THRESHOLD.value,
    ).length,
    zeroStock: total.filter((item) => item.currentStock === 0).length,
  }
})

// 获取库存数据
const fetchStock = async () => {
  loading.value = true
  try {
    const data = await axios.get('/stock')
    stockList.value = data || []

    // 刷新后重置排序状态，恢复默认显示
    sortBy.value = ''
    sortOrder.value = 'asc'
  } catch (error) {
    console.error('Failed to fetch stock', error)
  } finally {
    loading.value = false
  }
}

// 获取行样式类
const getRowClass = (item) => {
  if (item.currentStock === 0) return 'row-danger'
  if (item.currentStock < LOW_STOCK_THRESHOLD.value) return 'row-warning'
  return ''
}

// 获取库存数字样式类
const getStockClass = (stock) => {
  if (stock === 0) return 'stock-danger'
  if (stock < LOW_STOCK_THRESHOLD.value) return 'stock-warning'
  return 'stock-normal'
}

// 获取状态样式类
const getStatusClass = (stock) => {
  if (stock === 0) return 'status-danger'
  if (stock < LOW_STOCK_THRESHOLD.value) return 'status-warning'
  return 'status-normal'
}

// 获取状态文本
const getStatusText = (stock) => {
  if (stock === 0) return '无库存'
  if (stock < LOW_STOCK_THRESHOLD.value) return '库存不足'
  return '库存正常'
}

// 查看库存流水
const viewFlow = (item) => {
  router.push('/stock/flow')
}

// 调整库存
const adjustStock = (item) => {
  router.push('/stock/adjust')
}

// 删除相关状态
const showDeleteModal = ref(false)
const itemToDelete = ref(null)
const isDeleting = ref(false)

// 确认删除
const confirmDelete = (item) => {
  itemToDelete.value = item
  showDeleteModal.value = true
}

// 取消删除
const cancelDelete = () => {
  showDeleteModal.value = false
  itemToDelete.value = null
}

// 执行删除库存记录
const executeDelete = async () => {
  if (!itemToDelete.value || isDeleting.value) return

  isDeleting.value = true
  try {
    await axios.delete(`/stock/${itemToDelete.value.itemId}`)
    showToast(`商品 "${itemToDelete.value.itemName}" 的库存记录已成功删除`, 'success')
    showDeleteModal.value = false
    itemToDelete.value = null
    // 刷新列表
    await fetchStock()
  } catch (error) {
    console.error('删除库存记录失败', error)
    showToast('删除失败: ' + (error.response?.data?.message || '请稍后重试'), 'error')
  } finally {
    isDeleting.value = false
  }
}

// 复制到剪贴板
const copyToClipboard = async (text, fieldName) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast(`${fieldName}已复制: ${text}`)
  } catch (err) {
    // 降级处理：使用传统方法
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      showToast(`${fieldName}已复制: ${text}`)
    } catch (e) {
      showToast('复制失败，请手动复制', 'error')
    }
    document.body.removeChild(textArea)
  }
}

// 显示 Toast 提示
const showToast = (message, type = 'success') => {
  // 移除已存在的 toast
  const existingToast = document.querySelector('.copy-toast')
  if (existingToast) {
    existingToast.remove()
  }

  const toast = document.createElement('div')
  toast.className = `copy-toast copy-toast-${type}`
  toast.innerHTML = `<span class="toast-icon">${type === 'success' ? '✅' : '❌'}</span> ${message}`
  document.body.appendChild(toast)

  // 动画显示
  requestAnimationFrame(() => {
    toast.classList.add('show')
  })

  // 2秒后移除
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => toast.remove(), 300)
  }, 2000)
}

onMounted(() => {
  settingsStore.fetchSettings()
  fetchStock()
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
  flex-wrap: wrap;
}

.search-input {
  padding: 10px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  min-width: 220px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
}

.filter-select {
  padding: 10px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #3498db;
}



/* 统计卡片 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #dee2e6;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.total {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-color: #90caf9;
}

.stat-card.stock {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-color: #a5d6a7;
}

.stat-card.warning {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-color: #ffcc80;
}

.stat-card.danger {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-color: #ef9a9a;
}

.stat-icon {
  font-size: 2rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
}

.stat-label {
  font-size: 0.875rem;
  color: #7f8c8d;
  font-weight: 500;
}

/* 表格容器 */
.table-container {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid #e9ecef;
  position: relative;
  min-height: 400px; /* 保持最小高度防止抖动 */
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed; /* 固定表格布局，防止内容撑开 */
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
  position: sticky; /* 固定表头 */
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 排序按钮样式 */
.sort-btn {
  background: none;
  border: none;
  color: inherit;
  font-weight: inherit;
  font-size: inherit;
  text-transform: inherit;
  letter-spacing: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  width: 100%;
  text-align: left;
  transition: color 0.2s ease;
}

.sort-btn:hover {
  color: #3498db;
}

.sort-btn.active {
  color: #3498db;
  font-weight: 700;
}

.sort-icon {
  font-size: 0.8em;
  opacity: 0.8;
}

/* 固定各列宽度 */
/* 固定各列宽度 */
.th-id {
  width: 60px;
  min-width: 60px;
}

.th-sku {
  width: 180px;
  min-width: 180px;
}

.th-name {
  width: 200px;
  min-width: 200px;
}

.th-category {
  width: 100px;
  min-width: 100px;
}

.th-warehouse {
  width: 100px;
  min-width: 100px;
}

.th-stock {
  width: 100px;
  min-width: 100px;
}

.th-status {
  width: 100px;
  min-width: 100px;
}

.th-action {
  width: 200px;
  min-width: 200px;
}

.data-table tbody tr {
  transition: all 0.2s ease;
}

.data-table tbody tr:hover {
  background-color: #f8f9fa;
}

.row-warning {
  background-color: #fff8e1 !important;
}

.row-danger {
  background-color: #ffebee !important;
}

/* 徽章样式 */
.sku-badge {
  display: inline-block;
  padding: 4px 10px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  font-family: 'Courier New', monospace;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-name {
  font-weight: 500;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-flex;
  display: inline-flex;
  align-items: center;
}

/* 加载状态样式 */
.loading-row {
  text-align: center;
  padding: 60px !important;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #3498db;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(52, 152, 219, 0.1);
  border-left-color: #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 14px;
  font-weight: 500;
  color: #7f8c8d;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}


/* 可复制元素样式 */
.copyable {
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.copyable:hover {
  transform: scale(1.02);
}

.copyable .copy-icon {
  opacity: 0;
  font-size: 0.75rem;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.copyable:hover .copy-icon {
  opacity: 1;
}

.sku-badge.copyable {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.sku-badge.copyable:hover {
  background: #bbdefb;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.2);
}

.item-name.copyable:hover {
  color: #1976d2;
  background: rgba(227, 242, 253, 0.5);
  border-radius: 4px;
  padding: 2px 6px;
  margin: -2px -6px;
}

/* 库存数字 */
.stock-cell {
  text-align: center;
}

.stock-number {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 700;
  min-width: 50px;
}

.stock-normal {
  background: #e8f5e9;
  color: #2e7d32;
}

.stock-warning {
  background: #fff3e0;
  color: #ef6c00;
}

.stock-danger {
  background: #ffebee;
  color: #c62828;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-normal {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  color: white;
}

.status-warning {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  color: white;
}

.status-danger {
  background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
  color: white;
}

/* 操作按钮 */
.action-cell {
  white-space: nowrap;
}

.btn-small {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  margin-right: 6px;
  transition: all 0.3s ease;
}

.btn-flow {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-flow:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-adjust {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.btn-adjust:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(17, 153, 142, 0.4);
}

.btn-delete {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
}

.btn-delete:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

/* 删除确认对话框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 450px;
  width: 90%;
  overflow: hidden;
  animation: slideUp 0.3s ease;
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

.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
}

.modal-header .modal-icon {
  font-size: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.modal-body {
  padding: 24px;
}

.modal-body > p {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 1rem;
}

.delete-item-info {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #7f8c8d;
  font-size: 0.9rem;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  font-weight: 600;
  color: #2c3e50;
}

.info-value.sku {
  font-family: 'Courier New', monospace;
  background: #e3f2fd;
  color: #1976d2;
  padding: 2px 8px;
  border-radius: 4px;
}

.info-value.stock {
  color: #e74c3c;
}

.warning-text {
  color: #e74c3c;
  font-size: 0.875rem;
  background: #ffebee;
  padding: 12px;
  border-radius: 8px;
  margin: 0;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: #f8f9fa;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 10px 24px;
  border: 2px solid #bdc3c7;
  background: white;
  color: #7f8c8d;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #ecf0f1;
  border-color: #95a5a6;
  color: #2c3e50;
}

.btn-confirm-delete {
  padding: 10px 24px;
  border: none;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-confirm-delete:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

.btn-confirm-delete:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 空状态 */
.empty-row td {
  padding: 60px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 3rem;
}

.empty-text {
  color: #95a5a6;
  font-size: 1.1rem;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-group {
    width: 100%;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .stats-container {
    grid-template-columns: 1fr 1fr;
  }
}

/* Toast 提示样式 */
:global(.copy-toast) {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%) translateY(-100px);
  padding: 12px 24px;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  z-index: 10000;
  display: flex;
  align-items: center;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

:global(.copy-toast.show) {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

:global(.copy-toast-success) {
  background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

:global(.copy-toast-error) {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

:global(.copy-toast .toast-icon) {
  font-size: 1rem;
}
</style>
