<template>
  <div class="product-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-content">
        <h1 class="shop-title">我的小店</h1>
        <div class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <span class="nav-divider">|</span>
          <router-link to="/login" class="nav-link">登录</router-link>
          <span class="nav-divider">|</span>
          <router-link to="/register" class="nav-link">注册</router-link>
        </div>
      </div>
    </div>

    <div class="product-container">
      <div v-if="loading" class="loading">加载中...</div>

      <div v-if="error" class="alert alert-danger">
        {{ error }}
      </div>

      <!-- 商品详情内容 -->
      <div v-if="product && product.active" class="product-content">
        <!-- 左侧图片区域 -->
        <div class="product-images">
          <!-- 缩略图列表 -->
          <div class="thumbnail-list">
            <img 
              v-for="(image, index) in allProductImages" 
              :key="index"
              :src="image"
              :alt="`商品图片${index + 1}`"
              class="thumbnail-item"
              :class="{ active: currentImageIndex === index }"
              @click="openCarousel(index)"
              @error="handleImageError($event)"
            >
          </div>
          
          <!-- 主图 -->
          <div class="main-image">
            <img
              :src="currentMainImage || 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'"
              :alt="product.name || '商品图片'"
              @click="openCarousel(currentImageIndex)"
              @error="handleMainImageError"
              class="main-img clickable"
              title="点击查看大图"
            >
          </div>
        </div>

        <!-- 右侧商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.name }}</h1>
          
          <div class="product-price-section">
            <span class="price-label">价格</span>
            <span class="price-value">¥{{ product.price.toFixed(2) }}</span>
          </div>
          
          <div class="product-stock-section">
            <span class="stock-label">库存</span>
            <span v-if="product.stock > 0" class="stock-value stock-in">有货 ({{ product.stock }}件)</span>
            <span v-else class="stock-value stock-out">无货</span>
          </div>

          <!-- 注意：这里用的是 product.frozen（后端返回的字段名），不是 isFrozen -->
          <div v-if="product.frozen" class="alert alert-danger">
            该商品正在交易中，请稍后再试。
          </div>
          <div v-else-if="product.stock <= 0" class="alert alert-danger">
            该商品已售罄，请稍后再试。
          </div>

          <!-- 只有商品未冻结且有库存，才显示购买按钮 -->
          <button
            class="buy-btn"
            @click="handleBuyClick"
            v-if="!product.frozen && product.stock > 0"
          >
            我要购买
          </button>
          
          <div class="product-meta">
            <p class="meta-item">上架时间: {{ formatDate(product.createdAt) }}</p>
            <p class="meta-item">更新时间: {{ formatDate(product.updatedAt) }}</p>
            <p class="meta-item" v-if="product.subCategory">
              分类: 
              <span v-if="product.subCategory.category">{{ product.subCategory.category.name }} / </span>
              {{ product.subCategory.name }}
            </p>
          </div>
        </div>
      </div>

      <!-- 商品详情描述区域 -->
      <div v-if="product && product.active" class="product-detail-section">
        <div class="section-title">
          <h2>商品详情</h2>
        </div>
        <div class="description-content" v-html="product.description"></div>
      </div>

      <!-- 如果没有商品，或者商品不活跃（active === false），显示提示 -->
      <div v-if="!product || !product.active" class="alert alert-danger">
        商品不存在或已下架。
      </div>
    </div>

    <!-- 购买表单弹窗 -->
    <div v-if="showBuyForm && product" class="buy-form-overlay">
      <div class="buy-form card">
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
    </div>

    <!-- 提交成功提示 -->
    <div v-if="buySuccess" class="success-overlay">
      <div class="success-modal alert alert-success">
        <h3>提交成功</h3>
        <p>购买意向已提交，请等待卖家联系进行线下交易。</p>
        <button type="button" class="btn" @click="buySuccess = false">确定</button>
      </div>
    </div>
    
    <!-- 图片轮播组件 -->
    <ImageCarousel 
      :show="showCarousel" 
      :images="allProductImages"
      :initial-index="currentImageIndex"
      @close="closeCarousel"
    />
  </div>
</template>

<script>
import ImageCarousel from '../components/ImageCarousel.vue'

