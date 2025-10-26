<template>
  <div class="login-container">
    <div class="login-card card">
      <h2>客户登录</h2>
      
      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-danger']">
        {{ message }}
      </div>
      
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="form.username" 
            required
            placeholder="请输入用户名"
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            required
            placeholder="请输入密码"
          >
        </div>
        
        <button type="submit" class="btn primary-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '登录中...' : '登录' }}
        </button>
        
        <div class="register-link">
          还没有账号？<a href="/register" class="primary-link">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerLogin',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      message: '',
      success: false,
      isSubmitting: false
    }
  },
  created() {
    // 如果已登录为用户，跳转到首页
    if (localStorage.getItem('customerLoggedIn')) {
      this.$router.push('/')
    }
    // 如果已登录为卖家，提示并跳转到首页
    if (localStorage.getItem('sellerLoggedIn')) {
      this.message = '请先退出卖家登录'
      this.success = false
      setTimeout(() => {
        this.$router.push('/')
      }, 2000)
    }
  },
  methods: {
    async login() {
      this.isSubmitting = true;
      this.message = '';
      
      try {
        const response = await this.$axios.post('/login', null, {
          params: {
            username: this.form.username,
            password: this.form.password
          }
        });
        
        const result = response.data;
        if (result.success) {
          // 保存登录状态
          localStorage.setItem('customerLoggedIn', 'true');
          localStorage.setItem('customerUsername', this.form.username);
          
          this.success = true;
          this.message = '登录成功，正在跳转到首页...';
          
          // 跳转到首页
          setTimeout(() => {
            this.$router.push('/');
          }, 1000);
        } else {
          this.success = false;
          this.message = result.message;
        }
      } catch (err) {
        this.success = false;
        this.message = '登录失败，请稍后重试';
        console.error(err);
      } finally {
        this.isSubmitting = false;
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
  min-height: 70vh;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: #fff;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #E02E24; /* 拼多多红 */
}

.alert {
  padding: 12px;
  margin-bottom: 20px;
  border-radius: 4px;
  text-align: center;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.btn {
  width: 100%;
  padding: 12px;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.primary-btn {
  background-color: #E02E24; /* 拼多多红 */
}

.primary-btn:hover:not(:disabled) {
  background-color: #c81623;
}

.btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.primary-link {
  color: #E02E24; /* 拼多多红 */
  text-decoration: none;
  transition: opacity 0.2s;
}

.primary-link:hover {
  opacity: 0.8;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>