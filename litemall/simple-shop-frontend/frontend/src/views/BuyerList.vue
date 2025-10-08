<template>
  <div class="buyer-list">
    <h1>购买意向列表</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <table v-if="!loading && buyers.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>商品</th>
          <th>购买人</th>
          <th>电话</th>
          <th>地址</th>
          <th>时间</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="buyer in buyers" :key="buyer.id">
          <td>{{ buyer.id }}</td>
          <td>{{ buyer.product?.name || '未知商品' }}</td>
          <td>{{ buyer.name }}</td>
          <td>{{ buyer.phone }}</td>
          <td>{{ buyer.address }}</td>
          <td>{{ formatDate(buyer.createdAt) }}</td>
          <td>
            <span v-if="buyer.isCompleted">已完成</span>
            <span v-else>处理中</span>
          </td>
          <td v-if="!buyer.isCompleted">
            <button class="btn" @click="completeTransaction(buyer.id, true)">交易成功</button>
            <button class="btn btn-secondary" @click="completeTransaction(buyer.id, false)">交易失败</button>
          </td>
          <td v-else>已处理</td>
        </tr>
      </tbody>
    </table>
    
    <div v-if="!loading && buyers.length === 0" class="alert alert-danger">
      暂无购买意向记录
    </div>
    <br>
    <a href="/seller/dashboard" class="btn btn-secondary" style="margin-bottom: 1rem;">
      返回
    </a>
  </div>
</template>

<script>
export default {
  name: 'BuyerList',
  data() {
    return {
      buyers: [],
      loading: true,
      error: ''
    }
  },
  created() {
    this.fetchBuyers()
  },
  methods: {
    async fetchBuyers() {
      try {
        this.loading = true
        const response = await this.$axios.get('/buyers')
        this.buyers = response.data
        this.error = ''
      } catch (err) {
        this.error = '获取购买意向失败'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async completeTransaction(buyerId, success) {
      try {
        await this.$axios.put(`/buyers/${buyerId}/complete`, null, {
          params: { success }
        })
        this.fetchBuyers()
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
</style>
