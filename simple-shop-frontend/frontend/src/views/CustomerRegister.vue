<template>
  <div class="register-container">
    <div class="register-card card">
      <h2>客户注册</h2>
      
      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-danger']">
        {{ message }}
      </div>
      
      <form @submit.prevent="register">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="form.username" 
            required
            placeholder="请输入用户名"
          >
          <span v-if="errors.username" class="error">{{ errors.username }}</span>
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            required
            placeholder="请输入至少6位密码"
          >
          <span v-if="errors.password" class="error">{{ errors.password }}</span>
        </div>
        
        <div class="form-group">
          <label for="phone">电话</label>
          <input 
            type="tel" 
            id="phone" 
            v-model="form.phone" 
            required
            placeholder="请输入11位手机号码"
          >
          <span v-if="errors.phone" class="error">{{ errors.phone }}</span>
        </div>
        
        <div class="form-group">
          <label for="defaultLocation">默认交易地点</label>
          <input 
            type="text" 
            id="defaultLocation" 
            v-model="form.defaultLocation" 
            required
            placeholder="请输入默认交易地点"
          >
        </div>
        
        <button type="submit" class="btn primary-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '注册中...' : '注册' }}
        </button>
        
        <div class="login-link">
          已有账号？<a href="/login" class="primary-link">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerRegister',
  data() {
    return {
      form: {
        username: '',
        password: '',
        phone: '',
        defaultLocation: ''
      },
      errors: {},
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
      this.errorMessage = '请先退出卖家登录'
      setTimeout(() => {
        this.$router.push('/')
      }, 2000)
    }
  },
  methods: {
    // 客户端验证
    validateForm() {
      this.errors = {};
      let isValid = true;
      
      // 验证手机号
      if (!this.form.phone || !this.form.phone.match(/^1[3-9]\d{9}$/)) {
        this.errors.phone = '请输入有效的11位手机号码';
        isValid = false;
      }
      
      // 验证密码长度
      if (!this.form.password || this.form.password.length < 6) {
        this.errors.password = '密码长度不能少于6位';
        isValid = false;
      }
      
      // 验证用户名
      if (!this.form.username || this.form.username.trim().length === 0) {
        this.errors.username = '用户名不能为空';
        isValid = false;
      }
      
      return isValid;
    },
    
    async register() {
      // 客户端验证
      if (!this.validateForm()) {
        return;
      }
      
      this.isSubmitting = true;
      this.message = '';
      
      try {
        const response = await this.$axios.post('/register', null, {
          params: {
            username: this.form.username,
            password: this.form.password,
            phone: this.form.phone,
            defaultLocation: this.form.defaultLocation
          }
        });
        
        const result = response.data;
        if (result.success) {
          this.success = true;
          this.message = result.message;
          
          // 注册成功后自动登录
          await this.autoLogin();
        } else {
          this.success = false;
          this.message = result.message;
        }
      } catch (err) {
        this.success = false;
        this.message = '注册失败，请稍后重试';
        console.error(err);
      } finally {
        this.isSubmitting = false;
      }
    },
    
    async autoLogin() {
      try {
        const response = await this.$axios.post('/login', null, {
          params: {
            username: this.form.username,
            password: this.form.password
          }
        });
        
        if (response.data.success) {
          // 保存登录状态
          localStorage.setItem('customerLoggedIn', 'true');
          localStorage.setItem('customerUsername', this.form.username);
          
          // 跳转到首页
          setTimeout(() => {
            this.$router.push('/');
          }, 1000);
        }
      } catch (err) {
        console.error('自动登录失败:', err);
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 450px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: #fff;
}

.register-card h2 {
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

.error {
  display: block;
  color: #f44336;
  font-size: 14px;
  margin-top: 5px;
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

.login-link {
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

.login-link a:hover {
  text-decoration: underline;
}
</style>