<!--
  模块名称: 仪表盘页面
  功能描述: 系统首页，展示关键KPI指标、图表分析和快捷操作入口。
-->
<template>
  <div class="page-container">
    <div class="header-section">
      <h3 class="section-title">
        <span class="icon">📊</span>
        数据仪表盘
      </h3>
      <div class="actions">
        <span class="last-updated" v-if="lastUpdated">上次更新: {{ lastUpdated }}</span>
        <RefreshButton @click="refreshData" title="刷新数据" />
      </div>
    </div>



    <!-- 顶部内容区：KPI + 告警 -->
    <div class="top-section">
      <!-- KPI 区域 (6卡片) -->
      <div class="kpi-grid">
        <!-- Row 1 -->
        <div class="stat-card sku" @click="navigateTo('/stock')">
          <div class="stat-icon">🗂️</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalSku }}</span>
            <span class="stat-label">商品种类 (SKU)</span>
          </div>
        </div>

        <div class="stat-card stock" @click="navigateTo('/stock')">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalStock }}</span>
            <span class="stat-label">库存总数量</span>
          </div>
        </div>
        <div class="stat-card warning" @click="navigateTo('/stock')">
          <div class="stat-icon">⚠️</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.lowStock }}</span>
            <span class="stat-label">低库存预警</span>
          </div>
        </div>
        
        <!-- Row 2 -->
        <div class="stat-card danger" @click="navigateTo('/stock')">
          <div class="stat-icon">❌</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.zeroStock }}</span>
            <span class="stat-label">缺货0库存</span>
          </div>
        </div>
        <div class="stat-card task-blue" @click="navigateTo('/purchase')">
          <div class="stat-icon">📥</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingPurchaseOrders || 0 }}</span>
            <span class="stat-label">待入库采购</span>
          </div>
        </div>
        <div class="stat-card task-green" @click="navigateTo('/sales')">
          <div class="stat-icon">📤</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingSalesOrders || 0 }}</span>
            <span class="stat-label">待发货销售</span>
          </div>
        </div>
      </div>

      <!-- 告警区域 (右上角小模块) -->
      <div class="alert-panel">
        <div class="alert-header">
          <h4>🔔 异常告警</h4>
        </div>
        <div class="alert-list">
          <div class="alert-item" v-if="stats.zeroStock > 0">
            <span class="alert-dot danger"></span>
            <span class="alert-text">缺货0库存清单</span>
            <span class="alert-num danger">{{ stats.zeroStock }}</span>
          </div>
          <div class="alert-item" v-if="stats.lowStock > 0">
            <span class="alert-dot warning"></span>
            <span class="alert-text">低库存清单</span>
            <span class="alert-num warning">{{ stats.lowStock }}</span>
          </div>
          <div class="alert-item">
            <span class="alert-dot info"></span>
            <span class="alert-text">库存积压 (>{{ inventoryBacklogDays }}天)</span>
            <span class="alert-num info">{{ stats.overstockCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>



    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 库存状态分布 -->
      <div class="chart-card">
        <div class="card-header">
          <h4>库存健康度分析</h4>
        </div>
        <div class="chart-body" ref="pieChartRef"></div>
      </div>

      <!-- 分类库存占比 -->
      <div class="chart-card">
        <div class="card-header with-actions">
          <h4>商品分类分布 (Top 5)</h4>
          <div class="chart-actions">
            <select v-model="categoryChartType" @change="fetchCategoryData" class="chart-select">
              <option value="STOCK">📦 库存占比</option>
              <option value="SALES_QTY">🔥 销量 Top5</option>
              <option value="SALES_AMOUNT">💰 销售额 Top5</option>
            </select>
          </div>
        </div>
        <div class="chart-body" ref="barChartRef"></div>
      </div>
    </div>

    <!-- 出入库趋势 -->
    <div class="chart-card wide-chart trend-card">
      <div class="card-header trend-header">
        <div class="header-left">
          <h4>📊 近 30 天出入库趋势</h4>
        </div>
        <div class="header-filters">
          <select class="filter-select" v-model="trendFilters.days" @change="fetchTrendData">
            <option :value="30">近 30 天</option>
            <option :value="7">近 7 天</option>
            <option :value="90">近 3 个月</option>
          </select>
          <select class="filter-select" v-model="trendFilters.warehouseId" @change="fetchTrendData">
            <option :value="null">全部仓库</option>
            <!-- Assuming warehouses are loaded, for now hardcoded or fetched -->
            <option :value="1">主仓库</option>
          </select>
          <select class="filter-select" v-model="trendFilters.category" @change="fetchTrendData">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <div class="search-box">
            <input type="text" 
                   v-model="trendFilters.itemName" 
                   @keyup.enter="fetchTrendData"
                   placeholder="搜索特定商品趋势..." 
                   class="filter-input" 
                   title="输入商品名称并回车以查看特定商品的趋势" />
          </div>
        </div>
      </div>
      
      <div class="trend-body">
        <div class="chart-area" ref="lineChartRef"></div>
        <div class="trend-legend">
          <div class="legend-item">
            <span class="legend-line out"></span>
            <div class="legend-info">
              <span class="legend-label">出库量</span>
              <span class="legend-desc">销售、领用出库</span>
            </div>
          </div>
          <div class="legend-item">
            <span class="legend-line in"></span>
            <div class="legend-info">
              <span class="legend-label">入库量</span>
              <span class="legend-desc">采购、归还入库</span>
            </div>
          </div>
          
          
        </div>
      </div>
    </div>



  </div>
</template>

<script setup>
import axios from '@/api/request'
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import RefreshButton from '@/components/RefreshButton.vue'

const router = useRouter()

// 导航跳转
const navigateTo = (path) => {
  router.push(path)
}

// 状态数据
const stats = ref({

  totalStock: 0,
  lowStock: 0,
  zeroStock: 0,
  totalSku: 0,
  pendingPurchaseOrders: 0,
  pendingSalesOrders: 0,
  abnormalOrders: 0,
  overstockCount: 0
})


const lastUpdated = ref('')
const products = ref([])
const stockList = ref([])
const stockTrendData = ref([])
const categoryChartType = ref('STOCK')
const categoryStatsData = ref([])
const categories = ref([])
const trendFilters = ref({
  days: 30,
  warehouseId: null,
  category: '',
  itemName: ''
})

// 图表引用
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

import { useSettingsStore } from '@/stores/settings'
import { storeToRefs } from 'pinia'

const settingsStore = useSettingsStore()
const { lowStockThreshold: LOW_STOCK_THRESHOLD, inventoryBacklogDays } = storeToRefs(settingsStore)







// 初始化图表
const initCharts = () => {
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
  }
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

// 更新图表数据
const updateCharts = () => {
  if (!pieChart || !barChart) return

  // 1. 库存状态分布 (饼图)
  const normalStock = stats.value.totalSku - stats.value.lowStock - stats.value.zeroStock
  
  const pieOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [
      {
        name: '库存状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: normalStock, name: '正常', itemStyle: { color: '#4caf50' } },
          { value: stats.value.lowStock, name: '低库存', itemStyle: { color: '#ff9800' } },
          { value: stats.value.zeroStock, name: '缺货0库存', itemStyle: { color: '#f44336' } }
        ]
      }
    ]
  }
  pieChart.setOption(pieOption)

  pieChart.setOption(pieOption)

  // 2. 分类分布 (柱状图)
  const sortedCategories = categoryStatsData.value
  
  const barOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedCategories.map(i => i.category),
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: categoryChartType.value === 'STOCK' ? '库存数量' : (categoryChartType.value === 'SALES_QTY' ? '销售数量' : '销售金额'),
        type: 'bar',
        barWidth: '60%',
        data: sortedCategories.map(i => i.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  barChart.setOption(barOption)

  // 3. 出入库趋势 (折线图)
  const dates = stockTrendData.value.map(item => item.date)
  const inData = stockTrendData.value.map(item => item.inQuantity)
  const outData = stockTrendData.value.map(item => item.outQuantity)

  const lineOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: [], // Hide default legend
      show: false
    },
    grid: {
      left: '2%',
      right: '2%',
      bottom: '5%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        data: inData,
        itemStyle: { color: '#4caf50' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(76, 175, 80, 0.3)' },
            { offset: 1, color: 'rgba(76, 175, 80, 0.1)' }
          ])
        }
      },
      {
        name: '出库',
        type: 'line',
        smooth: true,
        data: outData,
        itemStyle: { color: '#f44336' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(244, 67, 54, 0.3)' },
            { offset: 1, color: 'rgba(244, 67, 54, 0.1)' }
          ])
        }
      }
    ]
  }
  lineChart.setOption(lineOption)
}

