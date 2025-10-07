<template>
  <div class="product-history">
    <h1>商品历史记录</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <table v-if="!loading && products.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>名称</th>
          <th>价格</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in products" :key="product.id">
          <td>{{ product.id }}</td>
          <td>{{ product.name }}</td>
          <td>¥{{ product.price.toFixed(2) }}</td>
          <td>
            <span v-if="product.isActive">在售</span>
            <span v-else>已下架</span>
            <span v-if="product.isFrozen"> (已冻结)</span>
          </td>
          <td>{{ formatDate(product.createdAt) }}</td>
          <td>
            <a :href="'/seller/product/new?id=' + product.id" class="btn">编辑</a>
            <button 
              class="btn btn-secondary" 
              @click="toggleActive(product)"
              :disabled="product.isActive && products.filter(p => p.isActive).length <= 1">
              {{ product.isActive ? '下架' : '上架' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    
    <div v-if="!loading && products.length === 0" class="alert alert-danger">
      暂无商品记录
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductHistory',
  data() {
    return {
      products: [],
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchProducts()
  },
  methods: {
    async fetchProducts() {
      try {
        this.loading = true
        const response = await this.$axios.get('/products')
        this.products = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取商品历史失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async toggleActive(product) {
      try {
        await this.$axios.put(`/products/${product.id}/freeze`, null, {
          params: { freeze: false }
        })
        
        await this.$axios.put(`/products/${product.id}`, {
          ...product,
          isActive: !product.isActive
        })
        
        // 如果是上架操作，需要将其他所有商品下架
        if (!product.isActive) {
          for (const p of this.products) {
            if (p.id !== product.id && p.isActive) {
              await this.$axios.put(`/products/${p.id}`, {
                ...p,
                isActive: false
              })
            }
          }
        }
        
        this.fetchProducts()
      } catch (err) {
        this.error = '操作失败，请重试'
        console.error(err)
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
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
</style>
