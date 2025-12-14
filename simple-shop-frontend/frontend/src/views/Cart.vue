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
        </div>
      </div>
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
      isAllSelected: false // 全选状态
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
        alert('更新数量失败，请稍后重试。');
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
      
      // 确认是否要结算
      if (!confirm('确定要购买选中的商品吗？')) {
        return;
      }
      
      try {
        // 从localStorage获取当前登录用户名
        const username = localStorage.getItem('customerUsername');
        const encodedUsername = encodeURIComponent(username || '');
        
        // 准备请求数据：选中的商品ID列表
        const requestData = {
          productIds: this.selectedItems
        };
        
        // 调用后端API进行批量下单
        const response = await this.$axios.post('/orders/create', requestData, {
          headers: {
            'X-Username': encodedUsername
          }
        });
        
        if (response.data.success) {
          // 下单成功，从购物车中移除已购买的商品
          // 更新购物车数据
          this.cartItems = this.cartItems.filter(item => !this.selectedItems.includes(item.productId));
          // 更新统计信息
          this.calculateTotalAmount();
          // 清空选中的商品
          this.selectedItems = [];
          this.isAllSelected = false;
          // 显示成功消息
          alert('订单创建成功！');
        } else {
          // 下单失败，显示错误消息
          alert('订单创建失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('批量下单失败:', error);
        alert('批量下单失败，请稍后重试。');
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
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  font-size: 16px;
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
</style>