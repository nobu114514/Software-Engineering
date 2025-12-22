<template>
  <div class="cart">
    <h2>我的购物车</h2>
    
    <div v-if="loading" class="loading">
      <p>加载购物车数据中...</p>
    </div>
    
    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <p>您的购物车是空的</p>
      <router-link to="/" class="btn">去购物</router-link>
    </div>
    
    <div v-else class="cart-content">
        <div class="cart-items">
          <!-- 全选复选框 -->
          <div class="cart-item-header">
            <div class="cart-item-checkbox">
              <input type="checkbox" v-model="isAllSelected" @change="toggleAllSelection">
              <span>全选</span>
            </div>
          </div>
          <!-- 购物车商品项 -->
          <div v-for="item in cartItems" :key="item.productId" class="cart-item">
            <div class="cart-item-checkbox">
              <input type="checkbox" :value="item.productId" v-model="selectedItems" @change="updateAllSelected">
            </div>
            <div class="cart-item-image">
              <img :src="item.imageUrl || 'https://img.pngsucai.com/00/87/02/31a2f72e4e901438.webp'" :alt="item.productName">
            </div>
            <div class="cart-item-info">
            <h3 class="cart-item-name">{{ item.productName }}</h3>
            <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
            
            <div class="cart-item-quantity">
              <button class="btn" @click="updateQuantity(item.productId, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
              <span>{{ item.quantity }}</span>
              <button class="btn" @click="updateQuantity(item.productId, item.quantity + 1)">+</button>
            </div>
            
            <div class="cart-item-subtotal">
              小计: ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
            
            <button class="btn btn-danger" @click="removeFromCart(item.productId)">移除</button>
          </div>
        </div>
      </div>
      
      <div class="cart-summary">
        <h3>购物车总计</h3>
        <div class="summary-item">
          <span>商品总数:</span>
          <span>{{ totalQuantity }}</span>
        </div>
        <div class="summary-item">
          <span>商品项数:</span>
          <span>{{ cartItems.length }}</span>
        </div>
        <div class="summary-item total">
          <span>总计金额:</span>
          <span>¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        
        <div class="cart-actions">
          <button class="btn" @click="clearCart">清空购物车</button>
          <button class="btn btn-primary" @click="checkout">去结算</button>
          <button 
            class="btn batch-favorite-btn" 
            @click="batchFavorite" 
            :disabled="selectedItems.length === 0"
            :class="{ 'active': selectedItems.length > 0 }"
          >
            批量收藏
          </button>
        </div>
      </div>
    </div>
    
    <!-- 购买表单弹窗 -->
    <div v-if="showBuyForm" class="modal-overlay">
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
    
    <!-- 错误提示 -->
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
  </div>
</template>

<script>


