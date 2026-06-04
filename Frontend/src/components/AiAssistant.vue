<!--
  模块名称: AI 仓储智能助手
  功能描述: 提供全系统可用的悬浮 AI 对话面板，支持打字机流式输出、工具调用展示、商品数据同步和快捷指令。
-->
<template>
  <div class="ai-assistant-wrapper">
    <!-- 悬浮球按钮 -->
    <button class="ai-floating-btn" :class="{ 'panel-open': isOpen }" @click="togglePanel" title="AI 仓储助手">
      <div class="glow-effect"></div>
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
          <div class="assistant-avatar">🤖</div>
          <div class="title-meta">
            <h4>AI 智能仓储助手</h4>
            <span class="status-online"><span class="dot"></span>在线分析中</span>
          </div>
        </div>
        <div class="header-actions">
          <button class="action-btn reindex-btn" @click="triggerReindex" :disabled="isReindexing" title="全量商品向量同步">
            🔄 {{ isReindexing ? '同步中...' : '同步向量' }}
          </button>
          <button class="close-btn" @click="isOpen = false" title="关闭面板">&times;</button>
        </div>
      </div>

      <!-- 对话内容区 -->
      <div class="chat-messages" ref="messageContainer">
        <!-- 欢迎卡片 -->
        <div class="welcome-card" v-if="messages.length === 0">
          <div class="welcome-icon">✨</div>
          <h3>您好，我是您的 AI 仓储分析师！</h3>
          <p>我可以帮您查询实时库存、寻找低库存商品、统计分类占比并给出补货建议。您可以点击下方快捷指令开始：</p>
          
          <div class="quick-chips">
            <button class="chip" @click="sendQuickPrompt('🔍 哪些商品快断货了？')">
              <span class="chip-icon">⚠️</span> 低库存警报
            </button>
            <button class="chip" @click="sendQuickPrompt('📊 统计一下各个分类的库存占比？')">
              <span class="chip-icon">📈</span> 品类库存分布
            </button>
            <button class="chip" @click="sendQuickPrompt('📋 根据当前低库存情况，帮我生成一份详细的补货建议？')">
              <span class="chip-icon">✍️</span> 生成补货方案
            </button>
            <button class="chip" @click="sendQuickPrompt('📦 仓库里有哪些商品？')">
              <span class="chip-icon">🛍️</span> 商品规格查询
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-else v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
          <div class="msg-avatar" v-if="msg.role === 'assistant'">🤖</div>
          <div class="msg-avatar user" v-else>👤</div>
          
          <div class="msg-bubble">
            <div class="msg-text" v-html="renderMarkdown(msg.content)"></div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 加载中动画 -->
        <div class="message-item assistant loading" v-if="isLoading">
          <div class="msg-avatar">🤖</div>
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
    content: '_正在重建全量商品向量索引，请稍候..._',
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
        content: '✅ **向量重建同步完成！** 所有商品已被转化为特征向量同步至 Redis Search，您现在可以进行智能商品与库存分析了。',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      })
    } else {
      throw new Error('同步失败，服务端返回异常')
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: `❌ **向量同步失败**: ${error.message}。请确认 Redis Stack 服务以及后端是否配置正常。`,
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
    messages.value[assistantMsgIndex].content = `⚠️ **发生错误**: ${error.message}`
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
  // 合并相邻的 ul 元素
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

/* 悬浮球核心样式 */
.ai-floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.ai-floating-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.6);
}

.ai-floating-btn.panel-open {
  transform: rotate(90deg) scale(0.95);
  background: #ff5252;
  box-shadow: 0 4px 12px rgba(255, 82, 82, 0.4);
}

.ai-icon {
  width: 28px;
  height: 28px;
  transition: transform 0.3s;
}

/* 气泡呼吸灯特效 */
.glow-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.5);
  animation: pulse 2.5s infinite cubic-bezier(0.4, 0, 0.6, 1);
  pointer-events: none;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.7); }
  70% { box-shadow: 0 0 0 15px rgba(102, 126, 234, 0); }
  100% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0); }
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
  transition: all 0.3s;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.ai-floating-btn:hover .btn-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(0);
}

/* 侧边滑出控制台 */
.ai-chat-panel {
  position: fixed;
  top: 0;
  right: -430px;
  width: 400px;
  height: 100vh;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.08);
  border-left: 1px solid rgba(255, 255, 255, 0.45);
  display: flex;
  flex-direction: column;
  transition: right 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
}

.ai-chat-panel.active {
  right: 0;
}

/* 头部样式 */
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.5);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-avatar {
  font-size: 1.6rem;
}

.title-meta h4 {
  margin: 0;
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 700;
}

.status-online {
  font-size: 0.7rem;
  color: #2ecc71;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  font-weight: 500;
}

.status-online .dot {
  width: 6px;
  height: 6px;
  background-color: #2ecc71;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 6px #2ecc71;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reindex-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  box-shadow: 0 2px 6px rgba(17, 153, 142, 0.2);
}

.reindex-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.reindex-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #7f8c8d;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

/* 消息对话列表区 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: rgba(248, 249, 250, 0.4);
}

/* 欢迎引导卡片 */
.welcome-card {
  text-align: center;
  padding: 30px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.02);
  border: 1px solid rgba(255, 255, 255, 0.9);
  margin-top: 10px;
}

.welcome-icon {
  font-size: 2.2rem;
  margin-bottom: 12px;
}

.welcome-card h3 {
  margin: 0 0 8px;
  font-size: 1.1rem;
  color: #2c3e50;
  font-weight: 700;
}

.welcome-card p {
  font-size: 0.85rem;
  color: #7f8c8d;
  line-height: 1.5;
  margin: 0 0 20px;
}

.quick-chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.chip {
  background: #f1f2f6;
  border: 1px solid rgba(0,0,0,0.04);
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 0.8rem;
  color: #475569;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.chip:hover {
  background: #e2e8f0;
  transform: scale(1.03);
  color: #667eea;
}

.chip-icon {
  font-size: 0.9rem;
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
  max-width: 85%;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  font-size: 1.1rem;
  flex-shrink: 0;
}

.msg-avatar.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.95rem;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 0.88rem;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.assistant .msg-bubble {
  background: white;
  color: #2c3e50;
  border-top-left-radius: 2px;
  border: 1px solid rgba(0,0,0,0.04);
}

.user .msg-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-top-right-radius: 2px;
}

.msg-time {
  font-size: 0.7rem;
  color: #94a3b8;
  align-self: flex-end;
  margin-top: 2px;
}

.user .msg-time {
  color: rgba(255,255,255,0.7);
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
  background: rgba(0,0,0,0.2);
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
  border-top: 1px solid rgba(0,0,0,0.06);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.7);
}

.panel-input-area textarea {
  flex: 1;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 0.88rem;
  resize: none;
  outline: none;
  font-family: inherit;
  line-height: 1.4;
  background: white;
  transition: border-color 0.2s;
}

.panel-input-area textarea:focus {
  border-color: #667eea;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

/* 适配移动端 */
@media (max-width: 480px) {
  .ai-chat-panel {
    width: 100%;
    right: -100%;
  }
}
</style>
