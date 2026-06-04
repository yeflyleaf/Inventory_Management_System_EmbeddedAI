
/**
 * 防抖/节流指令
 * 用法: <button v-throttle="1000">...</button>
 * 默认延迟: 500ms
 */
export const throttle = {
    mounted(el, binding) {
        el.addEventListener('click', (e) => {
            if (el.dataset.throttled) {
                e.preventDefault()
                e.stopPropagation()
                return
            }

            el.dataset.throttled = 'true'
            const originalPointerEvents = el.style.pointerEvents
            const originalOpacity = el.style.opacity

            // 禁用点击
            el.style.pointerEvents = 'none'
            // 可选：添加视觉反馈
            el.style.opacity = '0.6'

            // 如果是按钮，也设置 disabled 属性
            if (el.tagName === 'BUTTON') {
                // 使用 setTimeout 确保在当前事件循环结束后再禁用按钮
                // 否则会阻止 form 的 submit 事件触发
                setTimeout(() => {
                    el.disabled = true
                }, 0)
            }

            setTimeout(() => {
                el.dataset.throttled = ''
                el.style.pointerEvents = originalPointerEvents
                el.style.opacity = originalOpacity

                if (el.tagName === 'BUTTON') {
                    el.disabled = false
                }
            }, binding.value || 500)
        }, true) // 使用捕获阶段，确保在 Vue 事件处理之前触发
    }
}
