<template>
  <div class="home">
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="product && product.isActive" class="product-card card">
      <h1>{{ product.name }}</h1>
      <div class="product-image">
        <img :src="product.imageUrl || 'https://picsum.photos/600/400'" 
             :alt="product.name" 
             class="product-img">
      </div>
      <div class="product-price">价格: ¥{{ product.price.toFixed(2) }}</div>
      <div class="product-description">
        <h3>商品描述</h3>
        <p>{{ product.description }}</p>
      </div>
      
      <div v-if="product.isFrozen" class="alert alert-danger">
        该商品正在交易中，请稍后再试。
      </div>
      
      <button 
        class="btn" 
        @click="showBuyForm = true" 
        v-if="!product.isFrozen">
        我要购买
      </button>
    </div>
    
    <div v-if="!product || !product.isActive" class="alert alert-danger">
      当前没有可购买的商品。
    </div>
    
    <!-- 购买表单 -->
    <div v-if="showBuyForm && product" class="buy-form card">
      <h2>购买信息</h2>
      <form @submit.prevent="submitBuy">
        <div class="form-group">
          <label for="name">姓名</label>
          <input type="text" id="name" v-model="buyer.name" required>
        </div>
        <div class="form-group">
          <label for="phone">电话</label>
          <input type="tel" id="phone" v-model="buyer.phone" required>
        </div>
        <div class="form-group">
          <label for="address">地址</label>
          <textarea id="address" v-model="buyer.address" required></textarea>
        </div>
        <div class="form-group">
          <label for="notes">备注</label>
          <textarea id="notes" v-model="buyer.notes"></textarea>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn">提交购买意向</button>
          <button type="button" class="btn btn-secondary" @click="showBuyForm = false">取消</button>
        </div>
      </form>
    </div>
    
    <div v-if="buySuccess" class="alert alert-success">
      购买意向已提交，请等待卖家联系进行线下交易。
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      product: null,
      loading: true,
      error: '',
      showBuyForm: false,
      buySuccess: false,
      buyer: {
        name: '',
        phone: '',
        address: '',
        notes: ''
      }
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
        this.product = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async submitBuy() {
      try {
        await this.$axios.post(`/buyers/product/${this.product.id}`, this.buyer)
        this.buySuccess = true
        this.showBuyForm = false
        // 刷新商品状态
        this.fetchActiveProduct()
        // 5秒后隐藏成功提示
        setTimeout(() => {
          this.buySuccess = false
        }, 5000)
      } catch (err) {
        this.error = '提交购买意向失败'
        console.error(err)
      }
    }
  }
}
</script>

<style scoped>
.product-card {
  max-width: 800px;
  margin: 0 auto;
}

.product-image {
  margin: 1rem 0;
}

.product-img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

.product-price {
  font-size: 1.5rem;
  color: #f44336;
  margin: 1rem 0;
}

.product-description {
  margin: 1rem 0;
  line-height: 1.6;
}

.buy-form {
  max-width: 600px;
  margin: 2rem auto;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}
</style>
