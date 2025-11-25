import { createRouter, createWebHistory } from 'vue-router'

// 懒加载路由组件
const Home = () => import('./views/Home.vue')
const ArticleDetail = () => import('./views/ArticleDetail.vue')
const Login = () => import('./views/Login.vue')
const Register = () => import('./views/Register.vue')
const CreateArticle = () => import('./views/CreateArticle.vue')
const Profile = () => import('./views/Profile.vue')
const ForgotPassword = () => import('./views/ForgotPassword.vue')
// 管理员中心组件
const AdminDashboard = () => import('./views/AdminDashboard.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail,
    props: true
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword
  },
  {
    path: '/create/:id?',
    name: 'CreateArticle',
    component: CreateArticle,
    props: true,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      requiresAuth: true
    }
  },
  // 管理员中心路由
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  // 捕获所有未匹配的路由，重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，用于检查需要登录和管理员权限的路由
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  const isLoggedIn = localStorage.getItem('token') !== null
  
  // 获取用户信息检查是否为管理员
  let isAdmin = false
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      isAdmin = user.role === 'ADMIN'
    }
  } catch (e) {
    console.error('解析用户信息失败:', e)
  }

  if (requiresAuth && !isLoggedIn) {
    // 如果需要登录但用户未登录，重定向到登录页
    next('/login')
  } else if (requiresAdmin && !isAdmin) {
    // 如果需要管理员权限但用户不是管理员，重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router