/**
 * 模块名称: 系统设置状态管理
 * 功能描述: 管理系统全局配置（如公司信息、库存预警阈值等），支持从后端获取和更新设置。
 */
import axios from '@/api/request'
import { defineStore } from 'pinia'

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    settings: {
      company_name: '库存管理系统',
      company_address: '',
      company_phone: '',
      company_email: '',
      low_stock_threshold: '10',
      inventory_backlog_days: '60',
      order_prefix_purchase: 'PO',
      order_prefix_sales: 'SO',
      allow_negative_stock: 'false'
    },
    loaded: false
  }),

  getters: {
    companyName: (state) => state.settings.company_name || '库存管理系统',
    lowStockThreshold: (state) => parseInt(state.settings.low_stock_threshold) || 10,
    inventoryBacklogDays: (state) => parseInt(state.settings.inventory_backlog_days) || 60,
    allowNegativeStock: (state) => state.settings.allow_negative_stock === 'true',
    purchasePrefix: (state) => state.settings.order_prefix_purchase || 'PO',
    salesPrefix: (state) => state.settings.order_prefix_sales || 'SO'
  },

  actions: {
    async fetchSettings(force = false) {
      if (this.loaded && !force) {
        return
      }
      try {
        // 使用新的公共接口
        const data = await axios.get('/system/settings')
        if (data) {
          this.settings = { ...this.settings, ...data }
          this.loaded = true
        }
      } catch (error) {
        console.error('Failed to fetch system settings:', error)
      }
    },

    // 提供一个方法让组件更新 store (例如在设置页面保存后)
    updateSettings(newSettings) {
      this.settings = { ...this.settings, ...newSettings }
    }
  }
})
