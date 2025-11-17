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
          <p>状态: {{ product.frozen ? '交易中（已冻结）' : '可购买' }}</p>
          
          <div class="actions">
            <button 
              class="btn" 
              @click="toggleFreeze(product.id)"
              :disabled="!product">
              {{ product.frozen ? '解除冻结' : '冻结商品' }}
            </button>
            <a :href="'/seller/product/new?id=' + product.id" class="btn">编辑商品</a>
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
        const response = await this.$axios.get('/products')
        // 筛选出激活状态的商品
        this.activeProducts = Array.isArray(response.data) ? 
          response.data.filter(product => product.active) : []
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async toggleFreeze(productId) {
      try {
        // 找到要操作的商品
        const product = this.activeProducts.find(p => p.id === productId)
        if (!product) return
        
        // 修复：添加 /api 前缀 + 字段名改为 frozen
        await this.$axios.put(`/products/${productId}/freeze`, null, {
          params: { freeze: !product.frozen }
        })
        
        // 更新商品状态
        product.frozen = !product.frozen
        // （可选优化）清空之前的错误提示
        this.error = ''
      } catch (err) {
        this.error = '操作失败，请重试'
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