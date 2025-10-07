<template>
  <div class="product-form">
    <h1>{{ isEditing ? '编辑商品' : '发布新商品' }}</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="success" class="alert alert-success">
      {{ isEditing ? '商品更新成功' : '商品发布成功' }}
    </div>
    
    <form @submit.prevent="saveProduct" v-if="!loading" class="card">
      <div class="form-group">
        <label for="name">商品名称</label>
        <input type="text" id="name" v-model="product.name" required>
      </div>
      <div class="form-group">
        <label for="price">商品价格</label>
        <input type="number" id="price" v-model="product.price" min="0" step="0.01" required>
      </div>
      <div class="form-group">
        <label for="imageUrl">商品图片URL</label>
        <input type="text" id="imageUrl" v-model="product.imageUrl" 
               placeholder="输入图片URL，如：https://picsum.photos/600/400">
      </div>
      <div class="form-group">
        <label for="description">商品描述</label>
        <textarea id="description" v-model="product.description" rows="5" required></textarea>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn">{{ isEditing ? '更新商品' : '发布商品' }}</button>
        <a href="/seller/dashboard" class="btn btn-secondary">取消</a>
      </div>
    </form>
  </div>
</template>

<script>
export default {
  name: 'ProductForm',
  data() {
    return {
      product: {
        name: '',
        price: 0,
        imageUrl: '',
        description: ''
      },
      isEditing: false,
      loading: true,
      error: '',
      success: false
    }
  },
  created() {
    const productId = this.$route.query.id
    if (productId) {
      this.isEditing = true
      this.fetchProduct(productId)
    } else {
      this.loading = false
    }
  },
  methods: {
    async fetchProduct(id) {
      try {
        const response = await this.$axios.get(`/products/${id}`)
        this.product = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async saveProduct() {
      try {
        if (this.isEditing) {
          await this.$axios.put(`/products/${this.product.id}`, this.product)
        } else {
          await this.$axios.post('/products', this.product)
        }
        this.success = true
        // 重置表单或跳转
        setTimeout(() => {
          this.$router.push('/seller/dashboard')
        }, 1500)
      } catch (err) {
        this.error = this.isEditing ? '更新商品失败' : '发布商品失败'
        console.error(err)
      }
    }
  }
}
</script>

<style scoped>
.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}
</style>
