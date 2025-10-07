import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import SellerLogin from '../views/SellerLogin.vue'
import SellerDashboard from '../views/SellerDashboard.vue'
import ProductForm from '../views/ProductForm.vue'
import ProductHistory from '../views/ProductHistory.vue'
import BuyerList from '../views/BuyerList.vue'
import ChangePassword from '../views/ChangePassword.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
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
    meta: { requiresAuth: true }
  },
  {
    path: '/seller/product/new',
    name: 'newProduct',
    component: ProductForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/seller/products',
    name: 'productHistory',
    component: ProductHistory,
    meta: { requiresAuth: true }
  },
  {
    path: '/seller/buyers',
    name: 'buyerList',
    component: BuyerList,
    meta: { requiresAuth: true }
  },
  {
    path: '/seller/change-password',
    name: 'changePassword',
    component: ChangePassword,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫，检查是否已登录
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    if (!localStorage.getItem('sellerLoggedIn')) {
      next({ name: 'sellerLogin' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
