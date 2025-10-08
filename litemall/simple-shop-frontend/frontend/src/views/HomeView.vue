<template>
  <div class="home">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- 只有当 product 存在，且 product.active === true 时，才显示商品卡片 -->
    <div v-if="product && product.active" class="product-card card">
      <h1>{{ product.name }}</h1>
      <div class="product-image">
        <!-- 显示商品图片，如果没有图片则显示默认图片 -->
        <img
          :src="product.imageUrl || 'https://picsum.photos/800/400?grayscale'"
          :alt="product.name || '商品图片'"
          class="product-img"
          @error="handleImageError"
        >
      </div>
      <div class="product-price">价格: ¥{{ product.price.toFixed(2) }}</div>
      <div class="product-description">
        <h3>商品描述</h3>
        <p>{{ product.description }}</p>
      </div>

      <!-- 注意：这里用的是 product.frozen（后端返回的字段名），不是 isFrozen -->
      <div v-if="product.frozen" class="alert alert-danger">
        该商品正在交易中，请稍后再试。
      </div>

      <!-- 只有商品未冻结，才显示购买按钮 -->
      <button
        class="btn"
        @click="showBuyForm = true"
        v-if="!product.frozen">
        我要购买
      </button>
    </div>

    <!-- 如果没有商品，或者商品不活跃（active === false），显示提示 -->
    <div v-if="!product || !product.active" class="alert alert-danger">
      当前没有可购买的商品。
    </div>

    <!-- 购买表单弹窗 -->
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

    <!-- 提交成功提示 -->
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
      product: null,           // 当前活跃商品
      loading: true,           // 加载状态
      error: '',               // 错误信息
      showBuyForm: false,      // 是否显示购买表单
      buySuccess: false,       // 是否显示购买成功提示
      buyer: {
        name: '',
        phone: '',
        address: '',
        notes: ''
      }
    }
  },
  created() {
    this.fetchActiveProduct()  // 组件创建时获取活跃商品
  },
  methods: {
    // 获取当前活跃商品（后端接口返回字段是 active，不是 isActive）
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

    // 提交购买意向
    async submitBuy() {
      try {
        await this.$axios.post(`/buyers/product/${this.product.id}`, this.buyer)
        this.buySuccess = true
        this.showBuyForm = false
        // 刷新商品状态
        this.fetchActiveProduct()
        // 5秒后自动隐藏成功提示
        setTimeout(() => {
          this.buySuccess = false
        }, 5000)
      } catch (err) {
        this.error = '提交购买意向失败'
        console.error(err)
      }
    },

    // 处理图片加载失败的情况
    handleImageError(e) {
      // 如果图片加载失败，使用默认图片
      e.target.src = 'https://picsum.photos/800/400?grayscale'
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
  text-align: center; /* 图片居中显示 */
}

.product-img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1); /* 添加轻微阴影增强视觉效果 */
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

.alert {
  padding: 1rem;
  margin: 1rem 0;
  border-radius: 4px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1.5rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.btn {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  height: 80px;
  resize: vertical;
}
</style>
