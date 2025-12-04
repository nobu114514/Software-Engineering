<template>
  <div class="order-list">
    <h2>我的订单</h2>
    
    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-item">
        <label for="status-filter">订单状态：</label>
        <select id="status-filter" v-model="statusFilter" @change="fetchOrders">
          <option value="">全部状态</option>
          <option value="0">客户下单</option>
          <option value="1">商家确认</option>
          <option value="2">备货完成</option>
          <option value="3">开始发货</option>
          <option value="4">交易完成</option>
          <option value="5">交易失败</option>
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
            <span class="status-badge" :class="getOrderStatusClass(order.order_status || (order.is_completed ? 'completed' : 'pending'))">
              {{ getOrderStatusText(order.order_status || (order.is_completed ? 'completed' : 'pending')) }}
            </span>
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
            <span class="status-badge" :class="getOrderStatusClass(selectedOrder.order_status || (selectedOrder.is_completed ? 'completed' : 'pending'))">
              {{ getOrderStatusText(selectedOrder.order_status || (selectedOrder.is_completed ? 'completed' : 'pending')) }}
            </span>
          </div>
          
          <!-- 取消订单按钮 -->
          <div v-if="canCancelOrder(selectedOrder)" class="action-buttons">
            <button class="btn btn-danger" @click="confirmCancelOrder">取消订单</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认取消订单弹窗 -->
    <div v-if="showCancelConfirm" class="modal-overlay" @click.self="closeCancelConfirm">
      <div class="modal-content card" style="max-width: 400px;">
        <div class="modal-header">
          <h3>确认取消订单</h3>
          <button class="close-btn" @click="closeCancelConfirm">×</button>
        </div>
        <div class="modal-body">
          <p>您确定要取消订单 #{{ selectedOrder?.id }} 吗？</p>
          <p style="color: #666; font-size: 14px; margin-top: 10px;">取消后订单将无法恢复。</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeCancelConfirm">取消</button>
          <button class="btn btn-danger" @click="cancelOrder">确认取消</button>
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
      selectedOrder: null,
      showCancelConfirm: false
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

    getOrderStatusClass(status) {
      // 处理数字状态
      const statusNum = parseInt(status);
      if (!isNaN(statusNum)) {
        switch (statusNum) {
          case 0:
            return 'status-customer-ordered';
          case 1:
            return 'status-merchant-confirmed';
          case 2:
            return 'status-ready-for-shipment';
          case 3:
            return 'status-shipping';
          case 4:
            return 'status-completed';
          case 5:
            return 'status-failed';
          default:
            return '';
        }
      }
      // 兼容旧的文本状态
      switch (status) {
        case 'COMPLETED':
        case 'completed':
          return 'status-completed';
        case 'PENDING':
        case 'pending':
          return 'status-customer-ordered';
        case 'CANCELLED':
        case 'cancelled':
          return 'status-cancelled';
        case 'FAILED':
        case 'failed':
          return 'status-failed';
        default:
          return '';
      }
    },
    
    getOrderStatusText(status) {
      // 处理数字状态
      const statusNum = parseInt(status);
      if (!isNaN(statusNum)) {
        switch (statusNum) {
          case 0:
            return '客户下单';
          case 1:
            return '商家确认';
          case 2:
            return '备货完成';
          case 3:
            return '开始发货';
          case 4:
            return '交易完成';
          case 5:
            return '交易失败';
          default:
            return '未知';
        }
      }
      // 兼容旧的文本状态
      switch (status) {
        case 'COMPLETED':
        case 'completed':
          return '交易完成';
        case 'PENDING':
        case 'pending':
          return '客户下单';
        case 'CANCELLED':
        case 'cancelled':
          return '已取消';
        case 'FAILED':
        case 'failed':
          return '交易失败';
        default:
          return status || '未知';
      }
    },
    
    // 判断订单是否可以取消（客户规则：开始发货前，即状态<3）
    canCancelOrder(order) {
      const status = parseInt(order.order_status);
      // 不是NaN且状态小于3且不是终态
      return !isNaN(status) && status < 3 && status !== 4 && status !== 5;
    },
    
    // 显示确认取消订单对话框
    confirmCancelOrder() {
      this.showCancelConfirm = true;
    },
    
    // 关闭确认取消订单对话框
    closeCancelConfirm() {
      this.showCancelConfirm = false;
    },
    
    // 执行取消订单操作
    async cancelOrder() {
      if (!this.selectedOrder) return;
      
      try {
        // 从localStorage获取当前登录用户名
        const customerUsername = localStorage.getItem('customerUsername');
        
        // 构建请求头
        const headers = {
          'X-Username': customerUsername
        };
        
        // 调用后端API取消订单
        await this.$axios.put(`/buyers/${this.selectedOrder.id}/cancel`, null, {
          params: { isCustomer: true },
          headers: headers
        });
        
        // 关闭确认对话框
        this.closeCancelConfirm();
        
        // 刷新订单列表
        this.fetchOrders();
        
        // 显示成功提示
        alert('订单取消成功！');
        
        // 关闭订单详情
        this.closeDetail();
      } catch (err) {
        console.error('取消订单失败', err);
        // 显示失败提示
        alert(err.response?.data?.message || '取消订单失败，请稍后重试');
      }
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

.status-customer-ordered {
  background-color: #fff3cd;
  color: #856404;
}

.status-merchant-confirmed {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-ready-for-shipment {
  background-color: #e2e3e5;
  color: #383d41;
}

.status-shipping {
  background-color: #cce7ff;
  color: #004085;
}

.status-completed {
  background-color: #d4edda;
  color: #155724;
}

.status-failed {
  background-color: #f8d7da;
  color: #721c24;
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

/* 模态框底部按钮样式 */
.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.modal-footer .btn {
  margin-right: 0;
  margin-bottom: 0;
}
</style>