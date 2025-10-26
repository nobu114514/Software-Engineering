<template>
  <div class="customer-list">
    <h1>客户管理</h1>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-if="selectedCustomer" class="customer-details">
      <h2>客户详情</h2>
      <div class="detail-card">
        <p><strong>用户名：</strong>{{ selectedCustomer.username }}</p>
        <p><strong>电话：</strong>{{ selectedCustomer.phone }}</p>
        <p><strong>注册时间：</strong>{{ formatDate(selectedCustomer.updatedAt) }}</p>
        <p v-if="selectedCustomer.default_address"><strong>默认地址：</strong>{{ selectedCustomer.default_address }}</p>
        <button class="btn btn-secondary" @click="selectedCustomer = null">返回列表</button>
      </div>

      <h3>订单记录</h3>
      <div v-if="customerOrders.length === 0" class="alert alert-info">
        暂无订单记录
      </div>
      <table v-else>
        <thead>
          <tr>
            <th>订单ID</th>
            <th>商品</th>
            <th>价格</th>
            <th>数量</th>
            <th>下单时间</th>
            <th>订单状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in customerOrders" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ order.product_name || '未知商品' }}</td>
            <td>¥{{ (order.price || 0).toFixed(2) }}</td>
            <td>{{ order.quantity || 1 }}</td>
            <td>{{ formatDate(order.created_at) }}</td>
            <td>
              <span :class="getOrderStatusClass(order.status)">{{ getOrderStatusText(order.status) }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <table v-else-if="!loading && customers.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>电话</th>
          <th>注册时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="customer in customers" :key="customer.id">
          <td>{{ customer.id }}</td>
            <td>{{ customer.username }}</td>
            <td>{{ customer.phone }}</td>
            <td>{{ formatDate(customer.updatedAt) }}</td>
          <td>
            <button class="btn" @click="viewCustomerDetails(customer)">查看详情</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && customers.length === 0" class="alert alert-info">
      暂无客户记录
    </div>
    <br>
    <a href="/seller/dashboard" class="btn btn-secondary" style="margin-bottom: 1rem;">
      返回卖家后台
    </a>
  </div>
</template>

<script>
export default {
  name: 'CustomerList',
  data() {
    return {
      customers: [],
      selectedCustomer: null,
      customerOrders: [],
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchCustomers()
  },
  methods: {
    async fetchCustomers() {
      try {
        this.loading = true
        // 调用后端API获取客户列表
        const response = await this.$axios.get('/customers')
        this.customers = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取客户列表失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async viewCustomerDetails(customer) {
      try {
        this.selectedCustomer = customer
        this.loading = true
        // 调用后端API获取客户的订单记录
        const response = await this.$axios.get(`/customers/${customer.id}/orders`)
        this.customerOrders = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取订单记录失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getOrderStatusClass(status) {
      switch (status) {
        case 'COMPLETED':
        case 'completed':
          return 'status-completed'
        case 'PENDING':
        case 'pending':
          return 'status-pending'
        case 'CANCELLED':
        case 'cancelled':
          return 'status-cancelled'
        default:
          return ''
      }
    },
    getOrderStatusText(status) {
      switch (status) {
        case 'COMPLETED':
        case 'completed':
          return '已完成'
        case 'PENDING':
        case 'pending':
          return '处理中'
        case 'CANCELLED':
        case 'cancelled':
          return '已取消'
        default:
          return status || '未知'
      }
    }
  }
}
</script>

<style scoped>
.customer-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1, h2, h3 {
  color: #333;
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}

.alert {
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-info {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #333;
}

tr:hover {
  background-color: #f5f5f5;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: background-color 0.3s;
}

.btn {
  background-color: #007bff;
  color: white;
}

.btn:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.status-completed {
  color: #28a745;
  font-weight: bold;
}

.status-pending {
  color: #ffc107;
  font-weight: bold;
}

.status-cancelled {
  color: #dc3545;
  font-weight: bold;
}

.customer-details {
  margin-bottom: 30px;
}

.detail-card {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.detail-card p {
  margin: 10px 0;
  font-size: 16px;
}
</style>