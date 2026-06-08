/**
 * 模块名称: Electron 预加载脚本 (Preload Script)
 * 功能描述: 
 * 1. 作为渲染进程(Vue页面)和主进程(Electron)之间的安全桥梁
 * 2. 可以在这里暴露安全的 API 给前端使用
 * 3. 运行在隔离的上下文中，既能访问 Node.js API，又能访问 DOM
 */
const { contextBridge } = require('electron')

// 通过 contextBridge 将 API 暴露给渲染进程的 window 对象
// 前端可以通过 window.electronAPI 访问这些方法
contextBridge.exposeInMainWorld('electronAPI', {
  // 可以在这里暴露一些安全的 API 给渲染进程
  // 例如: sendNotification: (message) => ipcRenderer.send('notify', message)
})
