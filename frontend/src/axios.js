import axios from 'axios'

// 创建axios实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从localStorage中获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    console.log('API Request:', config.method?.toUpperCase(), config.url)
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 直接返回响应数据中的data字段
    console.log('API Response:', response.config.method?.toUpperCase(), response.config.url, response.status)
    console.log('Response data:', response.data)
    return response.data
  },
  error => {
    console.error('Response error:', error.response || error)
    // 处理响应错误
    if (error.response) {
      const { status } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          window.location.href = '/login'
          break
        // 移除其他错误情况下的alert弹窗，让页面内的错误处理逻辑来显示错误信息
        default:
          // 不显示alert，仅记录错误
          console.error('API Error Status:', status)
      }
    } else {
      // 仅记录错误，不显示alert
      console.error('Network or configuration error:', error.message || error)
    }
    return Promise.reject(error)
  }
)

export default instance