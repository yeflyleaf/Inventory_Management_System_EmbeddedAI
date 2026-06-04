<!--
  模块名称: 刷新按钮组件
  功能描述: 带有防抖功能和加载状态的刷新按钮，防止重复点击。
-->
<template>
  <button 
    class="btn-refresh" 
    :class="{ 'is-disabled': isDebouncing || loading }" 
    @click="handleClick" 
    :disabled="isDebouncing || loading"
    :title="title || '刷新'"
  >
    <slot>
      <span v-if="loading" class="spinner-small"></span>
      <span>刷新</span>
    </slot>
  </button>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: String,
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const isDebouncing = ref(false)

const handleClick = (event) => {
  if (isDebouncing.value || props.loading) return

  isDebouncing.value = true
  emit('click', event)
  
  setTimeout(() => {
    isDebouncing.value = false
  }, 500)
}
</script>

<style scoped>
.btn-refresh {
  padding: 10px 18px;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  line-height: 1.5;
}

.btn-refresh .icon {
  font-size: 1.1em;
}

.btn-refresh:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.35);
}

.btn-refresh.is-disabled {
  background: #bdc3c7 !important; /* Gray color */
  color: #7f8c8d;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
  filter: grayscale(100%);
}

.spinner-small {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-left-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
