<template>
  <div class="home">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- 分类筛选区域 -->
    <div class="filter-section card">
      <h3>商品分类筛选</h3>
      <div class="filter-form">
        <div class="filter-group">
          <label for="category">一级分类</label>
          <select id="category" v-model="selectedCategoryId">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="subCategory">二级分类</label>
          <select id="subCategory" v-model="selectedSubCategoryId">
            <option value="">全部子分类</option>
            <option v-for="subCategory in subCategories" :key="subCategory.id" :value="subCategory.id">
              {{ subCategory.name }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 商品列表区域 -->
    <div class="products-container" v-if="!loading">
      <!-- 只有当 products 数组有数据时，才显示商品卡片列表 -->
      <div class="product-list" v-if="products.length > 0">
        <div class="product-card card" v-for="product in products" :key="product.id">
          <!-- 商品名称链接到详情页 -->
          <router-link :to="{ name: 'productDetail', params: { id: product.id } }">
            <h3>{{ product.name }}</h3>
          </router-link>
          
          <!-- 图片链接到详情页 -->
          <router-link :to="{ name: 'productDetail', params: { id: product.id } }" class="product-image">
            <!-- 显示商品图片，如果没有图片则显示默认图片 -->
            <img
              :src="product.imageUrl || 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'"
              :alt="product.name || '商品图片'"
              class="product-img clickable"
              @error="handleImageError"
            >
          </router-link>
          
          <div class="product-price">价格: ¥{{ product.price.toFixed(2) }}</div>
          
          <!-- 显示商品分类信息 -->
          <div class="product-category" v-if="product.subCategory">
            <small>
              <span v-if="product.subCategory.category">{{ product.subCategory.category.name }} / </span>
              {{ product.subCategory.name }}
            </small>
          </div>

          <!-- 注意：这里用的是 product.frozen（后端返回的字段名），不是 isFrozen -->
          <div v-if="product.frozen" class="alert alert-danger">
            该商品正在交易中，请稍后再试。
          </div>

          <!-- 只有商品未冻结，才显示购买按钮 -->
          <button
            class="btn"
            @click="handleBuyClick(product)"
            v-if="!product.frozen">
            我要购买
          </button>
        </div>
      </div>

      <!-- 如果商品列表为空，显示提示 -->
      <div v-else class="alert alert-info">
        当前没有可购买的商品。
      </div>
    </div>

    <!-- 购买表单弹窗 -->
    <div v-if="showBuyForm && currentProduct" class="buy-form card">
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
import axios from 'axios';
export default {
  name: 'HomeView',
  data() {
    return {
      products: [],
      categories: [],
      subCategories: [],
      selectedCategoryId: '',
      selectedSubCategoryId: '',
      currentProduct: null,
      loading: true,
      error: null,
      showBuyForm: false,
      buyer: {
        name: '',
        phone: '',
        address: '',
        notes: ''
      },
      buySuccess: false
    };
  },
  created() {
    // 获取分类和商品数据
    this.fetchCategories();
    this.fetchProducts();
  },
  watch: {
    // 监听分类变化，加载对应的二级分类
    selectedCategoryId(newVal) {
      this.selectedSubCategoryId = ''; // 重置二级分类选择
      this.loadSubCategories(newVal);
      this.fetchProducts(); // 重新获取商品
    },
    // 监听二级分类变化，重新获取商品
    selectedSubCategoryId() {
      this.fetchProducts();
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await axios.get('http://localhost:8081/api/categories/active');
        // 正确访问嵌套在data字段中的分类列表
        this.categories = response.data.data || [];
      } catch (error) {
        console.error('获取分类信息失败:', error);
      }
    },
    async loadSubCategories(categoryId) {
      try {
        // 如果选择了一级分类，加载对应的二级分类
        if (categoryId) {
          const response = await axios.get(`http://localhost:8081/api/sub-categories/category/${categoryId}`);
          // SubCategoryController直接返回列表，不包含data字段
          this.subCategories = Array.isArray(response.data) ? response.data : [];
        } else {
          // 如果没有选择一级分类，清空二级分类列表
          this.subCategories = [];
        }
      } catch (error) {
        console.error('获取二级分类信息失败:', error);
        this.subCategories = [];
      }
    },
    async fetchProducts() {
      try {
        this.loading = true;
        let url = 'http://localhost:8081/api/products/active-list';
        
        // 根据选择的分类构建请求URL
        if (this.selectedSubCategoryId) {
          // 如果选择了二级分类，使用二级分类查询接口
          url = `http://localhost:8081/api/products/sub-category/${this.selectedSubCategoryId}/active`;
        } else if (this.selectedCategoryId) {
          // 如果只选择了一级分类，使用一级分类查询接口
          url = `http://localhost:8081/api/products/category/${this.selectedCategoryId}/active`;
        }
        
        const response = await axios.get(url);
        // 确保返回的是数组，考虑data字段嵌套结构
        const data = response.data.data || response.data;
        const productsArray = Array.isArray(data) ? data : [data].filter(Boolean);
        // 新端点已确保只返回激活状态的商品
        this.products = productsArray;
      } catch (error) {
        console.error('获取商品信息失败:', error);
        this.error = '获取商品信息失败，请稍后重试。';
        this.products = [];
      } finally {
        this.loading = false;
      }
    },
    handleImageError(event) {
      // 当图片加载失败时，设置默认图片
      event.target.src = 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp';
    },
    handleBuyClick(product) {
      this.currentProduct = product;
      this.showBuyForm = true;
    },
    async submitBuy() {
      // 再次检查用户是否已登录（防止绕过前端验证）
      if (!localStorage.getItem('customerLoggedIn')) {
        this.error = '未登录，跳转至登录界面'
        // 2秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login')
        }, 2000)
        return
      }
      
      try {
        // 获取当前登录的用户名并进行编码，确保符合HTTP请求头的ISO-8859-1编码要求
        const username = localStorage.getItem('customerUsername')
        const encodedUsername = encodeURIComponent(username || '')
        // 发送请求时携带编码后的用户名作为请求头
        await this.$axios.post(`/buyers/product/${this.currentProduct.id}`, this.buyer, {
          headers: {
            'X-Username': encodedUsername
          }
        })
        this.buySuccess = true
        this.showBuyForm = false
        // 刷新商品列表
        this.fetchProducts()
        // 5秒后自动隐藏成功提示
        setTimeout(() => {
          this.buySuccess = false
        }, 5000)
      } catch (err) {
        this.error = '提交购买意向失败'
        console.error(err)
      }
    }
  }
};
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}

.filter-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.filter-form {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  min-width: 200px;
}

.filter-group label {
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.products-container {
  margin-top: 20px;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.product-card h3 {
  margin: 15px;
  color: #333;
  font-size: 18px;
  transition: color 0.2s ease;
}

.product-card h3:hover {
  color: #007bff;
}

.product-image {
  display: block;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-img:hover {
  transform: scale(1.05);
}

.product-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
  margin: 15px;
}

.product-category {
  margin: 0 15px 15px;
  color: #666;
  font-size: 14px;
}

.btn {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 15px;
  margin: 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.btn:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.buy-form {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  z-index: 1000;
  min-width: 400px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.alert {
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.alert-danger {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.alert-success {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.alert-info {
  background-color: #d1ecf1;
  border: 1px solid #bee5eb;
  color: #0c5460;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
  
  .filter-form {
    flex-direction: column;
  }
  
  .filter-group {
    min-width: 100%;
  }
  
  .buy-form {
    min-width: 90%;
    padding: 20px;
  }
}
</style>
