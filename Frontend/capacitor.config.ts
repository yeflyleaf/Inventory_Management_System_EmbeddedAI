import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.inventory.system',
  appName: 'Inventory System',
  webDir: 'dist',
  
  // Android 服务器配置
  android: {
    // 允许混合内容 (HTTPS 页面加载 HTTP 资源)
    allowMixedContent: true,
  },
  
  // 服务器配置
  server: {
    // 关键配置：使用 http 而不是 https 加载 Android 应用
    // 这可以避免 "Mixed Content" 错误，允许应用访问 http://10.0.2.2 后端
    androidScheme: 'http',
    
    // 允许导航到任何主机 (如后端API服务器)
    allowNavigation: ['*'],
    // 清除 WebView 缓存，确保获取最新内容
    cleartext: true,
  }
};

export default config;
