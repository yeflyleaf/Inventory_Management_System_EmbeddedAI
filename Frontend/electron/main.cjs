/* eslint-env node */
const { app, BrowserWindow, shell } = require('electron')
const path = require('path')

// ============================================================================
// Electron 主进程入口文件
// 负责创建窗口、处理系统事件、管理应用生命周期
// ============================================================================

// 屏蔽安全警告 (控制台不显示安全警告信息)
process.env['ELECTRON_DISABLE_SECURITY_WARNINGS'] = 'true'

// ============================================================================
// 安全策略配置 (白名单/黑名单)
// 用于防止恶意网站在应用内打开，保护用户安全
// ============================================================================
const SECURITY_POLICY = {
  // 【推荐】白名单：仅允许访问以下受信任的域名
  // 只有列表中的域名才允许在 Electron 窗口内直接加载
  WHITELIST: [
    'http://localhost:5173', // 开发环境前端地址
    'http://localhost:8080', // 后端 API 地址 (如果涉及页面跳转)
  ],
  // 黑名单：明确禁止访问的域名 (通常配合白名单使用，或者作为补充)
  // 即使在白名单中，如果匹配黑名单也会被拦截
  BLACKLIST: [
    // 'malicious-site.com', // 示例：恶意网站
    // 'phishing.com'        // 示例：钓鱼网站
  ],
}

/**
 * 检查 URL 是否安全
 * @param {string} url - 需要检查的 URL
 * @returns {boolean} - true 表示安全，false 表示不安全
 */
function isSafeUrl(url) {
  try {
    const parsedUrl = new URL(url)

    // 1. 允许本地文件 (生产环境运行必需，因为加载的是 file:// 协议的 index.html)
    if (parsedUrl.protocol === 'file:') return true

    // 2. 检查黑名单 (优先级最高)
    // 如果域名包含黑名单中的关键字，直接拦截
    if (SECURITY_POLICY.BLACKLIST.some((domain) => parsedUrl.hostname.includes(domain))) {
      return false
    }

    // 3. 检查白名单
    // 如果 URL 的源 (Origin) 在白名单中，则允许访问
    if (SECURITY_POLICY.WHITELIST.includes(parsedUrl.origin)) {
      return true
    }

    // 默认拦截所有其他未知域名
    return false
  } catch {
    // URL 解析失败视为不安全
    return false
  }
}

let mainWindow

/**
 * 创建主应用窗口
 */
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    // frame: false, // 全部隐藏隐藏原生标题栏和边框
    titleBarStyle: 'hidden', // 隐藏原生标题栏背景，但保留窗口控制按钮
    titleBarOverlay: {
      color: '#00000000', // 设为透明，让前端背景透出来
      symbolColor: '#ee780aff', // 窗口控制按钮图标颜色 (根据你的深色主题设为白色)
      height: 30 // 按钮区域高度
    },
    autoHideMenuBar: true, // 隐藏 File/Edit 等菜单栏 (按 Alt 可显示)
    webPreferences: {
      // 指定预加载脚本 (使用 absolute path)
      preload: path.join(__dirname, 'preload.cjs'),
      // 禁用 Node.js 集成 (安全性建议)
      nodeIntegration: false,
      // 开启上下文隔离 (安全性建议，防止渲染进程直接访问 Electron API)
      contextIsolation: true,
      // webSecurity: true, // 默认为 true，保持开启以确保 CORS 和其他安全检查生效 (不要设置为 false!)
    },
    show: false, // 默认隐藏窗口，等待 ready-to-show 事件再显示，防止白屏/黑屏闪烁
  })

  // ============================================================================
  // 安全拦截器配置
  // ============================================================================

  // 1. 拦截应用内的页面跳转 (will-navigate 事件)
  // 防止恶意脚本通过 window.location.href 跳转到外部钓鱼网站
  mainWindow.webContents.on('will-navigate', (event, url) => {
    if (!isSafeUrl(url)) {
      event.preventDefault() // 阻止跳转
      console.warn(`已拦截非法跳转: ${url}`)
    }
  })

  // 2. 拦截新窗口打开 (target="_blank" 或 window.open)
  // 对于不安全的外部链接，选择在系统默认浏览器中打开 (沙箱隔离)，而不是在 App 内部打开
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (!isSafeUrl(url)) {
      console.log(`在系统浏览器中打开外部链接: ${url}`)
      shell.openExternal(url) // 调用系统默认浏览器打开
      return { action: 'deny' } // 阻止 Electron 内部创建新窗口
    }
    return { action: 'allow' } // 允许安全链接打开新窗口
  })

  // ============================================================================
  // 加载页面
  // ============================================================================

  // 开发环境下，加载 Vite 开发服务器地址 (http://localhost:5173)
  // 生产环境下，加载打包后的 index.html 文件 (file://...)
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools() // 开发模式下自动打开开发者工具
  } else {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  // 等待页面加载完成后再显示窗口，避免短暂的白屏/黑屏
  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  // 窗口关闭事件处理
  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// Electron 初始化完成
app.whenReady().then(() => {
  createWindow()

  // macOS 激活应用时重新创建窗口
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// 所有窗口关闭时退出应用 (Windows/Linux)
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
