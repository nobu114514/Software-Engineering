<template>
  <div class="product-batch-form">
    <h1>批量发布商品</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="success" class="alert alert-success">
      商品批量发布成功！
    </div>
    
    <div v-if="!loading" class="card">
      <div class="table-responsive">
        <table class="batch-table">
          <thead>
            <tr>
              <th style="width: 5%">序号</th>
              <th style="width: 15%">商品名称</th>
              <th style="width: 15%">商品分类</th>
              <th style="width: 15%">商品子分类</th>
              <th style="width: 10%">商品价格</th>
              <th style="width: 10%">商品主题</th>
              <th style="width: 24%">商品详情</th>
              <th style="width: 6%">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in products" :key="index">
              <td>{{ index + 1 }}</td>
              <td>
                <input type="text" v-model="item.name" required placeholder="请输入商品名称">
              </td>
              <td>
                <select v-model="item.categoryId" @change="onCategoryChange(index)" required>
                  <option v-if="index > 0" :value="'same'">同上</option>
                  <option v-for="category in categories" :key="category.id" :value="category.id">
                    {{ category.name }}
                  </option>
                </select>
              </td>
              <td>
                <select v-model="item.subCategory" required>
                  <!-- 当一级分类不为"同上"且与上一个一级分类相同时，才显示二级分类的"同上"选项 -->
                  <!-- 换句话说：如果一级分类为"同上"或与上一个一级分类相同，才显示"同上"选项 -->
                  <option v-if="index > 0 && (item.categoryId === 'same' || hasSameCategoryAsPrevious(index))" :value="'same'">同上</option>
                  <option v-for="subCategory in getSubCategories(index)" :key="subCategory.id" :value="subCategory">
                    {{ subCategory.name }}
                  </option>
                </select>
              </td>
              <td>
                <input type="number" v-model="item.price" min="0" step="0.01" required placeholder="0.00">
              </td>
              <td>
                <input type="text" v-model="item.theme" placeholder="非必填">
              </td>
              <td>
                <textarea v-model="item.description" placeholder="请输入商品详情"></textarea>
              </td>
              <td>
                <button @click="removeRow(index)" class="btn-circle remove-btn" title="删除一行" :disabled="products.length <= 1">-</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="add-button-container">
        <button @click="addRow" class="btn-circle add-btn" title="添加一行">+</button>
      </div>
      
      <div class="form-actions">
        <button @click="batchPublish" class="btn btn-primary">批量发布</button>
        <a href="/seller/product/new" class="btn btn-secondary">返回</a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductBatchForm',
  components: {
  },
  data() {
      return {
        products: [
          {
            name: '',
            categoryId: null,
            subCategory: null,
            price: 0,
            theme: '',
            description: ''
          },
          {
            name: '',
            categoryId: 'same',
            subCategory: 'same',
            price: 0,
            theme: '',
            description: ''
          }
        ],
        loading: true,
        error: '',
        success: false,
        categories: [],
        subCategoriesMap: {}
      }
    },
  created() {
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await this.$axios.get('http://localhost:8081/api/categories/active')
        this.categories = response.data.data || []
        // 如果有分类，自动选择第一个分类作为默认值
        if (this.categories.length > 0 && this.products[0].categoryId === null) {
          this.products[0].categoryId = this.categories[0].id
          this.loadSubCategories(this.categories[0].id, 0)
        }
        this.loading = false
      } catch (err) {
        this.error = '获取分类信息失败'
        this.loading = false
        console.error(err)
      }
    },
    
    async loadSubCategories(categoryId, index) {
      if (!categoryId || categoryId === 'same') {
        this.products[index].subCategory = index > 0 ? 'same' : null
        return
      }
      
      try {
        // 如果已经加载过该分类的子分类，则直接使用
        if (!this.subCategoriesMap[categoryId]) {
          const response = await this.$axios.get(`http://localhost:8081/api/sub-categories/category/${categoryId}`)
          this.subCategoriesMap[categoryId] = Array.isArray(response.data) ? response.data : []
        }
        
        // 如果有子分类，自动选择第一个子分类
        if (this.subCategoriesMap[categoryId].length > 0) {
          this.products[index].subCategory = this.subCategoriesMap[categoryId][0]
        } else {
          this.products[index].subCategory = null
        }
      } catch (err) {
        console.error('获取子分类信息失败', err)
      }
    },
    
    onCategoryChange(index) {
      this.loadSubCategories(this.products[index].categoryId, index)
      
      // 处理当前行的二级分类：如果一级分类为"同上"或与上一行相同，可以选择"同上"
      const currentItem = this.products[index]
      if (currentItem.categoryId === 'same' || this.hasSameCategoryAsPrevious(index)) {
        // 可以设置为"同上"
        currentItem.subCategory = 'same'
      } else {
        // 否则需要选择一个实际的子分类
        const actualCategoryId = this.getActualCategoryId(index)
        if (actualCategoryId && this.subCategoriesMap[actualCategoryId] && this.subCategoriesMap[actualCategoryId].length > 0) {
          currentItem.subCategory = this.subCategoriesMap[actualCategoryId][0]
        } else {
          currentItem.subCategory = null
        }
      }
      
      // 更新当前行之后的所有行的子分类
      for (let i = index + 1; i < this.products.length; i++) {
        if (this.products[i].categoryId === 'same') {
          // 对于选择"同上"的行，可以直接设置子分类为"同上"
          this.products[i].subCategory = 'same'
        }
      }
    },
    
    getSubCategories(index) {
      const categoryId = this.products[index].categoryId
      if (categoryId === 'same') {
        // 向上查找第一个非'same'的分类ID
        for (let i = index - 1; i >= 0; i--) {
          if (this.products[i].categoryId && this.products[i].categoryId !== 'same') {
            return this.subCategoriesMap[this.products[i].categoryId] || []
          }
        }
      }
      return categoryId && categoryId !== 'same' ? this.subCategoriesMap[categoryId] || [] : []
    },
    
    // 获取实际的分类ID（处理'same'逻辑）
    getActualCategoryId(index) {
      if (this.products[index].categoryId === 'same') {
        // 向上查找第一个有效的分类ID
        for (let i = index - 1; i >= 0; i--) {
          if (this.products[i].categoryId && this.products[i].categoryId !== 'same') {
            return this.products[i].categoryId
          }
        }
      }
      return this.products[index].categoryId
    },
    
    // 获取实际的子分类（处理'same'逻辑）
    getActualSubCategory(index) {
      if (this.products[index].subCategory === 'same') {
        // 向上查找第一个有效的子分类
        for (let i = index - 1; i >= 0; i--) {
          if (this.products[i].subCategory && this.products[i].subCategory !== 'same') {
            return this.products[i].subCategory
          }
        }
      }
      return this.products[index].subCategory
    },
    
    // 检查当前行的一级分类是否与上一行相同
    hasSameCategoryAsPrevious(index) {
      if (index <= 0) return false;
      
      const currentCategoryId = this.getActualCategoryId(index);
      const prevCategoryId = this.getActualCategoryId(index - 1);
      
      return currentCategoryId === prevCategoryId;
    },
    
    addRow() {
      // 创建新行，默认选择"同上"
      const newRow = {
        name: '',
        categoryId: 'same',
        subCategory: 'same', // 因为一级分类是"同上"，所以二级分类也可以默认为"同上"
        price: 0,
        theme: '',
        description: ''
      }
      
      // 在末尾添加新行
      this.products.push(newRow)
    },
    
    removeRow(index) {
      if (this.products.length > 1) {
        // 保存被删除行的实际分类信息
        const deletedCategoryId = this.getActualCategoryId(index)
        const deletedSubCategory = this.getActualSubCategory(index)
        
        // 删除该行
        this.products.splice(index, 1)
        
        // 只检查并更新删除行的下一个商品（如果该商品的一二级分类是"同上"）
        const nextIndex = index // 删除后，下一个商品的索引就是原来的删除位置
        if (nextIndex < this.products.length) {
          const nextProduct = this.products[nextIndex]
          // 只有当下一个商品的分类或子分类是"同上"时，才需要更新
          if (nextProduct.categoryId === 'same' || nextProduct.subCategory === 'same') {
            // 使用被删除行的实际分类信息
            if (nextProduct.categoryId === 'same' && deletedCategoryId) {
              nextProduct.categoryId = deletedCategoryId
            }
            if (nextProduct.subCategory === 'same' && deletedSubCategory) {
              nextProduct.subCategory = deletedSubCategory
            }
          }
        }
      }
    },
    
    async batchPublish() {
      // 验证所有必填字段
      for (let i = 0; i < this.products.length; i++) {
        const product = this.products[i]
        if (!product.name || !this.getActualCategoryId(i) || !this.getActualSubCategory(i) || !product.price) {
          this.error = `第 ${i + 1} 行商品信息不完整，请检查`
          return
        }
      }
      
      this.error = ''
      this.loading = true
      
      try {
        // 逐个发布商品
        const publishPromises = this.products.map((product, index) => {
          // 构建商品对象，使用实际的子分类
          const actualSubCategory = this.getActualSubCategory(index)
          const productData = {
            name: product.name,
            subCategory: actualSubCategory,
            price: product.price,
            description: product.description,
            theme: product.theme
          }
          return this.$axios.post('http://localhost:8081/api/products', productData)
        })
        
        await Promise.all(publishPromises)
        this.success = true
        
        // 发布成功后重置表单
        setTimeout(() => {
          this.$router.push('/seller/dashboard')
        }, 1500)
      } catch (err) {
        this.error = '批量发布失败，请重试'
        console.error(err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.product-batch-form {
  max-width: 100%;
  margin: 0 auto;
}

.table-responsive {
  overflow-x: auto;
  margin-bottom: 1.5rem;
}

.batch-table {
  width: 100%;
  border-collapse: collapse;
}

.batch-table th,
.batch-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.batch-table th {
  background-color: #f5f5f5;
  font-weight: 600;
}

.batch-table input,
.batch-table select,
.batch-table textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.875rem;
}

.batch-table textarea {
  min-height: 60px;
  resize: vertical;
}

.add-button-container {
  display: flex;
  justify-content: flex-start;
  margin-top: 1rem;
}

.action-buttons {
  display: flex;
  align-items: center;
}

.btn-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s ease;
}

.add-btn {
  background-color: #4CAF50;
}

.add-btn:hover {
  background-color: #45a049;
}

.remove-btn {
  background-color: #f44336;
}

.remove-btn:hover {
  background-color: #da190b;
}

.remove-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.alert {
  padding: 1rem;
  margin-bottom: 1.5rem;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .table-controls {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .batch-table {
    font-size: 0.875rem;
  }
  
  .batch-table th,
  .batch-table td {
    padding: 0.5rem;
  }
}
</style>