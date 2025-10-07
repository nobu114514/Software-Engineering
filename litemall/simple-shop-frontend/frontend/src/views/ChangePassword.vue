<template>
  <div class="change-password">
    <h1>修改密码</h1>
    
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
        <a href="/seller/dashboard" class="btn btn-secondary">取消</a>
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
    }
  }
}
</script>

<style scoped>
.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}
</style>
