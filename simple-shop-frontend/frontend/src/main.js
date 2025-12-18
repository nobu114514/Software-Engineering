import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 配置axios
axios.defaults.baseURL = 'http://localhost:8081/api'
// 允许跨域携带cookie
axios.defaults.withCredentials = true

// 添加请求拦截器，自动添加JWT token
axios.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const sellerToken = localStorage.getItem('sellerToken')
    const customerToken = localStorage.getItem('customerToken')
    
    // 根据当前路径判断使用哪个token
    if (config.url && config.url.startsWith('/seller')) {
      if (sellerToken) {
        config.headers.Authorization = `Bearer ${sellerToken}`
      }
    } else {
      if (customerToken) {
        config.headers.Authorization = `Bearer ${customerToken}`
      }
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器，处理401错误
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 清除localStorage中的token
      localStorage.removeItem('sellerToken')
      localStorage.removeItem('sellerUsername')
      localStorage.removeItem('customerToken')
      localStorage.removeItem('customerUsername')
      
      // 跳转到登录页面
      router.push('/login')
      
      // 显示错误信息
      alert('登录已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

// 全局注册axios
const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router).mount('#app')
