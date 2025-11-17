import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SellerLogin from '../views/SellerLogin.vue'
import CustomerLogin from '../views/CustomerLogin.vue'
import CustomerRegister from '../views/CustomerRegister.vue'
import SellerDashboard from '../views/SellerDashboard.vue'
import ProductForm from '../views/ProductForm.vue'
import ProductHistory from '../views/ProductHistory.vue'
import BuyerList from '../views/BuyerList.vue'
import ChangePassword from '../views/ChangePassword.vue'
import CustomerList from '../views/CustomerList.vue'
import CustomerOrders from '../views/CustomerOrders.vue'
import ProductBatchForm from '../views/ProductBatchForm.vue'
import SellerCategories from '../views/SellerCategories.vue'
import SellerSubCategories from '../views/SellerSubCategories.vue'
import StockLogs from '../views/StockLogs.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/product/:id',
    name: 'productDetail',
    component: () => import('../views/ProductDetail.vue')
  },
  {
    path: '/login',
    name: 'customerLogin',
    component: CustomerLogin
  },
  {
    path: '/register',
    name: 'customerRegister',
    component: CustomerRegister
  },
  {
    path: '/seller/login',
    name: 'sellerLogin',
    component: SellerLogin
  },
  {
    path: '/seller/dashboard',
    name: 'sellerDashboard',
    component: SellerDashboard,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {    path: '/seller/product/new',
    name: 'newProduct',
    component: ProductForm,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {    path: '/seller/product/batch',
    name: 'batchProductForm',
    component: ProductBatchForm,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {
    path: '/seller/products',
    name: 'productHistory',
    component: ProductHistory,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {
    path: '/seller/buyers',
    name: 'buyerList',
    component: BuyerList,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {
    path: '/seller/change-password',
    name: 'changePassword',
    component: ChangePassword,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {
    path: '/orders',
    name: 'customerOrders',
    component: CustomerOrders,
    meta: { requiresAuth: true, role: 'customer' }
  },
  {
    path: '/seller/customers',
    name: 'customerList',
    component: CustomerList,
    meta: { requiresAuth: true, role: 'seller' }
  },
  {
    path: '/seller/categories',
    name: 'sellerCategories',
    component: SellerCategories,
    meta: { requiresAuth: true, role: 'seller' }
  },
  { 
    path: '/seller/sub-categories',
    name: 'sellerSubCategories',
    component: SellerSubCategories,
    meta: { requiresAuth: true, role: 'seller' }
  },
  { 
    path: '/seller/stock-logs',
    name: 'stockLogs',
    component: StockLogs,
    meta: { requiresAuth: true, role: 'seller' }
  },
  ]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫，检查是否已登录
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

  if (requiresAuth) {
    const record = to.matched.find(r => r.meta.role);
    // 检查是否需要卖家登录
    if (record && record.meta.role === 'seller') {
      if (!localStorage.getItem('sellerLoggedIn')) {
        next({ name: 'sellerLogin' })
      } else {
        next()
      }
    } else {
      // 其他需要登录的页面（客户）
      if (!localStorage.getItem('customerLoggedIn')) {
        next({ name: 'customerLogin' })
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router