export default {
  name: 'CartView',
  data() {
    return {
      cartItems: [],
      totalQuantity: 0,
      totalAmount: 0,
      loading: true,
      selectedItems: [], // 存储选中的商品ID列表
      isAllSelected: false, // 全选状态
      // 购买表单相关
      showBuyForm: false,
      buySuccess: false,
      error: '',
      buyer: {
        name: '',
        phone: '',
        address: '',
        notes: ''
      }
    };
  },
  created() {
    this.fetchCartItems();
  },
  methods: {
    async fetchCartItems() {
      this.loading = true;
      try {
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        const response = await this.$axios.get('/cart/items', {
          headers: {
            'X-Username': encodedUsername
          }
        });
        if (response.data.success) {
          this.cartItems = response.data.cartItems;
          this.totalQuantity = response.data.totalQuantity;
          this.calculateTotalAmount();
        } else {
          alert('获取购物车数据失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('获取购物车数据失败:', error);
        alert('获取购物车数据失败，请稍后重试。');
      } finally {
        this.loading = false;
      }
    },
    
    calculateTotalAmount() {
      this.totalAmount = this.cartItems.reduce((total, item) => {
        return total + (item.price * item.quantity);
      }, 0);
    },
    
    async updateQuantity(productId, quantity) {
      try {
        // 获取当前商品信息
        const item = this.cartItems.find(item => item.productId === productId);
        if (!item) return;
        
        // 在前端先检查数量是否合理
        if (quantity <= 0) {
          quantity = 1; // 保证至少购买1件
        }
        
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        const response = await this.$axios.put('/cart/update', {
          productId: productId,
          quantity: quantity
        }, {
          headers: {
            'X-Username': encodedUsername
          }
        });
        
        if (response.data.success) {
          this.cartItems = response.data.cartItems;
          this.totalQuantity = response.data.totalQuantity;
          this.calculateTotalAmount();
        } else {
          alert('更新数量失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('更新数量失败:', error);
        alert('更新数量失败: ' + (error.response?.data?.message || '请稍后重试'));
      }
    },
    
    async removeFromCart(productId) {
      try {
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        const response = await this.$axios.delete(`/cart/remove/${productId}`, {
          headers: {
            'X-Username': encodedUsername
          }
        });
        
        if (response.data.success) {
          this.cartItems = response.data.cartItems;
          this.totalQuantity = response.data.totalQuantity;
          this.calculateTotalAmount();
          alert('商品已移除');
        } else {
          alert('移除商品失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('移除商品失败:', error);
        alert('移除商品失败，请稍后重试。');
      }
    },
    
    async clearCart() {
      if (confirm('确定要清空购物车吗？')) {
        try {
          const username = localStorage.getItem('customerUsername');
          const encodedUsername = encodeURIComponent(username || '');
          const response = await this.$axios.delete('/cart/clear', {
            headers: {
              'X-Username': encodedUsername
            }
          });
          
          if (response.data.success) {
            this.cartItems = [];
            this.totalQuantity = 0;
            this.totalAmount = 0;
            alert('购物车已清空');
          } else {
            alert('清空购物车失败: ' + response.data.message);
          }
        } catch (error) {
          console.error('清空购物车失败:', error);
          alert('清空购物车失败，请稍后重试。');
        }
      }
    },
    
    // 处理全选/取消全选
    toggleAllSelection() {
      if (this.isAllSelected) {
        // 全选：将所有商品的productId添加到selectedItems
        this.selectedItems = this.cartItems.map(item => item.productId);
      } else {
        // 取消全选：清空selectedItems
        this.selectedItems = [];
      }
    },
    
    // 当单个商品选择状态改变时更新全选状态
    updateAllSelected() {
      this.isAllSelected = this.selectedItems.length === this.cartItems.length;
    },
    
    async checkout() {
      // 检查是否有选中的商品
      if (this.selectedItems.length === 0) {
        alert('请先选择要购买的商品');
        return;
      }
      
      // 检查用户是否已登录
      if (!localStorage.getItem('customerLoggedIn')) {
        this.error = '未登录，跳转至登录界面';
        // 2秒后跳转到登录页面
        setTimeout(() => {
          this.$router.push('/login');
        }, 2000);
        return;
      }
      
      // 显示购买表单
      this.showBuyForm = true;
    },
    
    async submitBuy() {
      try {
        // 从localStorage获取当前登录用户名并进行编码
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        
        // 获取选中的商品项
        const selectedCartItems = [...this.cartItems.filter(item => this.selectedItems.includes(item.productId))];
        
        // 准备统计信息
        let successCount = 0;
        let failCount = 0;
        const failedProducts = [];
        
        // 遍历每个选中的商品项
        for (const item of selectedCartItems) {
          // 按照商品数量循环提交购买意向，每次只提交一个商品ID
          for (let i = 0; i < item.quantity; i++) {
            try {
              // 准备请求数据
              const buyRequest = { ...this.buyer };
              
              // 调用后端API提交单个商品的购买意向（模仿HomeView的实现）
              await this.$axios.post(`/buyers/product/${item.productId}`, buyRequest, {
                headers: {
                  'X-Username': encodedUsername
                }
              });
              
              // 下单成功
              successCount++;
            } catch (err) {
              // 下单失败
              failCount++;
              failedProducts.push(item.productName);
              console.error(`购买商品 ${item.productName} 失败:`, err);
            }
          }
        }
        
        // 更新购物车状态
        if (successCount > 0) {
          // 重新获取购物车数据，确保数据最新
          await this.fetchCartItems();
          
          // 清空选中的商品
          this.selectedItems = [];
          this.isAllSelected = false;
          
          // 清空表单
          this.buyer = {
            name: '',
            phone: '',
            address: '',
            notes: ''
          };
          
          // 显示成功消息
          this.buySuccess = true;
          this.showBuyForm = false;
          
          // 5秒后自动隐藏成功提示
          setTimeout(() => {
            this.buySuccess = false;
          }, 5000);
        }
        
        // 显示失败信息（如果有）
        if (failCount > 0) {
          this.error = `部分商品购买失败: ${failedProducts.join(', ')}`;
          // 3秒后自动隐藏错误提示
          setTimeout(() => {
            this.error = '';
          }, 3000);
        }
      } catch (error) {
        console.error('下单失败:', error);
        this.error = '下单失败，请稍后重试。';
        // 3秒后自动隐藏错误提示
        setTimeout(() => {
          this.error = '';
        }, 3000);
      }
    },
    
    // 批量收藏功能
    async batchFavorite() {
      // 检查用户是否登录
      if (!localStorage.getItem('customerToken')) {
        alert('请先登录再进行收藏操作');
        this.$router.push('/login');
        return;
      }
      
      try {
        const username = localStorage.getItem('customerUsername');
        let successCount = 0;
        
        // 对每个选中的商品进行收藏
        for (const productId of this.selectedItems) {
          try {
            // 检查商品是否已收藏
            const checkResponse = await this.$axios.get(`http://localhost:8081/api/favorites/${username}/${productId}`);
            
            if (!checkResponse.data.isFavorited) {
              // 商品未收藏，添加收藏
              await this.$axios.post(`http://localhost:8081/api/favorites/${username}/${productId}`);
              successCount++;
            }
          } catch (err) {
            console.error(`收藏商品 ${productId} 失败:`, err);
          }
        }
        
        // 显示收藏结果
        if (successCount > 0) {
          alert(`已成功收藏 ${successCount} 件商品！`);
        } else {
          alert('所有选中的商品都已在收藏夹中');
        }
      } catch (error) {
        console.error('批量收藏失败:', error);
        alert('批量收藏失败，请稍后重试。');
      }
    }
  }
};
</script>

<style scoped>
.cart {
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

.empty-cart {
  text-align: center;
  padding: 60px 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin: 20px 0;
}

.cart-content {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.cart-items {
  flex: 1;
  min-width: 300px;
}

.cart-item-header {
  display: flex;
  padding: 15px 0;
  margin-bottom: 10px;
  font-weight: bold;
}

.cart-item {
  display: flex;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: white;
  align-items: center;
}

.cart-item-checkbox {
  flex: 0 0 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-item-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.cart-item-image {
  flex: 0 0 150px;
  margin-right: 15px;
}

.cart-item-image img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.cart-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cart-item-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.cart-item-price {
  font-size: 20px;
  color: #e74c3c;
  font-weight: bold;
  margin-bottom: 10px;
}

.cart-item-quantity {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.cart-item-quantity button {
  width: 35px;
  height: 35px;
  border: 1px solid #ddd;
  background-color: #f8f9fa;
  cursor: pointer;
  font-size: 20px;
  font-weight: bold;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: #000000; /* 黑色文字 */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
}

.cart-item-quantity button:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.cart-item-quantity button:active {
  transform: translateY(1px);
}

.cart-item-quantity button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cart-item-quantity span {
  margin: 0 15px;
  font-size: 16px;
  width: 30px;
  text-align: center;
}

.cart-item-subtotal {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
  text-decoration: none;
  display: inline-block;
  text-align: center;
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
  background-color: #c82333;
}

.cart-summary {
  flex: 0 0 300px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.cart-summary h3 {
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-item.total {
  font-size: 18px;
  font-weight: bold;
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #ddd;
}

.cart-actions {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 批量收藏按钮样式 */
.batch-favorite-btn {
  background-color: #ccc;
  color: #666;
  cursor: not-allowed;
}

.batch-favorite-btn.active {
  background-color: #ffc107;
  color: #333;
  cursor: pointer;
}

.batch-favorite-btn.active:hover {
  background-color: #e0a800;
}

/* 弹窗样式 */
.modal-overlay {
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

.modal-content {
  width: 100%;
  max-width: 500px;
  margin: 20px;
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>