// 获取分类数据
const fetchCategoryData = async () => {
  try {
    const res = await axios.get('/dashboard/category-stats', {
      params: { type: categoryChartType.value }
    })
    categoryStatsData.value = res || []
    // 更新图表
    if (barChart) {
      // 重新调用 updateCharts 或者只更新 barChart
      updateCharts()
    }
  } catch (error) {
    console.error('Failed to fetch category stats', error)
  }
}

// 单独获取趋势数据
const fetchTrendData = async () => {
  try {
    const res = await axios.get('/dashboard/stats', {
      params: {
        days: trendFilters.value.days,
        warehouseId: trendFilters.value.warehouseId,
        category: trendFilters.value.category,
        itemName: trendFilters.value.itemName
      }
    })
    
    if (res && res.stockTrend) {
      stockTrendData.value = res.stockTrend
      // 仅更新折线图
      if (lineChart) {
        // Re-render line chart
        const dates = stockTrendData.value.map(item => item.date)
        const inData = stockTrendData.value.map(item => item.inQuantity)
        const outData = stockTrendData.value.map(item => item.outQuantity)
        
        lineChart.setOption({
          xAxis: { data: dates },
          series: [
            { data: inData },
            { data: outData }
          ]
        })
      }
    }
  } catch (error) {
    console.error('Failed to fetch trend data', error)
  }
}

