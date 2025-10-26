<template>
  <div class="login-container">
    <div class="login-card card">
      <h2>卖家登录</h2>
      
      <div v-if="error" class="alert alert-danger">
        {{ error }}
      </div>
      
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="submit" class="btn">登录</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SellerLogin',
  data() {
    return {
      username: '',
      password: '',
      error: ''
    }
  },
  created() {
    // 如果已登录为卖家，跳转到后台
    if (localStorage.getItem('sellerLoggedIn')) {
      this.$router.push('/seller/dashboard')
    }
    // 如果已登录为用户，提示并跳转到首页
    if (localStorage.getItem('customerLoggedIn')) {
      this.error = '请先退出用户登录'
      setTimeout(() => {
        this.$router.push('/')
      }, 2000)
    }
  },
  methods: {
    async login() {
      try {
        const response = await this.$axios.post('/seller/login', null, {
          params: {
            username: this.username,
            password: this.password
          }
        })
        
        if (response.data) {
          localStorage.setItem('sellerLoggedIn', 'true')
          this.$router.push('/seller/dashboard')
        } else {
          this.error = '用户名或密码错误'
        }
      } catch (err) {
        this.error = '登录失败，请重试'
        console.error(err)
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.login-card {
  width: 100%;
  max-width: 400px;
}
</style>
