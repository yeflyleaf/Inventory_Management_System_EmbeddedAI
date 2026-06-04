<!--
  模块名称: 采购入库页面
  功能描述: 处理采购订单的入库操作，确认实际入库数量。
-->
<template>
  <div class="page-container">
    <h2>采购入库</h2>
    <div v-if="order">
      <p><strong>订单号:</strong> {{ order.orderNo }}</p>
      <p><strong>供应商:</strong> {{ order.supplierName }}</p>

      <table class="data-table">
        <thead>
          <tr>
            <th>商品</th>
            <th>订购数量</th>
            <th>入库数量</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in order.items" :key="item.id">
            <td>{{ item.itemName }}</td>
            <td>{{ item.qty }}</td>
            <td>{{ item.qty }} (自动填充)</td>
          </tr>
        </tbody>
      </table>

      <div class="actions">
        <button @click="confirmInbound" class="btn-primary">确认入库</button>
      </div>
    </div>
    <div v-else>加载中...</div>
  </div>
</template>

<script setup>
import axios from '@/api/request'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccess, showError } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const order = ref(null)

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    try {
      const response = await axios.get(`/purchase-orders/${orderId}`)
      order.value = response
    } catch (error) {
      console.error('Failed to load order', error)
    }
  }
})

const confirmInbound = async () => {
  try {
    await axios.post(`/purchase-orders/${order.value.id}/finish`)
    showSuccess('入库成功')
    router.push('/purchase')
  } catch (error) {
    console.error('Inbound failed', error)
    showError('入库失败')
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
  background-color: #27ae60;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