// 获取数据
const refreshData = async () => {
  try {
    // 并行获取数据
    const [stockRes, statsRes] = await Promise.all([
      axios.get('/stock'),
      axios.get('/dashboard/stats', {
        params: {
          days: trendFilters.value.days,
          warehouseId: trendFilters.value.warehouseId,
          category: trendFilters.value.category,
          itemName: trendFilters.value.itemName
        }
      })
    ])

    stockList.value = stockRes || []
    
    // 提取所有分类
    const uniqueCategories = new Set(stockList.value.map(item => item.category).filter(c => c))
    categories.value = Array.from(uniqueCategories)

    // 初始加载分类数据
    await fetchCategoryData()
    
    // 计算统计数据
    const total = stockList.value
    const totalStockVal = total.reduce((sum, item) => sum + (item.currentStock || 0), 0)
    const lowStockVal = total.filter(item => item.currentStock > 0 && item.currentStock < LOW_STOCK_THRESHOLD.value).length
    const zeroStockVal = total.filter(item => item.currentStock === 0).length
    


    stats.value = {
      totalSku: total.length,
      totalStock: totalStockVal,
      lowStock: lowStockVal,
      zeroStock: zeroStockVal,
      lowStock: lowStockVal,
      zeroStock: zeroStockVal,

      pendingPurchaseOrders: statsRes?.pendingPurchaseOrders || 0,
      pendingSalesOrders: statsRes?.pendingSalesOrders || 0,
      abnormalOrders: statsRes?.abnormalOrders || 0,
      overstockCount: statsRes?.overstockCount || 0
    }



    // 如果 dashboard/stats 返回了 sales 数据，可以合并
    if (statsRes) {
      stockTrendData.value = statsRes.stockTrend || []
    }

    lastUpdated.value = new Date().toLocaleTimeString()
    
    // 更新图表
    nextTick(() => {
      updateCharts()
    })

  } catch (error) {
    console.error('Failed to refresh dashboard', error)
  }
}

