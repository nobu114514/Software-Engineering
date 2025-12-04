<template>
  <div class="container">
    <div class="header-container">
      <h2>修改密码</h2>
      <button class="btn btn-secondary" @click="goBack">返回</button>
    </div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="success" class="alert alert-success">
      密码修改成功
    </div>
    
    <form @submit.prevent="changePassword" class="card">
      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="username" required>
      </div>
      <div class="form-group">
        <label for="oldPassword">旧密码</label>
        <input type="password" id="oldPassword" v-model="oldPassword" required>
      </div>
      <div class="form-group">
        <label for="newPassword">新密码</label>
        <input type="password" id="newPassword" v-model="newPassword" required>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn">修改密码</button>
      </div>
    </form>
  </div>
</template>

<script>
export default {
  name: 'ChangePassword',
  data() {
    return {
      username: '',
      oldPassword: '',
      newPassword: '',
      error: '',
      success: false
    }
  },
  methods: {
    async changePassword() {
      try {
        const response = await this.$axios.post('/seller/change-password', null, {
          params: {
            username: this.username,
            oldPassword: this.oldPassword,
            newPassword: this.newPassword
          }
        })
        
        if (response.data) {
          this.success = true
          this.error = ''
          // 重置表单
          this.username = ''
          this.oldPassword = ''
          this.newPassword = ''
          // 3秒后跳转到后台首页
          setTimeout(() => {
            this.$router.push('/seller/dashboard')
          }, 3000)
        } else {
          this.error = '旧密码不正确'
          this.success = false
        }
      } catch (err) {
        this.error = '修改密码失败，请重试'
        this.success = false
        console.error(err)
      }
    },
    
    // 返回上一页
    goBack() {
      this.$router.push('/seller/dashboard')
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn.btn-secondary {
  padding: 8px 16px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
}

.btn.btn-secondary:hover {
  background-color: #5a6268;
}

.card {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.btn {
  display: inline-block;
  padding: 8px 16px;
  background-color: #E02E24;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn:hover {
  background-color: #c82333;
}

.alert {
  padding: 12px;
  margin-bottom: 15px;
  border-radius: 4px;
  font-size: 14px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}
</style>
