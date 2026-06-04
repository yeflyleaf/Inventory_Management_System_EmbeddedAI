<!--
  模块名称: 库存流水页面
  功能描述: 查看库存变动记录，支持按商品、类型和关联单号搜索。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">📋</span>
        库存流水记录
      </h3>
      <div class="filter-group">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索商品名称、类型或单号..." 
          class="search-input"
        />
        <RefreshButton @click="fetchFlows" />
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th class="th-id">编号</th>
            <th class="th-name">商品名称</th>
            <th class="th-amount">变动数量</th>
            <th class="th-type">类型</th>
            <th class="th-ref-type">关联类型</th>
            <th class="th-ref-id">关联编号</th>
            <th class="th-time">时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="flow in paginatedFlows" :key="flow.id">
            <td>{{ flow.id }}</td>
            <td class="item-name" :title="flow.itemName">{{ flow.itemName }}</td>
            <td :class="getAmountClass(flow.changeAmount)">
              <span class="amount-badge">
                {{ flow.changeAmount > 0 ? '+' : '' }}{{ flow.changeAmount }}
              </span>
            </td>
            <td>
              <span class="type-tag">{{ flow.changeType }}</span>
            </td>
            <td>{{ flow.refType }}</td>
            <td class="ref-id">{{ flow.refId }}</td>
            <td class="time-cell">{{ formatDate(flow.createdAt) }}</td>
          </tr>
          <tr v-if="filteredFlows.length === 0">
            <td colspan="7" class="empty-row">
              <div class="empty-state">
                <span class="empty-icon">📭</span>
                <span class="empty-text">暂无流水记录</span>
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
  </div>
</template>

<script setup>
import axios from '@/api/request'
import Pagination from '@/components/Pagination.vue'
import RefreshButton from '@/components/RefreshButton.vue'
import { computed, onMounted, ref, watch } from 'vue'

const flows = ref([])
const searchQuery = ref('')

// 获取流水数据
const fetchFlows = async () => {
  try {
    const response = await axios.get('/stock/flow')
    flows.value = response || []
  } catch (error) {
    console.error('Failed to fetch stock flow', error)
  }
}

// 过滤后的流水数据
const filteredFlows = computed(() => {
  if (!searchQuery.value) return flows.value
  const query = searchQuery.value.toLowerCase()
  return flows.value.filter(flow => 
    flow.itemName?.toLowerCase().includes(query) ||
    flow.changeType?.toLowerCase().includes(query) ||
    flow.refType?.toLowerCase().includes(query) ||
    String(flow.refId).includes(query)
  )
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(15) // 修改此处可以调整每页显示数量

// 监听搜索条件变化，重置页码
watch(searchQuery, () => {
  currentPage.value = 1
})

// 分页后的数据
const paginatedFlows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFlows.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredFlows.value.length / pageSize.value) || 1
})

// 处理页码变更
const handlePageChange = (page) => {
  currentPage.value = page
}

// 获取数量样式
const getAmountClass = (amount) => {
  return amount > 0 ? 'amount-positive' : 'amount-negative'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
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
  fetchFlows()
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

.item-name {
  font-weight: 500;
  color: #2c3e50;
}

.amount-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.amount-positive .amount-badge {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.amount-negative .amount-badge {
  background-color: #ffebee;
  color: #c62828;
}

.type-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #f0f2f5;
  color: #555;
  border-radius: 4px;
  font-size: 0.85rem;
}

.ref-id {
  font-family: 'Courier New', monospace;
  color: #666;
}

.time-cell {
  color: #888;
  font-size: 0.9rem;
  white-space: nowrap;
}

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
