import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Home from '../views/Home.vue'
import Dashboard from '../views/Dashboard.vue'
import Dorm from '../views/Dorm.vue'
import Payments from '../views/Payments.vue'
import Repairs from '../views/Repairs.vue'
import Profile from '../views/Profile.vue'
import Login from '../views/Login.vue'
import Notices from '../views/Notices.vue'
import NoticeDetail from '../views/NoticeDetail.vue'
import ExchangeList from '../views/ExchangeList.vue'
import ExchangeForm from '../views/ExchangeForm.vue'
import DormSearch from '../views/DormSearch.vue'
import PaymentAbnormal from '../views/PaymentAbnormal.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminStudents from '../views/AdminStudents.vue'
import AdminDormitories from '../views/AdminDormitories.vue'
import AdminAllocations from '../views/AdminAllocations.vue'
import AdminNotifications from '../views/AdminNotifications.vue'
// lazy for heavy admin lists

const routes: Array<RouteRecordRaw> = [
  { path: '/login', name: 'login', component: Login },
  {
    path: '/', component: MainLayout, children: [
      { path: '', name: 'home', component: Home, meta: { requiresAuth: true } },
      { path: 'dashboard', name: 'dashboard', component: Dashboard, meta: { requiresAuth: true } },
      { path: 'dorm', name: 'dorm', component: Dorm, meta: { requiresAuth: true } },
      { path: 'payments', name: 'payments', component: Payments, meta: { requiresAuth: true } },
      { path: 'repairs', name: 'repairs', component: Repairs, meta: { requiresAuth: true } },
      { path: 'profile', name: 'profile', component: Profile, meta: { requiresAuth: true } },
      { path: 'notices', name: 'notices', component: Notices, meta: { requiresAuth: true } },
      { path: 'notices/:id', name: 'notice-detail', component: NoticeDetail, meta: { requiresAuth: true } },
      { path: 'exchange', name: 'exchange-list', component: ExchangeList, meta: { requiresAuth: true } },
      { path: 'exchange/new', name: 'exchange-new', component: ExchangeForm, meta: { requiresAuth: true } },
      { path: 'dorm-search', name: 'dorm-search', component: DormSearch, meta: { requiresAuth: true } },
      { path: 'payment-abnormal', name: 'payment-abnormal', component: PaymentAbnormal, meta: { requiresAuth: true } }
    ]
  }
  ,
  {
    path: '/admin', component: AdminLayout, children: [
      { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
      { path: 'students', name: 'admin-students', component: AdminStudents, meta: { requiresAdmin: true } },
      { path: 'dormitories', name: 'admin-dormitories', component: AdminDormitories, meta: { requiresAdmin: true } },
      { path: 'allocations', name: 'admin-allocations', component: AdminAllocations, meta: { requiresAdmin: true } },
      { path: 'notifications', name: 'admin-notifications', component: AdminNotifications, meta: { requiresAdmin: true } },
      { path: 'repairs', name: 'admin-repairs', component: () => import('../views/AdminRepairs.vue'), meta: { requiresAdmin: true } },
      { path: 'payments', name: 'admin-payments', component: () => import('../views/AdminPayments.vue'), meta: { requiresAdmin: true } },
      { path: 'exchanges', name: 'admin-exchanges', component: () => import('../views/AdminExchanges.vue'), meta: { requiresAdmin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Route guards: simple auth check using store token
import store from '../store'
router.beforeEach((to, _from, next) => {
  const needAuth = to.meta && (to.meta as any).requiresAuth
  const requiresAdmin = to.meta && (to.meta as any).requiresAdmin
  
  // 检查localStorage中的token，确保store状态正确
  const token = localStorage.getItem('token')
  const user = localStorage.getItem('user')
  
  if (needAuth && !token) {
    next({ name: 'login' })
  } else if (requiresAdmin) {
    if (!token) return next({ name: 'login' })
    try {
      const userObj = user ? JSON.parse(user) : null
      if (userObj && userObj.userType === 'admin') return next()
      return next({ name: 'home' })
    } catch (e) {
      return next({ name: 'login' })
    }
  } else next()
})

export default router


