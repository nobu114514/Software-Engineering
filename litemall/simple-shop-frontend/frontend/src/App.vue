<template>
  <div id="app">
    <nav class="navbar">
      <div class="navbar-container">
        <a href="/" class="navbar-logo">极简商店</a>
        <div class="navbar-menu">
          <a href="/seller/login" v-if="!isSellerLoggedIn">卖家登录</a>
          <a href="/seller/dashboard" v-if="isSellerLoggedIn">卖家后台</a>
          <button @click="logout" v-if="isSellerLoggedIn">退出登录</button>
        </div>
      </div>
    </nav>
    <main class="container">
      <router-view />
    </main>
    <footer class="footer">
      <p>© 2023 极简商店 - 保留所有权利</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isSellerLoggedIn: false
    }
  },
  created() {
    this.checkLoginStatus()
  },
  methods: {
    checkLoginStatus() {
      this.isSellerLoggedIn = !!localStorage.getItem('sellerLoggedIn')
    },
    logout() {
      localStorage.removeItem('sellerLoggedIn')
      this.isSellerLoggedIn = false
      this.$router.push('/seller/login')
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
  font-family: Arial, sans-serif;
}

body {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  background-color: #333;
  color: white;
  padding: 1rem 0;
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
  font-size: 1.5rem;
  font-weight: bold;
}

.navbar-menu {
  display: flex;
  gap: 1rem;
}

.navbar-menu a {
  color: white;
  text-decoration: none;
}

.navbar-menu button {
  background: none;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  cursor: pointer;
}

.container {
  flex: 1;
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 1rem 0;
  margin-top: auto;
}

/* 通用样式 */
.btn {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  font-size: 1rem;
}

.btn-secondary {
  background-color: #f44336;
}

.btn:hover {
  opacity: 0.9;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.card {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
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
  background-color: #f2dede;
  color: #a94442;
  border: 1px solid #ebccd1;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

table th,
table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

table th {
  background-color: #f5f5f5;
}

.dashboard-menu {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}
</style>
