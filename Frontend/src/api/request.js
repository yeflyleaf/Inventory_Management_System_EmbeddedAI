/**
 * 模块名称: Axios 请求封装
 * 功能描述: 封装 Axios 实例，配置请求拦截器（添加 Token）和响应拦截器（统一错误处理、数据解包）。
 */
import axios from 'axios'

const service = axios.create({
  // API 基础路径：
  // 1. 优先使用环境变量 VITE_API_BASE_URL (Electron 模式下会读取 .env.electron 中的值)
  // 2. 如果未设置 (Web 模式)，则使用 '/api'，由 Nginx 或 Vite 开发服务器代理转发
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', 
  timeout: 5000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data   //第一次解包

    // 检查后端统一的响应结构
    if (res.success === false) {
      // 后端返回失败
      const error = new Error(res.message || '请求失败')
      error.code = res.code
      return Promise.reject(error)
    }

    // 返回实际的数据部分
    return res.data   //第二次解包
  },
  (error) => {
    console.error('Request Error:', error)

    // 处理 HTTP 错误
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '服务器错误'

      // 401 未授权 - 跳转登录
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }

      // 403 权限不足
      if (status === 403) {
        return Promise.reject(new Error(message || '权限不足，无法访问此资源'))
      }

      return Promise.reject(new Error(message))
    }

    return Promise.reject(error)
  },
)

export default service
