<template>
  <div class="home">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <!-- 分类按钮区域 -->
    <div class="category-buttons-section card">
      <h3>商品分类</h3>
      <!-- 一级分类按钮 -->
      <div class="category-buttons">
        <button 
          class="category-btn" 
          :class="{ active: selectedCategoryId === '' }"
          @click="selectCategory('')">
          全部分类
        </button>
        <button 
          v-for="category in categories" 
          :key="category.id" 
          class="category-btn"
          :class="{ active: selectedCategoryId === category.id }"
          @click="selectCategory(category.id)">
          {{ category.name }}
        </button>
      </div>
      
      <!-- 二级分类按钮 - 只在选择了一级分类时显示 -->
      <div class="sub-category-section" v-if="selectedCategoryId && subCategories.length > 0">
        <h4>子分类</h4>
        <div class="sub-category-buttons">
          <button 
            class="sub-category-btn" 
            :class="{ active: selectedSubCategoryId === '' }"
            @click="selectSubCategory('')">
            全部子分类
          </button>
          <button 
            v-for="subCategory in subCategories" 
            :key="subCategory.id" 
            class="sub-category-btn"
            :class="{ active: selectedSubCategoryId === subCategory.id }"
            @click="selectSubCategory(subCategory.id)">
            {{ subCategory.name }}
          </button>
        </div>
      </div>
    </div>

    <!-- 搜索和排序区域 -->
    <div class="search-sort-section card">
      <div class="search-container">
        <input 
          type="text" 
          v-model="searchKeyword" 
          @input="onSearchInput"
          placeholder="请输入商品名称或关键字搜索"
          class="search-input"
        >
        <button @click="performSearch" class="btn search-btn">搜索</button>
      </div>
      
      <div class="sort-container">
        <label>排序方式：</label>
        <select v-model="sortBy" @change="performSearch" class="sort-select">
          <option value="createdAt">上架时间</option>
          <option value="price">价格</option>
          <option value="salesCount">销量</option>
        </select>
        <button 
          @click="toggleSortDirection" 
          class="btn btn-secondary sort-direction-btn"
          :title="sortDirection === 'desc' ? '降序' : '升序'"
        >
          {{ sortDirection === 'desc' ? '↓' : '↑' }}
        </button>
      </div>
    </div>

    <!-- 商品列表区域 -->
    <div class="products-container" v-if="!loading">
      <!-- 只有当 products 数组有数据时，才显示商品卡片列表 -->
      <div class="product-list" v-if="products.length > 0">
        <div class="product-card card" v-for="product in products" :key="product.id" :class="{ 'disabled-card': !canBuy }">
          <!-- 商品名称链接到详情页 -->
          <router-link :to="{ name: 'productDetail', params: { id: product.id } }" :class="{ 'disabled-link': !canBuy }">
            <h3>{{ product.name }}</h3>
          </router-link>
          
          <!-- 图片链接到详情页 -->
          <router-link :to="{ name: 'productDetail', params: { id: product.id } }" class="product-image" :class="{ 'disabled-link': !canBuy }">
            <!-- 显示商品图片，如果没有图片则显示默认图片 -->
            <img
              :src="product.imageUrl || 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'"
              :alt="product.name || '商品图片'"
              class="product-img clickable"
              @error="handleImageError"
            >
          </router-link>
          
          <div class="product-price">价格: ¥{{ product.price.toFixed(2) }}</div>
          <div class="product-stock" v-if="product.stock > 0">库存: {{ product.stock }} 件</div>
          <div class="product-stock out-of-stock" v-else>库存: 无货</div>
          
          <!-- 显示商品分类信息 -->
          <div class="product-category" v-if="product.subCategory">
            <small>
              <span v-if="product.subCategory.category">{{ product.subCategory.category.name }} / </span>
              {{ product.subCategory.name }}
            </small>
          </div>

          <!-- 只有商品有库存，才显示购买和加入购物车按钮 -->
          <button
            class="btn"
            @click="handleBuyClick(product)"
            v-if="product.stock > 0"
            :disabled="!canBuy"
            :title="!canBuy ? '请登录买家账号后操作' : ''">
            我要购买
          </button>
          <button
            class="btn"
            @click="addToCart(product)"
            v-if="product.stock > 0"
            :disabled="!canBuy"
            :title="!canBuy ? '请登录买家账号后操作' : ''">
            加入购物车
          </button>
          <!-- 登录提示 -->
          <div class="login-prompt" v-if="product.stock > 0 && !canBuy">
            <span>{{ isSellerLoggedIn ? '卖家账号无法购买' : '请登录买家账号后操作' }}</span>
          </div>
        </div>
      </div>

      <!-- 如果商品列表为空，显示提示 -->
      <div v-else class="alert alert-info">
        {{ searchKeyword ? '未找到匹配的商品' : '当前没有可购买的商品。' }}
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 0"
          class="btn btn-secondary"
        >
          上一页
        </button>
        
        <span class="page-info">
          第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页 (总计 {{ totalElements }} 条记录)
        </span>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="btn btn-secondary"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 购买表单弹窗 -->
    <div v-if="showBuyForm && currentProduct" class="modal-overlay">
      <div class="buy-form card modal-content">
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
    <div v-if="buySuccess" class="alert alert-success">
      购买意向已提交，请等待卖家联系进行线下交易。
    </div>

    <!-- 临时弹窗提示 -->
    <div v-if="showToast" class="toast" :class="toastType">
      <div class="toast-content">
        {{ toastMessage }}
      </div>
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
      buySuccess: false,
      // 临时弹窗状态
      showToast: false,
      toastMessage: '',
      toastType: 'success', // success或error
      // 搜索相关
      searchKeyword: '',
      // 排序相关
      sortBy: 'createdAt',
      sortDirection: 'desc',
      // 分页相关
      currentPage: 0,
      pageSize: 20,
      totalPages: 0,
      totalElements: 0
    };
  },
  computed: {
    // 判断是否为买家登录状态
    isBuyerLoggedIn() {
      return !!localStorage.getItem('customerToken');
    },
    // 判断是否为卖家登录状态
    isSellerLoggedIn() {
      return !!localStorage.getItem('sellerToken');
    },
    // 判断是否可以进行购买操作
    canBuy() {
      return this.isBuyerLoggedIn && !this.isSellerLoggedIn;
    }
  },
  created() {
    // 获取分类和商品数据
    this.fetchCategories();
    this.fetchProducts();
  },
  watch: {
    // 监听分类变化，重新获取商品
    selectedCategoryId(newVal) {
      this.selectedSubCategoryId = ''; // 重置二级分类选择
      this.loadSubCategories(newVal);
      this.currentPage = 0; // 重置页码
      this.fetchProducts(); // 重新获取商品
    },
    // 监听二级分类变化，重新获取商品
    selectedSubCategoryId() {
      this.currentPage = 0; // 重置页码
      this.fetchProducts();
    },
    // 监听排序方式变化
    sortBy() {
      this.currentPage = 0; // 重置页码
      this.fetchProducts();
    },
    // 监听排序方向变化
    sortDirection() {
      this.currentPage = 0; // 重置页码
      this.fetchProducts();
    }
  },
  methods: {
    selectCategory(categoryId) {
      this.selectedCategoryId = categoryId;
    },
    selectSubCategory(subCategoryId) {
      this.selectedSubCategoryId = subCategoryId;
    },
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
        
        let url;
        
        // 根据分类选择不同的API端点
        if (this.selectedSubCategoryId) {
          // 使用现有的按子分类获取上架商品端点
          url = `http://localhost:8081/api/products/sub-category/${this.selectedSubCategoryId}/active`;
        } else if (this.selectedCategoryId) {
          // 使用现有的按分类获取上架商品端点
          url = `http://localhost:8081/api/products/category/${this.selectedCategoryId}/active`;
        } else {
          // 使用现有的获取所有上架商品端点
          url = 'http://localhost:8081/api/products/active-list';
        }
        
        const response = await axios.get(url);
        
        // 处理响应 - 后端返回的是简单的列表
        const data = response.data.data || response.data;
        const productsArray = Array.isArray(data) ? data : [data].filter(Boolean);
        this.products = productsArray;
        this.totalPages = 1;
        this.totalElements = productsArray.length;
        this.currentPage = 0;
      } catch (error) {
        console.error('获取商品信息失败:', error);
        this.error = '获取商品信息失败，请稍后重试。';
        this.products = [];
        this.totalPages = 0;
        this.totalElements = 0;
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
        const buyRequest = { ...this.buyer };
        await this.$axios.post(`/buyers/product/${this.currentProduct.id}`, buyRequest, {
          headers: {
            'X-Username': encodedUsername
          }
        })
        // 后端已在创建购买意向时减少库存并记录日志，无需前端额外请求
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
    },
    // 搜索相关方法
    onSearchInput() {
      // 防抖处理，避免频繁请求
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.performSearch();
      }, 500);
    },
    performSearch() {
      this.currentPage = 0; // 重置到第一页
      this.fetchProducts();
    },
    // 排序相关方法
    toggleSortDirection() {
      this.sortDirection = this.sortDirection === 'desc' ? 'asc' : 'desc';
    },
    // 分页相关方法
    changePage(newPage) {
      if (newPage >= 0 && newPage < this.totalPages) {
        this.currentPage = newPage;
        this.fetchProducts();
      }
    },
    // 购物车相关方法
    async addToCart(product) {
      try {
        // 获取当前登录的用户名并进行编码，确保符合HTTP请求头的ISO-8859-1编码要求
        const username = localStorage.getItem('customerUsername')
        const encodedUsername = encodeURIComponent(username || '')
        const response = await this.$axios.post('/cart/add', {
          productId: product.id,
          quantity: 1
        }, {
          headers: {
            'X-Username': encodedUsername
          }
        });
        
        if (response.data.success) {
          // 显示成功提示
          this.toastMessage = response.data.message + '\n购物车商品项数: ' + response.data.cartSize + '\n商品总数量: ' + response.data.totalQuantity;
          this.toastType = 'success';
          this.showToast = true;
          // 3秒后自动关闭
          setTimeout(() => {
            this.showToast = false;
          }, 3000);
          console.log('购物车大小:', response.data.cartSize);
          console.log('商品总数量:', response.data.totalQuantity);
        } else {
          this.toastMessage = '错误: ' + response.data.message;
          this.toastType = 'error';
          this.showToast = true;
          // 3秒后自动关闭
          setTimeout(() => {
            this.showToast = false;
          }, 3000);
        }
      } catch (error) {
        console.error('加入购物车失败:', error);
        this.toastMessage = '加入购物车失败，请稍后重试。';
        this.toastType = 'error';
        this.showToast = true;
        // 3秒后自动关闭
        setTimeout(() => {
          this.showToast = false;
        }, 3000);
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

.category-buttons-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.category-buttons-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.category-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.category-btn {
  background-color: #e9ecef;
  color: #495057;
  border: 1px solid #ced4da;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.category-btn:hover {
  background-color: #dee2e6;
  border-color: #adb5bd;
  transform: translateY(-1px);
}

.category-btn.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.category-btn.active:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.sub-category-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #dee2e6;
}

.sub-category-section h4 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.sub-category-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.sub-category-btn {
  background-color: #f8f9fa;
  color: #6c757d;
  border: 1px solid #e9ecef;
  padding: 6px 12px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.sub-category-btn:hover {
  background-color: #e9ecef;
  border-color: #dee2e6;
  transform: translateY(-1px);
}

.sub-category-btn.active {
  background-color: #28a745;
  color: white;
  border-color: #28a745;
}

.sub-category-btn.active:hover {
  background-color: #218838;
  border-color: #1e7e34;
}

.search-sort-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-container {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.search-btn {
  background-color: #28a745;
  white-space: nowrap;
}

.search-btn:hover {
  background-color: #218838;
}

.sort-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-container label {
  font-weight: 500;
  color: #333;
  margin: 0;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
  cursor: pointer;
}

.sort-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.sort-direction-btn {
  padding: 8px 12px;
  min-width: 40px;
  font-size: 16px;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  padding: 20px;
}

.page-info {
  color: #666;
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
  margin: 0 15px 10px;
  color: #666;
  font-size: 14px;
}

.product-stock {
  margin: 0 15px 15px;
  color: #28a745;
  font-size: 14px;
  font-weight: 500;
}

.product-stock.out-of-stock {
  color: #dc3545;
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

/* 禁用状态的商品卡片样式 */
.disabled-card {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 禁用状态的链接样式 */
.disabled-link {
  pointer-events: none;
}

/* 禁用状态的按钮样式 */
.btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 登录提示样式 */
.login-prompt {
  margin: 15px;
  padding: 10px;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
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
  
  .category-buttons {
    justify-content: center;
  }
  
  .category-btn {
    font-size: 12px;
    padding: 6px 12px;
  }

  .sub-category-btn {
    font-size: 11px;
    padding: 4px 8px;
  }
  
  .buy-form {
    min-width: 90%;
    padding: 20px;
  }
}

/* 临时弹窗样式 - 优化后更符合页面主题 */
.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 24px 32px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  font-size: 14px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  background-color: white;
  border: 1px solid #e9ecef;
  animation: toastFadeIn 0.3s ease, toastFadeOut 0.3s ease 2.7s forwards;
}

.toast.success {
  border-left: 4px solid #28a745;
  color: #155724;
  background-color: #f8fff8;
}

.toast.error {
  border-left: 4px solid #dc3545;
  color: #721c24;
  background-color: #fff8f8;
}

.toast-content {
  white-space: pre-line;
  font-weight: 500;
  line-height: 1.6;
}

@keyframes toastFadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) translateY(0);
  }
}

@keyframes toastFadeOut {
  from {
    opacity: 1;
    transform: translate(-50%, -50%) translateY(0);
  }
  to {
    opacity: 0;
    transform: translate(-50%, -50%) translateY(-20px);
  }
}
</style>
