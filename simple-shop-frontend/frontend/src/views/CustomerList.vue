<template>
  <div class="customer-list">
    <div class="header-container">
      <h1>客户管理</h1>
      <button class="btn btn-secondary" @click="goBack">返回</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- 搜索栏 -->
    <div v-if="!selectedCustomer && !loading" class="search-bar">
      <input 
        type="text" 
        v-model="searchKeyword" 
        placeholder="搜索用户名或手机号"
        @keyup.enter="handleSearch"
        @input="handleInput"
        :class="{ 'search-active': isSearchEnabled }"
      >
      <button class="btn" @click="handleSearch" :class="{ 'active': isSearchEnabled }">搜索</button>
      <button class="btn btn-secondary" @click="resetSearch">重置</button>
      <button class="btn btn-info" @click="toggleSortOrder">
        {{ isAscending ? '时间升序' : '时间倒序' }}
      </button>
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

      <div class="order-header">
        <h3>订单记录</h3>
        <button class="btn btn-info" @click="toggleSortOrder">
          {{ isAscending ? '时间升序' : '时间倒序' }}
        </button>
      </div>
      <div v-if="customerOrders.length === 0" class="alert alert-info">
        暂无订单记录
      </div>
      <table v-else>
        <thead>
          <tr>
            <th>订单ID</th>
            <th>商品</th>
            <th>类型</th>
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
            <td>{{ order.type === 'buyer' ? '购买意向' : '订单' }}</td>
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
      {{ searchKeyword ? '没有找到匹配的客户记录' : '暂无客户记录' }}
    </div>
    
    <!-- 分页控件 -->
    <div v-if="!loading && !selectedCustomer && totalPages > 1" class="pagination">
      <div class="pagination-info">
        共 {{ totalItems }} 条记录，第 {{ currentPage + 1 }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <button class="btn" @click="goToFirstPage" :disabled="currentPage === 0">首页</button>
        <button class="btn" @click="goToPrevPage" :disabled="currentPage === 0">上一页</button>
        
        <!-- 页码按钮 -->
        <button 
          v-for="page in visiblePages" 
          :key="page"
          class="btn"
          :class="{ 'active': page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page + 1 }}
        </button>
        
        <button class="btn" @click="goToNextPage" :disabled="currentPage === totalPages - 1">下一页</button>
        <button class="btn" @click="goToLastPage" :disabled="currentPage === totalPages - 1">末页</button>
      </div>
    </div>
    

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
      error: '',
      // 分页相关
      currentPage: 0,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0,
      // 搜索相关
      searchKeyword: '',
      isSearchEnabled: false, // 控制搜索功能是否启用，初始为false
      // 排序相关
      isAscending: false // false表示降序（最新的在前），true表示升序（最早的在前）
    }
  },
  created() {
    this.fetchCustomers()
  },
  computed: {
    // 计算显示的页码范围
    visiblePages() {
      const pages = []
      const total = this.totalPages
      const current = this.currentPage
      
      // 简化版本：只显示当前页附近的页码
      let start = Math.max(0, current - 2)
      let end = Math.min(total - 1, current + 2)
      
      // 如果总页数较少，显示所有页码
      if (total <= 5) {
        start = 0
        end = total - 1
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    }
  },
  methods: {
      // 切换排序顺序
      toggleSortOrder() {
        this.isAscending = !this.isAscending;
        // 如果正在查看客户详情，则直接对现有订单进行重新排序
        if (this.selectedCustomer && this.customerOrders.length > 0) {
          this.customerOrders.sort((a, b) => {
            const dateA = new Date(a.created_at);
            const dateB = new Date(b.created_at);
            return this.isAscending ? dateA - dateB : dateB - dateA;
          });
        } else {
          // 否则重新获取客户列表
          this.fetchCustomers();
        }
      },
      
      async fetchCustomers() {
      try {
        this.loading = true
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        
        let response;
        const trimmedKeyword = this.searchKeyword ? this.searchKeyword.trim() : '';
        
        console.log('API调用信息:', {
          isSearchEnabled: this.isSearchEnabled,
          trimmedKeyword: trimmedKeyword,
          params: params
        });
        
        // 核心逻辑：根据搜索模式决定调用哪个接口
        if (this.isSearchEnabled) {
          // 搜索模式下，调用搜索接口
          params.keyword = trimmedKeyword; // 即使是空字符串也传递，由后端处理
          console.log('调用搜索接口: /customers');
          response = await this.$axios.get('/customers', { params });
        } else {
          params.keyword = trimmedKeyword; // 即使是空字符串也传递，由后端处理
          console.log('调用搜索接口: /customers');
          response = await this.$axios.get('/customers', { params });
        }

        
        // 处理响应数据
        if (response.data.customers) {
          // 新接口返回的分页数据
          this.customers = response.data.customers
          // 根据isAscending变量决定升序还是降序排序
          this.customers.sort((a, b) => {
            const dateA = new Date(a.updatedAt)
            const dateB = new Date(b.updatedAt)
            return this.isAscending ? dateA - dateB : dateB - dateA
          })
          this.currentPage = response.data.currentPage
          this.totalItems = response.data.totalItems
          this.totalPages = response.data.totalPages
        } else {
          // 兼容旧接口返回的直接列表数据
          this.customers = response.data
          // 根据isAscending变量决定升序还是降序排序
          this.customers.sort((a, b) => {
            const dateA = new Date(a.updatedAt)
            const dateB = new Date(b.updatedAt)
            return this.isAscending ? dateA - dateB : dateB - dateA
          })
          this.totalItems = response.data.length
          this.totalPages = 1
          this.currentPage = 0
        }
        
        this.error = ''
      } catch (err) {
        this.error = '获取客户列表失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      // 先对搜索关键词进行trim处理
      const trimmedKeyword = this.searchKeyword.trim();
      // 保存trim后的值
      this.searchKeyword = trimmedKeyword;
      // 无论是否有关键词，都启用搜索功能并执行搜索
      // 后端已经修改为能正确处理空字符串的情况
      this.isSearchEnabled = true; // 启用搜索功能
      this.currentPage = 0; // 搜索时重置到第一页
      this.fetchCustomers();
    },
    
    // 处理输入框输入事件 - 不再自动启用搜索功能
    handleInput() {
      // 输入框输入时不自动启用搜索，只在点击搜索按钮时启用
      // 这样用户可以随意输入但只有点击搜索才会执行搜索
    },
    
    resetSearch() {
      // 清空搜索关键词
      this.searchKeyword = ''
      // 关闭搜索功能，回到初始状态
      this.isSearchEnabled = false
      // 重置页码
      this.currentPage = 0
      // 重新获取所有用户数据
      this.fetchCustomers()
    },
    

    
    goToPage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        this.fetchCustomers()
      }
    },
    
    goToFirstPage() {
      this.goToPage(0)
    },
    
    goToLastPage() {
      this.goToPage(this.totalPages - 1)
    },
    
    goToPrevPage() {
      this.goToPage(this.currentPage - 1)
    },
    
    goToNextPage() {
      this.goToPage(this.currentPage + 1)
    },
    async viewCustomerDetails(customer) {
      try {
        this.selectedCustomer = customer
        this.loading = true
        // 调用后端API获取客户的订单记录
        const response = await this.$axios.get(`/customers/${customer.id}/orders`)
        this.customerOrders = response.data
        // 根据isAscending变量决定升序还是降序排序
        this.customerOrders.sort((a, b) => {
          const dateA = new Date(a.created_at)
          const dateB = new Date(b.created_at)
          return this.isAscending ? dateA - dateB : dateB - dateA
        })
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
    
    // 返回上一页
    goBack() {
      this.$router.push('/seller/dashboard')
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

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h1 {
  color: #333;
  margin-bottom: 0;
}

h2, h3 {
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

/* 订单状态样式 */
.status-customer-ordered {
  color: #ffc107;
  font-weight: bold;
}

.status-merchant-confirmed {
  color: #17a2b8;
  font-weight: bold;
}

.status-ready-for-shipment {
  color: #6c757d;
  font-weight: bold;
}

.status-shipping {
  color: #007bff;
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

.status-cancelled {
  color: #dc3545;
  font-weight: bold;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-bar input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  max-width: 400px;
}

.search-bar input.search-active {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
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

.btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.btn.active {
  background-color: #28a745;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

.customer-details {
  margin-bottom: 30px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.pagination {
  margin-top: 20px;
  padding: 20px 0;
  border-top: 1px solid #eee;
}

.pagination-info {
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.pagination-controls .btn {
  min-width: 40px;
}
</style>