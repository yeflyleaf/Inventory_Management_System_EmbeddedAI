<!--
  模块名称: AI 仓储智能助手
  功能描述: 提供全系统可用的悬浮 AI 对话面板，支持打字机流式输出、工具调用展示、商品数据同步和快捷指令。
-->
<template>
  <div class="ai-assistant-wrapper">
    <!-- 悬浮球按钮 -->
    <button class="ai-floating-btn" :class="{ 'panel-open': isOpen }" @click="togglePanel" title="AI 仓储助手">
      <svg class="ai-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20Z" fill="currentColor"/>
        <path d="M8 13C8.55228 13 9 12.5523 9 12C9 11.4477 8.55228 11 8 11C7.44772 11 7 11.4477 7 12C7 12.5523 7.44772 13 8 13Z" fill="currentColor"/>
        <path d="M16 13C16.5523 13 17 12.5523 17 12C17 11.4477 16.5523 11 16 11C15.4477 11 15 11.4477 15 12C15 12.5523 15.4477 13 16 13Z" fill="currentColor"/>
        <path d="M12 17C14 17 15.5 15.5 15.5 14H8.5C8.5 15.5 10 17 12 17Z" fill="currentColor"/>
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

      <!-- 对话内容区 -->
      <div class="chat-messages" ref="messageContainer">
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

      <!-- 底部输入栏 -->
      <div class="panel-input-area">
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
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const isOpen = ref(false)
const isLoading = ref(false)
const isReindexing = ref(false)
const inputMsg = ref('')
const messages = ref([])
const messageContainer = ref(null)
const inputBox = ref(null)

// 从后端拉取当前登录用户的对话历史记录
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
        messages.value = data.data.map(item => ({
          role: item.role,
          content: item.content,
          time: item.time || ''
        }))
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
    loadChatHistory()
    nextTick(() => {
      inputBox.value?.focus()
      scrollToBottom()
    })
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

  // 2. 新增 AI 占位消息，准备打字机流式接收数据
  const assistantMsgIndex = messages.value.push({
    role: 'assistant',
    content: '',
    time: ''
  }) - 1

  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${baseUrl}/ai/chat?message=${encodeURIComponent(query)}`

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
        // Ignore parsing errors, fall back to default status message
      }
      throw new Error(`连接服务失败，状态码: ${response.status}`)
    }

    isLoading.value = false // 收到首帧后关闭 Loading 指示器
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      
      // SSE 协议解析：按行分割
      const lines = buffer.split('\n')
      // 最后一行可能不完整，留到下一批处理
      buffer = lines.pop()

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed) continue

        if (trimmed.startsWith('data:')) {
          const dataContent = trimmed.slice(5).trim()
          
          // 判断是否是 complete 事件发出的完成信号
          if (dataContent === '[DONE]') {
            continue
          }
          
          // 拼接打字机 Token
          messages.value[assistantMsgIndex].content += dataContent
          messages.value[assistantMsgIndex].time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          scrollToBottom()
        } else if (trimmed.startsWith('event:error')) {
          // 捕获错误事件
          throw new Error('AI 生成时发生内部错误')
        }
      }
    }
  } catch (error) {
    isLoading.value = false
    messages.value[assistantMsgIndex].content = `请求失败: ${error.message}。请检查网络连接或接口配置。`
    messages.value[assistantMsgIndex].time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
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

/* 适配移动端 */
@media (max-width: 480px) {
  .ai-chat-panel {
    width: 100%;
    right: -100%;
  }
}
</style>
