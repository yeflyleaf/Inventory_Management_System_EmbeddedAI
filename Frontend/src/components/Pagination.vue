<!--
  组件名称: 通用分页组件
  功能描述: 提供统一的分页控制，支持页码跳转、上一页/下一页导航。
  参数:
    - currentPage: 当前页码
    - totalPages: 总页数
  事件:
    - update:currentPage: 页码更新时触发
    - change: 页码改变时触发
-->
<template>
  <div class="pagination-container" v-if="totalPages > 0">
    <div class="pagination-content">
      <!-- 上一页 -->
      <button 
        class="page-btn prev-btn" 
        :disabled="currentPage === 1" 
        @click="changePage(currentPage - 1)"
        title="上一页"
      >
        <span class="icon">‹</span>
      </button>

      <!-- 页码列表 -->
      <div class="page-numbers">
        <template v-for="page in displayedPages" :key="page">
          <button
            v-if="page !== '...'"
            class="page-num-btn"
            :class="{ active: currentPage === page }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
          <span v-else class="ellipsis">...</span>
        </template>
      </div>

      <!-- 下一页 -->
      <button 
        class="page-btn next-btn" 
        :disabled="currentPage === totalPages" 
        @click="changePage(currentPage + 1)"
        title="下一页"
      >
        <span class="icon">›</span>
      </button>
    </div>

    <!-- 跳转控制 -->
    <div class="pagination-jumper">
      <span class="jumper-text">前往</span>
      <div class="input-wrapper">
        <input 
          type="number" 
          v-model.number="jumpPageInput" 
          @keydown.enter="handleJump"
          @blur="handleJump"
          min="1" 
          :max="totalPages"
          class="jumper-input"
        />
      </div>
      <span class="jumper-text">页</span>
      <span class="total-text">共 {{ totalPages }} 页</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  // 当前页码
  currentPage: {
    type: Number,
    required: true,
    default: 1
  },
  // 总页数
  totalPages: {
    type: Number,
    required: true,
    default: 1
  }
})

// 定义组件事件
const emit = defineEmits(['update:currentPage', 'change'])

// 跳转输入框的值
const jumpPageInput = ref(props.currentPage)

// 监听当前页码变化，同步输入框
watch(() => props.currentPage, (newVal) => {
  jumpPageInput.value = newVal
})

// 计算显示的页码，包含省略号逻辑
const displayedPages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  const delta = 2 // 当前页码前后显示的页数
  const range = []
  const rangeWithDots = []
  let l

  range.push(1)

  if (total <= 1) return range

  for (let i = current - delta; i <= current + delta; i++) {
    if (i < total && i > 1) {
      range.push(i)
    }
  }
  range.push(total)

  for (let i of range) {
    if (l) {
      if (i - l === 2) {
        rangeWithDots.push(l + 1)
      } else if (i - l !== 1) {
        rangeWithDots.push('...')
      }
    }
    rangeWithDots.push(i)
    l = i
  }

  return rangeWithDots
})

/**
 * 切换页码
 * @param {Number} page 目标页码
 */
const changePage = (page) => {
  if (page < 1 || page > props.totalPages || page === props.currentPage) return
  emit('update:currentPage', page)
  emit('change', page)
}

/**
 * 处理页码跳转输入
 */
const handleJump = () => {
  let page = parseInt(jumpPageInput.value)
  
  if (isNaN(page)) {
    jumpPageInput.value = props.currentPage
    return
  }

  if (page < 1) page = 1
  if (page > props.totalPages) page = props.totalPages
  
  jumpPageInput.value = page
  
  if (page !== props.currentPage) {
    changePage(page)
  }
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  padding: 16px 0;
  flex-wrap: wrap;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.pagination-content {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f9fa;
  padding: 4px;
  border-radius: 8px;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.05);
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  color: #495057;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.page-btn:hover:not(:disabled) {
  background: #fff;
  color: #3498db;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(52, 152, 219, 0.2);
}

.page-btn:disabled {
  background: #e9ecef;
  color: #adb5bd;
  cursor: not-allowed;
  box-shadow: none;
}

.page-btn .icon {
  font-size: 20px;
  line-height: 1;
  margin-top: -2px;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-num-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  transition: all 0.2s ease;
}

.page-num-btn:hover {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
}

.page-num-btn.active {
  background: #3498db;
  color: white;
  box-shadow: 0 2px 6px rgba(52, 152, 219, 0.3);
}

.ellipsis {
  color: #adb5bd;
  padding: 0 4px;
  font-size: 14px;
}

.pagination-jumper {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 14px;
}

.input-wrapper {
  position: relative;
}

.jumper-input {
  width: 48px;
  height: 32px;
  padding: 0 4px;
  text-align: center;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 14px;
  color: #495057;
  transition: all 0.2s ease;
  background: white;
}

.jumper-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.15);
}

/* 移除 input number 的箭头 */
.jumper-input::-webkit-outer-spin-button,
.jumper-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.jumper-input {
  -moz-appearance: textfield;
  appearance: textfield;
}

.total-text {
  margin-left: 8px;
  color: #adb5bd;
}
</style>
