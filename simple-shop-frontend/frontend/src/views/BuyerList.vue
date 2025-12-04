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
      
      <div v-if="success" class="alert alert-success">
        {{ success }}
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
            <span :class="getStatusClass(buyer.orderStatus)">{{ getStatusText(buyer.orderStatus) }}</span>
          </td>
          <td>
            <template v-if="buyer.orderStatus < 4">
              <button v-if="buyer.orderStatus < 1" class="btn" @click="updateOrderStatus(buyer.id, 1)" :disabled="buyer.orderStatus === 4 || buyer.orderStatus === 5">商家确认</button>
              <button v-if="buyer.orderStatus < 2" class="btn" @click="updateOrderStatus(buyer.id, 2)" :disabled="buyer.orderStatus === 4 || buyer.orderStatus === 5">备货完成</button>
              <button v-if="buyer.orderStatus < 3" class="btn" @click="updateOrderStatus(buyer.id, 3)" :disabled="buyer.orderStatus === 4 || buyer.orderStatus === 5">开始发货</button>
              <button v-if="buyer.orderStatus < 4" class="btn" @click="completeTransaction(buyer.id, true)" :disabled="buyer.orderStatus === 4 || buyer.orderStatus === 5">交易完成</button>
              <button class="btn btn-secondary" @click="completeTransaction(buyer.id, false)" :disabled="buyer.orderStatus === 4 || buyer.orderStatus === 5">交易失败</button>
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
        error: '',
        success: ''
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
        
        console.log('获取购买意向列表，用户名:', sellerUsername)
        
        // 构建请求头，添加认证信息
        const headers = {
          'X-Username': sellerUsername
        }
        
        // 发送API请求，带上认证信息
        const response = await this.$axios.get('/buyers', {
          headers: headers
        })
        
        console.log('获取购买意向成功，返回数据:', response.data)
        this.buyers = response.data
        this.error = ''
      } catch (err) {
        let errorMessage = '获取购买意向失败'
        if (err.response) {
          errorMessage = err.response.data?.message || `服务器错误: ${err.response.status}`
          console.error('获取购买意向 - 服务器错误响应:', {
            status: err.response.status,
            data: err.response.data
          })
        } else if (err.request) {
          errorMessage = '服务器无响应，请检查网络连接'
          console.error('获取购买意向 - 请求错误:', err.request)
        } else {
          errorMessage = `请求错误: ${err.message}`
          console.error('获取购买意向 - 请求配置错误:', err)
        }
        this.error = errorMessage
      } finally {
        this.loading = false
      }
    },
    async completeTransaction(buyerId, success) {
      try {
        // 交易成功 - 更新为状态4，交易失败 - 更新为状态5
        const status = success ? 4 : 5;
        await this.updateOrderStatus(buyerId, status);
      } catch (err) {
        let errorMessage = '操作失败，请重试'
        if (err.response) {
          errorMessage = err.response.data?.message || `服务器错误: ${err.response.status}`
          console.error('完成交易 - 服务器错误响应:', {
            status: err.response.status,
            data: err.response.data
          })
        } else if (err.request) {
          errorMessage = '服务器无响应，请检查网络连接'
          console.error('完成交易 - 请求错误:', err.request)
        } else {
          errorMessage = `请求错误: ${err.message}`
          console.error('完成交易 - 请求配置错误:', err)
        }
        
        this.error = errorMessage
        // 延长错误信息显示时间，便于查看
        setTimeout(() => {
          this.error = ''
        }, 10000) // 10秒后清除错误信息
      }
    },
    async updateOrderStatus(buyerId, status) {
      try {
        // 从localStorage获取当前登录的卖家用户名
        const sellerUsername = localStorage.getItem('sellerUsername')
        
        // 构建请求头，添加认证信息
        const headers = {
          'X-Username': sellerUsername
        }
        
        console.log('发送请求:', {
          url: `/api/buyers/${buyerId}/status`,
          method: 'PUT',
          params: { status },
          headers: headers
        })
        
        await this.$axios.put(`/buyers/${buyerId}/status`, null, {
          params: { status },
          headers: headers
        })
        
        // 显示成功信息
        this.error = ''
        this.success = '操作成功！'
        
        // 刷新列表
        this.fetchBuyers()
        
        // 显示成功信息3秒后自动清除
        setTimeout(() => {
          this.success = ''
        }, 3000)
      } catch (err) {
        // 增强错误信息显示
        let errorMessage = '操作失败，请重试'
        if (err.response) {
          // 服务器返回了错误响应
          if (err.response.status === 400 && err.response.data?.message) {
            errorMessage = `状态更新失败: ${err.response.data.message}`
          } else {
            errorMessage = err.response.data?.message || `服务器错误: ${err.response.status}`
          }
          console.error('服务器错误响应:', {
            status: err.response.status,
            data: err.response.data,
            headers: err.response.headers
          })
        } else if (err.request) {
          // 请求已发送但没有收到响应
          errorMessage = '服务器无响应，请检查网络连接'
          console.error('请求错误:', err.request)
        } else {
          // 请求配置出错
          errorMessage = `请求错误: ${err.message}`
          console.error('请求配置错误:', err)
        }
        
        this.error = errorMessage
        // 延长错误信息显示时间，便于查看
        setTimeout(() => {
          this.error = ''
        }, 10000) // 10秒后清除错误信息
      }
    },
    getStatusText(orderStatus) {
      switch (orderStatus) {
        case 0: return '客户下单'
        case 1: return '商家确认'
        case 2: return '备货完成'
        case 3: return '开始发货'
        case 4: return '交易完成'
        case 5: return '交易失败'
        default: return '未知状态'
      }
    },
    getStatusClass(orderStatus) {
      switch (orderStatus) {
        case 0: return 'status-ordered'
        case 1: return 'status-confirmed'
        case 2: return 'status-prepared'
        case 3: return 'status-shipping'
        case 4: return 'status-completed'
        case 5: return 'status-failed'
        default: return 'status-unknown'
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

.status-ordered {
  color: #ffc107;
  font-weight: bold;
}

.status-confirmed {
  color: #17a2b8;
  font-weight: bold;
}

.status-prepared {
  color: #007bff;
  font-weight: bold;
}

.status-shipping {
  color: #6f42c1;
  font-weight: bold;
}

.status-completed {
  color: #28a745;
  font-weight: bold;
}

.status-failed {
  color: #dc3545;
  font-weight: bold;
}

.status-unknown {
  color: #6c757d;
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
