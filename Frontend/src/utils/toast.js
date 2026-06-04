/**
 * 模块名称: 全局消息提示工具
 * 功能描述: 提供全局 Toast 消息提示的工具函数，支持成功、错误、警告等多种消息类型。
 */
import { ref } from 'vue'

// Toast 消息列表
const toasts = ref([])

// Toast 计数器用于生成唯一ID
let toastId = 0

/**
 * Toast 类型枚举
 */
export const ToastType = {
    SUCCESS: 'success',
    ERROR: 'error',
    WARNING: 'warning',
    INFO: 'info'
}

/**
 * 显示 Toast 消息
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型 (success, error, warning, info)
 * @param {number} duration - 显示时长 (毫秒), 默认 3000ms
 */
export const showToast = (message, type = ToastType.INFO, duration = 4000) => {
    const id = ++toastId

    const toast = {
        id,
        message,
        type,
        visible: true
    }

    toasts.value.push(toast)

    // 自动移除
    setTimeout(() => {
        removeToast(id)
    }, duration)

    return id
}

/**
 * 移除指定的 Toast
 * @param {number} id - Toast ID
 */
export const removeToast = (id) => {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index > -1) {
        toasts.value.splice(index, 1)
    }
}

/**
 * 快捷方法：显示成功消息
 */
export const showSuccess = (message, duration = 4000) => {
    return showToast(message, ToastType.SUCCESS, duration)
}

/**
 * 快捷方法：显示错误消息
 */
export const showError = (message, duration = 4000) => {
    return showToast(message, ToastType.ERROR, duration)
}

/**
 * 快捷方法：显示警告消息
 */
export const showWarning = (message, duration = 4000) => {
    return showToast(message, ToastType.WARNING, duration)
}

/**
 * 快捷方法：显示信息消息
 */
export const showInfo = (message, duration = 4000) => {
    return showToast(message, ToastType.INFO, duration)
}

/**
 * 获取 Toast 列表 (响应式)
 */
export const useToasts = () => {
    return toasts
}

export default {
    toasts,
    showToast,
    removeToast,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    useToasts,
    ToastType
}
