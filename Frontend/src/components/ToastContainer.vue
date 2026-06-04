<!--
  模块名称: 全局消息提示容器
  功能描述: 用于显示全局Toast消息的容器组件，支持成功、错误、警告等多种消息类型。
-->
<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['toast', `toast-${toast.type}`]"
          @click="removeToast(toast.id)"
        >
          <span class="toast-icon">{{ getIcon(toast.type) }}</span>
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" @click.stop="removeToast(toast.id)">×</button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToasts, removeToast, ToastType } from '@/utils/toast'

const toasts = useToasts()

const getIcon = (type) => {
  switch (type) {
    case ToastType.SUCCESS:
      return '✓'
    case ToastType.ERROR:
      return '✕'
    case ToastType.WARNING:
      return '⚠'
    case ToastType.INFO:
    default:
      return 'ℹ'
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  min-width: 280px;
  backdrop-filter: blur(10px);
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.toast-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
}

.toast-close:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.2);
}

/* 成功样式 */
.toast-success {
  background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
  color: white;
}

.toast-success .toast-icon {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.toast-success .toast-close {
  color: white;
}

/* 错误样式 */
.toast-error {
  background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
  color: white;
}

.toast-error .toast-icon {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.toast-error .toast-close {
  color: white;
}

/* 警告样式 */
.toast-warning {
  background: linear-gradient(135deg, #e67e22 0%, #f39c12 100%);
  color: white;
}

.toast-warning .toast-icon {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.toast-warning .toast-close {
  color: white;
}

/* 信息样式 */
.toast-info {
  background: linear-gradient(135deg, #2980b9 0%, #3498db 100%);
  color: white;
}

.toast-info .toast-icon {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.toast-info .toast-close {
  color: white;
}

/* 过渡动画 */
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100px);
  }
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
