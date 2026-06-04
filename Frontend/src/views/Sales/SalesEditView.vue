<!--
  模块名称: 销售订单编辑/新增页面
  功能描述: 创建新的销售订单，选择客户和商品，计算总金额。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">🛍️</span>
        新建销售订单
      </h3>
      <button class="btn-back" @click="$router.back()">
        ↩️ 返回
      </button>
    </div>

    <div class="content-wrapper">
      <!-- 顶部信息卡片 -->
      <div class="info-card">
        <div class="form-group">
          <label class="form-label">选择客户 <span class="required">*</span></label>
          <select v-model="form.customerId" class="form-select">
            <option value="" disabled>请选择客户</option>
            <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
      </div>

      <!-- 商品明细表格 -->
      <div class="items-section">
        <div class="section-header">
          <h4>商品明细</h4>
          <button @click="addItem" class="btn-add-item">
            ➕ 添加商品
          </button>
        </div>
        
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 40%;">商品</th>
                <th style="width: 20%;">销售价 (¥)</th>
                <th style="width: 20%;">数量</th>
                <th style="width: 10%;">小计 (¥)</th>
                <th style="width: 10%;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in form.items" :key="index">
                <td>
                  <select v-model="item.itemId" @change="onItemSelect(item)" class="form-select-small">
                    <option value="" disabled>请选择商品</option>
                    <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </td>
                <td>
                  <input type="number" v-model.number="item.salePrice" class="form-input-small" min="0" step="0.01" />
                </td>
                <td>
                  <input type="number" v-model.number="item.qty" class="form-input-small" min="1" step="1" />
                </td>
                <td class="amount-cell">{{ (item.salePrice * item.qty).toFixed(2) }}</td>
                <td class="action-cell">
                  <button @click="removeItem(index)" class="btn-icon-delete" title="移除">
                    ✕
                  </button>
                </td>
              </tr>
              <tr v-if="form.items.length === 0">
                <td colspan="5" class="empty-row">
                  <div class="empty-state">
                    <span class="empty-text">点击上方按钮添加商品</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="footer-bar">
        <div class="total-section">
          <span class="total-label">总计金额:</span>
          <span class="total-amount">¥{{ totalAmount }}</span>
        </div>
        <div class="footer-actions">
          <button @click="$router.back()" class="btn-cancel">取消</button>
          <button @click="submitOrder" class="btn-submit" :disabled="isSubmitting">
            <span v-if="isSubmitting">提交中...</span>
            <span v-else>✅ 提交订单</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from '@/api/request'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccess, showError, showWarning } from '@/utils/toast'

const router = useRouter()
const customers = ref([])
const products = ref([])
const isSubmitting = ref(false)

const form = ref({
  customerId: '',
  items: [],
})

const totalAmount = computed(() => {
  return form.value.items.reduce((sum, item) => sum + item.salePrice * item.qty, 0).toFixed(2)
})

onMounted(async () => {
  try {
    const [custData, prodData] = await Promise.all([
      axios.get('/customers'),
      axios.get('/products'),
    ])
    customers.value = custData || []
    products.value = prodData || []
  } catch (error) {
    console.error('Failed to load initial data', error)
    showError('加载数据失败')
  }
})

const addItem = () => {
  form.value.items.push({ itemId: '', salePrice: 0, qty: 1 })
}

const removeItem = (index) => {
  form.value.items.splice(index, 1)
}

const onItemSelect = (itemRow) => {
  const product = products.value.find((p) => p.id === itemRow.itemId)
  if (product) {
    itemRow.salePrice = product.salePrice
  }
}

const submitOrder = async () => {
  if (!form.value.customerId) {
    showWarning('请选择客户')
    return
  }
  if (form.value.items.length === 0) {
    showWarning('请至少添加一个商品')
    return
  }
  for (const item of form.value.items) {
    if (!item.itemId) {
      showWarning('请选择商品')
      return
    }
    if (!Number.isInteger(item.qty) || item.qty <= 0) {
      showWarning('商品数量必须为正整数')
      return
    }
    if (item.salePrice < 0) {
      showWarning('销售价不能为负数')
      return
    }
  }

  isSubmitting.value = true
  try {
    await axios.post('/sale-orders', {
      customerId: form.value.customerId,
      items: form.value.items,
    })
    showSuccess('销售订单创建成功')
    router.push('/sales')
  } catch (error) {
    console.error('Failed to create order', error)
    showError('创建订单失败')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
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

.btn-back {
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e9ecef;
  color: #495057;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.form-group {
  max-width: 400px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.required {
  color: #e74c3c;
}

.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  background-color: white;
  transition: border-color 0.2s;
}

.form-select:focus {
  outline: none;
  border-color: #8e44ad;
  box-shadow: 0 0 0 3px rgba(142, 68, 173, 0.1);
}

.items-section {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.section-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.btn-add-item {
  padding: 8px 16px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-add-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.data-table th {
  background: #fff;
  font-weight: 600;
  color: #6c757d;
  font-size: 0.9rem;
}

.form-select-small,
.form-input-small {
  width: 100%;
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
}

.form-select-small:focus,
.form-input-small:focus {
  outline: none;
  border-color: #8e44ad;
}

.amount-cell {
  font-weight: 600;
  color: #e67e22;
}

.btn-icon-delete {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: #fee2e2;
  color: #dc2626;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-icon-delete:hover {
  background: #fecaca;
  transform: scale(1.1);
}

.empty-row {
  text-align: center;
  padding: 40px !important;
}

.empty-text {
  color: #adb5bd;
  font-style: italic;
}

.footer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-label {
  font-size: 1.1rem;
  color: #6c757d;
}

.total-amount {
  font-size: 1.8rem;
  font-weight: 700;
  color: #e67e22;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.btn-cancel {
  padding: 12px 24px;
  background: white;
  border: 1px solid #ced4da;
  color: #495057;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.btn-submit {
  padding: 12px 32px;
  background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(142, 68, 173, 0.3);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
