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

// 全局注册axios
const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router).mount('#app')
