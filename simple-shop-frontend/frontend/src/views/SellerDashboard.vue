<template>
  <div class="seller-dashboard">
    <h1>卖家后台</h1>
    
    <div class="dashboard-menu">
      <a href="/seller/product/new" class="btn">发布新商品</a>
      <a href="/seller/products" class="btn">商品历史</a>
      <a href="/seller/stock-logs" class="btn">库存日志</a>
      <a href="/seller/categories" class="btn">分类管理</a>
      <a href="/seller/sub-categories" class="btn">二级分类管理</a>
      <a href="/seller/buyers" class="btn">购买意向列表</a>
      <a href="/seller/customers" class="btn">客户管理</a>
      <a href="/seller/change-password" class="btn">修改密码</a>
      <a href="/" class="btn">返回买家平台</a>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="!loading && activeProducts.length > 0" class="active-products">
      <h2>当前在售商品列表</h2>
      <div class="product-list">
        <div v-for="product in activeProducts" :key="product.id" class="card product-card">
          <h3>{{ product.name }}</h3>
          <p>价格: ¥{{ product.price.toFixed(2) }}</p>
          <p>库存: {{ product.stock }} 件</p>
          <p>状态: {{ product.stock > 0 ? '可购买' : '已售罄' }}</p>
          
          <div class="actions">
            <a :href="'/seller/product/new?id=' + product.id" class="btn">编辑商品</a>
            <button 
              class="btn btn-danger" 
              @click="confirmDelete(product)"
              :disabled="!product">
              删除商品
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="!loading && activeProducts.length === 0" class="alert alert-danger">
      当前没有在售商品，请发布新商品。
    </div>
  </div>
</template>

<script>
export default {
  name: 'SellerDashboard',
  data() {
    return {
      activeProducts: [],
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchActiveProducts()
  },
  methods: {
    async fetchActiveProducts() {
      try {
        this.loading = true
        const response = await this.$axios.get('/products/active-list')
        // active-list端点已经返回激活状态的商品，不需要再次筛选
        this.activeProducts = Array.isArray(response.data) ? response.data : []
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },

    // 确认删除商品
    confirmDelete(product) {
      if (confirm(`确定要删除商品"${product.name}"吗？此操作不可恢复。`)) {
        this.deleteProduct(product.id)
      }
    },

    // 删除商品
    async deleteProduct(productId) {
      try {
        await this.$axios.delete(`/products/${productId}`)
        // 刷新商品列表
        this.fetchActiveProducts()
        // 清空之前的错误提示
        this.error = ''
      } catch (err) {
        this.error = '删除商品失败，请重试'
        console.error(err)
      }
    }
  }
}
</script>

<style scoped>
.actions {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.active-products {
  margin-top: 2rem;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1.5rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.product-card h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.2rem;
}

.product-card p {
  margin-bottom: 0.5rem;
  color: #666;
}

.btn {
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s ease;
}

.btn:hover {
  background-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
}

.btn-danger:hover {
  background-color: #c82333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list {
    grid-template-columns: 1fr;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
    text-align: center;
  }
}
</style>