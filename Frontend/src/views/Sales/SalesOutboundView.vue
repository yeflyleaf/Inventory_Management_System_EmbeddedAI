<!--
  模块名称: 销售出库页面
  功能描述: 处理销售订单的出库操作，检查库存是否充足并确认出库。
-->
<template>
  <div class="page-container">
    <h2>销售出库</h2>
    <div v-if="order">
      <p><strong>订单号:</strong> {{ order.orderNo }}</p>
      <p><strong>客户:</strong> {{ order.customerName }}</p>

      <table class="data-table">
        <thead>
          <tr>
            <th>商品</th>
            <th>订购数量</th>
            <th>当前库存</th>
            <th>出库数量</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in order.items" :key="item.id">
            <td>{{ item.itemName }}</td>
            <td>{{ item.qty }}</td>
            <td>
              <span :class="{ 'text-danger': getStock(item.itemId) < item.qty }">
                {{ getStock(item.itemId) }}
              </span>
            </td>
            <td>{{ item.qty }} (自动填充)</td>
            <td>
              <span v-if="getStock(item.itemId) < item.qty" class="text-danger">库存不足</span>
              <span v-else class="text-success">可出库</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="actions">
        <button 
          @click="confirmOutbound" 
          class="btn-primary" 
        >
          确认出库
        </button>
      </div>
    </div>
    <div v-else>加载中...</div>
  </div>
</template>

<script setup>
import axios from '@/api/request'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccess, showError } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const stocks = ref([])

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    try {
      const [orderRes, stockRes] = await Promise.all([
        axios.get(`/sale-orders/${orderId}`),
        axios.get('/stock')
      ])
      order.value = orderRes
      stocks.value = stockRes || []
    } catch (error) {
      console.error('Failed to load data', error)
    }
  }
})

const getStock = (itemId) => {
  const stockItem = stocks.value.find(s => s.itemId === itemId)
  return stockItem ? stockItem.currentStock : 0
}

const canOutbound = computed(() => {
  if (!order.value || !order.value.items) return false
  return order.value.items.every(item => getStock(item.itemId) >= item.qty)
})

const confirmOutbound = async () => {
  if (!canOutbound.value) {
    showError('当前库存不足，无法出库')
    return
  }
  
  try {
    await axios.post(`/sale-orders/${order.value.id}/ship`)
    showSuccess('出库成功')
    router.push('/sales')
  } catch (error) {
    console.error('Outbound failed', error)
    showError('出库失败: ' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.data-table th,
.data-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
}

.btn-primary {
  background-color: #8e44ad;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #732d91;
}

.text-danger {
  color: #e74c3c;
  font-weight: bold;
}

.text-success {
  color: #27ae60;
  font-weight: bold;
}
</style>
