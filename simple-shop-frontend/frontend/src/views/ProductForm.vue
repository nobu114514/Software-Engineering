<template>
  <div class="product-form">
    <div class="header-section">
      <h1>{{ isEditing ? '编辑商品' : '发布新商品' }}</h1>
      <div class="header-actions">
        <button type="button" class="btn btn-secondary" @click="goBack">返回</button>
        <button v-if="!isEditing" @click="goToBatchPublish" class="btn btn-primary batch-publish-btn">批量发布</button>
      </div>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <div v-if="success" class="alert alert-success">
      {{ isEditing ? '商品更新成功' : '商品发布成功' }}
    </div>
    
    <form @submit.prevent="saveProduct" v-if="!loading" class="card">
      <div class="form-group">
        <label for="name">商品名称</label>
        <input type="text" id="name" v-model="product.name" required>
      </div>
      <div class="form-group">
        <label for="category">商品分类</label>
        <select id="category" v-model="selectedCategoryId" required>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label for="subCategory">商品子分类</label>
        <select id="subCategory" v-model="product.subCategory" required>
          <option v-for="subCategory in subCategories" :key="subCategory.id" :value="subCategory">
            {{ subCategory.name }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label for="price">商品价格</label>
        <input type="number" id="price" v-model="product.price" min="0" step="0.01" required>
      </div>
      <div class="form-group">
        <label for="imageUrl">商品主图URL</label>
        <input type="text" id="imageUrl" v-model="product.imageUrl" 
               placeholder="输入图片URL，如：https://picsum.photos/600/400">
        <p class="help-text">在下方的商品详情中插入更多图片</p>
      </div>
      <div class="form-group">
        <label for="stock">商品库存</label>
        <input type="number" id="stock" v-model="product.stock" min="1" step="1" required>
        <p class="help-text"><strong style="color: #E02E24;">（库存必须大于0）</strong></p>
      </div>
      <div class="form-group">
        <label>商品详情</label>
        <RichTextEditor v-model="product.description" label="" />
      </div>
      <div class="form-actions">
        <button type="submit" class="btn">{{ isEditing ? '更新商品' : '发布商品' }}</button>
      </div>
    </form>
  </div>
</template>

<script>
import RichTextEditor from '../components/RichTextEditor.vue'

export default {
  name: 'ProductForm',
  components: {
    RichTextEditor
  },
  data() {
    return {
      product: {
        name: '',
        price: 0,
        imageUrl: '',
        description: '',
        stock: 1,
        subCategory: null
      },
      isEditing: false,
      loading: true,
      error: '',
      success: false,
      categories: [],
      subCategories: [],
      selectedCategoryId: null
    }
  },
  created() {
    const productId = this.$route.query.id
    if (productId) {
      this.isEditing = true
      Promise.all([this.fetchCategories(), this.fetchProduct(productId)])
        .finally(() => {
          this.loading = false
        })
    } else {
      this.fetchCategories().finally(() => {
        this.loading = false
      })
    }
  },
  watch: {
    selectedCategoryId(newVal) {
      this.loadSubCategories(newVal)
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await this.$axios.get('http://localhost:8081/api/categories/active')
        // 正确访问嵌套在data字段中的分类列表
        this.categories = response.data.data || []
        // 如果有分类且不是编辑模式，自动选择第一个分类
        if (this.categories.length > 0 && !this.isEditing && !this.selectedCategoryId) {
          this.selectedCategoryId = this.categories[0].id
        }
      } catch (err) {
        console.error('获取分类信息失败', err)
      }
    },
    
    async loadSubCategories(categoryId) {
      if (!categoryId) {
        this.subCategories = []
        this.product.subCategory = null
        return
      }
      try {
        const response = await this.$axios.get(`http://localhost:8081/api/sub-categories/category/${categoryId}`)
        // SubCategoryController直接返回列表，不包含data字段
        this.subCategories = Array.isArray(response.data) ? response.data : []
        // 如果有子分类且之前没有选择过，自动选择第一个子分类
        if (this.subCategories.length > 0 && !this.product.subCategory) {
          this.product.subCategory = this.subCategories[0]
        }
      } catch (err) {
        console.error('获取二级分类信息失败', err)
      }
    },
    
    async fetchProduct(id) {
      try {
        const response = await this.$axios.get(`http://localhost:8081/api/products/${id}`)
        // 正确访问嵌套在data字段中的商品数据
        const productData = response.data.data || response.data
        // 合并响应数据到现有product对象以保留stock等属性的响应式特性
        this.product = Object.assign({}, this.product, productData)
        // 如果商品有二级分类，设置选中的分类
        if (this.product.subCategory && this.product.subCategory.category) {
          this.selectedCategoryId = this.product.subCategory.category.id
          // 等待分类列表加载完成后再加载二级分类
          await this.$nextTick()
          this.loadSubCategories(this.selectedCategoryId)
        }
        this.error = ''
      } catch (err) {
        this.error = '获取商品信息失败'
        console.error(err)
      }
    },
    goToBatchPublish() {
      this.$router.push('/seller/product/batch')
    },
    
    // 返回上一页
    goBack() {
      this.$router.push('/seller/dashboard')
    },
    async saveProduct() {
      try {
        console.log('Sending product data:', this.product); // 添加日志查看发送的数据
        if (this.isEditing) {
          await this.$axios.put(`http://localhost:8081/api/products/${this.product.id}`, this.product)
        } else {
          await this.$axios.post('http://localhost:8081/api/products', this.product)
        }
        this.success = true
        // 重置表单或跳转
        setTimeout(() => {
          this.$router.push('/seller/dashboard')
        }, 1500)
      } catch (err) {
        this.error = this.isEditing ? '更新商品失败' : '发布商品失败'
        console.error(err)
      }
    }
  }
}
</script>

<style scoped>
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.batch-publish-btn {
  padding: 0.5rem 1rem;
}

.form-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

input[type="text"],
input[type="number"],
textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.help-text {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #666;
}

/* 富文本编辑器容器样式 */
:deep(.rich-text-editor) {
  min-height: 200px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 0.5rem;
  outline: none;
}

:deep(.rich-text-editor:focus) {
  border-color: #4a90e2;
}

:deep(.toolbar) {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

:deep(.toolbar button) {
  padding: 0.5rem 0.75rem;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

:deep(.toolbar button:hover) {
  background-color: #e9e9e9;
}
</style>
