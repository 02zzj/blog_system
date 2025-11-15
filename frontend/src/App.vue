<template>
  <div class="app">
    <header class="header">
      <nav class="nav">
        <div class="container">
          <router-link to="/" class="logo">博客系统</router-link>
          <div class="nav-links">
            <router-link to="/" class="nav-link">首页</router-link>
            <router-link v-if="isLoggedIn" to="/create" class="nav-link">写文章</router-link>
            <router-link v-if="isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
            <div v-else>
              <router-link to="/login" class="nav-link">登录</router-link>
              <router-link to="/register" class="nav-link">注册</router-link>
            </div>
            <button v-if="isLoggedIn" @click="logout" class="logout-btn">退出</button>
          </div>
        </div>
      </nav>
    </header>
    
    <main class="main">
      <div class="container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
    
    <footer class="footer">
      <div class="container">
        <p>&copy; 2023 博客系统. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from './axios'

export default {
  name: 'App',
  setup() {
    const isLoggedIn = ref(false)
    const router = useRouter()

    const checkLoginStatus = () => {
      const token = localStorage.getItem('token')
      isLoggedIn.value = !!token
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      isLoggedIn.value = false
      router.push('/')
    }

    onMounted(() => {
      checkLoginStatus()
      
      // 全局设置axios的token
      const token = localStorage.getItem('token')
      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
      
      // 监听路由变化，在每次路由切换后检查登录状态
      router.afterEach(() => {
        checkLoginStatus()
      })
    })

    return {
      isLoggedIn,
      logout
    }
  }
}
</script>

<style>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav {
  padding: 20px 0;
}

.nav .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  color: #333;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: #f5f5f5;
}

.logout-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #ff7875;
}

.main {
  padding: 40px 0;
  min-height: calc(100vh - 200px);
}

.footer {
  background-color: #f5f5f5;
  padding: 20px 0;
  text-align: center;
  color: #666;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>