export default {
  name: 'ProductDetail',
  components: {
    ImageCarousel
  },
  data() {
    return {
      product: null,           // 当前商品
      loading: true,           // 加载状态
      error: '',               // 错误信息
      showBuyForm: false,      // 是否显示购买表单
      buySuccess: false,       // 是否显示购买成功提示
      showCarousel: false,     // 是否显示图片轮播
      currentImageIndex: 0,    // 当前图片索引
      allProductImages: [],    // 所有商品图片URL列表
      buyer: {
        name: '',
        phone: '',
        address: '',
        notes: ''
      }
    }
  },
  computed: {
    // 获取当前显示的主图
    currentMainImage() {
      return this.allProductImages[this.currentImageIndex] || this.product?.imageUrl;
    }
  },
  created() {
    const productId = this.$route.params.id;
    if (productId) {
      this.fetchProduct(productId);
    } else {
      this.error = '无效的商品ID';
      this.loading = false;
    }
  },
  watch: {
    product(newVal) {
      if (newVal) {
        this.extractImagesFromDescription();
      }
    }
  },
  methods: {
    // 获取商品信息
    async fetchProduct(id) {
      try {
        this.loading = true;
        const response = await this.$axios.get(`http://localhost:8081/api/products/${id}`);
        this.product = response.data;
        this.error = '';
      } catch (err) {
        this.error = '获取商品信息失败';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    // 提交购买意向
    async submitBuy() {
      // 再次检查用户是否已登录（防止绕过前端验证）
      if (!localStorage.getItem('customerLoggedIn')) {
        this.error = '未登录，跳转至登录界面';
        // 2秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);
        return;
      }
      
      try {
        // 获取当前登录的用户名并进行编码，确保符合HTTP请求头的ISO-8859-1编码要求
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        // 发送请求时携带编码后的用户名作为请求头
        await this.$axios.post(`http://localhost:8081/api/buyers/product/${this.product.id}`, this.buyer, {
          headers: {
            'X-Username': encodedUsername
          }
        });
        this.buySuccess = true;
        this.showBuyForm = false;
        // 重置表单
        this.buyer = {
          name: '',
          phone: '',
          address: '',
          notes: ''
        };
      } catch (err) {
        this.error = '提交购买意向失败';
        console.error(err);
      }
    },

    // 处理主图片加载失败的情况
    handleMainImageError(e) {
      // 如果图片加载失败，使用默认图片
      e.target.src = 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp';
    },
    
    // 处理缩略图加载失败的情况
    handleImageError(e) {
      e.target.src = 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp';
    },
    
    // 处理购买按钮点击事件
    handleBuyClick() {
      // 检查用户是否已登录
      if (!localStorage.getItem('customerLoggedIn')) {
        // 未登录，显示提示信息
        this.error = '未登录，跳转至登录界面';
        // 2秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);
      } else {
        // 已登录，显示购买表单
        this.showBuyForm = true;
      }
    },
    
    // 从商品描述中提取图片URL
    extractImagesFromDescription() {
      const images = [];
      
      // 首先添加主图
      if (this.product.imageUrl) {
        images.push(this.product.imageUrl);
      }
      
      // 然后从商品描述中提取图片
      if (this.product.description) {
        // 使用正则表达式匹配<img>标签中的src属性
        const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/g;
        let match;
        while ((match = imgRegex.exec(this.product.description)) !== null) {
          const imgUrl = match[1];
          // 避免重复添加相同的图片
          if (!images.includes(imgUrl)) {
            images.push(imgUrl);
          }
        }
      }
      
      this.allProductImages = images;
    },
    
    // 打开图片轮播
    openCarousel(index) {
      if (this.allProductImages.length > 0) {
        this.currentImageIndex = index;
        this.showCarousel = true;
      }
    },
    
    // 关闭图片轮播
    closeCarousel() {
      this.showCarousel = false;
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.top-nav {
  background-color: #fff;
  border-bottom: 1px solid #ddd;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
}

.shop-title {
  font-size: 1.5rem;
  color: #f44336;
  margin: 0;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-link {
  color: #333;
  text-decoration: none;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #f44336;
}

.nav-divider {
  color: #ddd;
}

.product-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.product-content {
  display: flex;
  gap: 2rem;
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.product-images {
  display: flex;
  gap: 1rem;
  flex-shrink: 0;
}

.thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-item:hover,
.thumbnail-item.active {
  border-color: #f44336;
}

.main-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.main-img {
  max-width: 500px;
  max-height: 500px;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: transform 0.3s;
}

.main-img.clickable {
  cursor: pointer;
}

.main-img.clickable:hover {
  transform: scale(1.02);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-title {
  font-size: 1.8rem;
  margin-bottom: 1.5rem;
  line-height: 1.4;
  color: #333;
}

.product-price-section {
  margin-bottom: 2rem;
  padding: 1rem;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.price-label {
  font-size: 1rem;
  color: #666;
  margin-right: 0.5rem;
}

.price-value {
  font-size: 2rem;
  font-weight: bold;
  color: #f44336;
}

.buy-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 4px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 2rem;
  width: 100%;
  max-width: 300px;
}

.buy-btn:hover {
  background-color: #da190b;
}

.product-meta {
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.meta-item {
  color: #666;
  margin: 0.5rem 0;
}

.product-detail-section {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-title {
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
  margin-bottom: 2rem;
}

.section-title h2 {
  font-size: 1.5rem;
  color: #333;
  margin: 0;
}

.description-content {
  line-height: 1.8;
}

.description-content img {
  max-width: 100%;
  height: auto;
  margin: 1rem 0;
}

.buy-form-overlay {
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

.buy-form {
  width: 90%;
  max-width: 500px;
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.success-overlay {
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

.success-modal {
  width: 90%;
  max-width: 400px;
  text-align: center;
  padding: 2rem;
}

.success-modal h3 {
  margin-top: 0;
  margin-bottom: 1rem;
}

.loading {
  text-align: center;
  padding: 4rem;
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
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #45a049;
}

.btn-secondary {
  background-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }
  
  .product-images {
    flex-direction: column;
  }
  
  .thumbnail-list {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 0.5rem;
  }
  
  .thumbnail-item {
    flex-shrink: 0;
  }
  
  .main-img {
    max-width: 100%;
  }
  
  .buy-btn {
    max-width: 100%;
  }
}
</style>