<template>
  <div class="subcategory-management">
    <div class="header-container">
      <h1>二级分类管理</h1>
      <button class="btn btn-secondary" @click="goBack">返回</button>
    </div>
    
    <div class="card">
      <h2>添加新二级分类</h2>
      <form @submit.prevent="addSubCategory">
        <div class="form-group">
          <label for="categorySelect">所属一级分类</label>
          <select 
            id="categorySelect" 
            v-model="newSubCategory.categoryId" 
            required
            @change="onCategoryChange"
          >
            <option value="">请选择一级分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label for="subCategoryName">二级分类名称</label>
          <input 
            type="text" 
            id="subCategoryName" 
            v-model="newSubCategory.name" 
            required 
            placeholder="请输入二级分类名称"
          >
        </div>
        <div class="form-group">
          <label for="subCategoryDescription">二级分类描述</label>
          <textarea 
            id="subCategoryDescription" 
            v-model="newSubCategory.description" 
            placeholder="请输入二级分类描述"
          ></textarea>
        </div>
        <div class="form-group">
          <label for="subCategorySortOrder">排序序号</label>
          <input 
            type="number" 
            id="subCategorySortOrder" 
            v-model.number="newSubCategory.sortOrder" 
            min="0" 
            required
          >
        </div>
        <button type="submit" class="btn">添加二级分类</button>
      </form>
    </div>
    
    <div class="card">
      <h2>二级分类列表</h2>
      
      <div class="filter-section">
        <label for="categoryFilter">按一级分类筛选：</label>
        <select id="categoryFilter" v-model="selectedCategoryFilter" @change="fetchSubCategories">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}</option>
        </select>
      </div>
      
      <div v-if="loading" class="loading">加载中...</div>
      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      <div v-if="!loading && subCategories.length === 0" class="empty-message">
        暂无二级分类数据
      </div>
      
      <table v-if="!loading && subCategories.length > 0" class="subcategory-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>所属一级分类</th>
            <th>名称</th>
            <th>描述</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="subCategory in subCategories" :key="subCategory.id">
            <td>{{ subCategory.id }}</td>
            <td>{{ getCategoryName(subCategory.categoryId) }}</td>
            <td>{{ subCategory.name }}</td>
            <td>{{ subCategory.description }}</td>
            <td>{{ subCategory.sortOrder }}</td>
            <td>
              <span :class="subCategory.active ? 'status-active' : 'status-inactive'">
                {{ subCategory.active ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button 
                class="btn btn-sm" 
                @click="toggleSubCategoryStatus(subCategory)"
              >
                {{ subCategory.active ? '禁用' : '启用' }}
              </button>
              <button 
                class="btn btn-sm btn-secondary" 
                @click="editSubCategory(subCategory)"
              >
                编辑
              </button>
              <button 
                class="btn btn-sm btn-danger" 
                @click="deleteSubCategory(subCategory)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 编辑二级分类对话框 -->
    <div v-if="editingSubCategory" class="modal-overlay" @click.self="cancelEdit">
      <div class="modal">
        <h3>编辑二级分类</h3>
        <form @submit.prevent="updateSubCategory">
          <div class="form-group">
            <label for="editCategoryId">所属一级分类</label>
            <select 
              id="editCategoryId" 
              v-model="editingSubCategory.categoryId" 
              required
            >
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="editName">二级分类名称</label>
            <input 
              type="text" 
              id="editName" 
              v-model="editingSubCategory.name" 
              required
            >
          </div>
          <div class="form-group">
            <label for="editDescription">二级分类描述</label>
            <textarea 
              id="editDescription" 
              v-model="editingSubCategory.description"
            ></textarea>
          </div>
          <div class="form-group">
            <label for="editSortOrder">排序序号</label>
            <input 
              type="number" 
              id="editSortOrder" 
              v-model.number="editingSubCategory.sortOrder" 
              min="0" 
              required
            >
          </div>
          <div class="form-actions">
            <button type="submit" class="btn">更新</button>
            <button type="button" class="btn btn-secondary" @click="cancelEdit">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>


export default {
  name: 'SellerSubCategories',
  data() {
    return {
      categories: [],
      subCategories: [],
      newSubCategory: {
        categoryId: '',
        name: '',
        description: '',
        sortOrder: 0
      },
      editingSubCategory: null,
      selectedCategoryFilter: '',
      loading: true,
      error: null
    };
  },
  created() {
    this.fetchCategories();
  },
  methods: {
    async fetchCategories() {
      try {
        // 使用商家后台专用API端点，使用相对路径避免CORS错误
        const response = await this.$axios.get('/categories/seller/categories');
        // 确保categories始终为数组类型
        this.categories = Array.isArray(response.data?.data) ? response.data.data : [];
        this.error = null;
        // 获取一级分类后，再获取二级分类
        this.fetchSubCategories();
      } catch (err) {
        console.error('获取一级分类列表失败:', err);
        this.error = '获取一级分类列表失败，请稍后重试';
        // 出错时确保categories为数组
        this.categories = [];
        this.loading = false;
      }
    },
    
    async fetchSubCategories() {
      try {
        let url = '/sub-categories/seller/sub-categories';
        if (this.selectedCategoryFilter) {
          url = `/sub-categories/seller/sub-categories/category/${this.selectedCategoryFilter}`;
        }
        
        const response = await this.$axios.get(url);
        // 确保subCategories始终为数组类型
        this.subCategories = Array.isArray(response.data?.data) ? response.data.data : [];
        this.error = null;
      } catch (err) {
        console.error('获取二级分类列表失败:', err);
        this.error = '获取二级分类列表失败，请稍后重试';
        // 出错时确保subCategories为数组
        this.subCategories = [];
      } finally {
        this.loading = false;
      }
    },
    
    async addSubCategory() {
      try {
        // 验证categoryId是否有效
        if (!this.newSubCategory.categoryId) {
          this.error = '请选择所属分类';
          return;
        }
        
        // 验证名称是否填写
        if (!this.newSubCategory.name || this.newSubCategory.name.trim() === '') {
          this.error = '请输入二级分类名称';
          return;
        }
        
        // 确保categoryId为数字类型，与后端SubCategoryDTO的Long类型匹配
        const categoryId = Number(this.newSubCategory.categoryId);
        if (isNaN(categoryId)) {
          this.error = '无效的分类ID';
          return;
        }
        
        // 构建请求数据，确保与后端SubCategoryDTO格式完全匹配
        const requestData = {
          categoryId: categoryId,
          name: this.newSubCategory.name.trim(),
          description: this.newSubCategory.description || '',
          active: true,
          sortOrder: this.newSubCategory.sortOrder || 0,
          icon: this.newSubCategory.icon || ''
        };
        
        console.log('提交的二级分类数据:', requestData);
        
        // 使用商家后台专用API端点
        const response = await this.$axios.post('/sub-categories/seller/sub-categories', requestData);
        
        // 处理响应数据，确保subCategories是数组
        if (Array.isArray(this.subCategories) && response.data?.success && response.data?.data) {
          this.subCategories.push(response.data.data);
          // 重置表单
          this.newSubCategory = {
            categoryId: '',
            name: '',
            description: '',
            sortOrder: 0
          };
        }
        
        this.error = null;
      } catch (err) {
        console.error('添加二级分类失败:', err);
        // 显示更详细的错误信息
        if (err.response) {
          console.error('错误响应状态:', err.response.status);
          console.error('错误响应数据:', err.response.data);
          this.error = `添加二级分类失败: ${err.response.data?.message || '请求被服务器拒绝'}`;
        } else {
          this.error = '添加二级分类失败，请稍后重试';
        }
      }
    },
    
    async toggleSubCategoryStatus(subCategory) {
      try {
        const updatedSubCategory = {
          ...subCategory,
          active: !subCategory.active
        };
        
        // 使用商家后台专用API端点
        await this.$axios.put(`/sub-categories/seller/sub-categories/${subCategory.id}`, updatedSubCategory);
        subCategory.active = !subCategory.active;
        this.error = null;
      } catch (err) {
        console.error('更新二级分类状态失败:', err);
        this.error = '更新二级分类状态失败，请稍后重试';
      }
    },
    
    editSubCategory(subCategory) {
      // 创建一个新对象，避免直接修改原数组中的对象
      this.editingSubCategory = { ...subCategory };
    },
    
    cancelEdit() {
      this.editingSubCategory = null;
    },
    
    async updateSubCategory() {
      try {
        // 验证categoryId是否有效
        if (!this.editingSubCategory.categoryId) {
          this.error = '请选择所属分类';
          return;
        }
        
        // 验证名称是否填写
        if (!this.editingSubCategory.name || this.editingSubCategory.name.trim() === '') {
          this.error = '请输入二级分类名称';
          return;
        }
        
        // 确保categoryId为数字类型，与后端SubCategoryDTO的Long类型匹配
        const categoryId = Number(this.editingSubCategory.categoryId);
        if (isNaN(categoryId)) {
          this.error = '无效的分类ID';
          return;
        }
        
        // 构建请求数据，确保与后端SubCategoryDTO格式完全匹配
        const requestData = {
          categoryId: categoryId,
          name: this.editingSubCategory.name.trim(),
          description: this.editingSubCategory.description || '',
          active: this.editingSubCategory.active,
          sortOrder: this.editingSubCategory.sortOrder || 0,
          icon: this.editingSubCategory.icon || ''
        };
        
        console.log('更新的二级分类数据:', requestData);
        
        // 使用商家后台专用API端点
        await this.$axios.put(`/sub-categories/seller/sub-categories/${this.editingSubCategory.id}`, requestData);
        
        // 更新本地数组中的二级分类，确保subCategories是数组
        if (Array.isArray(this.subCategories)) {
          const index = this.subCategories.findIndex(sc => sc.id === this.editingSubCategory.id);
          if (index !== -1) {
            this.subCategories[index] = { ...this.editingSubCategory };
          }
        }
        
        this.editingSubCategory = null;
        this.error = null;
      } catch (err) {
        console.error('更新二级分类失败:', err);
        this.error = '更新二级分类失败，请稍后重试';
      }
    },

    getCategoryName(categoryId) {
      const category = this.categories.find(c => c.id === categoryId);
      return category ? category.name : '-';
    },
    
    onCategoryChange() {
      // 当选择不同的一级分类时，可以在这里添加额外的处理逻辑
    },
    
    async deleteSubCategory(subCategory) {
      // 确认删除操作
      if (confirm(`确定要删除二级分类"${subCategory.name}"吗？此操作不可撤销。`)) {
        try {
          // 使用正确的API端点
          const response = await this.$axios.delete(`http://localhost:8081/api/sub-categories/${subCategory.id}`);
          
          // 从本地数组中移除该二级分类
          if (Array.isArray(this.subCategories)) {
            const index = this.subCategories.findIndex(sc => sc.id === subCategory.id);
            if (index !== -1) {
              this.subCategories.splice(index, 1);
            }
          }
          
          this.error = null;
          console.log('二级分类删除成功:', response.data);
        } catch (err) {
          console.error('删除二级分类失败:', err);
          this.error = `删除二级分类失败: ${err.response?.data?.message || '请求被服务器拒绝'}`;
        }
      }
    },
    
    // 返回上一页
    goBack() {
      this.$router.push('/seller/dashboard')
    }
  }
};
</script>

<style scoped>
.subcategory-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h1 {
  color: #E02E24;
  margin-bottom: 0;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

h2 {
  color: #333;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.filter-section {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-section label {
  font-weight: 500;
  min-width: 120px;
}

.filter-section select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  flex: 1;
  max-width: 300px;
}

.btn {
  display: inline-block;
  padding: 8px 16px;
  background-color: #E02E24;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn:hover {
  background-color: #c82333;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
  margin-right: 5px;
}

.btn-secondary {
  background-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.btn-danger {
  background-color: #dc3545;
}

.btn-danger:hover {
  background-color: #c82333;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.alert {
  padding: 12px;
  margin-bottom: 15px;
  border-radius: 4px;
  font-size: 14px;
}

.alert-danger {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #999;
  font-style: italic;
}

.subcategory-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.subcategory-table th,
.subcategory-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.subcategory-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.subcategory-table tr:hover {
  background-color: #f8f9fa;
}

.status-active {
  color: #28a745;
  font-weight: 500;
}

.status-inactive {
  color: #dc3545;
  font-weight: 500;
}

/* 模态框样式 */
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

.modal {
  background: white;
  border-radius: 8px;
  padding: 25px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.modal h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>