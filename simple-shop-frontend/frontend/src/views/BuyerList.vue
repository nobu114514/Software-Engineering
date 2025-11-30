<template>
  <div class="buyer-list">
    <div class="header-container">
      <h1>购买意向列表</h1>
      <button class="btn btn-secondary" @click="goBack">返回</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <table v-if="!loading && buyers.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>商品</th>
          <th>购买人</th>
          <th>电话</th>
          <th>地址</th>
          <th>时间</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="buyer in buyers" :key="buyer.id">
          <td>{{ buyer.id }}</td>
          <td>{{ buyer.product?.name || '未知商品' }}</td>
          <td>{{ buyer.name }}</td>
          <td>{{ buyer.phone }}</td>
          <td>{{ buyer.address }}</td>
          <td>{{ formatDate(buyer.createdAt) }}</td>
          <td>
            <span v-if="buyer.completed" class="status-completed">已完成</span>
            <span v-else class="status-pending">处理中</span>
          </td>
          <td>
            <template v-if="!buyer.completed">
              <button class="btn" @click="completeTransaction(buyer.id, true)">交易成功</button>
              <button class="btn btn-secondary" @click="completeTransaction(buyer.id, false)">交易失败</button>
            </template>
            <span v-else>已处理</span>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && buyers.length === 0" class="alert alert-danger">
      暂无购买意向记录
    </div>
  </div>
</template>

<script>
export default {
  name: 'BuyerList',
  data() {
    return {
      buyers: [],
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchBuyers()
  },
  methods: {
    async fetchBuyers() {
      try {
        this.loading = true
        // 从localStorage获取当前登录的卖家用户名
        const sellerUsername = localStorage.getItem('sellerUsername')
        
        // 构建请求头，添加认证信息
        const headers = {
          'X-Username': sellerUsername
        }
        
        // 发送API请求，带上认证信息
        const response = await this.$axios.get('/buyers', {
          headers: headers
        })
        this.buyers = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取购买意向失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async completeTransaction(buyerId, success) {
      try {
        // 从localStorage获取当前登录的卖家用户名
        const sellerUsername = localStorage.getItem('sellerUsername')
        
        // 构建请求头，添加认证信息
        const headers = {
          'X-Username': sellerUsername
        }
        
        await this.$axios.put(`/buyers/${buyerId}/complete`, null, {
          params: { success },
          headers: headers
        })
        // 刷新列表
        this.fetchBuyers()
      } catch (err) {
        this.error = '操作失败，请重试'
        console.error(err)
        // 显示错误信息3秒后自动清除
        setTimeout(() => {
          this.error = ''
        }, 3000)
      }
    },
    // 返回上一页
    goBack() {
      this.$router.push('/seller/dashboard')
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      // 格式化日期为更友好的格式
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

th, td {
  border: 1px solid #ddd;
  padding: 0.8rem;
  text-align: left;
}

th {
  background-color: #f8f9fa;
  font-weight: bold;
}

tr:nth-child(even) {
  background-color: #f2f2f2;
}

.btn {
  padding: 0.3rem 0.6rem;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

h1 {
  margin-bottom: 0;
}

.btn-secondary {
  background: #6c757d;
}

.btn-secondary:hover {
  background: #545b62;
}

.status-completed {
  color: #28a745;
  font-weight: bold;
}

.status-pending {
  color: #ffc107;
  font-weight: bold;
}

.alert {
  padding: 1rem;
  margin: 1rem 0;
  border-radius: 4px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
</style>
