import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { defineConfig } from 'vite'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => ({
  // 基础路径配置：
  // 1. Electron/Android 模式下使用 './' (相对路径)，因为加载的是本地文件 (file://)
  // 2. Web 模式下使用 '/' (绝对路径)，适配 Nginx 等 Web 服务器的部署结构
  base: mode === 'electron' || mode === 'android' ? './' : '/',
  plugins: [vue(), vueJsx(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // ============================================================================
  // 构建配置 - 安全设置
  // ============================================================================
  build: {
    // 1. 显式配置：构建前清空输出目录 (默认是 dist)
    emptyOutDir: true, // <--- 先清除后构建
    outDir: 'dist', // <--- (可选) 明确指定输出目录名，默认就是 dist
    // 禁用 source maps，防止生产环境源码泄露
    sourcemap: false,

    // 压缩混淆配置
    // 方案 A：如果你想用 Terser (移除 log 更彻底，但需要安装 terser)
    // 需先运行：npm add -D terser
    // minify: 'terser',
    // terserOptions: {
    //   compress: {
    //     drop_console: true, // 直接设为 true 即可移除所有 console
    //     drop_debugger: true
    //   }
    // }

    // 方案 B (推荐)：继续使用默认的 Esbuild (速度快，不用安装额外包)
    // 如果选这个，就把上面的 terserOptions 删掉，换成下面这个：
    minify: 'esbuild',
    esbuild: {
      drop: ['console', 'debugger'], // esbuild 移除 console 的原生配置
    },
  },
  server: {
    port: 5173, // <--- 设置前端端口
    proxy: {
      // AI 微服务代理 (必须放在 /api 前面，以防被覆盖)
      '/api/ai': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/ai/, '/ai'),
      },
      // Java 后端代理
      '/api': {
        target: 'http://localhost:8080', // <--- 设置后端地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/uploads': {
        target: 'http://localhost:8080', // <--- 图片资源代理到后端
        changeOrigin: true,
      },
    },
  },
}))
