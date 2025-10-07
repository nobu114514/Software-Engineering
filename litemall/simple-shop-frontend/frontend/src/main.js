import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 配置axios
axios.defaults.baseURL = 'http://localhost:8081/api'
// 允许跨域携带cookie
axios.defaults.withCredentials = true

// 全局注册axios
const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router).mount('#app')
