<!--
  模块名称: 商品列表页面
  功能描述: 展示商品列表，支持搜索、排序、分页、查看图片和删除商品。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">📦</span>
        商品列表
      </h3>
      <div class="filter-group">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索商品名称、编码或分类..."
          class="search-input"
        />
        <RefreshButton @click="fetchItems" :loading="loading" />
        <button class="btn-primary" @click="$router.push('/items/add')" v-throttle>➕ 新增商品</button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-id">编号</th>
            <th class="th-image">商品图片</th>
            <th class="th-sku">商品编码</th>
            <th class="th-name">
              <button
                class="sort-btn"
                @click="sortItems('name')"
                :class="{ active: sortBy === 'name' }"
              >
                商品名称
                <span class="sort-icon" v-if="sortBy === 'name'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-category">
              <button
                class="sort-btn"
                @click="sortItems('category')"
                :class="{ active: sortBy === 'category' }"
              >
                分类
                <span class="sort-icon" v-if="sortBy === 'category'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-unit">
              <button
                class="sort-btn"
                @click="sortItems('unit')"
                :class="{ active: sortBy === 'unit' }"
              >
                单位
                <span class="sort-icon" v-if="sortBy === 'unit'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
            <th class="th-price">
              <button
                class="sort-btn"
                @click="sortItems('salePrice')"
                :class="{ active: sortBy === 'salePrice' }"
              >
                销售价
                <span class="sort-icon" v-if="sortBy === 'salePrice'">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </button>
            </th>
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
            <tr v-for="(item, index) in paginatedItems" :key="item.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="image-cell">
                <div v-if="hasImages(item)" class="product-thumbnail" @click="viewImage(item)">
                  <img :src="getImageUrl(getFirstImage(item))" :alt="item.name" />
                  <div class="view-overlay">
                    <span class="view-icon">🔍</span>
                  </div>
                  <span v-if="getImageCount(item) > 1" class="image-count-badge">
                    {{ getImageCount(item) }}
                  </span>
                </div>
                <span v-else class="no-image">暂无图片</span>
              </td>
              <td class="sku-cell" :title="'点击复制: ' + item.sku">
                <span class="sku-badge copyable" @click="copyToClipboard(item.sku, '商品编码')" v-throttle>
                  {{ item.sku }}
                  <span class="copy-icon">📋</span>
                </span>
              </td>
              <td class="name-cell" :title="'点击复制: ' + item.name">
                <span class="item-name copyable" @click="copyToClipboard(item.name, '商品名称')" v-throttle>
                  {{ item.name }}
                  <span class="copy-icon">📋</span>
                </span>
              </td>
              <td>
                <span class="category-tag">{{ item.category || '未分类' }}</span>
              </td>
              <td>{{ item.unit }}</td>
              <td class="price-cell">¥{{ item.salePrice }}</td>
              <td class="action-cell">
                <button
                  class="btn-small btn-edit"
                  @click="$router.push(`/items/edit/${item.id}`)"
                  title="编辑"
                  v-throttle
                >
                  ✏️ 编辑
                </button>
                <button class="btn-small btn-delete" @click="confirmDelete(item)" title="删除" v-throttle>
                  🗑️ 删除
                </button>
              </td>
            </tr>
            <tr v-if="filteredItems.length === 0">
              <td colspan="8" class="empty-row">
                <div class="empty-state">
                  <span class="empty-icon">📭</span>
                  <span class="empty-text">暂无商品数据</span>
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

    <!-- 图片查看弹窗（支持多图片轮播） -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedItem?.name }}</h3>
          <button class="close-modal-btn" @click="closeImageModal">×</button>
        </div>
        <div 
          class="modal-body"
          @touchstart="handleTouchStart"
          @touchend="handleTouchEnd"
          @mousedown="handleMouseDown"
          @mouseup="handleMouseUp"
          @mouseleave="handleMouseUp"
        >
          <button 
            class="nav-btn prev-btn" 
            @click.stop="prevImageInModal" 
            v-if="selectedItemImages.length > 1"
          >‹</button>
          <transition name="fade" mode="out-in">
            <img
              v-if="selectedItemImages.length > 0"
              :key="currentImageIndex"
              :src="getImageUrl(selectedItemImages[currentImageIndex])"
              :alt="selectedItem?.name"
              class="modal-image"
              draggable="false"
            />
          </transition>
          <button 
            class="nav-btn next-btn" 
            @click.stop="nextImageInModal" 
            v-if="selectedItemImages.length > 1"
          >›</button>
        </div>
        <div v-if="selectedItemImages.length > 1" class="image-indicator">
          {{ currentImageIndex + 1 }} / {{ selectedItemImages.length }}
        </div>
        <div class="modal-footer">
          <div class="item-info">
            <span class="info-item"><strong>编码:</strong> {{ selectedItem?.sku }}</span>
            <span class="info-item"
              ><strong>分类:</strong> {{ selectedItem?.category || '-' }}</span
            >
            <span class="info-item"><strong>单位:</strong> {{ selectedItem?.unit }}</span>
            <span class="info-item"><strong>售价:</strong> ¥{{ selectedItem?.salePrice }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="delete-modal-overlay" @click="closeDeleteModal">
      <div class="delete-modal" @click.stop>
        <div class="delete-modal-header">
          <span class="delete-icon">⚠️</span>
          <h3>确认删除商品</h3>
        </div>
        <div class="delete-modal-body">
          <p class="delete-warning">您确定要删除以下商品吗？此操作不可撤销！</p>
          <div class="delete-item-info">
            <div class="info-row">
              <span class="info-label">商品编码:</span>
              <span class="info-value">{{ itemToDelete?.sku }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">商品名称:</span>
              <span class="info-value">{{ itemToDelete?.name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">分类:</span>
              <span class="info-value">{{ itemToDelete?.category || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">销售价:</span>
              <span class="info-value">¥{{ itemToDelete?.salePrice }}</span>
            </div>
          </div>
        </div>
        <div class="delete-modal-footer">
          <button class="btn-cancel" @click="closeDeleteModal" :disabled="isDeleting">取消</button>
          <button class="btn-confirm-delete" @click="deleteItem" :disabled="isDeleting">
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
import { useRouter } from 'vue-router'

const router = useRouter()
const items = ref([])
const searchQuery = ref('')
const showImageModal = ref(false)
const selectedItem = ref(null)
const loading = ref(false)

// 排序相关状态
const sortBy = ref('')
const sortOrder = ref('asc') // 'asc' 或 'desc'

// 删除相关状态
const showDeleteModal = ref(false)
const itemToDelete = ref(null)
const isDeleting = ref(false)

// 过滤后的商品数据
const filteredItems = computed(() => {
  if (!searchQuery.value) return items.value
  const query = searchQuery.value.toLowerCase()
  return items.value.filter(
    (item) =>
      item.name?.toLowerCase().includes(query) ||
      item.sku?.toLowerCase().includes(query) ||
      item.category?.toLowerCase().includes(query),
  )
})

// 排序后的商品数据
const sortedItems = computed(() => {
  if (!sortBy.value) return filteredItems.value

  return [...filteredItems.value].sort((a, b) => {
    let aValue, bValue

    switch (sortBy.value) {
      case 'name':
        aValue = a.name || ''
        bValue = b.name || ''
        break
      case 'category':
        aValue = a.category || ''
        bValue = b.category || ''
        break
      case 'unit':
        aValue = a.unit || ''
        bValue = b.unit || ''
        break
      case 'salePrice':
        aValue = parseFloat(a.salePrice) || 0
        bValue = parseFloat(b.salePrice) || 0
        break
      default:
        return 0
    }

    if (sortBy.value === 'salePrice') {
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
const pageSize = ref(10) // 修改此处可以调整每页显示数量

// 监听搜索条件变化，重置页码
watch(searchQuery, () => {
  currentPage.value = 1
})

// 监听排序条件变化，重置页码
watch(sortBy, () => {
  currentPage.value = 1
})

// 分页后的数据
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedItems.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(sortedItems.value.length / pageSize.value) || 1
})

// 处理页码变更
const handlePageChange = (page) => {
  currentPage.value = page
}

/**
 * 排序函数
 */
const sortItems = (field) => {
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

/**
 * 获取完整的图片URL
 */
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  
  let baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  
  // 调试日志：查看实际的环境变量和路径
  console.log('[Image Debug] Raw BaseURL:', import.meta.env.VITE_API_BASE_URL);
  console.log('[Image Debug] Processed BaseURL (before fix):', baseUrl);
  console.log('[Image Debug] Image Path:', path);

  // 修复：移除 /api 后缀，确保指向服务器根目录的 /uploads
  if (baseUrl.endsWith('/api')) {
    baseUrl = baseUrl.slice(0, -4)
  }
  
  const finalUrl = `${baseUrl}${path}`;
  console.log('[Image Debug] Final URL:', finalUrl);
  
  return finalUrl
}

/**
 * 判断商品是否有图片
 */
const hasImages = (item) => {
  return item.imageUrls && item.imageUrls.length > 0
}

/**
 * 获取第一张图片URL（用于缩略图显示）
 */
const getFirstImage = (item) => {
  if (item.imageUrls && item.imageUrls.length > 0) {
    return item.imageUrls[0]
  }
  return null
}

/**
 * 获取商品图片数量
 */
const getImageCount = (item) => {
  return item.imageUrls ? item.imageUrls.length : 0
}

/**
 * 当前弹窗显示的图片索引
 */
const currentImageIndex = ref(0)

/**
 * 选中商品的图片列表
 */
const selectedItemImages = computed(() => {
  if (!selectedItem.value || !selectedItem.value.imageUrls) {
    return []
  }
  return selectedItem.value.imageUrls
})

/**
 * 查看商品图片
 */
const viewImage = (item) => {
  if (!hasImages(item)) return
  selectedItem.value = item
  currentImageIndex.value = 0
  showImageModal.value = true
}

/**
 * 关闭图片弹窗
 */
const closeImageModal = () => {
  showImageModal.value = false
  selectedItem.value = null
  currentImageIndex.value = 0
}

/**
 * 上一张图片
 */
const prevImageInModal = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else {
    currentImageIndex.value = selectedItemImages.value.length - 1
  }
}

/**
 * 下一张图片
 */
const nextImageInModal = () => {
  if (currentImageIndex.value < selectedItemImages.value.length - 1) {
    currentImageIndex.value++
  } else {
    currentImageIndex.value = 0
  }
}

// 滑动/拖拽相关逻辑
const touchStartX = ref(0)
const touchEndX = ref(0)
const isDragging = ref(false)
const startX = ref(0)

const handleTouchStart = (e) => {
  touchStartX.value = e.changedTouches[0].screenX
}

const handleTouchEnd = (e) => {
  touchEndX.value = e.changedTouches[0].screenX
  handleSwipe(touchStartX.value, touchEndX.value)
}

const handleMouseDown = (e) => {
  isDragging.value = true
  startX.value = e.clientX
}

const handleMouseUp = (e) => {
  if (!isDragging.value) return
  isDragging.value = false
  const endX = e.clientX
  handleSwipe(startX.value, endX)
}

const handleSwipe = (start, end) => {
  const threshold = 50
  if (end < start - threshold) {
    // 向左滑动，显示下一张
    nextImageInModal()
  } else if (end > start + threshold) {
    // 向右滑动，显示上一张
    prevImageInModal()
  }
}

/**
 * 复制到剪贴板
 */
const copyToClipboard = async (text, fieldName) => {
  try {
    await navigator.clipboard.writeText(text)
    showSuccess(`${fieldName}已复制: ${text}`)
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
      showSuccess(`${fieldName}已复制: ${text}`)
    } catch (e) {
      showError('复制失败，请手动复制')
    }
    document.body.removeChild(textArea)
  }
}

/**
 * 打开删除确认弹窗
 */
const confirmDelete = (item) => {
  itemToDelete.value = item
  showDeleteModal.value = true
}

/**
 * 关闭删除确认弹窗
 */
const closeDeleteModal = () => {
  showDeleteModal.value = false
  itemToDelete.value = null
  isDeleting.value = false
}

/**
 * 执行删除商品操作
 */
const deleteItem = async () => {
  if (!itemToDelete.value || isDeleting.value) return

  isDeleting.value = true
  try {
    await axios.delete(`/products/${itemToDelete.value.id}`)
    // 从列表中移除已删除的商品
    items.value = items.value.filter((item) => item.id !== itemToDelete.value.id)
    closeDeleteModal()
    // 显示成功提示
    showSuccess('商品删除成功！')
  } catch (error) {
    console.error('Failed to delete item', error)
    showError('删除失败，请稍后重试！')
  } finally {
    isDeleting.value = false
  }
}

const fetchItems = async () => {
  loading.value = true
  try {
    // 响应拦截器已经提取了 data，直接使用返回值
    const data = await axios.get('/products')
    items.value = data || []

    // 刷新后重置排序状态，恢复默认显示
    sortBy.value = ''
    sortOrder.value = 'asc'
  } catch (error) {
    console.error('Failed to fetch items', error)
    showError('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchItems()
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
  position: relative;
  min-height: 400px; /* 保持最小高度防止抖动 */
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed; /* 固定表格布局 */
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
  position: sticky; /* 固定表头 */
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 固定各列宽度，防止内容撑开导致抖动 */
.th-id {
  width: 60px;
  min-width: 60px;
}

.th-image {
  width: 80px;
  min-width: 80px;
}

.th-sku {
  width: 180px;
  min-width: 180px;
}

.th-name {
  width: 250px;
  min-width: 250px;
}

.th-category {
  width: 120px;
  min-width: 120px;
}

.th-unit {
  width: 80px;
  min-width: 80px;
}

.th-price {
  width: 100px;
  min-width: 100px;
}

.th-action {
  width: 180px;
  min-width: 180px;
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

.data-table tbody tr {
  transition: all 0.2s ease;
}

.data-table tbody tr:hover {
  background-color: #f8f9fa;
}

/* 单元格样式 */
.image-cell {
  width: 80px;
  text-align: center;
}

.sku-cell {
  font-family: 'Courier New', monospace;
}

.sku-badge {
  display: inline-block;
  padding: 4px 8px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  margin-left: 4px;
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

.item-name.copyable {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-name.copyable:hover {
  color: #1976d2;
  background: rgba(227, 242, 253, 0.5);
  border-radius: 4px;
  padding: 2px 6px;
  margin: -2px -6px;
}

.name-cell {
  font-weight: 500;
  color: #2c3e50;
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

.price-cell {
  font-weight: 600;
  color: #e67e22;
}

.action-cell {
  white-space: nowrap;
}

.product-thumbnail {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.product-thumbnail:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.product-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.view-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-thumbnail:hover .view-overlay {
  opacity: 1;
}

.view-icon {
  font-size: 20px;
}

.no-image {
  color: #95a5a6;
  font-size: 12px;
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

/* 图片查看弹窗 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 90%;
  max-height: 90%;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
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
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-modal-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-modal-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.modal-body {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #f8f9fa;
}

.modal-image {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.modal-footer {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e9ecef;
}

.item-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.info-item {
  font-size: 14px;
  color: #495057;
}

.info-item strong {
  color: #2c3e50;
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

/* 图片计数徽章 */
.image-count-badge {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

/* 弹窗中的导航按钮 */
.modal-body {
  position: relative;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: none;
  cursor: pointer;
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.nav-btn:hover {
  background: white;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.prev-btn {
  left: 10px;
}

.next-btn {
  right: 10px;
}

/* 图片指示器 */
.image-indicator {
  text-align: center;
  padding: 8px 16px;
  background: #f8f9fa;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}


/* 图片切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.modal-body {
  user-select: none; /* 防止拖拽时选中文字 */
  cursor: grab;
  touch-action: pan-y; /* 允许垂直滚动，拦截水平滑动 */
}

.modal-body:active {
  cursor: grabbing;
}
</style>
