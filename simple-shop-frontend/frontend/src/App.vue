<template>
  <div id="app">
    <nav class="navbar">
      <div class="navbar-container">
        <a href="/" class="navbar-logo">拼夕夕</a>
        <div class="navbar-menu">
          <!-- 用户入口 - 未登录状态且卖家未登录时显示 -->
          <a href="/login" v-if="!isCustomerLoggedIn && !isSellerLoggedIn" class="navbar-link">用户登录</a>
          <a href="/register" v-if="!isCustomerLoggedIn && !isSellerLoggedIn" class="navbar-link">注册账号</a>
          
          <!-- 用户已登录状态 -->
          <span v-if="isCustomerLoggedIn">{{ customerUsername || '用户' }}</span>
          <a href="/orders" v-if="isCustomerLoggedIn" class="navbar-link">我的订单</a>
          <button @click="customerLogout" v-if="isCustomerLoggedIn" class="navbar-btn">退出登录</button>
          
          <!-- 卖家入口 - 未登录状态且用户未登录时显示 -->
          <a href="/seller/login" v-if="!isSellerLoggedIn && !isCustomerLoggedIn">卖家入口</a>
          <a href="/seller/dashboard" v-if="isSellerLoggedIn">卖家后台</a>
          <button @click="sellerLogout" v-if="isSellerLoggedIn">退出登录</button>
        </div>
      </div>
    </nav>
    <main class="container">
      <router-view />
    </main>
    <footer class="footer">
      <p>拼夕夕</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isSellerLoggedIn: false,
      isCustomerLoggedIn: false,
      customerUsername: ''
    }
  },
  created() {
    this.checkLoginStatus()
  },
  methods: {
    checkLoginStatus() {
      // 检查卖家登录状态
      this.isSellerLoggedIn = !!localStorage.getItem('sellerToken')
      // 检查客户登录状态
      this.isCustomerLoggedIn = !!localStorage.getItem('customerToken')
      this.customerUsername = localStorage.getItem('customerUsername') || ''
    },
    // 卖家退出登录
    sellerLogout() {
      localStorage.removeItem('sellerToken')
      localStorage.removeItem('sellerUsername')
      this.isSellerLoggedIn = false
      this.$router.push('/seller/login')
    },
    // 客户退出登录
    customerLogout() {
      localStorage.removeItem('customerToken')
      localStorage.removeItem('customerUsername')
      this.isCustomerLoggedIn = false
      this.customerUsername = ''
      this.$router.push('/login')
    }
  },
  watch: {
    $route() {
      this.checkLoginStatus()
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "PingFang SC", "Helvetica Neue", Arial, sans-serif;
}

body {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  color: #333;
}

/* 导航栏样式 - 拼多多红色主题 */
.navbar {
  background-color: #E02E24; /* 拼多多红 */
  color: white;
  padding: 0.8rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-logo {
  color: white;
  text-decoration: none;
  font-size: 1.8rem;
  font-weight: bold;
  letter-spacing: 1px;
}

.navbar-menu {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.navbar-menu a, .navbar-link {
  color: white;
  text-decoration: none;
  font-size: 1rem;
  font-weight: 500;
  transition: opacity 0.2s;
}

.navbar-menu a:hover, .navbar-link:hover {
  opacity: 0.8;
}

.navbar-menu button, .navbar-btn {
  background: white;
  color: #E02E24;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}

.navbar-menu button:hover {
  background-color: #f8f8f8;
}

.container {
  flex: 1;
  max-width: 1200px;
  margin: 1.5rem auto;
  padding: 0 1rem;
}

/* 页脚样式调整 */
.footer {
  background-color: white;
  color: #666;
  text-align: center;
  padding: 1.5rem 0;
  margin-top: auto;
  border-top: 1px solid #eee;
}

/* 通用样式调整 - 保持功能不变 */
.btn {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #E02E24; /* 拼多多红主按钮 */
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  font-size: 1rem;
  transition: background-color 0.2s;
}

.btn-secondary {
  background-color: #f0c14b; /* 辅助色 */
  color: #111;
}

.btn:hover {
  opacity: 0.9;
  background-color: #c81623;
}

.btn-secondary:hover {
  background-color: #e6b325;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #E02E24;
  outline: none;
}

.card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.2rem;
  margin-bottom: 1rem;
  background-color: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.alert {
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.alert-success {
  background-color: #dff0d8;
  color: #3c763d;
  border: 1px solid #d6e9c6;
}

.alert-danger {
  background-color: #fce4e4; /* 更贴近拼多多红色调的错误提示 */
  color: #E02E24;
  border: 1px solid #fccac9;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
}

table th,
table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

table th {
  background-color: #f9f9f9;
  color: #666;
  font-weight: 600;
}

.dashboard-menu {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}
</style>
