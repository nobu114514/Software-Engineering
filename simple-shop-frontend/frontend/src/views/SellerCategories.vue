<template>
  <div class="category-management">
    <h1>分类管理</h1>
    
    <div class="card">
      <h2>添加新分类</h2>
      <form @submit.prevent="addCategory">
        <div class="form-group">
          <label for="categoryName">分类名称</label>
          <input 
            type="text" 
            id="categoryName" 
            v-model="newCategory.name" 
            required 
            placeholder="请输入分类名称"
          >
        </div>
        <div class="form-group">
          <label for="categoryDescription">分类描述</label>
          <textarea 
            id="categoryDescription" 
            v-model="newCategory.description" 
            placeholder="请输入分类描述"
          ></textarea>
        </div>
        <div class="form-group">
          <label for="categoryIcon">分类图标</label>
          <input 
            type="text" 
            id="categoryIcon" 
            v-model="newCategory.icon" 
            placeholder="请输入图标名称或URL"
          >
        </div>
        <div class="form-group">
          <label for="sortOrder">排序序号</label>
          <input 
            type="number" 
            id="sortOrder" 
            v-model.number="newCategory.sortOrder" 
            min="0" 
            required
          >
        </div>
        <button type="submit" class="btn">添加分类</button>
      </form>
    </div>
    
    <div class="card">
      <h2>分类列表</h2>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      
      <table v-if="!loading" class="category-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>描述</th>
            <th>图标</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in categories" :key="category.id">
            <td>{{ category.id }}</td>
            <td>{{ category.name }}</td>
            <td>{{ category.description }}</td>
            <td>
              <img 
                v-if="category.icon" 
                :src="category.icon" 
                alt="分类图标" 
                class="category-icon"
              >
              <span v-else>-</span>
            </td>
            <td>{{ category.sortOrder }}</td>
            <td>
              <span :class="category.active ? 'status-active' : 'status-inactive'">
                {{ category.active ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button 
                class="btn btn-sm" 
                @click="toggleCategoryStatus(category)"
              >
                {{ category.active ? '禁用' : '启用' }}
              </button>
              <button 
                class="btn btn-sm btn-secondary" 
                @click="editCategory(category)"
              >
                编辑
              </button>
              <button 
                class="btn btn-sm btn-danger" 
                @click="deleteCategory(category)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 编辑分类对话框 -->
    <div v-if="editingCategory" class="modal-overlay" @click.self="cancelEdit">
      <div class="modal">
        <h3>编辑分类</h3>
        <form @submit.prevent="updateCategory">
          <div class="form-group">
            <label for="editName">分类名称</label>
            <input 
              type="text" 
              id="editName" 
              v-model="editingCategory.name" 
              required
            >
          </div>
          <div class="form-group">
            <label for="editDescription">分类描述</label>
            <textarea 
              id="editDescription" 
              v-model="editingCategory.description"
            ></textarea>
          </div>
          <div class="form-group">
            <label for="editIcon">分类图标</label>
            <input 
              type="text" 
              id="editIcon" 
              v-model="editingCategory.icon"
            >
          </div>
          <div class="form-group">
            <label for="editSortOrder">排序序号</label>
            <input 
              type="number" 
              id="editSortOrder" 
              v-model.number="editingCategory.sortOrder" 
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
import axios from 'axios';

export default {
  name: 'SellerCategories',
  data() {
    return {
      categories: [],
      newCategory: {
        name: '',
        description: '',
        icon: '',
        sortOrder: 0
      },
      editingCategory: null,
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
        // 使用正确的商家后台API端点
        const response = await axios.get('http://localhost:8081/api/categories/seller/categories');
        // 处理商家API的响应格式
        this.categories = Array.isArray(response.data?.data) ? response.data.data : [];
        this.error = null;
      } catch (err) {
        console.error('获取分类列表失败:', err);
        this.error = '获取分类列表失败，请稍后重试';
        // 出错时也要确保categories是数组
        this.categories = [];
      } finally {
        this.loading = false;
      }
    },
    
    async addCategory() {
      try {
        // 使用正确的商家后台API端点
        const response = await axios.post('http://localhost:8081/api/categories/seller/categories', {
          ...this.newCategory,
          active: true
        });
        
        // 处理商家API的响应格式
        if (response.data?.success && response.data?.data) {
          this.categories.push(response.data.data);
          // 重置表单
          this.newCategory = {
            name: '',
            description: '',
            icon: '',
            sortOrder: 0
          };
        }
        
        this.error = null;
      } catch (err) {
        console.error('添加分类失败:', err);
        this.error = '添加分类失败，请稍后重试';
      }
    },
    
    async toggleCategoryStatus(category) {
      try {
        const updatedCategory = {
          ...category,
          active: !category.active
        };
        
        // 使用正确的商家后台API端点
        await axios.put(`http://localhost:8081/api/categories/seller/categories/${category.id}`, updatedCategory);
        category.active = !category.active;
        this.error = null;
      } catch (err) {
        console.error('更新分类状态失败:', err);
        this.error = '更新分类状态失败，请稍后重试';
      }
    },
    
    editCategory(category) {
      // 创建一个新对象，避免直接修改原数组中的对象
      this.editingCategory = { ...category };
    },
    
    cancelEdit() {
      this.editingCategory = null;
    },
    
    async updateCategory() {
      if (!this.editingCategory) return;
      
      try {
        // 使用正确的商家后台API端点
        await axios.put(`http://localhost:8081/api/categories/seller/categories/${this.editingCategory.id}`, this.editingCategory);
        
        // 更新本地数组中的分类
        const index = this.categories.findIndex(c => c.id === this.editingCategory.id);
        if (index !== -1) {
          this.categories[index] = { ...this.editingCategory };
        }
        
        this.editingCategory = null;
        this.error = null;
      } catch (err) {
        console.error('更新分类失败:', err);
        this.error = '更新分类失败，请稍后重试';
      }
    },
    
    async deleteCategory(category) {
      // 确认删除操作
      if (confirm(`确定要删除分类"${category.name}"吗？此操作不可撤销。`)) {
        try {
          // 使用正确的API端点，与SellerSubCategories.vue保持一致
          const response = await this.$axios.delete(`http://localhost:8081/api/categories/${category.id}`);
          
          // 从本地数组中移除该分类
          const index = this.categories.findIndex(c => c.id === category.id);
          if (index !== -1) {
            this.categories.splice(index, 1);
          }
          
          this.error = null;
          console.log('分类删除成功:', response.data);
        } catch (err) {
          console.error('删除分类失败:', err);
          this.error = `删除分类失败: ${err.response?.data?.message || '请求被服务器拒绝'}`;
        }
      }
    }
  }
};
</script>

<style scoped>
.category-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  color: #E02E24;
  margin-bottom: 20px;
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
.form-group textarea {
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

/* 分类图标样式 */
.category-icon {
  width: 64px;
  height: 64px;
  object-fit: contain;
  vertical-align: middle;
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

.category-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.category-table th,
.category-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.category-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.category-table tr:hover {
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