onMounted(async () => {
  settingsStore.fetchSettings()
  initCharts()
  await refreshData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  color: #2c3e50;
  letter-spacing: -0.5px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.last-updated {
  color: #95a5a6;
  font-size: 0.85rem;
}





/* 顶部布局 */
.top-section {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

/* KPI 网格 */
.kpi-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 告警面板 */
.alert-panel {
  width: 280px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}

.alert-header h4 {
  margin: 0 0 16px 0;
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 700;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 12px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.alert-item:hover {
  background: #f0f2f5;
}

.alert-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 10px;
}

.alert-dot.danger { background: #f44336; }
.alert-dot.warning { background: #ff9800; }
.alert-dot.info { background: #90a4ae; }

.alert-text {
  flex: 1;
  font-size: 0.9rem;
  color: #546e7a;
  font-weight: 500;
}

.alert-num {
  font-weight: 700;
  font-size: 0.95rem;
}

.alert-num.danger { color: #f44336; }
.alert-num.warning { color: #ef6c00; }
.alert-num.info { color: #546e7a; }

/* 统计卡片调整 */
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}


.stat-card.sku .stat-icon { background: #f3e5f5; color: #8e24aa; }
.stat-card.stock .stat-icon { background: #e8f5e9; color: #2e7d32; }
.stat-card.warning .stat-icon { background: #fff3e0; color: #f57c00; }
.stat-card.danger .stat-icon { background: #ffebee; color: #c62828; }
.stat-card.task-blue .stat-icon { background: #e1f5fe; color: #0288d1; }
.stat-card.task-green .stat-icon { background: #f1f8e9; color: #558b2f; }

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.9rem;
  color: #7f8c8d;
  font-weight: 500;
  margin-top: 4px;
}





/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f5;
  display: flex;
  flex-direction: column;
}



.card-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 12px;
}

.card-header.with-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-select {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #606266;
  outline: none;
  cursor: pointer;
}

.chart-select:focus {
  border-color: #409eff;
}

.card-header h4 {
  margin: 0;
  font-size: 1.1rem;
  color: #2c3e50;
  font-weight: 700;
}

.chart-body {
  height: 350px;
  width: 100%;
}

.wide-chart {
  margin-top: 24px;
}

/* 趋势图特定样式 */
.trend-card {
  padding: 0;
  overflow: hidden;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #f0f2f5;
  background: #fff;
}

.header-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 0.9rem;
  color: #606266;
  background: #fff;
  cursor: pointer;
  outline: none;
}

.filter-select:hover {
  border-color: #c0c4cc;
}

.search-box {
  position: relative;
}

.filter-input {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 0.9rem;
  width: 150px;
  outline: none;
}

.trend-body {
  display: flex;
  height: 400px;
}

.chart-area {
  flex: 1;
  height: 100%;
  padding: 20px;
}

.trend-legend {
  width: 240px;
  background: #fcfcfc;
  border-left: 1px solid #f0f2f5;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.legend-item {
  display: flex;
  gap: 12px;
}

.legend-line {
  width: 4px;
  height: 40px;
  border-radius: 4px;
  margin-top: 4px;
}

.legend-line.out { background: #f44336; box-shadow: 0 2px 6px rgba(244, 67, 54, 0.3); }
.legend-line.in { background: #4caf50; box-shadow: 0 2px 6px rgba(76, 175, 80, 0.3); }

.legend-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.legend-label {
  font-weight: 700;
  color: #2c3e50;
  font-size: 1rem;
}

.legend-desc {
  font-size: 0.8rem;
  color: #909399;
}





/* 响应式调整 */
@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
}
</style>
