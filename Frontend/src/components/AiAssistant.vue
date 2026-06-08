<!--
  模块名称: AI 仓储智能助手
  功能描述: 提供全系统可用的悬浮 AI 对话面板，支持打字机流式输出、工具调用展示、商品数据同步和快捷指令。
-->
<template>
  <div class="ai-assistant-wrapper" :style="wrapperStyle">
    <!-- 悬浮球按钮 -->
    <button 
      class="ai-floating-btn" 
      :class="{ 'panel-open': isOpen }" 
      @mousedown="startDrag" 
      @touchstart="startDrag" 
      @click="handleBtnClick"
      title="AI 仓储智能助手"
    >
      <svg class="ai-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2L2 7V17L12 22L22 17V7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round" fill="none"/>
        <text x="50%" y="58%" dominant-baseline="middle" text-anchor="middle" font-family="'Outfit', 'Inter', system-ui, sans-serif" font-weight="900" font-size="8.5" fill="currentColor" letter-spacing="0.2">AI</text>
      </svg>
      <span class="btn-tooltip" v-if="!isOpen">AI 助手</span>
    </button>

    <!-- 侧边滑出式对话面板 -->
    <div class="ai-chat-panel" :class="{ 'active': isOpen }">
      <!-- 头部 -->
      <div class="panel-header">
        <div class="header-info">
          <div class="header-avatar-badge">AI</div>
          <div class="title-meta">
            <h4>AI 仓储助手</h4>
            <span class="status-online"><span class="dot"></span>在线分析中</span>
          </div>
        </div>
        <div class="header-actions">
          <button 
            v-if="activeTab === 'chat'"
            class="action-btn new-chat-btn" 
            @click="startNewChat" 
            :disabled="isClearing" 
            title="清空上下文，开启新对话"
          >
            <svg class="icon-new" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            开启新对话
          </button>
          <button class="action-btn reindex-btn" @click="triggerReindex" :disabled="isReindexing" title="全量商品向量同步">
            <svg class="icon-sync" :class="{ 'spinning': isReindexing }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
            </svg>
            {{ isReindexing ? '正在同步...' : '同步商品数据' }}
          </button>
          <button class="close-btn" @click="isOpen = false" title="关闭面板">
            <svg class="close-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>

      <!-- 选项卡 -->
      <div class="chat-tabs">
        <button 
          class="tab-item" 
          :class="{ active: activeTab === 'chat' }" 
          @click="activeTab = 'chat'"
        >
          智能对话
        </button>
        <button 
          class="tab-item" 
          :class="{ active: activeTab === 'history' }" 
          @click="activeTab = 'history'"
        >
          历史记录
        </button>
      </div>

      <!-- 对话内容区 (当前会话) -->
      <div class="chat-messages" ref="messageContainer" v-if="activeTab === 'chat'">
        <!-- 欢迎卡片 -->
        <div class="welcome-card" v-if="messages.length === 0">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
          </div>
          <h3>欢迎使用 AI 仓储助手</h3>
          <p>我可以为您提供实时库存查询、低库存商品预警、品类占比统计及补货建议。请选择下方快捷指令或在输入框中直接提问：</p>
          
          <div class="quick-chips">
            <button class="chip" @click="sendQuickPrompt('哪些商品处于低库存状态？')">
              低库存预警
            </button>
            <button class="chip" @click="sendQuickPrompt('统计各商品分类的库存占比？')">
              品类库存统计
            </button>
            <button class="chip" @click="sendQuickPrompt('基于当前低库存情况，生成补货建议')">
              生成补货建议
            </button>
            <button class="chip" @click="sendQuickPrompt('查询仓库所有商品列表')">
              商品列表查询
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
          <div class="msg-avatar" v-if="msg.role === 'assistant'">AI</div>
          <div class="msg-avatar user" v-else>我</div>
          
          <div class="msg-bubble">
            <div class="msg-text" v-html="renderMarkdown(msg.content)"></div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 加载中动画 -->
        <div class="message-item assistant loading" v-if="isLoading">
          <div class="msg-avatar">AI</div>
          <div class="msg-bubble">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 历史记录展示区 -->
      <div class="history-list" v-else>
        <div v-if="isHistoryLoading" class="empty-history">
          <svg class="icon-sync spinning" style="width: 24px; height: 24px; color: #3498db;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
          </svg>
          <p>正在加载历史记录...</p>
        </div>
        <div v-else-if="historyMessages.length === 0" class="empty-history">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="empty-history-icon">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          <p>暂无历史对话记录</p>
        </div>
        <div v-else v-for="(msgs, date) in groupedHistory" :key="date" class="history-group">
          <div class="history-date-divider">
            <span>{{ date }}</span>
          </div>
          <div v-for="(msg, idx) in msgs" :key="idx" :class="['message-item', msg.role]">
            <div class="msg-avatar" v-if="msg.role === 'assistant'">AI</div>
            <div class="msg-avatar user" v-else>我</div>
            <div class="msg-bubble">
              <div class="msg-text" v-html="renderMarkdown(msg.content)"></div>
              <div class="msg-time">{{ msg.time ? msg.time.split(' ')[1] || msg.time : '' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部输入栏 -->
      <div class="panel-input-area" v-if="activeTab === 'chat'">
        <textarea 
          ref="inputBox" 
          v-model="inputMsg" 
          placeholder="问问 AI，例如：帮我分析哪些商品需要采购..." 
          @keydown.enter.prevent="handleEnter"
          rows="2"
        ></textarea>
        <button class="send-btn" :disabled="!inputMsg.trim() || isLoading" @click="sendMessage" title="发送消息">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M2.01 21L23 12L2.01 3L2 10L17 12L2 14L2.01 21Z" fill="currentColor"/>
          </svg>
        </button>
      </div>

      <!-- 历史只读底栏 -->
      <div class="history-footer-note" v-else>
        提示：仅展示最近 90 天内的完整对话历史记录
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'

const isOpen = ref(false)
const isLoading = ref(false)
const isReindexing = ref(false)
const inputMsg = ref('')
const messages = ref([])
const messageContainer = ref(null)
const inputBox = ref(null)

// --- 悬浮球拖拽逻辑 ---
const wrapperLeft = ref(null)
const wrapperTop = ref(null)
const isDragging = ref(false)

const wrapperStyle = computed(() => {
  if (wrapperLeft.value && wrapperTop.value) {
    return {
      left: wrapperLeft.value,
      top: wrapperTop.value,
      right: 'auto',
      bottom: 'auto'
    }
  }
  return {
    right: '24px',
    bottom: '24px'
  }
})

const startDrag = (event) => {
  if (event.type === 'touchstart') {
    // 允许触摸点击
  } else {
    event.preventDefault()
  }

  const isTouch = event.type.startsWith('touch')
  const startX = isTouch ? event.touches[0].clientX : event.clientX
  const startY = isTouch ? event.touches[0].clientY : event.clientY

  const wrapper = document.querySelector('.ai-assistant-wrapper')
  if (!wrapper) return
  const rect = wrapper.getBoundingClientRect()

  const offsetX = startX - rect.left
  const offsetY = startY - rect.top

  let moved = false

  const onDrag = (moveEvent) => {
    moved = true
    const currentX = isTouch ? moveEvent.touches[0].clientX : moveEvent.clientX
    const currentY = isTouch ? moveEvent.touches[0].clientY : moveEvent.clientY

    let newLeft = currentX - offsetX
    let newTop = currentY - offsetY

    const margin = 10
    const minLeft = margin
    const maxLeft = window.innerWidth - rect.width - margin
    const minTop = margin
    const maxTop = window.innerHeight - rect.height - margin

    newLeft = Math.max(minLeft, Math.min(newLeft, maxLeft))
    newTop = Math.max(minTop, Math.min(newTop, maxTop))

    wrapperLeft.value = `${newLeft}px`
    wrapperTop.value = `${newTop}px`
  }

  const stopDrag = () => {
    document.removeEventListener(isTouch ? 'touchmove' : 'mousemove', onDrag)
    document.removeEventListener(isTouch ? 'touchend' : 'mouseup', stopDrag)

    if (moved) {
      isDragging.value = true
      setTimeout(() => {
        isDragging.value = false
      }, 50)
    }
  }

  document.addEventListener(isTouch ? 'touchmove' : 'mousemove', onDrag)
  document.addEventListener(isTouch ? 'touchend' : 'mouseup', stopDrag)
}

const handleBtnClick = () => {
  if (isDragging.value) return
  togglePanel()
}

// --- 选项卡与历史记录状态 ---
const activeTab = ref('chat') // 'chat' 智能对话 | 'history' 历史记录
const isHistoryLoading = ref(false)
const historyMessages = ref([])
const isClearing = ref(false)

const groupedHistory = computed(() => {
  const groups = {}
  for (const msg of historyMessages.value) {
    const dateStr = msg.time ? msg.time.split(' ')[0] : '未知日期'
    if (!groups[dateStr]) {
      groups[dateStr] = []
    }
    groups[dateStr].push(msg)
  }
  return groups
})

// 开启新对话（清空会话上下文）
const startNewChat = async () => {
  if (isClearing.value) return
  isClearing.value = true
  
  const token = localStorage.getItem('token')
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  
  try {
    const res = await fetch(`${baseUrl}/ai/clear`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (res.ok) {
      messages.value = []
      const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      messages.value.push({
        role: 'assistant',
        content: '当前会话已重置。我已经忘记了之前的对话上下文，我们可以开始新的话题了！',
        time: timeStr
      })
      scrollToBottom()
    }
  } catch (error) {
    console.error('清空会话失败:', error)
  } finally {
    isClearing.value = false
  }
}

// 从后端拉取完整的历史记录
const loadHistoryList = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  isHistoryLoading.value = true
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  try {
    const res = await fetch(`${baseUrl}/ai/history`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (res.ok) {
      const data = await res.json()
      if (data.success && data.data) {
        historyMessages.value = data.data.map(item => ({
          role: item.role,
          content: item.content,
          time: item.time || ''
        }))
      }
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    isHistoryLoading.value = false
  }
}

// 从后端拉取当前登录用户的对话历史记录 (用于智能对话初始渲染，限制 10 条)
const loadChatHistory = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  try {
    const res = await fetch(`${baseUrl}/ai/history`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    if (res.ok) {
      const data = await res.json()
      if (data.success && data.data) {
        const rawMsgs = data.data.map(item => ({
          role: item.role,
          content: item.content,
          time: item.time ? item.time.split(' ')[1] || item.time : ''
        }))
        // 为避免智能对话窗口过于臃肿，默认只加载最新的 10 条消息
        messages.value = rawMsgs.slice(-10)
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

// 监听面板开启，自动聚焦输入框并拉取历史记录
watch(isOpen, (newVal) => {
  if (newVal) {
    if (activeTab.value === 'chat') {
      loadChatHistory()
    } else {
      loadHistoryList()
    }
    nextTick(() => {
      inputBox.value?.focus()
      scrollToBottom()
    })
  }
})

// 监听 Tab 切换
watch(activeTab, (newVal) => {
  if (newVal === 'history') {
    loadHistoryList()
  } else if (newVal === 'chat') {
    loadChatHistory()
  }
})

// 切换面板显示
const togglePanel = () => {
  isOpen.value = !isOpen.value
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 回车发送消息（Shift + Enter 换行）
const handleEnter = (e) => {
  if (e.shiftKey) {
    inputMsg.value += '\n'
  } else {
    sendMessage()
  }
}

// 快捷 Prompt 发送
const sendQuickPrompt = (promptText) => {
  inputMsg.value = promptText
  sendMessage()
}

// 触发重新建立向量索引
const triggerReindex = async () => {
  if (isReindexing.value) return
  isReindexing.value = true
  
  const token = localStorage.getItem('token')
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  
  // 添加一条系统通知
  const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  messages.value.push({
    role: 'assistant',
    content: '正在同步商品向量数据，请稍候...',
    time: timeStr
  })
  scrollToBottom()

  try {
    const res = await fetch(`${baseUrl}/ai/reindex`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (res.ok) {
      messages.value.push({
        role: 'assistant',
        content: '商品向量数据同步完成。所有商品已成功转化为特征向量并导入 Redis 搜索引擎，现在您可以进行商品与库存智能分析了。',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      })
    } else {
      throw new Error('同步失败，服务端返回异常')
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: `商品向量同步失败: ${error.message}。请检查 Redis 搜索引擎服务及后端接口配置是否正常。`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    })
  } finally {
    isReindexing.value = false
    scrollToBottom()
  }
}

// 发送消息
const sendMessage = async () => {
  const query = inputMsg.value.trim()
  if (!query || isLoading.value) return

  const token = localStorage.getItem('token')
  const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  // 1. 将用户发送的消息推入数组
  messages.value.push({
    role: 'user',
    content: query,
    time: timeStr
  })

  inputMsg.value = ''
  isLoading.value = true
  scrollToBottom()

  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${baseUrl}/ai/chat?message=${encodeURIComponent(query)}`

  let assistantMsgIndex = -1

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('登录已过期，请重新登录')
      }
      try {
        const text = await response.text()
        const errJson = JSON.parse(text)
        if (errJson && errJson.message) {
          throw new Error(errJson.message)
        }
      } catch (e) {
        // Ignore parsing errors
      }
      throw new Error(`连接服务失败，状态码: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let currentEvent = 'message'

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      
      // SSE 协议解析：按行分割
      const lines = buffer.split('\n')
      // 最后一行可能不完整，留到下一批处理
      buffer = lines.pop()

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) continue

        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          let dataContent = line.slice(5)
          // 标准 SSE 规范：如果 data: 后面有空格，则去掉首个空格
          if (dataContent.startsWith(' ')) {
            dataContent = dataContent.slice(1)
          }

          if (currentEvent === 'error') {
            throw new Error(dataContent || 'AI 生成时发生内部错误')
          } else if (currentEvent === 'complete' || dataContent === '[DONE]') {
            currentEvent = 'message' // 重置状态
            continue
          } else {
            // 收到第一帧数据，关闭思考状态，并创建助手消息气泡
            if (assistantMsgIndex === -1) {
              isLoading.value = false
              assistantMsgIndex = messages.value.push({
                role: 'assistant',
                content: '',
                time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              }) - 1
            }
            
            // 拼接打字机 Token，保留空格与换行
            messages.value[assistantMsgIndex].content += dataContent
            scrollToBottom()
          }
        }
      }
    }

    // 确保没有空白回答
    if (assistantMsgIndex === -1) {
      isLoading.value = false
      messages.value.push({
        role: 'assistant',
        content: 'AI 助手未返回任何有效回复。请尝试重新提问或检查后台服务。',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      })
    } else if (!messages.value[assistantMsgIndex].content.trim()) {
      messages.value[assistantMsgIndex].content = 'AI 助手返回了空回复。请尝试重新提问。'
    }

  } catch (error) {
    isLoading.value = false
    if (assistantMsgIndex === -1) {
      messages.value.push({
        role: 'assistant',
        content: `请求失败: ${error.message}。请检查网络连接或接口配置。`,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      })
    } else {
      messages.value[assistantMsgIndex].content += `\n\n[生成中断: ${error.message}]`
    }
    scrollToBottom()
  }
}

// 轻量级、高能的 Markdown 解析器，防跨站脚本且无第三方依赖
const renderMarkdown = (text) => {
  if (!text) return ''
  
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 1. 解析多行代码块: ```java ... ```
  html = html.replace(/```(?:[a-zA-Z0-9]+)?\n([\s\S]*?)```/g, (match, code) => {
    return `<pre class="code-block"><code>${code.trim()}</code></pre>`
  })

  // 2. 解析行内单标记代码: `code`
  html = html.replace(/`([^`\n]+)`/g, '<code class="inline-code">$1</code>')

  // 3. 解析加粗: **text**
  html = html.replace(/\*\*([\s\S]*?)\*\*/g, '<strong>$1</strong>')

  // 4. 解析列表项: - item 样式
  html = html.replace(/^\s*-\s+(.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>[\s\S]*?<\/li>)/g, '<ul class="md-list">$1</ul>')
  // 合并相邻 of ul 元素
  html = html.replace(/<\/ul>\s*<ul class="md-list">/g, '')

  // 5. 解析换行
  html = html.replace(/\n/g, '<br/>')

  return html
}
</script>

<style scoped>
.ai-assistant-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 悬浮球样式 - 简洁实用 */
.ai-floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #2c3e50;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(44, 62, 80, 0.15);
  position: relative;
  transition: all 0.2s ease;
  outline: none;
}

.ai-floating-btn:hover {
  background-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(52, 152, 219, 0.3);
}

.ai-floating-btn.panel-open {
  background-color: #e2e8f0;
  color: #475569;
  box-shadow: none;
  transform: scale(0.95);
}

.ai-icon {
  width: 24px;
  height: 24px;
}

.btn-tooltip {
  position: absolute;
  right: 70px;
  background: #2c3e50;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.8rem;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transform: translateX(10px);
  transition: all 0.2s ease;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.ai-floating-btn:hover .btn-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(0);
}

/* 侧边对话面板 */
.ai-chat-panel {
  position: fixed;
  top: 0;
  right: -430px;
  width: 400px;
  height: 100vh;
  background: #ffffff;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.05);
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
}

.ai-chat-panel.active {
  right: 0;
}

/* 头部样式 */
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar-badge {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background-color: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
}

.title-meta h4 {
  margin: 0;
  font-size: 1rem;
  color: #1e293b;
  font-weight: 600;
}

.status-online {
  font-size: 0.7rem;
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  font-weight: 500;
}

.status-online .dot {
  width: 6px;
  height: 6px;
  background-color: #10b981;
  border-radius: 50%;
  display: inline-block;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reindex-btn {
  background-color: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.reindex-btn:hover:not(:disabled) {
  background-color: #f1f5f9;
  color: #1e293b;
  border-color: #cbd5e1;
}

.reindex-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon-sync {
  width: 14px;
  height: 14px;
}

.icon-sync.spinning {
  animation: spin 1s linear infinite;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #f1f5f9;
  color: #1e293b;
}

.close-icon {
  width: 18px;
  height: 18px;
}

/* 消息对话列表区 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

/* 欢迎引导卡片 */
.welcome-card {
  padding: 24px 20px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  text-align: center;
  margin-top: 10px;
}

.welcome-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px auto;
  color: #3498db;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-icon svg {
  width: 32px;
  height: 32px;
}

.welcome-card h3 {
  margin: 0 0 8px;
  font-size: 1.05rem;
  color: #1e293b;
  font-weight: 600;
}

.welcome-card p {
  font-size: 0.85rem;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 16px;
}

.quick-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.chip {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.chip:hover {
  background: #e2e8f0;
  color: #3498db;
  border-color: #cbd5e1;
}

/* 对话气泡通用 */
.message-item {
  display: flex;
  gap: 12px;
  max-width: 85%;
  align-self: flex-start;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

.assistant .msg-avatar {
  background-color: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.user .msg-avatar {
  background-color: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 0.88rem;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.assistant .msg-bubble {
  background: #ffffff;
  color: #334155;
  border-top-left-radius: 2px;
  border: 1px solid #e2e8f0;
}

.user .msg-bubble {
  background: #2c3e50;
  color: #ffffff;
  border-top-right-radius: 2px;
}

.msg-time {
  font-size: 0.7rem;
  color: #94a3b8;
  align-self: flex-end;
  margin-top: 2px;
}

.user .msg-time {
  color: #94a3b8;
}

/* 消息内容渲染支持 Markdown */
.msg-text :deep(.code-block) {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 8px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-family: 'Fira Code', 'Courier New', Courier, monospace;
  font-size: 0.8rem;
}

.msg-text :deep(.inline-code) {
  background: #f1f5f9;
  color: #e11d48;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.85em;
}

.user .msg-text :deep(.inline-code) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.msg-text :deep(.md-list) {
  padding-left: 20px;
  margin: 6px 0;
}

.msg-text :deep(li) {
  margin-bottom: 4px;
}

/* 加载中 Bouncing Dots */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 2px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background-color: #94a3b8;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 底部输入框 */
.panel-input-area {
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: #ffffff;
}

.panel-input-area textarea {
  flex: 1;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 0.88rem;
  resize: none;
  outline: none;
  font-family: inherit;
  line-height: 1.4;
  background: #ffffff;
  transition: border-color 0.2s;
  color: #1e293b;
}

.panel-input-area textarea:focus {
  border-color: #3498db;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: #3498db;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.send-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 选项卡样式 */
.chat-tabs {
  display: flex;
  background-color: #f1f5f9;
  padding: 4px;
  margin: 12px 20px 0 20px;
  border-radius: 8px;
  gap: 4px;
}

.tab-item {
  flex: 1;
  border: none;
  background: none;
  padding: 8px 12px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  outline: none;
}

.tab-item:hover {
  color: #1e293b;
}

.tab-item.active {
  background-color: #ffffff;
  color: #2c3e50;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* 历史记录布局 */
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f8fafc;
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94a3b8;
  gap: 12px;
}

.empty-history-icon {
  width: 40px;
  height: 40px;
  opacity: 0.5;
  color: #94a3b8;
}

.history-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-date-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px 0;
  position: relative;
}

.history-date-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background-color: #e2e8f0;
  z-index: 1;
}

.history-date-divider span {
  position: relative;
  z-index: 2;
  background-color: #f8fafc;
  padding: 0 12px;
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 600;
}

.history-footer-note {
  padding: 12px 20px;
  border-top: 1px solid #e2e8f0;
  background-color: #ffffff;
  text-align: center;
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 500;
}

/* 按钮样式微调 */
.new-chat-btn {
  background-color: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.new-chat-btn:hover:not(:disabled) {
  background-color: #d1fae5;
  color: #047857;
  border-color: #6ee7b7;
}

.new-chat-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon-new {
  width: 14px;
  height: 14px;
}

/* 适配移动端 */
@media (max-width: 480px) {
  .ai-chat-panel {
    width: 100%;
    right: -100%;
  }
}
</style>
