/**
 * 模块名称: 计数器状态管理
 * 功能描述: Pinia 示例 Store，演示基本的计数器状态管理（count, doubleCount, increment）。
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
