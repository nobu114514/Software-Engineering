<template>
  <div class="container">
    <h2>库存日志</h2>
    <div class="stock-logs-container">
      <div class="filter-section">
        <input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          class="search-input"
        />
        <button class="filter-button" @click="searchStockLogs">搜索</button>
      </div>
      <div class="stock-logs-table-container">
        <table class="stock-logs-table">
          <thead>
            <tr>
              <th>商品名称</th>
              <th>变更数量</th>
              <th>变更前库存</th>
              <th>变更后库存</th>
              <th>操作类型</th>
              <th>描述</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in stockLogs" :key="log.id">
              <td>{{ log.productName }}</td>
              <td :class="{ 'positive': log.changeQuantity > 0, 'negative': log.changeQuantity < 0 }">
                {{ log.changeQuantity > 0 ? '+' : '' }}{{ log.changeQuantity }}
              </td>
              <td>{{ log.previousStock }}</td>
              <td>{{ log.currentStock }}</td>
              <td>{{ log.action }}</td>
              <td>{{ log.description }}</td>
              <td>{{ formatDateTime(log.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="stockLogs.length === 0" class="empty-message">
          没有库存日志记录
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StockLogs',
  data() {
    return {
      stockLogs: [],
      searchKeyword: ''
    }
  },
  mounted() {
    this.fetchStockLogs()
  },

  methods: {
    async fetchStockLogs() {
      try {
        const response = await this.$axios.get('stock-logs')
        this.stockLogs = response.data
      } catch (error) {
        console.error('获取库存日志失败:', error)
        alert('获取库存日志失败')
      }
    },
    searchStockLogs() {
      if (this.searchKeyword) {
        this.$axios.get('stock-logs', {
          params: {
            productName: this.searchKeyword
          }
        })
        .then(response => {
          this.stockLogs = response.data
        })
        .catch(error => {
          alert('搜索库存日志失败')
          console.error('搜索库存日志失败:', error)
        })
      } else {
        this.fetchStockLogs()
      }
    },
    formatDateTime(dateTimeString) {
      // 格式化日期时间
      const date = new Date(dateTimeString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.stock-logs-container {
  margin-top: 20px;
}

.filter-section {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 200px;
}

.filter-button {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.filter-button:hover {
  background-color: #66b1ff;
}

.stock-logs-table-container {
  overflow-x: auto;
}

.stock-logs-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.stock-logs-table th,
.stock-logs-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.stock-logs-table th {
  background-color: #f5f7fa;
  font-weight: 700;
  color: #606266;
  white-space: nowrap;
}

.stock-logs-table tr:hover {
  background-color: #f5f7fa;
}

.stock-logs-table td.positive {
  color: #67c23a;
  font-weight: 500;
}

.stock-logs-table td.negative {
  color: #f56c6c;
  font-weight: 500;
}

.empty-message {
  text-align: center;
  padding: 50px 0;
  color: #909399;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>