<template>
  <div class="seller-dashboard">
    <h1>卖家后台</h1>
    
    <div class="dashboard-menu">
      <a href="/seller/product/new" class="btn">发布新商品</a>
      <a href="/seller/products" class="btn">商品历史</a>
      <a href="/seller/buyers" class="btn">购买意向列表</a>
      <a href="/seller/customers" class="btn">客户管理</a>
      <a href="/seller/change-password" class="btn">修改密码</a>
      <a href="/" class="btn">返回买家平台</a>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="activeProduct" class="card">
      <h2>当前在售商品</h2>
      <h3>{{ activeProduct.name }}</h3>
      <p>价格: ¥{{ activeProduct.price.toFixed(2) }}</p>
      <p>状态: {{ activeProduct.frozen ? '交易中（已冻结）' : '可购买' }}</p>
      
      <div class="actions">
        <button 
          class="btn" 
          @click="toggleFreeze"
          :disabled="!activeProduct">
          {{ activeProduct.frozen ? '解除冻结' : '冻结商品' }}
        </button>
        <a :href="'/seller/product/new?id=' + activeProduct.id" class="btn">编辑商品</a>
      </div>
    </div>
    
    <div v-if="!loading && !activeProduct" class="alert alert-danger">
      当前没有在售商品，请发布新商品。
    </div>
  </div>
</template>

<script>
export default {
  name: 'SellerDashboard',
  data() {
    return {
      activeProduct: null,
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchActiveProduct()
  },
  methods: {
    async fetchActiveProduct() {
      try {
        this.loading = true
        const response = await this.$axios.get('/products/active')
        this.activeProduct = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async toggleFreeze() {
      if (!this.activeProduct) return

      try {
        // 修复：添加 /api 前缀 + 字段名改为 frozen
        await this.$axios.put(`/products/${this.activeProduct.id}/freeze`, null, {
          params: { freeze: !this.activeProduct.frozen }
        })
        // 修复：字段名改为 frozen
        this.activeProduct.frozen = !this.activeProduct.frozen
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
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}
</style>
