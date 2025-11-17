<template>
    <div class="product-history">
        <h1>商品历史记录</h1>

        <div v-if="loading" class="loading">加载中...</div>

        <div v-if="error" class="alert alert-danger">
            {{ error }}
        </div>

        <!-- 商品历史列表 -->

        <table v-if="!loading && products.length > 0" class="product-table">
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
                    <span v-if="product.active" class="status-active">在售</span>
                    <span v-else class="status-inactive">已下架</span>
                    <span v-if="product.frozen" class="status-frozen"> (已冻结)</span>
                </td>
                <td>{{ formatDate(product.createdAt) }}</td>
                <td>
                    <!-- 编辑按钮 -->
                    <a :href="'/seller/product/new?id=' + product.id" class="btn">编辑</a>

                    <!-- 上架 / 下架 按钮 -->
                    <button
                            class="btn"
                            :class="{ 'btn-secondary': product.active, 'btn-success': !product.active }"
                            @click="toggleActive(product)"
                            :disabled="false">
                        {{ product.active ? '下架' : '上架' }}
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
            const response = await this.$axios.get('/products') // 实际请求：http://localhost:8081/api/products
            this.products = response.data
            this.error = ''
          } catch (err) {
            this.error = '获取商品列表失败'
            console.error(err)
          } finally {
            this.loading = false
          }
        },

        // 获取当前在售商品数量
        getActiveProductsCount() {
          return this.products.filter(p => p.isActive).length
        },

        async toggleActive(product) {
          try {
            if (product.active) {
              // 当前是"在售"，执行下架
              await this.$axios.put(`/products/${product.id}/deactivate`)
              // 设置本地存储标志，指示有商品状态已变更
              localStorage.setItem('productStatusChanged', 'true')
            } else {
              // 当前是"已下架"，执行上架
              await this.$axios.put(`/products/${product.id}/activate`)
              // 设置本地存储标志，指示有商品状态已变更
              localStorage.setItem('productStatusChanged', 'true')
            }

            // 刷新商品列表
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

    .alert-info {
      background-color: #d1ecf1;
      color: #0c5460;
      border: 1px solid #bee5eb;
    }

    .product-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 1rem;
    }

    .product-table th,
    .product-table td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .status-active {
      color: #28a745;
      font-weight: bold;
    }

    .status-inactive {
      color: #6c757d;
    }

    .status-frozen {
      color: #dc3545;
    }

    .btn {
      margin-right: 0.5rem;
      padding: 0.25rem 0.75rem;
      color: white;
      text-decoration: none;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    /* 下架按钮 - 灰色 */
    .btn-secondary {
      background: #6c757d;
    }

    /* 上架按钮 - 绿色 */
    .btn-success {
      background: #28a745;
    }

    .btn:disabled {
      background: #ccc;
      cursor: not-allowed;
    }
</style>
