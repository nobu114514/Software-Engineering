<template>
  <div class="favorite-products-page">
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

    <div class="container">
      <h2 class="page-title">我的收藏</h2>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-if="error" class="alert alert-danger">
        {{ error }}
      </div>
      
      <div v-if="!isLoggedIn" class="alert alert-warning">
        请先登录查看收藏列表
        <router-link to="/login" class="btn btn-primary ml-2">去登录</router-link>
      </div>
      
      <div v-if="!loading && !error && isLoggedIn" class="favorite-products-container">
        <div v-if="favoriteProducts.length === 0" class="no-favorites">
          <p>您还没有收藏任何商品</p>
          <router-link to="/" class="btn btn-primary">去逛逛</router-link>
        </div>
        
        <div v-else class="products-grid">
          <div v-for="favorite in favoriteProducts" :key="favorite.id" class="product-card">
            <router-link :to="`/product/${favorite.product.id}`" class="product-image-link">
              <div class="product-image-container">
                <img 
                  :src="favorite.product.imageUrl || 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'" 
                  :alt="favorite.product.name" 
                  class="product-image"
                  @error="handleImageError($event)"
                >
              </div>
            </router-link>
            
            <div class="product-info">
              <router-link :to="`/product/${favorite.product.id}`" class="product-name">{{ favorite.product.name }}</router-link>
              
              <div class="product-price">¥{{ favorite.product.price.toFixed(2) }}</div>
              
              <div class="product-stock">
                <span v-if="favorite.product.stock > 0" class="stock-in">有货 ({{ favorite.product.stock }}件)</span>
                <span v-else class="stock-out">无货</span>
              </div>
              
              <div class="product-actions">
                <button 
                  class="btn btn-danger unfavorite-btn"
                  @click="unfavoriteProduct(favorite.id, favorite.product.id)"
                >
                  取消收藏
                </button>
                
                <router-link 
                  :to="`/product/${favorite.product.id}`" 
                  class="btn btn-primary"
                  v-if="favorite.product.stock > 0"
                >
                  查看详情
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FavoriteProducts',
  data() {
    return {
      favoriteProducts: [],  // 用户收藏的商品列表
      loading: true,         // 加载状态
      error: ''              // 错误信息
    }
  },
  computed: {
    isLoggedIn() {
      return typeof localStorage !== 'undefined' && localStorage.getItem('customerLoggedIn');
    }
  },
  created() {
    this.loadFavoriteProducts();
  },
  methods: {
    // 加载用户收藏的商品列表
    async loadFavoriteProducts() {
      if (!localStorage.getItem('customerLoggedIn')) {
        this.loading = false;
        return;
      }
      
      try {
        this.loading = true;
        const username = localStorage.getItem('customerUsername');
        const response = await this.$axios.get(`http://localhost:8081/api/favorites/${username}`);
        this.favoriteProducts = response.data;
        this.error = '';
      } catch (err) {
        console.error('加载收藏列表失败:', err);
        this.error = '加载收藏列表失败，请稍后重试';
      } finally {
        this.loading = false;
      }
    },
    
    // 取消收藏商品
    async unfavoriteProduct(favoriteId, productId) {
      try {
        const username = localStorage.getItem('customerUsername');
        await this.$axios.delete(`http://localhost:8081/api/favorites/${username}/${productId}`);
        
        // 从列表中移除取消收藏的商品
        this.favoriteProducts = this.favoriteProducts.filter(favorite => favorite.id !== favoriteId);
        
        alert('取消收藏成功');
      } catch (err) {
        console.error('取消收藏失败:', err);
        this.error = '取消收藏失败，请稍后重试';
      }
    },
    
    // 处理图片加载失败
    handleImageError(e) {
      e.target.src = 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp';
    }
  }
}
</script>

<style scoped>
.favorite-products-page {
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

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
}

.no-favorites {
  text-align: center;
  padding: 4rem;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.no-favorites p {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 1.5rem;
}

.favorite-products-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.product-card {
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.product-image-link {
  display: block;
}

.product-image-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f8f8f8;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  padding: 1.5rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 0.5rem;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s;
}

.product-name:hover {
  color: #f44336;
}

.product-price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #f44336;
  margin-bottom: 0.5rem;
}

.product-stock {
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.stock-in {
  color: #28a745;
}

.stock-out {
  color: #dc3545;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border: none;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #bd2130;
}

.unfavorite-btn {
  flex: 1;
}

.alert {
  padding: 1rem;
  margin: 1rem 0;
  border-radius: 4px;
  text-align: center;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.alert-warning {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.ml-2 {
  margin-left: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .product-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>