<template>
  <div class="order-list">
    <h2>我的订单</h2>
    
    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-item">
        <label for="status-filter">订单状态：</label>
        <select id="status-filter" v-model="statusFilter" @change="fetchOrders">
          <option value="">全部状态</option>
          <option value="pending">待处理</option>
          <option value="completed">已完成</option>
        </select>
      </div>
      <div class="filter-item">
        <label for="time-filter">时间排序：</label>
        <select id="time-filter" v-model="timeFilter" @change="fetchOrders">
          <option value="desc">最新优先</option>
          <option value="asc">最早优先</option>
        </select>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 错误提示 -->
    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <!-- 订单列表 -->
    <div v-if="orders.length > 0" class="orders-container">
      <div v-for="order in orders" :key="order.id" class="order-card card">
        <div class="order-header">
          <span class="order-id">订单号: {{ order.id }}</span>
          <span class="order-time">{{ formatDate(order.created_at) }}</span>
        </div>
        
        <div class="order-content">
          <div class="product-info">
            <span class="product-name">{{ order.product_name }}</span>
            <span class="order-amount">¥{{ order.price ? formatPrice(order.price) : '0.00' }}</span>
          </div>
          
          <div class="order-status">
            <span class="status-badge" :class="order.is_completed ? 'status-completed' : 'status-pending'">{{ order.is_completed ? '已完成' : '待处理' }}</span>
            <button class="btn btn-secondary" @click="showOrderDetail(order)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 无订单提示 -->
    <div v-else-if="!loading" class="alert alert-info">暂无订单记录</div>

    <!-- 订单详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content card">
        <div class="modal-header">
          <h3>订单详情</h3>
          <button class="close-btn" @click="closeDetail">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedOrder">

          <div class="detail-item">
            <label>订单编号：</label>
            <span>{{ selectedOrder.id }}</span>
          </div>
          <div class="detail-item">
            <label>商品名称：</label>
            <span>{{ selectedOrder.product_name }}</span>
          </div>
          <div class="detail-item">
            <label>订单金额：</label>
            <span>¥{{ selectedOrder.price ? formatPrice(selectedOrder.price) : '0.00' }}</span>
          </div>
          <div class="detail-item">
            <label>收货人：</label>
            <span>{{ selectedOrder.buyer_name }}</span>
          </div>
          <div class="detail-item">
            <label>联系电话：</label>
            <span>{{ selectedOrder.phone }}</span>
          </div>
          <div class="detail-item">
            <label>收货地址：</label>
            <span>{{ selectedOrder.address }}</span>
          </div>
          <div class="detail-item">
            <label>订单备注：</label>
            <span>{{ selectedOrder.notes || '无' }}</span>
          </div>
          <div class="detail-item">
            <label>下单时间：</label>
            <span>{{ formatDateTime(selectedOrder.created_at) }}</span>
          </div>
          <div class="detail-item">
            <label>订单状态：</label>
            <span class="status-badge" :class="selectedOrder.is_completed ? 'status-completed' : 'status-pending'">{{ selectedOrder.is_completed ? '已完成' : '待处理' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerOrders',
  data() {
    return {
      orders: [],
      loading: false,
      error: '',
      statusFilter: '',
      timeFilter: 'desc',
      showDetail: false,
      selectedOrder: null
    }
  },
  created() {
    this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      this.loading = true
      this.error = ''
      
      try {
        // 从localStorage获取当前登录用户名
        const customerUsername = localStorage.getItem('customerUsername')
        
        // 构建请求参数
        const params = {
          status: this.statusFilter || undefined,
          sort: this.timeFilter
        }
        
        // 构建请求头
        const headers = {
          'X-Username': customerUsername
        }
        
        // 发送API请求，带上筛选参数和用户信息
        const response = await this.$axios.get(`/orders`, {
          params: params,
          headers: headers
        })
        
        this.orders = response.data
      } catch (err) {
        this.error = '获取订单列表失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    
    showOrderDetail(order) {
      this.selectedOrder = order
      this.showDetail = true
    },
    
    closeDetail() {
      this.showDetail = false
      this.selectedOrder = null
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    
    formatPrice(price) {
      if (typeof price === 'string') {
        price = parseFloat(price)
      }
      return price.toFixed(2)
    },
    

  }
}
</script>

<style scoped>
.order-list {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px 0;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
}

.filter-item select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.orders-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: white;
  transition: box-shadow 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.order-id {
  font-weight: 600;
  color: #333;
}

.order-time {
  color: #666;
  font-size: 14px;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #333;
  margin-right: 15px;
}

.order-amount {
  font-weight: 600;
  color: #E02E24;
  font-size: 18px;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-completed {
  background-color: #d4edda;
  color: #155724;
}

.status-cancelled {
  background-color: #f8d7da;
  color: #721c24;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f5f5f5;
}

.modal-body {
  padding: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  width: 100px;
  font-weight: 500;
  color: #666;
}

.detail-item span {
  flex: 1;
  color: #333;
}
</style>