<template>
  <div class="app">
    <header class="header">
      <nav class="nav">
        <div class="container">
          <router-link to="/" class="logo">博客系统</router-link>
          
          <!-- 搜索功能组件 -->
          <div class="search-container">
            <select v-model="searchType" class="search-type-select">
              <option value="all">全部搜索</option>
              <option value="title_content">文章内容</option>
              <option value="author">作者昵称</option>
            </select>
            <input 
              type="text" 
              v-model="searchKeyword" 
              :placeholder="getSearchPlaceholder()" 
              class="search-input"
              @keyup.enter="handleSearch"
            >
            <button @click="handleSearch" class="search-button">搜索</button>
          </div>
          
          <div class="nav-links">
            <router-link to="/" class="nav-link" @click.native="handleHomeClick">首页</router-link>
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
    const homeResetEvent = ref(false)
    
    // 搜索相关数据
    const searchKeyword = ref('')
    const searchType = ref('all')
    
    // 全局搜索状态标志，用于通知Home组件更新搜索
    window.globalSearchState = window.globalSearchState || {}

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

    // 处理首页链接点击事件，确保重置搜索状态
    const handleHomeClick = () => {
      // 清除搜索输入框和搜索类型
      searchKeyword.value = ''
      searchType.value = 'all'
      
      // 清除全局搜索状态
      window.globalSearchState = {
        keyword: '',
        type: 'all',
        timestamp: Date.now()
      }
      
      // 通过设置一个全局状态来通知Home组件重置搜索状态
      window.homeResetFlag = Date.now() // 使用时间戳确保每次都触发变化
    }
    
    // 获取搜索框占位符文本
    const getSearchPlaceholder = () => {
      switch (searchType.value) {
        case 'author':
          return '搜索作者昵称...'
        case 'title_content':
          return '搜索文章标题或内容...'
        default:
          return '搜索文章标题、内容或作者昵称...'
      }
    }
    
    // 处理搜索
    const handleSearch = () => {
      // 更新全局搜索状态
      window.globalSearchState = {
        keyword: searchKeyword.value,
        type: searchType.value,
        timestamp: Date.now()
      }
      
      // 如果不在首页，则跳转到首页
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      } else {
        // 如果已经在首页，通过重置标志触发搜索
        window.homeResetFlag = Date.now()
      }
    }
    
    return {
      isLoggedIn,
      logout,
      handleHomeClick,
      searchKeyword,
      searchType,
      getSearchPlaceholder,
      handleSearch
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
  align-items: center;
  gap: 20px;
}

.logo {
  margin-right: auto;
}

/* 搜索功能样式 */
.search-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  max-width: 500px;
}

.search-type-select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  background-color: white;
  cursor: pointer;
}

.search-type-select:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-input {
  flex: 1;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}

.search-input:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-button {
  padding: 6px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-button:hover {
  background-color: #40a9ff;
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